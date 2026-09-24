// Test script for verifying all QueueLess Smart Queue backend endpoints
const BASE_URL = 'http://127.0.0.1:5000/api';

async function runTests() {
  console.log('🧪 Starting QueueLess Backend API Automated Verification...\n');
  let passed = 0;
  let failed = 0;

  async function test(name, fn) {
    try {
      await fn();
      console.log(`✅ [PASS] ${name}`);
      passed++;
    } catch (err) {
      console.error(`❌ [FAIL] ${name}:`, err.message);
      failed++;
    }
  }

  // 1. Health
  await test('GET /api/health', async () => {
    const res = await fetch(`${BASE_URL}/health`);
    const data = await res.json();
    if (data.status !== 'online') throw new Error('Invalid health status');
  });

  // 2. Organizations
  await test('GET /api/organizations', async () => {
    const res = await fetch(`${BASE_URL}/organizations`);
    const data = await res.json();
    if (!Array.isArray(data) || data.length === 0) throw new Error('Expected organizations array');
    if (!data[0].name || typeof data[0].isOpen !== 'boolean') throw new Error('Invalid organization model format');
  });

  // 3. Services for Org 1
  await test('GET /api/organizations/org_1/services', async () => {
    const res = await fetch(`${BASE_URL}/organizations/org_1/services`);
    const data = await res.json();
    if (!Array.isArray(data) || data.length === 0) throw new Error('Expected services array');
    if (typeof data[0].estimatedWaitMinutes !== 'number') throw new Error('Missing estimatedWaitMinutes');
  });

  // 3.1 Register New Organization with Services & Fetch Back
  let registeredOrgId = null;
  await test('POST /api/organizations (Register Organization with Services)', async () => {
    const res = await fetch(`${BASE_URL}/organizations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: 'Apex Diagnostic Center',
        category: 'Hospital',
        iconEmoji: '🧪',
        address: '42 Health Boulevard',
        activeCountersCount: 2,
        services: [
          { name: 'Blood Test & Pathology', categoryEmoji: '🩸', tokenPrefix: 'B', avgServiceTimeMinutes: 3.0 },
          { name: 'MRI & Radiology', categoryEmoji: '🩻', tokenPrefix: 'M', avgServiceTimeMinutes: 5.0 }
        ]
      })
    });
    const data = await res.json();
    if (!data.id || data.name !== 'Apex Diagnostic Center' || !Array.isArray(data.services) || data.services.length !== 2) {
      throw new Error(`Invalid register response: ${JSON.stringify(data)}`);
    }
    registeredOrgId = data.id;

    // Fetch its services from database
    const servicesRes = await fetch(`${BASE_URL}/organizations/${registeredOrgId}/services`);
    const srvs = await servicesRes.json();
    if (!Array.isArray(srvs) || srvs.length !== 2) {
      throw new Error(`Expected 2 services for registered org, got: ${JSON.stringify(srvs)}`);
    }
    console.log(`     -> Registered Org ID: ${registeredOrgId} with ${srvs.length} services fetched from DB`);
  });


  // 4. Issue a new Token (User flow)
  let issuedTokenNumber = null;
  await test('POST /api/tokens/take', async () => {
    const res = await fetch(`${BASE_URL}/tokens/take`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ serviceId: 'serv_1', userId: 'test_user_42' })
    });
    const data = await res.json();
    if (!data.tokenNumber || !data.serviceName || data.status !== 'WAITING') {
      throw new Error(`Invalid token response: ${JSON.stringify(data)}`);
    }
    issuedTokenNumber = data.tokenNumber;
    console.log(`     -> Token successfully issued: ${issuedTokenNumber}`);
  });

  // 5. Staff Call Next Token
  await test('POST /api/staff/queue/next', async () => {
    const res = await fetch(`${BASE_URL}/staff/queue/next`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ serviceId: 'serv_1', counterId: 2 })
    });
    const data = await res.json();
    if (!data.success || !data.currentlyServingToken) throw new Error('Failed to advance queue');
    console.log(`     -> Queue advanced to serving: ${data.currentlyServingToken}`);
  });

  // 6. Toggle Counter and verify dynamic ETA recalculation
  await test('PATCH /api/counters/3/toggle', async () => {
    const res = await fetch(`${BASE_URL}/counters/3/toggle`, {
      method: 'PATCH'
    });
    const data = await res.json();
    if (!data.success || typeof data.isActive !== 'boolean' || !data.etaUpdateReason) {
      throw new Error('Invalid counter toggle response');
    }
    console.log(`     -> Counter 3 is now: ${data.isActive ? 'ACTIVE' : 'OFFLINE'} (${data.etaUpdateReason})`);
  });

  // 7. Notifications
  await test('GET /api/notifications', async () => {
    const res = await fetch(`${BASE_URL}/notifications?userId=user_1`);
    const data = await res.json();
    if (!Array.isArray(data)) throw new Error('Expected array of notifications');
    console.log(`     -> Retrieved ${data.length} notifications`);
  });

  // 8. Admin Analytics
  await test('GET /api/admin/analytics', async () => {
    const res = await fetch(`${BASE_URL}/admin/analytics`);
    const data = await res.json();
    if (!data.summary || !data.hourlyDistribution) throw new Error('Invalid analytics response');
    console.log(`     -> Queue Efficiency: ${data.summary.queueEfficiency}, Avg Wait: ${data.summary.avgWaitMinutes} min`);
  });

  console.log(`\n===========================================`);
  console.log(`🏁 Test Summary: ${passed} Passed, ${failed} Failed`);
  console.log(`===========================================`);

  if (failed > 0) process.exit(1);
}

runTests();
