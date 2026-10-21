// Ground-truth check of one matrix run, independent of the semantic validator's code.
//
// Inputs (in <run dir>): run.json (runner metadata), traffic.har.json (recording proxy in active runs: every request
// the validator sent, including its read-back GETs; capture-har.js in passive runs), semval.json (validator report),
// db-final.json (DumpLoans.java, final database state).
//
// 1. Traffic: separates the validator's read-backs from scenario requests and checks that the scenario requests are
//    exactly the scenario steps (operation, path) in order, and that the report covers the same exchanges.
// 2. Oracle: derives every semantic violation that actually happened from traffic + final database, with its own
//    implementation: invariants on response bodies, the transition table parsed from the OpenAPI description of
//    updateLoanStatus (not from the rules file), state changes after rejected requests seen by later observations,
//    and the final database compared with what the API claimed in its successful responses.
// 3. Compares the oracle with the validator's FAIL findings (TP / FP / FN, FN split into "not evaluable in this
//    configuration" vs "missed"), and checks that each FAIL's evidence equals the recorded traffic.
// Usage: node verify-run.js <run dir> <scenario.json> <openapi.json>
const fs = require('fs');
const path = require('path');
const [runDir, scenarioFile, specFile] = process.argv.slice(2);
// PowerShell 5.1 redirection writes UTF-16LE and Set-Content writes a UTF-8 BOM; accept both.
const readText = file => {
  const b = fs.readFileSync(file);
  const text = b[0] === 0xff && b[1] === 0xfe ? b.toString('utf16le') : b.toString('utf8');
  return text.replace(/^﻿/, '');
};
const read = f => JSON.parse(readText(path.join(runDir, f)));
const run = read('run.json');
const scenario = JSON.parse(fs.readFileSync(scenarioFile, 'utf8'));
const spec = JSON.parse(fs.readFileSync(specFile, 'utf8'));
const har = read('traffic.har.json');
const report = fs.existsSync(path.join(runDir, 'semval.json')) ? read('semval.json') : null;
const dbFinal = read('db-final.json');

const parse = t => { if (t == null || t === '') return null; try { return JSON.parse(t); } catch { return t; } };
const deepEqual = (a, b) => JSON.stringify(canon(a)) === JSON.stringify(canon(b));
function canon(v) {
  if (Array.isArray(v)) return v.map(canon);
  if (v && typeof v === 'object') return Object.fromEntries(Object.keys(v).sort().map(k => [k, canon(v[k])]));
  return v === undefined ? null : v;
}
const ok2xx = s => s >= 200 && s < 300;

// --- operations and documented transition table ---
const ops = {};
for (const [template, item] of Object.entries(spec.paths)) {
  for (const [method, op] of Object.entries(item)) {
    ops[op.operationId] = { method: method.toUpperCase(), template, regex: new RegExp('^' + template.replace(/\{[^}]+\}/g, '([^/]+)') + '$'), description: op.description || '' };
  }
}
const ALLOWED = {};
const desc = ops.updateLoanStatus.description;
for (const m of desc.matchAll(/([A-Z]+) -> ([A-Z |]+?)[;.]/g)) ALLOWED[m[1]] = m[2].split('|').map(s => s.trim());
for (const m of desc.matchAll(/([A-Z]+) and ([A-Z]+) are final/g)) { ALLOWED[m[1]] = []; ALLOWED[m[2]] = []; }
const STATES = spec.components.schemas.Loan.properties.status.enum;

