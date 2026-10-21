// Seeds a freshly started API with orders driven to a given status through the public API only.
// Usage: node seed-orders.js <count> <targetStatus> <out seeded.json>
// The lifecycle used is CREATED -> PAID -> SHIPPED -> DELIVERED -> COMPLETED (stopping at targetStatus).
const fs = require('fs');
const [count, target, outFile] = [Number(process.argv[2]), process.argv[3], process.argv[4]];
const path = ['PAID', 'SHIPPED', 'DELIVERED', 'COMPLETED'];
if (!count || !outFile || (target !== 'CREATED' && !path.includes(target))) {
  console.error('usage: node seed-orders.js <count> <CREATED|PAID|SHIPPED|DELIVERED|COMPLETED> <out seeded.json>');
  process.exit(2);
}
const base = 'http://localhost:8080';
const json = { 'Content-Type': 'application/json', Accept: 'application/json' };

(async () => {
  const seeded = [];
  for (let i = 0; i < count; i++) {
    let res = await fetch(`${base}/api/orders`, { method: 'POST', headers: json, body: JSON.stringify({ ownerUserId: 1000 + i, total: 10 + i }) });
    if (res.status !== 201) throw new Error(`seed POST failed: ${res.status}`);
    const order = await res.json();
    for (const s of path.slice(0, target === 'CREATED' ? 0 : path.indexOf(target) + 1)) {
      res = await fetch(`${base}/api/orders/${order.id}/status`, { method: 'PATCH', headers: json, body: JSON.stringify({ status: s }) });
      if (res.status !== 200) throw new Error(`seed PATCH ${s} failed for ${order.id}: ${res.status}`);
    }
    const check = await (await fetch(`${base}/api/orders/${order.id}`, { headers: json })).json();
    if (check.status !== target) throw new Error(`seed check failed for ${order.id}: ${check.status}`);
    seeded.push({ id: order.id, status: check.status, completedAt: check.completedAt });
  }
  fs.writeFileSync(outFile, JSON.stringify({ target, seeded }, null, 2));
  console.log(`seeded ${seeded.length} orders in ${target}: ids ${seeded[0].id}..${seeded[seeded.length - 1].id}`);
})().catch(e => { console.error(e.message); process.exit(1); });
