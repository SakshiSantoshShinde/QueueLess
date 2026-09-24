import { Router } from 'express';
import { db } from '../db/database.js';
import { broadcast } from '../ws.js';

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

// POST /api/organizations - Register a new organization with its services
router.post('/', (req, res) => {
  try {
    const {
      name,
      category = 'General',
      iconEmoji = '🏢',
      address = '',
      activeCountersCount = 2,
      isOpen = true,
      services = []
    } = req.body;

    if (!name || name.trim() === '') {
      return res.status(400).json({ error: 'Organization name is required' });
    }

    const orgId = `org_${Date.now()}`;
    const numCounters = Math.max(1, Number(activeCountersCount) || 2);

    const insertOrg = db.prepare(`
      INSERT INTO organizations (id, name, category, icon_emoji, address, active_counters_count, is_open)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `);

    insertOrg.run(
      orgId,
      name.trim(),
      category,
      iconEmoji || '🏢',
      address.trim(),
      numCounters,
      isOpen ? 1 : 0
    );

    // Insert services if provided
    const insertService = db.prepare(`
      INSERT INTO services (id, org_id, name, current_serving_token, people_waiting, estimated_wait_minutes, active_counters, category_emoji, token_prefix, last_token_num, avg_service_time_minutes)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);

    const createdServices = [];
    if (Array.isArray(services) && services.length > 0) {
      services.forEach((srv, index) => {
        const servId = `serv_${Date.now()}_${index + 1}`;
        const prefix = (srv.tokenPrefix || String.fromCharCode(65 + (index % 26))).toUpperCase();
        const initialToken = `${prefix}01`;
        const emoji = srv.categoryEmoji || '📄';
        const avgTime = Number(srv.avgServiceTimeMinutes) || 2.5;

        insertService.run(
          servId,
          orgId,
          srv.name.trim(),
          initialToken,
          0,
          0,
          numCounters,
          emoji,
          prefix,
          0,
          avgTime
        );

        createdServices.push({
          id: servId,
          orgId,
          name: srv.name.trim(),
          currentServingToken: initialToken,
          peopleWaiting: 0,
          estimatedWaitMinutes: 0,
          activeCounters: numCounters,
          categoryEmoji: emoji
        });
      });
    }

    // Insert counters for this organization
    const insertCounter = db.prepare(`
      INSERT INTO counters (org_id, name, currently_serving_token, is_active)
      VALUES (?, ?, ?, ?)
    `);

    for (let c = 1; c <= numCounters; c++) {
      insertCounter.run(orgId, `Counter ${c}`, null, 1);
    }

    const newOrg = {
      id: orgId,
      name: name.trim(),
      category,
      iconEmoji: iconEmoji || '🏢',
      address: address.trim(),
      activeCountersCount: numCounters,
      isOpen: Boolean(isOpen),
      services: createdServices
    };

    try {
      broadcast('ORGANIZATION_REGISTERED', newOrg);
    } catch (e) {
      console.warn('WebSocket broadcast error:', e.message);
    }

    res.status(201).json(newOrg);
  } catch (err) {
    console.error('Error registering organization:', err);
    res.status(500).json({ error: 'Failed to register organization', message: err.message });
  }
});

export default router;

