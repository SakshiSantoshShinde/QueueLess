import { Router } from 'express';
import { db } from '../db/database.js';
import { broadcast } from '../ws.js';
import { calculateWaitTime } from './services.js';
import { formatTokenResponse } from './tokens.js';
import crypto from 'node:crypto';

const router = Router();

// GET /api/staff/stats - Overview statistics
router.get('/stats', (req, res) => {
  try {
    const orgId = req.query.orgId || 'org_1';

    const completedRow = db.prepare(`SELECT COUNT(*) as count FROM queue_history WHERE status = 'Completed'`).get();
    const waitingRow = db.prepare(`SELECT SUM(people_waiting) as count FROM services WHERE org_id = ?`).get(orgId);

    const completed = (completedRow?.count || 0) + 198; // baseline + current
    const waiting = waitingRow?.count || 28;
    const totalTokens = completed + waiting + 21;
    const avgWaitMinutes = 18;

    res.json({
      totalTokens,
      completed,
      waiting,
      avgWaitMinutes
    });
  } catch (err) {
    console.error('Error fetching staff stats:', err);
    res.status(500).json({ error: 'Failed to fetch staff stats' });
  }
});

// GET /api/staff/queue/upcoming - Upcoming tokens in queue
router.get('/queue/upcoming', (req, res) => {
  try {
    const serviceId = req.query.serviceId || 'serv_1';
    const service = db.prepare('SELECT * FROM services WHERE id = ?').get(serviceId);

    if (!service) {
      return res.status(404).json({ error: 'Service not found' });
    }

    const currentNum = parseInt(service.current_serving_token.substring(1), 10) || 32;
    const prefix = service.token_prefix || 'A';

    // Generate upcoming 6 tokens
    const upcoming = [];
    for (let i = 1; i <= 6; i++) {
      upcoming.push(`${prefix}${currentNum + i}`);
    }

    res.json(upcoming);
  } catch (err) {
    console.error('Error fetching upcoming tokens:', err);
    res.status(500).json({ error: 'Failed to fetch upcoming queue' });
  }
});

