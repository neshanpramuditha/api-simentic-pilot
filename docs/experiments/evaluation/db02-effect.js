// For a DB-02-mode HAR: how many 409 responses were followed by a read (GET of the same order, or the list) showing
// that the rejected status had been persisted? This is the evidence that the defect changed state in the run.
// Usage: node db02-effect.js <har>
const fs = require('fs');
const entries = JSON.parse(fs.readFileSync(process.argv[2], 'utf8').replace(/^﻿/, '')).log.entries;
const parse = t => { try { return JSON.parse(t); } catch { return undefined; } };
let rejected = 0, confirmed = 0, noObservation = 0, sameAsBefore = 0;
const lastSeen = new Map();
const examples = [];
entries.forEach((e, i) => {
  const path = new URL(e.request.url).pathname;
  const m = path.match(/^\/api\/orders\/(\d+)\/status$/);
  const body = parse(e.response.content?.text);
  if (e.method !== 'PATCH' && e.request.method === 'PATCH' && m && e.response.status === 409) {
    rejected++;
    const id = m[1];
    const requested = parse(e.request.postData?.text)?.status;
    if (lastSeen.get(id) === requested) { sameAsBefore++; return; }
    const later = entries.slice(i + 1).find(x => {
      const p = new URL(x.request.url).pathname;
      return x.response.status === 200 && (p === `/api/orders/${id}` || /^\/api\/users\/[^/]+\/orders$/.test(p)) &&
        [].concat(parse(x.response.content?.text) ?? []).some(o => String(o?.id) === id);
    });
    if (!later) { noObservation++; return; }
    const seen = [].concat(parse(later.response.content?.text)).find(o => String(o?.id) === id);
    if (seen.status === requested) { confirmed++; if (examples.length < 3) examples.push(`#${i} PATCH ${id} -> ${requested} answered 409; #${entries.indexOf(later)} read shows ${seen.status}`); }
  }
  if (e.response.status >= 200 && e.response.status < 300) for (const o of [].concat(body ?? [])) if (o?.id !== undefined && o?.status) lastSeen.set(String(o.id), o.status);
});
console.log(`409 responses: ${rejected}; requested status equal to last observed (no visible change): ${sameAsBefore}; no later read of that order: ${noObservation}; later read shows the rejected status persisted: ${confirmed}`);
examples.forEach(x => console.log('  ' + x));