// --- 1. traffic ---
const entries = har.log.entries.map((e, i) => {
  const u = new URL(e.request.url);
  return { i, method: e.request.method, path: u.pathname, reqBody: parse(e.request.postData && e.request.postData.text),
    status: e.response.status, body: parse(e.response.content && e.response.content.text) };
});
const loanIdOf = p => (p.match(/^\/api\/loans\/([^/]+)(\/status)?$/) || [])[1];
for (let k = 0; k < entries.length; k++) {
  const e = entries[k], next = entries[k + 1];
  e.readBack = e.method === 'GET' && /^\/api\/loans\/[^/]+$/.test(e.path) && !!next && next.method === 'PATCH'
    && next.path === e.path + '/status';
}
const steps = entries.filter(e => !e.readBack);
const readBacks = entries.filter(e => e.readBack);
const trafficProblems = [];
if (steps.length !== scenario.steps.length) trafficProblems.push(`scenario requests ${steps.length} != steps ${scenario.steps.length}`);
scenario.steps.forEach((s, k) => {
  const op = ops[s.operationId], e = steps[k];
  if (!e || e.method !== op.method || !op.regex.test(e.path)) trafficProblems.push(`step ${k} (${s.operationId}) not matched by ${e ? e.method + ' ' + e.path : 'nothing'}`);
});
if (run.config === 'semval-passive' && readBacks.length) trafficProblems.push(`passive traffic contains ${readBacks.length} read-backs`);
if (run.config !== 'semval-passive') {
  const patches = steps.filter(e => e.method === 'PATCH').length;
  if (readBacks.length !== patches) trafficProblems.push(`read-backs ${readBacks.length} != PATCH requests ${patches}`);
}
if (report && (report.exchanges !== scenario.steps.length || report.unmatchedRequests !== 0)) {
  trafficProblems.push(`report exchanges ${report.exchanges}, unmatched ${report.unmatchedRequests}`);
}

// --- 2. oracle ---
const loansIn = body => (Array.isArray(body) ? body : body && typeof body === 'object' && body.id ? [body] : []);
const snapshot = l => ({ status: l.status, issuedAt: l.issuedAt != null, returnedAt: l.returnedAt != null });
const violations = []; // {key, ruleId, step, item, kind, detail}
const add = (ruleId, step, item, kind, detail) => violations.push({ key: `${ruleId}@${step}${item == null ? '' : '#' + item}`, ruleId, step, item, kind, detail });

steps.forEach((e, k) => {
  if (!ok2xx(e.status)) return;
  const array = Array.isArray(e.body);
  loansIn(e.body).forEach((l, j) => {
    if ((l.returnedAt != null) !== (l.status === 'RETURNED')) add('LIB-INV-001', k, array ? j : null, 'invariant', `status=${l.status} returnedAt=${l.returnedAt}`);
    if ((l.issuedAt != null) !== (l.status === 'ISSUED' || l.status === 'RETURNED')) add('LIB-INV-002', k, array ? j : null, 'invariant', `status=${l.status} issuedAt=${l.issuedAt}`);
  });
});

// Observations of each loan in traffic order (every 2xx body that contains it, read-backs included).
const observations = [];
entries.forEach(e => { if (ok2xx(e.status)) loansIn(e.body).forEach(l => observations.push({ entry: e.i, id: l.id, snap: snapshot(l) })); });
const lastObservedBefore = (id, entryIndex) => observations.filter(o => o.id === id && o.entry < entryIndex).pop();
// State persisted by the request at entryIndex: the first read (GET, read-back or list) of the loan after it and before
// the next status change request for the same loan; if the loan is never changed again, the final database row.
function persistedAfter(id, entryIndex) {
  const nextPatch = entries.find(x => x.i > entryIndex && x.method === 'PATCH' && loanIdOf(x.path) === id);
  const limit = nextPatch ? nextPatch.i : Infinity;
  const read = observations.find(o => o.id === id && o.entry > entryIndex && o.entry < limit && entries[o.entry].method === 'GET');
  if (read) return { snap: read.snap, source: `observed in traffic entry ${read.entry}` };
  if (!nextPatch) { const row = finalRow(id); return row ? { snap: row, source: 'final database' } : null; }
  return null;
}
const finalRow = id => { const r = dbFinal.find(x => x.id === id); return r && { status: r.status, issuedAt: r.issuedAt != null, returnedAt: r.returnedAt != null }; };

