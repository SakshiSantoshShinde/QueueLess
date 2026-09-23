import { WebSocketServer, WebSocket } from 'ws';

let wss = null;

export function initWebSocketServer(server) {
  wss = new WebSocketServer({ server, path: '/ws' });

  wss.on('connection', (ws, req) => {
    console.log(`🔌 WebSocket client connected from ${req.socket.remoteAddress}`);

    // Send welcome / connection confirmed message
    ws.send(JSON.stringify({
      event: 'CONNECTED',
      message: 'Connected to QueueLess Real-time Smart Queue Engine',
      timestamp: new Date().toISOString()
    }));

    ws.on('message', (data) => {
      try {
        const parsed = JSON.parse(data.toString());
        console.log('Received WebSocket message:', parsed);
      } catch (err) {
        console.error('Invalid WS message received:', err.message);
      }
    });

    ws.on('close', () => {
      console.log('🔌 WebSocket client disconnected');
    });

    ws.on('error', (err) => {
      console.error('WebSocket error:', err.message);
    });
  });

  return wss;
}

export function broadcast(event, payload) {
  if (!wss) return;

  const message = JSON.stringify({
    event,
    data: payload,
    timestamp: new Date().toISOString()
  });

  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });
}
