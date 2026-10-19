// Reachability of ST-01 in a Schemathesis run: did any order reach COMPLETED, and was COMPLETED -> CANCELLED attempted?
// Optionally compares the request/response sequence with a second run.
// Usage: node reachability.js <requests.har.json> [<other requests.har.json>]
const fs = require('fs');

const load = f => JSON.parse(fs.readFileSync(f, 'utf8')).log.entries;
const body = t => { try { return JSON.parse(t); } catch { return undefined; } };

function analyse(entries) {
  const lastStatus = new Map(); // order id -> last status observed in any 2xx body
  const seenStatuses = new Set();
  const patchTargets = {};
  let completedToCancelledAttempts = 0;
  const attempts = [];
  for (const e of entries) {
    const url = new URL(e.request.url);
    const m = url.pathname.match(/^\/api\/orders\/(\d+)\/status$/);
    const reqBody = body(e.request.postData?.text);
    if (e.request.method === 'PATCH' && m) {
      const target = reqBody?.status;
      patchTargets[String(target)] = (patchTargets[String(target)] || 0) + 1;
      if (lastStatus.get(m[1]) === 'COMPLETED' && target === 'CANCELLED') {
        completedToCancelledAttempts++;
        attempts.push(`${e.request.url} -> ${e.response.status}`);
      }
    }
    if (e.response.status >= 200 && e.response.status < 300) {
      const b = body(e.response.content?.text);
      for (const o of Array.isArray(b) ? b : [b]) {
        if (o && o.id !== undefined && o.status) { lastStatus.set(String(o.id), o.status); seenStatuses.add(o.status); }
      }
    }
  }
  return { seenStatuses: [...seenStatuses].sort(), patchTargets, completedToCancelledAttempts, attempts };
}

const [a, b] = process.argv.slice(2);
const ea = load(a);
const ra = analyse(ea);
console.log(`requests: ${ea.length}`);
console.log(`order statuses observed in 2xx bodies: ${ra.seenStatuses.join(', ')}`);
console.log(`reached COMPLETED: ${ra.seenStatuses.includes('COMPLETED')}`);
console.log(`PATCH target statuses requested: ${JSON.stringify(ra.patchTargets)}`);
console.log(`COMPLETED -> CANCELLED attempts on a known-COMPLETED order: ${ra.completedToCancelledAttempts}${ra.attempts.length ? ' ' + JSON.stringify(ra.attempts) : ''}`);
if (b) {
  const sig = es => es.map(e => `${e.request.method} ${e.request.url} ${e.request.postData?.text ?? ''} -> ${e.response.status}`);
  const sa = sig(ea), sb = sig(load(b));
  const firstDiff = sa.findIndex((s, i) => s !== sb[i]);
  console.log(`request/response sequence identical to second run: ${sa.length === sb.length && firstDiff === -1}${firstDiff >= 0 ? ` (first difference at #${firstDiff}: "${sa[firstDiff]}" vs "${sb[firstDiff]}")` : ''}`);
}