const causes = [];
steps.forEach((e, k) => {
  if (e.method !== 'PATCH') return;
  const id = loanIdOf(e.path);
  const target = e.reqBody && e.reqBody.status;
  const before = lastObservedBefore(id, e.i);
  if (!before || !STATES.includes(target)) return;
  const prev = before.snap.status;
  const allowed = (ALLOWED[prev] || []).includes(target);
  if (ok2xx(e.status)) {
    if (!allowed) add('LIB-ST-001', k, null, 'transition', `${prev} -> ${target} accepted with ${e.status}`);
    else if (!e.body || e.body.status !== target) add('LIB-ST-001', k, null, 'transition', `${prev} -> ${target} returned ${e.body && e.body.status}`);
  } else if (e.status === 409 && allowed) {
    add('LIB-ST-001', k, null, 'transition', `allowed ${prev} -> ${target} rejected`);
  }
  // Persisted effect of this request, from later reads or the final database.
  const persisted = persistedAfter(id, e.i);
  if (!persisted) return;
  const after = persisted.snap, afterSource = persisted.source;
  if (e.status >= 400 && e.status < 500) {
    if (!deepEqual(after, before.snap)) add('LIB-DB-001', k, null, 'db', `HTTP ${e.status} but loan changed ${JSON.stringify(before.snap)} -> ${JSON.stringify(after)} (${afterSource})`);
  } else if (ok2xx(e.status) && e.body && after.status !== e.body.status) {
    add('LIB-DB-001', k, null, 'db', `response status ${e.body.status} but persisted ${after.status} (${afterSource})`);
  }
});

// What the API claimed in its successful create / status-change responses, vs the final database.
const claimed = {};
steps.forEach(e => { if (ok2xx(e.status) && (e.method === 'POST' || e.method === 'PATCH') && e.body && e.body.id) claimed[e.body.id] = snapshot(e.body); });
const hiddenStateChanges = dbFinal.filter(r => claimed[r.id] && !deepEqual(claimed[r.id], finalRow(r.id)))
  .map(r => ({ id: r.id, claimed: claimed[r.id], persisted: finalRow(r.id) }));
const dbInvariantViolations = dbFinal.filter(r => (r.returnedAt != null) !== (r.status === 'RETURNED')
  || (r.issuedAt != null) !== (r.status === 'ISSUED' || r.status === 'RETURNED')).map(r => ({ id: r.id, ...finalRow(r.id) }));
// Where each defect acts (the request that causes the violation), as opposed to later requests that only expose it.
const causeKinds = {
  'lib-st01': v => v.kind === 'transition',
  'lib-db01': v => v.kind === 'db',
  'lib-ri01': v => v.ruleId === 'LIB-INV-001' && steps[v.step].method === 'PATCH',
};
violations.filter(causeKinds[run.mode] || (() => false)).forEach(v => causes.push(v.step));

const exercised = {
  'lib-st01': violations.some(v => v.kind === 'transition' && /^REQUESTED -> ISSUED accepted/.test(v.detail)),
  'lib-db01': hiddenStateChanges.length > 0 || violations.some(v => v.kind === 'db'),
  'lib-ri01': violations.some(v => v.ruleId === 'LIB-INV-001' && /status=RETURNED returnedAt=null/.test(v.detail))
    || dbFinal.some(r => r.status === 'RETURNED' && r.returnedAt == null),
};

// --- 3. validator vs oracle ---
const findings = report ? report.findings : [];
const keyOf = f => `${f.ruleId}@${f.exchangeIndex}${f.evidence && f.evidence.itemIndex != null ? '#' + f.evidence.itemIndex : ''}`;
const fails = findings.filter(f => f.outcome === 'FAIL');
const failKeys = new Set(fails.map(keyOf));
const oracleKeys = new Set(violations.map(v => v.key));
const tp = [...failKeys].filter(k => oracleKeys.has(k));
const fp = [...failKeys].filter(k => !oracleKeys.has(k));
const fn = violations.filter(v => !failKeys.has(v.key)).map(v => {
  const own = findings.filter(f => f.ruleId === v.ruleId && f.exchangeIndex === v.step);
  const na = own.find(f => f.outcome === 'NOT_APPLICABLE');
  return { ...v, validator: own.map(f => f.outcome).join(',') || 'none', notEvaluable: !!na && !own.some(f => f.outcome === 'PASS'), reason: na ? na.message : null };
});

