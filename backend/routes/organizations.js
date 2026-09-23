import { Router } from 'express';
import { db } from '../db/database.js';

const router = Router();

// GET /api/organizations - List all organizations
router.get('/', (req, res) => {
  try {
    const orgs = db.prepare(`
      SELECT 
        o.id, 
        o.name, 
        o.category, 
        o.icon_emoji as iconEmoji, 
        o.address, 
        (SELECT COUNT(*) FROM counters WHERE org_id = o.id AND is_active = 1) as activeCountersCount,
        o.is_open as isOpen
      FROM organizations o
    `).all();

    const formatted = orgs.map(org => ({
      ...org,
      isOpen: Boolean(org.isOpen)
    }));

    res.json(formatted);
  } catch (err) {
    console.error('Error fetching organizations:', err);
    res.status(500).json({ error: 'Failed to fetch organizations' });
  }
});

// GET /api/organizations/:id - Single organization
router.get('/:id', (req, res) => {
  try {
    const org = db.prepare(`
      SELECT 
        o.id, 
        o.name, 
        o.category, 
        o.icon_emoji as iconEmoji, 
        o.address, 
        (SELECT COUNT(*) FROM counters WHERE org_id = o.id AND is_active = 1) as activeCountersCount,
        o.is_open as isOpen
      FROM organizations o
      WHERE o.id = ?
    `).get(req.params.id);

    if (!org) {
      return res.status(404).json({ error: 'Organization not found' });
    }

    res.json({
      ...org,
      isOpen: Boolean(org.isOpen)
    });
  } catch (err) {
    console.error('Error fetching organization:', err);
    res.status(500).json({ error: 'Failed to fetch organization' });
  }
});

export default router;
