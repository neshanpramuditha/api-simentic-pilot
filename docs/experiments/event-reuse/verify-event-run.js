// Ground-truth check of one Event API matrix run, independent of the semantic validator and of the Postman collection.
//
// 1. Traffic (traffic.har.json): separates read-backs (validator: GET just before a transition; Postman: GET just
//    after a transition) from scenario requests and checks the scenario requests are the 19 scenario steps in order.
// 2. Oracle: every violation that actually happened, from traffic + final database (db-final.json), using the
//    contract (contract-design.md / OpenAPI): the transition table is parsed from the OpenAPI description of
//    transitionRegistration; invariants 1-4 of the contract; persisted state of each transition from the next read of
//    the registration or the final database. Also: hidden state changes (final database vs what 2xx responses
//    claimed), whether the mode's defect was exercised, the cause steps, and "affected" steps (an oracle violation, or
//    a status/state that differs from the scenario's clean expectation).
// 3. Tools present in the run directory are compared with the oracle: semantic validator reports (key level: rule @
//    step # item, with FN reasons), Postman CLI output (assertion tag @ step # item), Ajv schema results.
//    A tool failure on a step that is not affected is a false positive.
// Usage: node verify-event-run.js <run dir> <scenario.json> <openapi.json>
const fs = require('fs');
const path = require('path');
const [runDir, scenarioFile, specFile] = process.argv.slice(2);
const readText = f => { const b = fs.readFileSync(f); return (b[0] === 0xff && b[1] === 0xfe ? b.toString('utf16le') : b.toString('utf8')).replace(/^﻿/, ''); };
const has = f => fs.existsSync(path.join(runDir, f));
const read = f => JSON.parse(readText(path.join(runDir, f)));
const run = read('run.json');
const scenario = JSON.parse(readText(scenarioFile));
const spec = JSON.parse(readText(specFile));
const har = read('traffic.har.json');
const dbFinal = read('db-final.json');
const parse = t => { if (t == null || t === '') return null; try { return JSON.parse(t); } catch { return t; } };
const canon = v => Array.isArray(v) ? v.map(canon) : v && typeof v === 'object' ? Object.fromEntries(Object.keys(v).sort().map(k => [k, canon(v[k])])) : v === undefined ? null : v;
const deepEqual = (a, b) => JSON.stringify(canon(a)) === JSON.stringify(canon(b));
const ok2xx = s => s >= 200 && s < 300;
const set = v => v !== null && v !== undefined;
// Exact ordering of RFC 3339 UTC timestamps ("...Z") at full precision: pad the fraction to 9 digits.
const norm = s => { const m = /^(.*T\d{2}:\d{2}:\d{2})(?:\.(\d+))?Z$/.exec(s); return m ? m[1] + '.' + (m[2] || '').padEnd(9, '0') : s; };
const notAfter = (a, b) => norm(a) <= norm(b);

// --- contract ---
const ops = {};
for (const [template, item] of Object.entries(spec.paths)) {
  for (const [method, op] of Object.entries(item)) ops[op.operationId] = { method: method.toUpperCase(), regex: new RegExp('^' + template.replace(/\{[^}]+\}/g, '([^/]+)') + '$'), description: op.description || '' };
}
const ALLOWED = {};
for (const m of ops.transitionRegistration.description.matchAll(/([A-Z_]+) -> ([A-Z_ |]+?)[;.]/g)) ALLOWED[m[1]] = m[2].split('|').map(s => s.trim());
for (const m of ops.transitionRegistration.description.matchAll(/([A-Z_]+) and ([A-Z_]+) are final/g)) { ALLOWED[m[1]] = []; ALLOWED[m[2]] = []; }
const STATES = spec.components.schemas.Registration.properties.state.enum;