const evidenceProblems = [];
for (const f of fails) {
  const e = steps[f.exchangeIndex];
  const ev = f.evidence || {};
  if (!e) { evidenceProblems.push(`${keyOf(f)}: no such exchange`); continue; }
  if (!ev.request || ev.request.method !== e.method || ev.request.url !== e.path || !deepEqual(ev.request.body, e.reqBody)) evidenceProblems.push(`${keyOf(f)}: request differs from traffic`);
  if (!ev.response || ev.response.status !== e.status || !deepEqual(ev.response.body, e.body)) evidenceProblems.push(`${keyOf(f)}: response differs from traffic`);
  if (f.ruleId === 'LIB-ST-001') {
    if (!ev.state || !ev.state.previous) evidenceProblems.push(`${keyOf(f)}: no state evidence`);
    const rb = entries[e.i - 1];
    if (run.config !== 'semval-passive' && (!rb || !rb.readBack || !deepEqual(ev.state && ev.state.readBack && ev.state.readBack.body, rb.body))) evidenceProblems.push(`${keyOf(f)}: read-back evidence differs from traffic`);
  }
  if (f.ruleId === 'LIB-DB-001') {
    const db = ev.database;
    if (!db || !db.rowBefore || !db.rowAfter) { evidenceProblems.push(`${keyOf(f)}: no database evidence`); continue; }
    const id = loanIdOf(e.path);
    const persisted = persistedAfter(id, e.i);
    const truth = persisted ? persisted.snap.status : null;
    if (truth == null) evidenceProblems.push(`${keyOf(f)}: no ground truth for the persisted state`);
    else if (db.rowAfter.status !== truth) evidenceProblems.push(`${keyOf(f)}: rowAfter.status ${db.rowAfter.status} != ground truth ${truth}`);
    const before = lastObservedBefore(id, e.i);
    if (before && db.rowBefore.status !== before.snap.status) evidenceProblems.push(`${keyOf(f)}: rowBefore.status ${db.rowBefore.status} != observed ${before.snap.status}`);
  }
}

// Database rule outcomes: genuine evidence (PASS/FAIL) only with a JDBC connection; otherwise NOT_APPLICABLE with
// the reason stated.
const dbFindings = findings.filter(f => f.ruleId === 'LIB-DB-001');
const dbOutcomes = {};
dbFindings.forEach(f => { const m = `${f.outcome}: ${f.message.replace(/HTTP \d+/, 'HTTP n').replace(/ \S+ -> \S+/, '')}`; dbOutcomes[m] = (dbOutcomes[m] || 0) + 1; });
const dbEvidenceCorrectlyScoped = run.config === 'semval-jdbc'
  ? dbFindings.every(f => f.outcome !== 'NOT_APPLICABLE' || /no row before the request/.test(f.message))
  : dbFindings.length > 0 && dbFindings.every(f => f.outcome === 'NOT_APPLICABLE' && /no database access/.test(f.message));

const causeSteps = [...new Set(causes)].sort((a, b) => a - b);
const result = {
  run: { mode: run.mode, config: run.config, repeat: run.repeat, exitCode: run.exitCode, durationMs: run.durationMs, defectLines: run.defectLines },
  traffic: { harEntries: entries.length, readBacks: readBacks.length, scenarioRequests: steps.length, problems: trafficProblems },
  oracle: { transitions: ALLOWED, violations, causeSteps, hiddenStateChanges, dbInvariantViolations, exercised: run.mode === 'clean' ? null : exercised[run.mode],
    anyDefectSignature: Object.values(exercised).some(Boolean) },
  validator: report ? { totals: report.totals, byRule: report.byRule, failKeys: [...failKeys] } : null,
  comparison: { tp, fp, fn, detectedAtCause: fails.some(f => causeSteps.includes(f.exchangeIndex)), failSteps: [...new Set(fails.map(f => f.exchangeIndex))].sort((a, b) => a - b) },
  evidence: { problems: evidenceProblems, failsWithFullEvidence: fails.length - new Set(evidenceProblems.map(p => p.split(':')[0])).size, fails: fails.length },
  database: { outcomes: dbOutcomes, correctlyScoped: dbEvidenceCorrectlyScoped },
};
fs.writeFileSync(path.join(runDir, 'verify.json'), JSON.stringify(result, null, 2));
console.log(`${run.mode}/${run.config}-r${run.repeat}: traffic ${trafficProblems.length ? 'PROBLEMS' : 'ok'}, oracle ${violations.length} violations, fails ${fails.length}, TP ${tp.length} FP ${fp.length} FN ${fn.length} (not evaluable ${fn.filter(x => x.notEvaluable).length}), evidence problems ${evidenceProblems.length}, db scoped ${dbEvidenceCorrectlyScoped}`);
