import { Router } from 'express';
import { db } from '../db/database.js';

const router = Router({ mergeParams: true });

// Helper to calculate estimated wait minutes dynamically
export function calculateWaitTime(peopleWaiting, activeCounters, avgTimeMinutes = 2.5) {
  if (peopleWaiting <= 0) return 0;
  const counters = Math.max(1, activeCounters);
  return Math.max(1, Math.round((peopleWaiting * avgTimeMinutes) / counters));
}

// GET /api/organizations/:orgId/services
router.get('/', (req, res) => {
  try {
    const { orgId } = req.params;
    const services = db.prepare(`
      SELECT 
        s.id,
        s.org_id as orgId,
        s.name,
        s.current_serving_token as currentServingToken,
        s.people_waiting as peopleWaiting,
        s.estimated_wait_minutes as estimatedWaitMinutes,
        (SELECT COUNT(*) FROM counters WHERE org_id = s.org_id AND is_active = 1) as activeCounters,
        s.category_emoji as categoryEmoji,
        s.avg_service_time_minutes as avgServiceTimeMinutes
      FROM services s
      WHERE s.org_id = ?
    `).all(orgId);

    const formatted = services.map(s => {
      const waitTime = calculateWaitTime(s.peopleWaiting, s.activeCounters, s.avgServiceTimeMinutes);
      return {
        id: s.id,
        orgId: s.orgId,
        name: s.name,
        currentServingToken: s.currentServingToken,
        peopleWaiting: s.peopleWaiting,
        estimatedWaitMinutes: waitTime,
        activeCounters: Math.max(1, s.activeCounters),
        categoryEmoji: s.categoryEmoji
      };
    });

    res.json(formatted);
  } catch (err) {
    console.error('Error fetching services:', err);
    res.status(500).json({ error: 'Failed to fetch services' });
  }
});

// GET /api/services/:id
router.get('/:id', (req, res) => {
  try {
    const s = db.prepare(`
      SELECT 
        s.id,
        s.org_id as orgId,
        s.name,
        s.current_serving_token as currentServingToken,
        s.people_waiting as peopleWaiting,
        s.estimated_wait_minutes as estimatedWaitMinutes,
        (SELECT COUNT(*) FROM counters WHERE org_id = s.org_id AND is_active = 1) as activeCounters,
        s.category_emoji as categoryEmoji,
        s.avg_service_time_minutes as avgServiceTimeMinutes
      FROM services s
      WHERE s.id = ?
    `).get(req.params.id);

    if (!s) {
      return res.status(404).json({ error: 'Service not found' });
    }

    const waitTime = calculateWaitTime(s.peopleWaiting, s.activeCounters, s.avgServiceTimeMinutes);
    res.json({
      id: s.id,
      orgId: s.orgId,
      name: s.name,
      currentServingToken: s.currentServingToken,
      peopleWaiting: s.peopleWaiting,
      estimatedWaitMinutes: waitTime,
      activeCounters: Math.max(1, s.activeCounters),
      categoryEmoji: s.categoryEmoji
    });
  } catch (err) {
    console.error('Error fetching service:', err);
    res.status(500).json({ error: 'Failed to fetch service' });
  }
});

export default router;
