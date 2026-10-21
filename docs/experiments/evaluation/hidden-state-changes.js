// Compares each order's final persisted state (DumpOrders.java output) with the last state the client observed for it
// in any 2xx response of the run's HAR (seeded orders start from their seeded state). In clean mode every state change
// is visible to the client, so a difference means the database changed without a successful response saying so.
// Usage: node hidden-state-changes.js <har> <db-orders.json> [<seeded.json>]
const fs = require('fs');
const strip = t => t.replace(/^﻿/, '');
const [harFile, dbFile, seededFile] = process.argv.slice(2);
const entries = JSON.parse(strip(fs.readFileSync(harFile, 'utf8'))).log.entries;
const db = JSON.parse(strip(fs.readFileSync(dbFile, 'utf8')));
const parse = t => { try { return JSON.parse(t); } catch { return undefined; } };
const observed = new Map();
if (seededFile) for (const s of JSON.parse(strip(fs.readFileSync(seededFile, 'utf8'))).seeded) observed.set(String(s.id), s.status);
for (const e of entries) {
  if (e.response.status < 200 || e.response.status >= 300) continue;
  for (const o of [].concat(parse(e.response.content?.text) ?? [])) if (o?.id !== undefined && o?.status) observed.set(String(o.id), o.status);
}
const hidden = db.filter(row => observed.has(String(row.id)) && observed.get(String(row.id)) !== row.status);
const never = db.filter(row => !observed.has(String(row.id))).length;
console.log(`orders in database: ${db.length}; never observed by the client: ${never}; final DB status differs from last observed status: ${hidden.length}`);
hidden.slice(0, 5).forEach(r => console.log(`  order ${r.id}: last observed ${observed.get(String(r.id))}, persisted ${r.status}`));
