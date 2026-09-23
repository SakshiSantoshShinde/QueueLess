import { Router } from 'express';
import { db } from '../db/database.js';
import { broadcast } from '../ws.js';
import { calculateWaitTime } from './services.js';
import crypto from 'node:crypto';

const router = Router();

// Helper to format TokenInfo matching Android QueueModels.kt
export function formatTokenResponse(t, serviceName, orgName) {
  let progressSteps = [];
  try {
    progressSteps = typeof t.progress_steps === 'string' ? JSON.parse(t.progress_steps) : t.progress_steps;
  } catch {
    progressSteps = [t.currently_serving_token, t.token_number];
  }

  return {
    tokenNumber: t.token_number,
    serviceId: t.service_id,
    serviceName: serviceName || t.service_name || 'General Service',
    orgName: orgName || t.org_name || 'Organization',
    currentlyServingToken: t.currently_serving_token,
    peopleAhead: t.people_ahead,
    estimatedWaitMinutes: t.estimated_wait_minutes,
    assignedCounter: t.assigned_counter || 'Counter 1',
    recommendedArrival: t.recommended_arrival || 'In 15 mins',
    status: t.status, // WAITING, SERVING, COMPLETED, CANCELLED
    progressSteps: progressSteps || [],
    etaUpdateReason: t.eta_update_reason || null
  };
}

// Generate realistic milestone progress steps between current token and user's token
function generateProgressSteps(currentServingToken, userToken) {
  const prefix = userToken.charAt(0);
  const currentNum = parseInt(currentServingToken.substring(1), 10) || 32;
  const targetNum = parseInt(userToken.substring(1), 10) || currentNum + 15;

  if (targetNum <= currentNum) {
    return [currentServingToken, userToken];
  }

  const stepCount = 5;
  const interval = (targetNum - currentNum) / (stepCount - 1);
  const steps = [];

  for (let i = 0; i < stepCount; i++) {
    const num = Math.round(currentNum + interval * i);
    steps.push(`${prefix}${num}`);
  }

  // Ensure start and end match exactly
  steps[0] = currentServingToken;
  steps[steps.length - 1] = userToken;

  return Array.from(new Set(steps)); // eliminate any duplicates
}

