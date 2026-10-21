// Reads the current state of the given order ids through GET /api/orders/{id} (fresh request per id).
// Usage: node snapshot-orders.js <seeded.json> <out snapshot.json>
const fs = require('fs');
const [seededFile, outFile] = process.argv.slice(2);
const { seeded } = JSON.parse(fs.readFileSync(seededFile, 'utf8'));
(async () => {
  const snapshot = [];
  for (const { id } of seeded) {
    const res = await fetch(`http://localhost:8080/api/orders/${id}`, { headers: { Accept: 'application/json' } });
    snapshot.push({ id, httpStatus: res.status, order: res.status === 200 ? await res.json() : null });
  }
  fs.writeFileSync(outFile, JSON.stringify(snapshot, null, 2));
  const counts = {};
  for (const s of snapshot) counts[s.order?.status ?? `HTTP ${s.httpStatus}`] = (counts[s.order?.status ?? `HTTP ${s.httpStatus}`] || 0) + 1;
  console.log(`snapshot of ${snapshot.length} orders: ${JSON.stringify(counts)}`);
})().catch(e => { console.error(e.message); process.exit(1); });
