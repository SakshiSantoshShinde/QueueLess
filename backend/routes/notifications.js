import { Router } from 'express';
import { db } from '../db/database.js';

const router = Router();

// GET /api/notifications - Fetch user notifications
router.get('/', (req, res) => {
  try {
    const userId = req.query.userId || 'user_1';

    const notifs = db.prepare(`
      SELECT 
        id,
        type,
        title,
        message,
        time_ago as timeAgo,
        is_read as isRead,
        created_at as createdAt
      FROM notifications
      WHERE user_id = ?
      ORDER BY created_at DESC
      LIMIT 20
    `).all(userId);

    res.json(notifs);
  } catch (err) {
    console.error('Error fetching notifications:', err);
    res.status(500).json({ error: 'Failed to fetch notifications' });
  }
});

// PATCH /api/notifications/:id/read - Mark as read
router.patch('/:id/read', (req, res) => {
  try {
    db.prepare('UPDATE notifications SET is_read = 1 WHERE id = ?').run(req.params.id);
    res.json({ success: true });
  } catch (err) {
    console.error('Error marking notification read:', err);
    res.status(500).json({ error: 'Failed to update notification' });
  }
});

export default router;
