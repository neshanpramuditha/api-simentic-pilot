// Ground-truth reachability of ST-01 in a run against seeded COMPLETED orders.
// For every seeded id it follows the HAR in order, tracking the server-side state implied by 2xx PATCH responses,
// and reports each PATCH {"status":"CANCELLED"} sent while the order was COMPLETED, with the response, and whether a
// later GET of that id was made (i.e. whether the tool looked at the persisted state afterwards).
// Usage: node reachability-seeded.js <requests.har.json> <seeded.json> [<post-run snapshot.json>]
const fs = require('fs');
const [harFile, seededFile, snapshotFile] = process.argv.slice(2);
const entries = JSON.parse(fs.readFileSync(harFile, 'utf8')).log.entries;
const { seeded } = JSON.parse(fs.readFileSync(seededFile, 'utf8'));
const state = new Map(seeded.map(s => [String(s.id), s.status]));
const parse = t => { try { return JSON.parse(t); } catch { return undefined; } };

const attempts = [];
const patchesToSeeded = {};
entries.forEach((e, index) => {
  const p = new URL(e.request.url).pathname;
  const m = p.match(/^\/api\/orders\/([^/]+)\/status$/);
  if (e.request.method === 'PATCH' && m && state.has(m[1])) {
    const target = parse(e.request.postData?.text)?.status;
    patchesToSeeded[`${target} -> ${e.response.status}`] = (patchesToSeeded[`${target} -> ${e.response.status}`] || 0) + 1;
    if (state.get(m[1]) === 'COMPLETED' && target === 'CANCELLED') {
      const body = parse(e.response.content?.text);
      const laterGet = entries.slice(index + 1).find(x => x.request.method === 'GET' && new URL(x.request.url).pathname === `/api/orders/${m[1]}`);
      attempts.push({ index, id: m[1], status: e.response.status, responseStatus: body?.status ?? null, laterGetOfSameId: laterGet ? { index: entries.indexOf(laterGet), status: laterGet.response.status, orderStatus: parse(laterGet.response.content?.text)?.status ?? null } : null });
    }
    if (e.response.status >= 200 && e.response.status < 300) state.set(m[1], parse(e.response.content?.text)?.status ?? state.get(m[1]));
  }
});

console.log(`requests: ${entries.length}; seeded ids: ${seeded.length}`);
console.log(`PATCH requests to seeded ids (target -> HTTP status): ${JSON.stringify(patchesToSeeded)}`);
console.log(`COMPLETED -> CANCELLED attempts on seeded orders: ${attempts.length}`);
for (const a of attempts.slice(0, 10)) console.log(`  #${a.index} id=${a.id} -> ${a.status} body.status=${a.responseStatus}; later GET of same id: ${a.laterGetOfSameId ? JSON.stringify(a.laterGetOfSameId) : 'none'}`);
if (attempts.length > 10) console.log(`  ... ${attempts.length - 10} more`);
if (snapshotFile) {
  const snap = JSON.parse(fs.readFileSync(snapshotFile, 'utf8'));
  const counts = {};
  for (const s of snap) counts[s.order?.status ?? `HTTP ${s.httpStatus}`] = (counts[s.order?.status ?? `HTTP ${s.httpStatus}`] || 0) + 1;
  const cancelledWithCompletedAt = snap.filter(s => s.order?.status === 'CANCELLED' && s.order?.completedAt).length;
  console.log(`post-run persisted state of seeded orders: ${JSON.stringify(counts)}; CANCELLED with completedAt set: ${cancelledWithCompletedAt}`);
}