// POST /api/tokens/take - Join a queue and get a token
router.post('/take', (req, res) => {
  try {
    const { serviceId, userId = 'user_1' } = req.body;

    if (!serviceId) {
      return res.status(400).json({ error: 'serviceId is required' });
    }

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

    const nextNum = service.last_token_num + 1;
    const newTokenNumber = `${service.token_prefix}${nextNum}`;
    const peopleAhead = service.people_waiting;
    const activeCounters = Math.max(1, service.active_counters_count);
    const estimatedWaitMinutes = calculateWaitTime(peopleAhead, activeCounters, service.avg_service_time_minutes);

    // Calculate arrival time
    const arrivalDate = new Date(Date.now() + Math.max(1, estimatedWaitMinutes - 5) * 60000);
    const hours = arrivalDate.getHours();
    const minutes = arrivalDate.getMinutes();
    const ampm = hours >= 12 ? 'PM' : 'AM';
    const formattedHours = hours % 12 || 12;
    const formattedMinutes = minutes < 10 ? `0${minutes}` : minutes;
    const recommendedArrival = `${formattedHours}:${formattedMinutes} ${ampm}`;

    const progressSteps = generateProgressSteps(service.current_serving_token, newTokenNumber);
    const tokenId = `tok_${crypto.randomUUID()}`;
    const assignedCounter = 'Counter 2';

    // Insert new token
    db.prepare(`
      INSERT INTO tokens (
        id, token_number, service_id, org_id, user_id, currently_serving_token,
        people_ahead, estimated_wait_minutes, assigned_counter, recommended_arrival,
        status, progress_steps, eta_update_reason, created_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).run(
      tokenId,
      newTokenNumber,
      service.id,
      service.org_id,
      userId,
      service.current_serving_token,
      peopleAhead,
      estimatedWaitMinutes,
      assignedCounter,
      recommendedArrival,
      'WAITING',
      JSON.stringify(progressSteps),
      null,
      new Date().toISOString()
    );

    // Update service's last_token_num and people_waiting
    db.prepare(`
      UPDATE services
      SET last_token_num = ?, people_waiting = people_waiting + 1
      WHERE id = ?
    `).run(nextNum, service.id);

    const tokenRecord = db.prepare('SELECT * FROM tokens WHERE id = ?').get(tokenId);
    const responsePayload = formatTokenResponse(tokenRecord, service.name, service.org_name);

    // Broadcast event
    broadcast('TOKEN_ISSUED', {
      token: responsePayload,
      serviceId: service.id,
      newQueueLength: peopleAhead + 1
    });

    res.status(201).json(responsePayload);
  } catch (err) {
    console.error('Error taking token:', err);
    res.status(500).json({ error: 'Failed to issue token' });
  }
});

// GET /api/tokens/active - Get the user's current active token
router.get('/active', (req, res) => {
  try {
    const userId = req.query.userId || 'user_1';

    const token = db.prepare(`
      SELECT 
        t.*, 
        s.name as service_name, 
        o.name as org_name
      FROM tokens t
      JOIN services s ON t.service_id = s.id
      JOIN organizations o ON t.org_id = o.id
      WHERE t.user_id = ? AND t.status IN ('WAITING', 'SERVING')
      ORDER BY t.created_at DESC
      LIMIT 1
    `).get(userId);

    if (!token) {
      return res.json(null);
    }

    res.json(formatTokenResponse(token, token.service_name, token.org_name));
  } catch (err) {
    console.error('Error fetching active token:', err);
    res.status(500).json({ error: 'Failed to fetch active token' });
  }
});

// POST /api/tokens/:tokenNumber/cancel - Cancel a token
router.post('/:tokenNumber/cancel', (req, res) => {
  try {
    const { tokenNumber } = req.params;

    const token = db.prepare(`
      SELECT t.*, s.name as service_name, o.name as org_name
      FROM tokens t
      JOIN services s ON t.service_id = s.id
      JOIN organizations o ON t.org_id = o.id
      WHERE t.token_number = ? AND t.status IN ('WAITING', 'SERVING')
    `).get(tokenNumber);

    if (!token) {
      return res.status(404).json({ error: 'Active token not found' });
    }

    db.prepare(`
      UPDATE tokens 
      SET status = 'CANCELLED', completed_at = ?
      WHERE id = ?
    `).run(new Date().toISOString(), token.id);

    // Decrement people_waiting on service
    db.prepare(`
      UPDATE services
      SET people_waiting = MAX(0, people_waiting - 1)
      WHERE id = ?
    `).run(token.service_id);

    // Add to history
    db.prepare(`
      INSERT INTO queue_history (id, user_id, service_name, org_name, date_text, token_number, status, created_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `).run(
      `hist_${crypto.randomUUID()}`,
      token.user_id,
      token.service_name,
      token.org_name,
      `Today • ${token.token_number}`,
      token.token_number,
      'Cancelled',
      new Date().toISOString()
    );

    broadcast('TOKEN_CANCELLED', { tokenNumber });

    res.json({ success: true, message: `Token ${tokenNumber} cancelled` });
  } catch (err) {
    console.error('Error cancelling token:', err);
    res.status(500).json({ error: 'Failed to cancel token' });
  }
});

// GET /api/tokens/history - Fetch past completed / cancelled tokens
router.get('/history', (req, res) => {
  try {
    const userId = req.query.userId || 'user_1';

    const history = db.prepare(`
      SELECT 
        id,
        service_name as serviceName,
        org_name as orgName,
        date_text as dateText,
        token_number as tokenNumber,
        status
      FROM queue_history
      WHERE user_id = ?
      ORDER BY created_at DESC
    `).all(userId);

    res.json(history);
  } catch (err) {
    console.error('Error fetching history:', err);
    res.status(500).json({ error: 'Failed to fetch history' });
  }
});

export default router;