// --- 1. traffic ---
const entries = har.log.entries.map((e, i) => {
  const u = new URL(e.request.url);
  return { i, method: e.request.method, path: u.pathname, reqBody: parse(e.request.postData && e.request.postData.text), status: e.response.status, body: parse(e.response.content && e.response.content.text) };
});
const fetchId = p => (p.match(/^\/v1\/registrations\/([^/]+)$/) || [])[1];
const transitionId = p => (p.match(/^\/v1\/registrations\/([^/]+)\/transitions$/) || [])[1];
const regId = p => fetchId(p) || transitionId(p);
entries.forEach((e, k) => {
  const prev = entries[k - 1], next = entries[k + 1];
  e.readBack = e.method === 'GET' && !!fetchId(e.path) && (run.type === 'postman'
    ? !!prev && prev.method === 'POST' && transitionId(prev.path) === fetchId(e.path) && !prev.readBack
    : run.type.startsWith('semval') && !!next && next.method === 'POST' && transitionId(next.path) === fetchId(e.path));
});
const steps = entries.filter(e => !e.readBack);
const readBacks = entries.filter(e => e.readBack);
const trafficProblems = [];
if (steps.length !== scenario.steps.length) trafficProblems.push(`scenario requests ${steps.length} != ${scenario.steps.length}`);
scenario.steps.forEach((s, k) => { const op = ops[s.operationId], e = steps[k]; if (!e || e.method !== op.method || !op.regex.test(e.path)) trafficProblems.push(`step ${k} not matched`); });
const transitions = steps.filter(e => transitionId(e.path)).length;
if (run.type !== 'capture' && readBacks.length !== transitions) trafficProblems.push(`read-backs ${readBacks.length} != transitions ${transitions}`);
if (run.type === 'capture' && readBacks.length) trafficProblems.push('passive capture contains read-backs');

// --- 2. oracle ---
const regsIn = body => !body || typeof body !== 'object' ? [] : Array.isArray(body.items) ? body.items : body.registrationId ? [body] : [];
const snap = r => ({ state: r.state, confirmedAt: set(r.confirmedAt), checkedInAt: set(r.checkedInAt), cancelledAt: set(r.cancelledAt), cancellationReason: r.cancellationReason ?? null });
const violations = [];
const add = (rule, step, item, kind, detail) => violations.push({ key: `${rule}@${step}${item == null ? '' : '#' + item}`, rule, step, item, kind, detail });
steps.forEach((e, k) => {
  if (!ok2xx(e.status)) return;
  const env = e.body && Array.isArray(e.body.items);
  regsIn(e.body).forEach((r, j) => {
    const it = env ? j : null;
    if (set(r.checkedInAt) !== (r.state === 'CHECKED_IN')) add('EVT-INV-001', k, it, 'invariant', `state=${r.state} checkedInAt=${r.checkedInAt}`);
    if (set(r.cancelledAt) !== (r.state === 'CANCELLED')) add('EVT-INV-002', k, it, 'invariant', `state=${r.state} cancelledAt=${r.cancelledAt}`);
    if (set(r.cancellationReason) !== (r.state === 'CANCELLED')) add('EVT-INV-003', k, it, 'invariant', `state=${r.state} cancellationReason=${r.cancellationReason}`);
    if ((r.state === 'CHECKED_IN' && !set(r.confirmedAt)) || (set(r.confirmedAt) && !['CONFIRMED', 'CHECKED_IN', 'CANCELLED'].includes(r.state))) add('EVT-INV-004', k, it, 'invariant', `state=${r.state} confirmedAt=${r.confirmedAt}`);
    if ((set(r.confirmedAt) && !notAfter(r.requestedAt, r.confirmedAt)) || (set(r.confirmedAt) && set(r.checkedInAt) && !notAfter(r.confirmedAt, r.checkedInAt))) add('EVT-INV-005', k, it, 'invariant', `requestedAt=${r.requestedAt} confirmedAt=${r.confirmedAt} checkedInAt=${r.checkedInAt}`);
  });
});
const observations = [];
entries.forEach(e => { if (ok2xx(e.status)) regsIn(e.body).forEach(r => observations.push({ entry: e.i, id: r.registrationId, snap: snap(r) })); });
const lastObservedBefore = (id, i) => observations.filter(o => o.id === id && o.entry < i).pop();
const finalRow = id => { const r = dbFinal.find(x => x.registration_id === id); return r && { state: r.state, confirmedAt: set(r.confirmed_at), checkedInAt: set(r.checked_in_at), cancelledAt: set(r.cancelled_at), cancellationReason: r.cancellation_reason ?? null }; };
function persistedAfter(id, i) {
  const nextT = entries.find(x => x.i > i && x.method === 'POST' && transitionId(x.path) === id);
  const limit = nextT ? nextT.i : Infinity;
  const r = observations.find(o => o.id === id && o.entry > i && o.entry < limit && entries[o.entry].method === 'GET');
  if (r) return { snap: r.snap, source: `traffic entry ${r.entry}` };
  if (!nextT) { const row = finalRow(id); return row ? { snap: row, source: 'final database' } : null; }
  return null;
}
steps.forEach((e, k) => {
  const id = transitionId(e.path);
  if (!id) return;
  const to = e.reqBody && e.reqBody.to;
  const before = lastObservedBefore(id, e.i);
  if (!before || !STATES.includes(to)) return;
  const from = before.snap.state;
  const allowed = (ALLOWED[from] || []).includes(to);
  if (ok2xx(e.status) && !allowed) add('EVT-ST-001', k, null, 'transition', `${from} -> ${to} accepted`);
  else if (ok2xx(e.status) && e.body.state !== to) add('EVT-ST-001', k, null, 'transition', `${from} -> ${to} returned ${e.body.state}`);
  else if (e.status === 422 && allowed) add('EVT-ST-001', k, null, 'transition', `allowed ${from} -> ${to} rejected with 422`);
  const p = persistedAfter(id, e.i);
  if (!p) return;
  if (e.status >= 400 && e.status < 500 && !deepEqual(p.snap, before.snap)) add('EVT-DB-001', k, null, 'db-rejected', `HTTP ${e.status} but stored ${JSON.stringify(before.snap)} -> ${JSON.stringify(p.snap)} (${p.source})`);
  if (ok2xx(e.status) && (p.snap.state !== e.body.state || p.snap.cancellationReason !== (e.body.cancellationReason ?? null))) add('EVT-DB-001', k, null, 'db-success', `response state ${e.body.state} but stored ${p.snap.state} (${p.source})`);
});
const claimed = {};
steps.forEach(e => { if (ok2xx(e.status) && e.method === 'POST' && e.body && e.body.registrationId) claimed[e.body.registrationId] = snap(e.body); });
const hiddenStateChanges = dbFinal.filter(r => claimed[r.registration_id] && !deepEqual(claimed[r.registration_id], finalRow(r.registration_id)))
  .map(r => ({ id: r.registration_id, claimed: claimed[r.registration_id], stored: finalRow(r.registration_id) }));
