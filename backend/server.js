import express from 'express';
import cors from 'cors';
import http from 'node:http';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

import { initDatabase } from './db/database.js';
import { initWebSocketServer } from './ws.js';

import organizationsRouter from './routes/organizations.js';
import servicesRouter from './routes/services.js';
import countersRouter from './routes/counters.js';
import tokensRouter from './routes/tokens.js';
import staffRouter from './routes/staff.js';
import notificationsRouter from './routes/notifications.js';
import analyticsRouter from './routes/analytics.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const server = http.createServer(app);

// 1. Initialize SQLite Database & Seeder
initDatabase();

// 2. Initialize Real-Time WebSocket Server
initWebSocketServer(server);

// 3. Global Middlewares
app.use(cors());
app.use(express.json());

// Serve Interactive Web Dashboard for Live Testing & College Demonstrations
app.use(express.static(path.join(__dirname, 'public')));

// 4. API Routes
app.use('/api/organizations', organizationsRouter);
app.use('/api/organizations/:orgId/services', servicesRouter);
app.use('/api/services', servicesRouter);
app.use('/api/counters', countersRouter);
app.use('/api/tokens', tokensRouter);
app.use('/api/staff', staffRouter);
app.use('/api/notifications', notificationsRouter);
app.use('/api/admin/analytics', analyticsRouter);

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({
    status: 'online',
    system: 'QueueLess Smart Queue API & WebSocket Server',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Unhandled Server Error:', err);
  res.status(500).json({ error: 'Internal Server Error', message: err.message });
});

const PORT = process.env.PORT || 5000;

server.listen(PORT, () => {
  console.log(`=======================================================`);
  console.log(`🚀 QueueLess Smart Queue Backend is running!`);
  console.log(`📍 Local API URL:     http://localhost:${PORT}/api/health`);
  console.log(`💻 Web Dashboard:     http://localhost:${PORT}/`);
  console.log(`🔌 WebSocket Server:  ws://localhost:${PORT}/ws`);
  console.log(`📱 Android Emulator:  http://10.0.2.2:${PORT}/api/`);
  console.log(`=======================================================`);
});
