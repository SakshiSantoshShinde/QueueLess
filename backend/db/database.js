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
    INSERT OR REPLACE INTO organizations (id, name, category, icon_emoji, address, active_counters_count, is_open)
    VALUES (?, ?, ?, ?, ?, ?, ?)
  `);
  insertOrg.run('org_1', 'RIT College Office', 'College', '🏫', 'Administrative Services • Main Campus', 3, 1);
  insertOrg.run('org_2', 'City Central Hospital', 'Hospital', '🏥', 'OPD & Registration Section • Building B', 4, 1);
  insertOrg.run('org_3', 'National Apex Bank', 'Bank', '🏦', 'Customer Service & Forex • Central Branch', 3, 1);
  insertOrg.run('org_4', 'Municipal Regional Office', 'Government', '🏢', 'Citizens Desk & Permits • City Center', 3, 1);

  // 2. Services
  const insertService = db.prepare(`
    INSERT OR REPLACE INTO services (id, org_id, name, current_serving_token, people_waiting, estimated_wait_minutes, active_counters, category_emoji, token_prefix, last_token_num, avg_service_time_minutes)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `);

  // Org 1 Services (College)
  insertService.run('serv_1', 'org_1', 'Bonafide Certificate', 'A36', 14, 35, 2, '📜', 'A', 50, 2.5);
  insertService.run('serv_2', 'org_1', 'Scholarship Section', 'B18', 8, 18, 2, '🎓', 'B', 26, 2.2);
  insertService.run('serv_3', 'org_1', 'Exam & Transcript', 'C05', 15, 30, 3, '📝', 'C', 20, 2.0);
  insertService.run('serv_4', 'org_1', 'Fees & Finance Dept', 'F12', 21, 42, 1, '💰', 'F', 33, 2.0);
  insertService.run('serv_admit', 'org_1', 'Admission & Verification', 'D08', 6, 15, 2, '📂', 'D', 14, 2.5);

  // Org 2 Services (Hospital)
  insertService.run('serv_5', 'org_2', 'General OPD Consultation', 'H14', 12, 36, 3, '🩺', 'H', 26, 3.0);
  insertService.run('serv_6', 'org_2', 'Laboratory & Blood Test', 'L09', 6, 15, 2, '🧪', 'L', 15, 2.5);
  insertService.run('serv_rad', 'org_2', 'Radiology & X-Ray', 'R04', 5, 25, 2, '🩻', 'R', 9, 5.0);
  insertService.run('serv_pharm', 'org_2', 'Pharmacy Dispensing', 'P25', 9, 12, 3, '💊', 'P', 34, 1.5);

  // Org 3 Services (Bank)
  insertService.run('serv_7', 'org_3', 'Cash Deposit & Withdrawal', 'D15', 7, 14, 2, '💵', 'D', 22, 2.0);
  insertService.run('serv_8', 'org_3', 'Account Opening & KYC', 'N04', 5, 20, 1, '💳', 'N', 9, 4.0);
  insertService.run('serv_loan', 'org_3', 'Loans & Mortgages Desk', 'M06', 4, 24, 1, '🏦', 'M', 10, 6.0);
  insertService.run('serv_forex', 'org_3', 'Forex & International Wire', 'X02', 2, 10, 1, '🌐', 'X', 4, 5.0);

  // Org 4 Services (Government)
  insertService.run('serv_9', 'org_4', 'Property Tax & Assessment', 'T22', 10, 25, 2, '🏠', 'T', 32, 2.5);
  insertService.run('serv_10', 'org_4', 'Birth & Death Certificates', 'C11', 8, 16, 2, '📜', 'C', 19, 2.0);
  insertService.run('serv_11', 'org_4', 'Trade License & Permits', 'P05', 4, 12, 1, '📑', 'P', 9, 3.0);
  insertService.run('serv_water', 'org_4', 'Water & Utilities Desk', 'W07', 6, 18, 2, '💧', 'W', 13, 3.0);

  // 3. Counters for ALL Organizations
  const insertCounter = db.prepare(`
    INSERT OR REPLACE INTO counters (id, org_id, name, currently_serving_token, is_active)
    VALUES (?, ?, ?, ?, ?)
  `);

  // Org 1 Counters
  insertCounter.run(1, 'org_1', 'Counter 1', 'A31', 1);
  insertCounter.run(2, 'org_1', 'Counter 2', 'A36', 1);
  insertCounter.run(3, 'org_1', 'Counter 3', null, 0);

  // Org 2 Counters
  insertCounter.run(201, 'org_2', 'OPD Counter 1', 'H14', 1);
  insertCounter.run(202, 'org_2', 'Lab Counter 2', 'L09', 1);
  insertCounter.run(203, 'org_2', 'Pharmacy Counter 3', 'P25', 1);
  insertCounter.run(204, 'org_2', 'Emergency Counter 4', null, 0);

  // Org 3 Counters
  insertCounter.run(301, 'org_3', 'Teller Counter 1', 'D15', 1);
  insertCounter.run(302, 'org_3', 'Service Desk 2', 'N04', 1);
  insertCounter.run(303, 'org_3', 'Loan Counter 3', 'M06', 1);

  // Org 4 Counters
  insertCounter.run(401, 'org_4', 'Civic Desk 1', 'T22', 1);
  insertCounter.run(402, 'org_4', 'Certificate Counter 2', 'C11', 1);
  insertCounter.run(403, 'org_4', 'Permits Counter 3', 'P05', 1);

  // 4. Initial Active Token for user_1
  const insertToken = db.prepare(`
    INSERT OR REPLACE INTO tokens (
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
    'A36',
    11,
    28,
    'Counter 2',
    '11:45 AM',
    'WAITING',
    JSON.stringify(['A36', 'A39', 'A42', 'A45', 'A47']),
    null,
    new Date().toISOString()
  );

  // 5. Notifications
  const insertNotification = db.prepare(`
    INSERT OR REPLACE INTO notifications (id, user_id, type, title, message, time_ago, created_at, is_read)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
  `);
  insertNotification.run('n1', 'user_1', 'APPROACHING', 'Your turn is approaching', 'Only 3 people are ahead of you in the queue.', '2m ago', new Date(Date.now() - 120000).toISOString(), 0);
  insertNotification.run('n2', 'user_1', 'UPDATED', 'Queue updated', 'Your estimated waiting time is now 25 minutes.', '10m ago', new Date(Date.now() - 600000).toISOString(), 0);
  insertNotification.run('n3', 'user_1', 'PROCEED', 'Proceed to Counter 2', 'Token A36 is currently being served at Counter 2.', '15m ago', new Date(Date.now() - 900000).toISOString(), 0);

  // 6. History
  const insertHistory = db.prepare(`
    INSERT OR REPLACE INTO queue_history (id, user_id, service_name, org_name, date_text, token_number, status, created_at)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
  `);
  insertHistory.run('h1', 'user_1', 'Bonafide Certificate', 'RIT College Office', 'Today • A47', 'A47', 'Completed', new Date().toISOString());
  insertHistory.run('h2', 'user_1', 'Scholarship Department', 'RIT College Office', 'Yesterday • B23', 'B23', 'Completed', new Date(Date.now() - 86400000).toISOString());
  insertHistory.run('h3', 'user_1', 'General OPD Consultation', 'City Central Hospital', '15 Aug • H12', 'H12', 'Completed', new Date(Date.now() - 86400000 * 5).toISOString());
  insertHistory.run('h4', 'user_1', 'Cash Deposit & Withdrawal', 'National Apex Bank', '10 Aug • D05', 'D05', 'Completed', new Date(Date.now() - 86400000 * 10).toISOString());

  console.log('✅ Initial database seed completed with rich multi-organization data.');
}