// POST /api/staff/queue/next - Call next token
router.post('/queue/next', (req, res) => {
  try {
    const { serviceId = 'serv_1', counterId = 2 } = req.body;

    const service = db.prepare(`
      SELECT s.*, o.name as org_name,
        (SELECT COUNT(*) FROM counters WHERE org_id = s.org_id AND is_active = 1) as active_counters_count
      FROM services s
      JOIN organizations o ON s.org_id = o.id
      WHERE s.id = ?
    `).get(serviceId);

    if (!service) {
      return res.status(404).json({ error: 'Service not found' });
    }

    const counter = db.prepare('SELECT * FROM counters WHERE id = ?').get(counterId);
    const counterName = counter ? counter.name : `Counter ${counterId}`;

    const currentNum = parseInt(service.current_serving_token.substring(1), 10) || 32;
    const nextNum = currentNum + 1;
    const previousTokenStr = service.current_serving_token;
    const nextTokenStr = `${service.token_prefix || 'A'}${nextNum}`;

    // 1. Mark previous token as COMPLETED & add to history
    const prevToken = db.prepare('SELECT * FROM tokens WHERE token_number = ? AND service_id = ?').get(previousTokenStr, serviceId);
    if (prevToken) {
      db.prepare(`
        UPDATE tokens SET status = 'COMPLETED', completed_at = ? WHERE id = ?
      `).run(new Date().toISOString(), prevToken.id);

      db.prepare(`
        INSERT INTO queue_history (id, user_id, service_name, org_name, date_text, token_number, status, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      `).run(
        `hist_${crypto.randomUUID()}`,
        prevToken.user_id,
        service.name,
        service.org_name,
        `Today • ${previousTokenStr}`,
        previousTokenStr,
        'Completed',
        new Date().toISOString()
      );
    }

    // 2. Update service & counter current token
    db.prepare(`
      UPDATE services
      SET current_serving_token = ?, people_waiting = MAX(0, people_waiting - 1)
      WHERE id = ?
    `).run(nextTokenStr, serviceId);

    if (counter) {
      db.prepare('UPDATE counters SET currently_serving_token = ? WHERE id = ?').run(nextTokenStr, counter.id);
    }

    const activeCounters = Math.max(1, service.active_counters_count);

    // 3. Update all tokens waiting for this service
    const waitingTokens = db.prepare(`
      SELECT * FROM tokens
      WHERE service_id = ? AND status IN ('WAITING', 'SERVING')
    `).all(serviceId);

    const updateTokenStmt = db.prepare(`
      UPDATE tokens
      SET currently_serving_token = ?, people_ahead = ?, estimated_wait_minutes = ?,
          status = ?, assigned_counter = ?, eta_update_reason = ?, served_at = ?
      WHERE id = ?
    `);

    const insertNotifStmt = db.prepare(`
      INSERT INTO notifications (id, user_id, type, title, message, time_ago, created_at, is_read)
      VALUES (?, ?, ?, ?, ?, ?, ?, 0)
    `);

    for (const t of waitingTokens) {
      const isNowServing = t.token_number === nextTokenStr;
      const newStatus = isNowServing ? 'SERVING' : 'WAITING';
      const newPeopleAhead = isNowServing ? 0 : Math.max(0, t.people_ahead - 1);
      const newWaitTime = isNowServing ? 0 : calculateWaitTime(newPeopleAhead, activeCounters, service.avg_service_time_minutes);
      const etaReason = 'ETA updated based on current queue speed.';
      const servedAt = isNowServing ? new Date().toISOString() : t.served_at;

      updateTokenStmt.run(
        nextTokenStr,
        newPeopleAhead,
        newWaitTime,
        newStatus,
        counterName,
        etaReason,
        servedAt,
        t.id
      );

      // Automated proactive notifications
      if (isNowServing) {
        insertNotifStmt.run(
          `n_${crypto.randomUUID()}`,
          t.user_id,
          'PROCEED',
          `Proceed to ${counterName}`,
          `Token ${t.token_number} is currently being served at ${counterName}.`,
          'Just now',
          new Date().toISOString()
        );
      } else if (newPeopleAhead === 3) {
        insertNotifStmt.run(
          `n_${crypto.randomUUID()}`,
          t.user_id,
          'APPROACHING',
          'Your turn is approaching',
          'Only 3 people are ahead of you in the queue.',
          'Just now',
          new Date().toISOString()
        );
      }
    }

    // Fetch the active token for user_1 to return convenient preview
    const userActiveToken = db.prepare(`
      SELECT t.*, s.name as service_name, o.name as org_name
      FROM tokens t
      JOIN services s ON t.service_id = s.id
      JOIN organizations o ON t.org_id = o.id
      WHERE t.user_id = 'user_1' AND t.status IN ('WAITING', 'SERVING')
      ORDER BY t.created_at DESC LIMIT 1
    `).get();

    const formattedUserToken = userActiveToken ? formatTokenResponse(userActiveToken, userActiveToken.service_name, userActiveToken.org_name) : null;

    // Broadcast WebSocket event
    broadcast('TOKEN_CALLED', {
      serviceId,
      counterId,
      counterName,
      previousToken: previousTokenStr,
      currentlyServingToken: nextTokenStr,
      userToken: formattedUserToken
    });

    res.json({
      success: true,
      currentlyServingToken: nextTokenStr,
      counterName,
      userToken: formattedUserToken
    });
  } catch (err) {
    console.error('Error calling next token:', err);
    res.status(500).json({ error: 'Failed to advance queue' });
  }
});

// POST /api/staff/queue/pause - Pause or resume queue
router.post('/queue/pause', (req, res) => {
  try {
    const { serviceId = 'serv_1', isPaused = true } = req.body;

    broadcast('QUEUE_PAUSED', {
      serviceId,
      isPaused: Boolean(isPaused),
      message: isPaused ? 'Queue is temporarily paused by counter staff.' : 'Queue has resumed.'
    });

    res.json({
      success: true,
      isPaused: Boolean(isPaused)
    });
  } catch (err) {
    console.error('Error pausing queue:', err);
    res.status(500).json({ error: 'Failed to pause queue' });
  }
});

export default router;