const dbRowsCheckedInWithoutTime = dbFinal.filter(r => r.state === 'CHECKED_IN' && !set(r.checked_in_at)).length;
const dbRowsOutOfOrder = dbFinal.filter(r => set(r.confirmed_at) && set(r.checked_in_at) && !notAfter(r.confirmed_at.replace(/\+00:00$/, 'Z'), r.checked_in_at.replace(/\+00:00$/, 'Z'))).length;

const signature = {
  'evt-st01': violations.some(v => v.kind === 'transition' && /allowed WAITLISTED -> CONFIRMED rejected/.test(v.detail)),
  'evt-db01': violations.some(v => v.kind === 'db-rejected'),
  'evt-db02': violations.some(v => v.kind === 'db-success') || hiddenStateChanges.some(h => h.claimed.state === 'CANCELLED'),
  'evt-ri01': violations.some(v => v.rule === 'EVT-INV-001' && /state=CHECKED_IN checkedInAt=null/.test(v.detail)) || dbRowsCheckedInWithoutTime > 0,
  'evt-cf01': violations.some(v => v.rule === 'EVT-INV-005') || dbRowsOutOfOrder > 0,
};
const causeOf = {
  'evt-st01': v => v.kind === 'transition',
  'evt-db01': v => v.kind === 'db-rejected',
  'evt-db02': v => v.kind === 'db-success',
  'evt-ri01': v => v.rule === 'EVT-INV-001' && !!transitionId(steps[v.step].path),
  'evt-cf01': v => v.rule === 'EVT-INV-005' && !!transitionId(steps[v.step].path),
};
const causeSteps = [...new Set(violations.filter(causeOf[run.mode] || (() => false)).map(v => v.step))].sort((a, b) => a - b);
const deviations = [];
scenario.steps.forEach((s, k) => {
  const e = steps[k], x = s.expect;
  if (!e) return;
  if (e.status !== x.status) deviations.push({ step: k, detail: `status ${e.status} != ${x.status}` });
  else if (x.state && e.body.state !== x.state) deviations.push({ step: k, detail: `state ${e.body.state} != ${x.state}` });
  else if (x.listStates && JSON.stringify(e.body.items.map(r => r.state)) !== JSON.stringify(x.listStates)) deviations.push({ step: k, detail: `list ${e.body.items.map(r => r.state)} != ${x.listStates}` });
});
const affected = new Set([...violations.map(v => v.step), ...deviations.map(d => d.step)]);

