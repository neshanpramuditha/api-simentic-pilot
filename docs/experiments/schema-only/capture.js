// Captures raw responses from the running API (fresh database expected).
// Usage: node capture.js <output responses.json>
const fs = require('fs');
const outFile = process.argv[2];
if (!outFile) { console.error('usage: node capture.js <output responses.json>'); process.exit(2); }
const base = 'http://localhost:8080';
const calls = [
  ['POST', '/api/orders', '/api/orders', { ownerUserId: 7, total: 49.99 }],
  ['POST', '/api/orders', '/api/orders', { ownerUserId: 8, total: 10 }],
  ['GET', '/api/orders/{id}', '/api/orders/1'],
  ['PATCH', '/api/orders/{id}/status', '/api/orders/1/status', { status: 'PAID' }],
  ['PATCH', '/api/orders/{id}/status', '/api/orders/1/status', { status: 'SHIPPED' }],
  ['PATCH', '/api/orders/{id}/status', '/api/orders/1/status', { status: 'DELIVERED' }],
  ['PATCH', '/api/orders/{id}/status', '/api/orders/1/status', { status: 'COMPLETED' }],
  ['GET', '/api/orders/{id}', '/api/orders/1'],
  ['PATCH', '/api/orders/{id}/status', '/api/orders/2/status', { status: 'CANCELLED' }],
  ['GET', '/api/users/{userId}/orders', '/api/users/7/orders'],
  ['GET', '/api/users/{userId}/orders', '/api/users/99/orders'],
  ['GET', '/api/orders/{id}', '/api/orders/999'],
  ['GET', '/api/orders/{id}', '/api/orders/abc'],
  ['GET', '/api/users/{userId}/orders', '/api/users/abc/orders'],
  ['POST', '/api/orders', '/api/orders', { ownerUserId: 7, total: 0 }],
  ['PATCH', '/api/orders/{id}/status', '/api/orders/1/status', { status: 'CANCELLED' }],
];
(async () => {
  const out = [];
  for (const [method, template, url, body] of calls) {
    const res = await fetch(base + url, {
      method,
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    const text = await res.text();
    out.push({ method, template, url, status: res.status, contentType: res.headers.get('content-type'), body: text ? JSON.parse(text) : null });
  }
  fs.writeFileSync(outFile, JSON.stringify(out, null, 2));
  console.log(`captured ${out.length} responses`);
})();
