import { Router } from 'express';
import { db } from '../db/database.js';
import { broadcast } from '../ws.js';
import { calculateWaitTime } from './services.js';

const router = Router();

// GET /api/counters - List all counters
router.get('/', (req, res) => {
  try {
    const orgId = req.query.orgId || 'org_1';
    const counters = db.prepare(`
      SELECT 
        id,
        name,
        currently_serving_token as currentlyServingToken,
        is_active as isActive
      FROM counters
      WHERE org_id = ?
      ORDER BY id ASC
    `).all(orgId);

    const formatted = counters.map(c => ({
      ...c,
      isActive: Boolean(c.isActive)
    }));

    res.json(formatted);
  } catch (err) {
    console.error('Error fetching counters:', err);
    res.status(500).json({ error: 'Failed to fetch counters' });
  }
});

// PATCH /api/counters/:id/toggle - Toggle counter active/inactive
router.patch('/:id/toggle', (req, res) => {
  try {
    const counterId = parseInt(req.params.id, 10);
    const counter = db.prepare('SELECT * FROM counters WHERE id = ?').get(counterId);

    if (!counter) {
      return res.status(404).json({ error: 'Counter not found' });
    }

    const newActiveState = counter.is_active === 1 ? 0 : 1;
    db.prepare('UPDATE counters SET is_active = ? WHERE id = ?').run(newActiveState, counterId);

    // Calculate total active counters for this organization
    const activeCountObj = db.prepare('SELECT COUNT(*) as count FROM counters WHERE org_id = ? AND is_active = 1').get(counter.org_id);
    const activeCounters = Math.max(1, activeCountObj.count);

    // Dynamic reason text matching Android app logic
    const etaReason = newActiveState === 1
      ? `ETA updated because ${counter.name} is now active.`
      : `ETA updated: ${counter.name} is offline.`;

    // Recalculate ETA for all active tokens
    const waitingTokens = db.prepare(`
      SELECT t.*, s.avg_service_time_minutes
      FROM tokens t
      JOIN services s ON t.service_id = s.id
      WHERE t.org_id = ? AND t.status IN ('WAITING', 'SERVING')
    `).all(counter.org_id);

    const updateTokenStmt = db.prepare(`
      UPDATE tokens
      SET estimated_wait_minutes = ?, eta_update_reason = ?
      WHERE id = ?
    `);

    for (const t of waitingTokens) {
      const newWait = calculateWaitTime(t.people_ahead, activeCounters, t.avg_service_time_minutes);
      updateTokenStmt.run(newWait, etaReason, t.id);
    }

    // Fetch updated counters list
    const updatedCounters = db.prepare(`
      SELECT 
        id,
        name,
        currently_serving_token as currentlyServingToken,
        is_active as isActive
      FROM counters
      WHERE org_id = ?
      ORDER BY id ASC
    `).all(counter.org_id).map(c => ({
      ...c,
      isActive: Boolean(c.isActive)
    }));

    // Broadcast counter toggle event to all connected clients
    broadcast('COUNTER_TOGGLED', {
      counterId,
      isActive: Boolean(newActiveState),
      activeCounters,
      etaUpdateReason: etaReason,
      counters: updatedCounters
    });

    res.json({
      success: true,
      counterId,
      isActive: Boolean(newActiveState),
      etaUpdateReason: etaReason,
      counters: updatedCounters
    });
  } catch (err) {
    console.error('Error toggling counter:', err);
    res.status(500).json({ error: 'Failed to toggle counter' });
  }
});

export default router;