// --- 3. tools ---
const oracleKeys = new Set(violations.map(v => v.key));
function semvalTool(file) {
  const rep = read(file);
  const keyOf = f => `${f.ruleId}@${f.exchangeIndex}${f.evidence && f.evidence.itemIndex != null ? '#' + f.evidence.itemIndex : ''}`;
  const fails = rep.findings.filter(f => f.outcome === 'FAIL');
  const failKeys = [...new Set(fails.map(keyOf))];
  const fn = violations.filter(v => !failKeys.includes(v.key)).map(v => {
    const own = rep.findings.filter(f => f.ruleId === v.rule && f.exchangeIndex === v.step);
    const reason = !rep.byRule[v.rule] ? 'rule absent from this rule set'
      : own.length && own.every(f => f.outcome === 'NOT_APPLICABLE') ? 'not evaluable: ' + own[0].message
      : v.item != null && !own.some(f => f.evidence && f.evidence.itemIndex === v.item) && own.length === 1 ? 'list item not checked (one finding for the whole envelope)'
      : 'missed (' + [...new Set(own.map(f => f.outcome))].join(',') + ')';
    return { key: v.key, reason };
  });
  const extra = failKeys.filter(k => !oracleKeys.has(k));
  const evidenceProblems = [];
  for (const f of fails) {
    const e = steps[f.exchangeIndex], ev = f.evidence || {};
    if (!e || !ev.request || ev.request.method !== e.method || ev.request.url !== e.path || !deepEqual(ev.request.body, e.reqBody)) evidenceProblems.push(`${keyOf(f)} request`);
    if (!e || !ev.response || ev.response.status !== e.status || !deepEqual(ev.response.body, e.body)) evidenceProblems.push(`${keyOf(f)} response`);
    if (f.ruleId === 'EVT-ST-001' && run.type !== 'capture') {
      const rb = entries[e.i - 1];
      if (!ev.state || !rb || !rb.readBack || !deepEqual(ev.state.readBack && ev.state.readBack.body, rb.body)) evidenceProblems.push(`${keyOf(f)} read-back`);
    }
    if (f.ruleId === 'EVT-DB-001') {
      const p = persistedAfter(transitionId(e.path), e.i);
      if (!ev.database || !ev.database.rowAfter || !p || ev.database.rowAfter.state !== p.snap.state) evidenceProblems.push(`${keyOf(f)} database rowAfter`);
    }
  }
  const failSteps = [...new Set(fails.map(f => f.exchangeIndex))].sort((a, b) => a - b);
  return { exitCode: null, totals: rep.totals, failsByRule: Object.fromEntries(Object.entries(rep.byRule).map(([r, m]) => [r, m.FAIL])), failKeys, tp: failKeys.filter(k => oracleKeys.has(k)).length,
    extraKeys: extra.map(k => ({ key: k, onAffectedStep: affected.has(Number(k.split('@')[1].split('#')[0])) })), fn, failSteps,
    falsePositiveSteps: failSteps.filter(s => !affected.has(s)), detectedAtCause: failSteps.some(s => causeSteps.includes(s)), evidenceProblems, fails: fails.length };
}
function postmanTool() {
  const lines = readText(path.join(runDir, 'postman.txt')).split(/\r?\n/);
  const fails = [];
  let step = null;
  for (const line of lines) {
    const root = /^Root (\d+) /.exec(line);
    if (root) { step = Number(root[1]); continue; }
    const f = /^\s+\d+\.\s+\[([A-Z0-9-]+|schema)\]\s+(.*)$/.exec(line);
    if (f && step != null) { const it = /\(item (\d+)\)/.exec(f[2]); fails.push({ tag: f[1], step, item: it ? Number(it[1]) : null, name: f[2].trim() }); }
  }
  const summary = /\|\s+assertions \|\s+(\d+) \|\s+(\d+) \|/.exec(lines.join('\n'));
  const failSteps = [...new Set(fails.map(f => f.step))].sort((a, b) => a - b);
  const keyed = fails.map(f => (f.tag.startsWith('INV-') ? 'EVT-' + f.tag : f.tag) + '@' + f.step + (f.item == null ? '' : '#' + f.item));
  return { assertions: summary ? Number(summary[1]) : null, failedAssertions: summary ? Number(summary[2]) : null, parsedFailures: fails.length, failsByTag: fails.reduce((m, f) => (m[f.tag] = (m[f.tag] || 0) + 1, m), {}),
    fails: keyed, failSteps, falsePositiveSteps: failSteps.filter(s => !affected.has(s)), detectedAtCause: failSteps.some(s => causeSteps.includes(s)),
    invariantKeysMatchOracle: keyed.filter(k => k.startsWith('EVT-INV')).every(k => oracleKeys.has(k)) && violations.filter(v => v.kind === 'invariant').every(v => keyed.includes(v.key)) };
}
function ajvTool() {
  const a = read('ajv.json');
  const failSteps = [...new Set(a.violations.map(v => v.entry))].sort((x, y) => x - y);
  return { checked: a.checked, violations: a.violations.length, failSteps, falsePositiveSteps: failSteps.filter(s => !affected.has(s)), detectedAtCause: failSteps.some(s => causeSteps.includes(s)) };
}
const tools = {};
if (has('semval.json')) tools[run.type.replace('-jdbc', '-active')] = { ...semvalTool('semval.json'), exitCode: run.exitCode, durationMs: run.durationMs };
if (has('semval-v01-passive.json')) tools['semval-v01-passive'] = { ...semvalTool('semval-v01-passive.json'), exitCode: run.exitCodeV01Passive, durationMs: run.durationMsV01Passive };
if (has('semval-v02-passive.json')) tools['semval-v02-passive'] = { ...semvalTool('semval-v02-passive.json'), exitCode: run.exitCodeV02Passive, durationMs: run.durationMsV02Passive };
if (has('ajv.json')) tools.ajv = { ...ajvTool(), durationMs: run.durationMsAjv };
if (has('postman.txt')) tools.postman = { ...postmanTool(), exitCode: run.exitCode, durationMs: run.durationMs };

