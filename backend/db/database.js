import { DatabaseSync } from 'node:sqlite';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const DB_PATH = path.join(__dirname, 'queueless.db');

export const db = new DatabaseSync(DB_PATH);

// Enable WAL mode and foreign keys for optimal performance and integrity
db.exec('PRAGMA journal_mode = WAL;');
db.exec('PRAGMA foreign_keys = ON;');

export function initDatabase() {
  db.exec(`
    CREATE TABLE IF NOT EXISTS organizations (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      category TEXT NOT NULL,
      icon_emoji TEXT NOT NULL,
      address TEXT NOT NULL,
      active_counters_count INTEGER NOT NULL DEFAULT 3,
      is_open INTEGER NOT NULL DEFAULT 1
    );

    CREATE TABLE IF NOT EXISTS services (
      id TEXT PRIMARY KEY,
      org_id TEXT NOT NULL,
      name TEXT NOT NULL,
      current_serving_token TEXT NOT NULL DEFAULT 'A32',
      people_waiting INTEGER NOT NULL DEFAULT 0,
      estimated_wait_minutes INTEGER NOT NULL DEFAULT 15,
      active_counters INTEGER NOT NULL DEFAULT 2,
      category_emoji TEXT NOT NULL DEFAULT '📄',
      token_prefix TEXT NOT NULL DEFAULT 'A',
      last_token_num INTEGER NOT NULL DEFAULT 47,
      avg_service_time_minutes REAL NOT NULL DEFAULT 2.5,
      FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS counters (
      id INTEGER PRIMARY KEY,
      org_id TEXT NOT NULL,
      name TEXT NOT NULL,
      currently_serving_token TEXT,
      is_active INTEGER NOT NULL DEFAULT 1,
      FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS tokens (
      id TEXT PRIMARY KEY,
      token_number TEXT NOT NULL,
      service_id TEXT NOT NULL,
      org_id TEXT NOT NULL,
      user_id TEXT NOT NULL DEFAULT 'user_1',
      currently_serving_token TEXT NOT NULL,
      people_ahead INTEGER NOT NULL DEFAULT 0,
      estimated_wait_minutes INTEGER NOT NULL DEFAULT 10,
      assigned_counter TEXT,
      recommended_arrival TEXT,
      status TEXT NOT NULL DEFAULT 'WAITING',
      progress_steps TEXT NOT NULL DEFAULT '[]',
      eta_update_reason TEXT,
      created_at TEXT NOT NULL,
      served_at TEXT,
      completed_at TEXT,
      FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
      FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS notifications (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL DEFAULT 'user_1',
      type TEXT NOT NULL,
      title TEXT NOT NULL,
      message TEXT NOT NULL,
      time_ago TEXT NOT NULL,
      created_at TEXT NOT NULL,
      is_read INTEGER NOT NULL DEFAULT 0
    );

    CREATE TABLE IF NOT EXISTS queue_history (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL DEFAULT 'user_1',
      service_name TEXT NOT NULL,
      org_name TEXT NOT NULL,
      date_text TEXT NOT NULL,
      token_number TEXT NOT NULL,
      status TEXT NOT NULL,
      created_at TEXT NOT NULL
    );
  `);

  // Seed default data if database is empty
  const orgCount = db.prepare('SELECT COUNT(*) as count FROM organizations').get().count;
  if (orgCount === 0) {
    seedDatabase();
  }
}

