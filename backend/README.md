# QueueLess Smart Queue Backend ⚡

High-performance, real-time backend API and live monitoring dashboard for the **QueueLess (Smart Queue Management System)**.

Built with **Node.js (v25)**, **Express**, **Native SQLite (`node:sqlite`)**, and **WebSockets (`ws`)**.

---

## 🌟 Key Features

- **Zero-Config Database**: Utilizes Node 25's embedded `node:sqlite` engine (`queueless.db`). No external database server (MySQL/Postgres) installation required! Automatically creates tables and seeds initial project data on first run.
- **Real-Time Live Updates**: Instant duplex WebSocket broadcasts (`ws://localhost:5000/ws`) for token progression, counter state toggles, and queue notifications.
- **Dynamic ETA Engine**: Automatically recalculates wait times and estimated arrival whenever queue length changes or counters are toggled active/offline.
- **Embedded Web Control Panel**: Sleek, glassmorphic dark-mode web dashboard at `http://localhost:5000` to call tokens, toggle counters, issue tickets, and observe real-time WebSocket feeds during project demonstrations or college viva.
- **Android Integration**: Native Retrofit & OkHttp networking layer with automatic fallback mode. Works smoothly even if the server is offline.

---

## 🚀 Quick Start

### 1. Run the Backend Server

From the `backend/` directory:

```bash
# Install dependencies (first time only)
npm install

# Start the server
npm start

# Or start in hot-reload development mode
npm run dev
```

The server will start on port `5000`:
- **Web Dashboard**: `http://localhost:5000/`
- **Health Check**: `http://localhost:5000/api/health`
- **WebSocket Feed**: `ws://localhost:5000/ws`
- **Android Emulator Endpoint**: `http://10.0.2.2:5000/api/`

---

## 🧪 Run Automated Tests

Run the built-in test suite to verify all endpoints:

```bash
npm test
```

Expected output:
```
🧪 Starting QueueLess Backend API Automated Verification...

✅ [PASS] GET /api/health
✅ [PASS] GET /api/organizations
✅ [PASS] GET /api/organizations/org_1/services
✅ [PASS] POST /api/tokens/take
✅ [PASS] POST /api/staff/queue/next
✅ [PASS] PATCH /api/counters/3/toggle
✅ [PASS] GET /api/notifications
✅ [PASS] GET /api/admin/analytics

===========================================
🏁 Test Summary: 8 Passed, 0 Failed
===========================================
```

---

## 📱 Android App Connection

### On Android Emulator
No configuration needed! Android Emulator automatically forwards `http://10.0.2.2:5000/` to your computer's `localhost:5000`.

### On Physical Android Device
1. Connect your phone and computer to the same Wi-Fi network.
2. Find your computer's local IP address (e.g. `192.168.1.15`).
3. In Android Studio, open:
   `app/src/main/java/com/example/queueless_smartqueue/network/ApiClient.kt`
4. Set `baseUrl`:
   ```kotlin
   var baseUrl: String = "http://192.168.1.15:5000/api/"
   ```

---

## 📡 API Reference

### 1. Organizations & Services
- **`GET /api/organizations`**: Returns all organizations with counter counts and operating status.
- **`GET /api/organizations/:orgId/services`**: Returns queue services for an organization with current serving token, people waiting, and estimated wait minutes.

### 2. User Queue Flow
- **`POST /api/tokens/take`**: Join a service queue.
  ```json
  // Request
  { "serviceId": "serv_1", "userId": "user_1" }

  // Response (TokenInfo)
  {
    "tokenNumber": "A48",
    "serviceId": "serv_1",
    "serviceName": "Bonafide Certificate",
    "orgName": "RIT College Office",
    "currentlyServingToken": "A32",
    "peopleAhead": 15,
    "estimatedWaitMinutes": 42,
    "assignedCounter": "Counter 2",
    "recommendedArrival": "11:45 AM",
    "status": "WAITING",
    "progressSteps": ["A32", "A36", "A40", "A44", "A48"]
  }
  ```
- **`GET /api/tokens/active?userId=user_1`**: Get active waiting/serving token.
- **`POST /api/tokens/:tokenNumber/cancel`**: Cancel an active token.
- **`GET /api/tokens/history?userId=user_1`**: Fetch past completed/cancelled tokens.

### 3. Staff & Counter Controls
- **`POST /api/staff/queue/next`**: Advance queue (calls next token, marks previous completed, notifies waiting users).
  ```json
  // Request
  { "serviceId": "serv_1", "counterId": 2 }
  ```
- **`PATCH /api/counters/:id/toggle`**: Toggle counter active/offline (triggers instant wait time recalculation across all waiting tokens).
- **`GET /api/staff/stats`**: Today's stats (total tokens, completed, waiting, avg wait time).
- **`GET /api/admin/analytics`**: Queue throughput, hourly distribution, peak times, efficiency rating.

---

## 🔌 WebSocket Events Broadcasted

Clients connected to `ws://localhost:5000/ws` receive real-time JSON packets:

| Event | Trigger | Payload |
|---|---|---|
| `TOKEN_ISSUED` | New token taken | `{ token, serviceId, newQueueLength }` |
| `TOKEN_CALLED` | Staff calls next token | `{ serviceId, counterName, currentlyServingToken, userToken }` |
| `COUNTER_TOGGLED` | Counter turned on/off | `{ counterId, isActive, activeCounters, etaUpdateReason, counters }` |
| `QUEUE_PAUSED` | Staff pauses queue | `{ serviceId, isPaused, message }` |
| `TOKEN_CANCELLED` | User cancels ticket | `{ tokenNumber }` |