const expectedLine = { 'evt-st01': 'DEFECT EVT-ST-01', 'evt-db01': 'DEFECT EVT-DB-01', 'evt-db02': 'DEFECT EVT-DB-02', 'evt-ri01': 'DEFECT EVT-RI-01', 'evt-cf01': 'DEFECT EVT-CF-01' };
const lines = [].concat(run.defectLines || []);
const result = {
  run: { mode: run.mode, type: run.type, repeat: run.repeat, defectSwitchOk: run.mode === 'clean' ? lines.length === 0 : lines.length === 1 && lines[0].startsWith(expectedLine[run.mode]) },
  traffic: { harEntries: entries.length, readBacks: readBacks.length, scenarioRequests: steps.length, problems: trafficProblems },
  oracle: { transitions: ALLOWED, violations, causeSteps, deviations, affectedSteps: [...affected].sort((a, b) => a - b), hiddenStateChanges, dbRowsCheckedInWithoutTime, dbRowsOutOfOrder,
    exercised: run.mode === 'clean' ? null : signature[run.mode], anyDefectSignature: Object.values(signature).some(Boolean) },
  tools,
};
fs.writeFileSync(path.join(runDir, 'verify.json'), JSON.stringify(result, null, 2));
console.log(`${run.mode}/${run.type}-r${run.repeat}: switch ${result.run.defectSwitchOk ? 'ok' : 'BAD'}, traffic ${trafficProblems.length ? trafficProblems.join('; ') : 'ok'}, exercised ${result.oracle.exercised}, oracle ${violations.length}, cause ${causeSteps}, affected ${result.oracle.affectedSteps} | `
  + Object.entries(tools).map(([t, x]) => `${t}: failSteps ${x.failSteps} FPsteps ${x.falsePositiveSteps} cause ${x.detectedAtCause}${x.fn ? ' FN ' + x.fn.length : ''}`).join(' | '));
