// Summarise a Schemathesis run directory: HAR status counts per operation, check executions from NDJSON.
// Usage: node analyze-run.js <run directory>   (accepts raw report names or the renamed files kept in docs)
const fs = require('fs');
const path = require('path');
const dir = process.argv[2];
const files = fs.readdirSync(dir);
const har = JSON.parse(fs.readFileSync(path.join(dir, files.find(f => f.startsWith('har-') || f === 'requests.har.json')), 'utf8'));

const template = u => {
  const p = new URL(u).pathname;
  if (/^\/api\/orders\/[^/]+\/status$/.test(p)) return '/api/orders/{id}/status';
  if (/^\/api\/users\/[^/]+\/orders$/.test(p)) return '/api/users/{userId}/orders';
  if (/^\/api\/orders\/[^/]+$/.test(p)) return '/api/orders/{id}';
  return p;
};
const counts = {};
for (const e of har.log.entries) {
  const key = `${e.request.method} ${template(e.request.url)}`;
  counts[key] ??= {};
  counts[key][e.response.status] = (counts[key][e.response.status] || 0) + 1;
}
console.log(`HAR entries: ${har.log.entries.length}`);
for (const [k, v] of Object.entries(counts).sort()) {
  const total = Object.values(v).reduce((a, b) => a + b, 0);
  console.log(`  ${k.padEnd(36)} total=${String(total).padStart(4)}  ${Object.entries(v).map(([s, n]) => `${s}:${n}`).join(' ')}`);
}

// 2xx GET /api/orders/{id} bodies: how many reached an existing order, and in which statuses
const getOk = har.log.entries.filter(e => e.request.method === 'GET' && template(e.request.url) === '/api/orders/{id}' && e.response.status === 200);
const statuses = {};
for (const e of getOk) { try { const s = JSON.parse(e.response.content.text).status; statuses[s] = (statuses[s] || 0) + 1; } catch {} }
console.log(`GET /api/orders/{id} 200 bodies by order status: ${JSON.stringify(statuses)}`);
const patchOk = har.log.entries.filter(e => e.request.method === 'PATCH' && e.response.status === 200);
const reached = {};
for (const e of patchOk) { try { const s = JSON.parse(e.response.content.text).status; reached[s] = (reached[s] || 0) + 1; } catch {} }
console.log(`PATCH 200 resulting statuses: ${JSON.stringify(reached)}`);

// NDJSON: every check execution recorded in ScenarioFinished events, by check name and outcome
const nd = fs.readFileSync(path.join(dir, files.find(f => f.startsWith('ndjson-') || f === 'events.ndjson')), 'utf8')
  .trim().split('\n').map(l => JSON.parse(l));
const results = {};
for (const line of nd) {
  const recorder = line.ScenarioFinished?.recorder;
  if (!recorder) continue;
  for (const checks of Object.values(recorder.checks || {})) {
    for (const check of checks) {
      results[check.name] ??= {};
      results[check.name][check.status] = (results[check.name][check.status] || 0) + 1;
    }
  }
}
console.log('check executions (name -> {status: count}):');
for (const [name, counts] of Object.entries(results).sort()) console.log(`  ${name.padEnd(30)} ${JSON.stringify(counts)}`);