export function seedDatabase() {
  console.log('🌱 Seeding initial QueueLess database records...');

  // 1. Organizations
  const insertOrg = db.prepare(`
    INSERT INTO organizations (id, name, category, icon_emoji, address, active_counters_count, is_open)
    VALUES (?, ?, ?, ?, ?, ?, ?)
  `);
  insertOrg.run('org_1', 'RIT College Office', 'College', '🏫', 'Administrative Services • Main Campus', 3, 1);
  insertOrg.run('org_2', 'City Central Hospital', 'Hospital', '🏥', 'OPD & Registration Section', 4, 1);
  insertOrg.run('org_3', 'National Apex Bank', 'Bank', '🏦', 'Customer Service & Forex', 2, 1);
  insertOrg.run('org_4', 'Municipal Regional Office', 'Government', '🏢', 'Citizens Desk & Permits', 3, 1);

  // 2. Services for org_1
  const insertService = db.prepare(`
    INSERT INTO services (id, org_id, name, current_serving_token, people_waiting, estimated_wait_minutes, active_counters, category_emoji, token_prefix, last_token_num, avg_service_time_minutes)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `);
  insertService.run('serv_1', 'org_1', 'Bonafide Certificate', 'A32', 15, 42, 2, '📜', 'A', 47, 2.5);
  insertService.run('serv_2', 'org_1', 'Scholarship Section', 'B18', 8, 18, 2, '🎓', 'B', 26, 2.2);
  insertService.run('serv_3', 'org_1', 'Exam & Transcript', 'C05', 15, 30, 3, '📝', 'C', 20, 2.0);
  insertService.run('serv_4', 'org_1', 'Fees & Finance Dept', 'F12', 21, 42, 1, '💰', 'F', 33, 2.0);

  // Services for org_2
  insertService.run('serv_5', 'org_2', 'General OPD Consultation', 'H14', 12, 35, 3, '🩺', 'H', 26, 3.0);
  insertService.run('serv_6', 'org_2', 'Laboratory & Blood Test', 'L09', 6, 15, 2, '🧪', 'L', 15, 2.5);

  // 3. Counters for org_1
  const insertCounter = db.prepare(`
    INSERT INTO counters (id, org_id, name, currently_serving_token, is_active)
    VALUES (?, ?, ?, ?, ?)
  `);
  insertCounter.run(1, 'org_1', 'Counter 1', 'A31', 1);
  insertCounter.run(2, 'org_1', 'Counter 2', 'A32', 1);
  insertCounter.run(3, 'org_1', 'Counter 3', null, 0);

  // 4. Initial Active Token for user_1
  const insertToken = db.prepare(`
    INSERT INTO tokens (
      id, token_number, service_id, org_id, user_id, currently_serving_token,
      people_ahead, estimated_wait_minutes, assigned_counter, recommended_arrival,
      status, progress_steps, eta_update_reason, created_at
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `);
  insertToken.run(
    'token_init_1',
    'A47',
    'serv_1',
    'org_1',
    'user_1',
    'A32',
    15,
    42,
    'Counter 2',
    '11:45 AM',
    'WAITING',
    JSON.stringify(['A32', 'A35', 'A39', 'A43', 'A47']),
    null,
    new Date().toISOString()
  );

  // 5. Notifications
  const insertNotification = db.prepare(`
    INSERT INTO notifications (id, user_id, type, title, message, time_ago, created_at, is_read)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
  `);
  insertNotification.run('n1', 'user_1', 'APPROACHING', 'Your turn is approaching', 'Only 3 people are ahead of you in the queue.', '2m ago', new Date(Date.now() - 120000).toISOString(), 0);
  insertNotification.run('n2', 'user_1', 'UPDATED', 'Queue updated', 'Your estimated waiting time is now 25 minutes.', '10m ago', new Date(Date.now() - 600000).toISOString(), 0);
  insertNotification.run('n3', 'user_1', 'PROCEED', 'Proceed to Counter 2', 'Token A32 is currently being served at Counter 2.', '15m ago', new Date(Date.now() - 900000).toISOString(), 0);

  // 6. History
  const insertHistory = db.prepare(`
    INSERT INTO queue_history (id, user_id, service_name, org_name, date_text, token_number, status, created_at)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
  `);
  insertHistory.run('h1', 'user_1', 'Bonafide Certificate', 'RIT College Office', 'Today • A47', 'A47', 'Completed', new Date().toISOString());
  insertHistory.run('h2', 'user_1', 'Scholarship Department', 'RIT College Office', 'Yesterday • B23', 'B23', 'Completed', new Date(Date.now() - 86400000).toISOString());
  insertHistory.run('h3', 'user_1', 'OPD Consultation', 'City Central Hospital', '15 Aug • H12', 'H12', 'Completed', new Date(Date.now() - 86400000 * 5).toISOString());

  console.log('✅ Initial database seed completed.');
}
