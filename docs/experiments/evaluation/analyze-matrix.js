// Summarises a run-matrix.ps1 output directory: per mode, tool and repeat, the verdict, the failures attributed to
// the run, and whether the injected defect was actually exercised (judged from the traffic, not from the tool).
// Usage: node analyze-matrix.js <matrix dir> <out summary.json> <out summary.md>
const fs = require('fs');
const path = require('path');
const [root, outJson, outMd] = process.argv.slice(2);
const runs = JSON.parse(fs.readFileSync(path.join(root, 'runs.json'), 'utf8').replace(/^﻿/, ''));
const read = f => fs.readFileSync(f, 'utf8').replace(/^﻿/, '');
const parse = t => { try { return JSON.parse(t); } catch { return undefined; } };
const first = (dir, prefix) => fs.readdirSync(dir).find(f => f.startsWith(prefix));

function schemathesisFailures(dir) {
  const lines = read(path.join(dir, 'stdout.txt')).split(/\r?\n/);
  const set = new Set();
  let op = null;
  for (const l of lines) {
    const m = l.match(/^_+ (\S+ \S+) _+$/);
    if (m) { op = m[1]; continue; }
    const t = l.match(/^- (.+)$/);
    if (t && op) set.add(`${op} :: ${t[1].trim()}`);
    if (/^=+ SUMMARY =+$/.test(l)) break;
  }
  return [...set].sort();
}

function harEntries(dir) {
  const f = first(dir, 'har-');
  return f ? JSON.parse(read(path.join(dir, f))).log.entries : [];
}

// Ground truth from traffic: did the defect's code path produce its effect in this run?
function exercised(mode, tool, dir) {
  if (mode === 'clean') return { exercised: null, note: 'no defect' };
  if (tool === 'ajv') {
    const r = JSON.parse(read(path.join(dir, 'responses.json')));
    return exercisedFromScenario(mode, r.map(x => ({ method: x.method, url: x.url, status: x.status, body: x.body })));
  }
  if (tool === 'postman' || tool === 'semval') {
    return { exercised: true, note: 'same scenario as ajv (requests 1/8 for SV-01, 6 for ST-01, 6 and 9 for DB-02)' };
  }
  const entries = harEntries(dir).map(e => ({ method: e.request.method, url: new URL(e.request.url).pathname, status: e.response.status, body: parse(e.response.content?.text), req: parse(e.request.postData?.text) }));
  if (mode.startsWith('sv01')) {
    const n = entries.filter(e => e.method === 'POST' && e.url === '/api/orders' && e.status === 201 && typeof e.body?.total === 'string').length;
    return { exercised: n > 0, note: `${n} POST 201 responses with total as a string` };
  }
  const seededFile = path.join(dir, 'seeded.json');
  const seeded = fs.existsSync(seededFile) ? JSON.parse(read(seededFile)).seeded.map(s => String(s.id)) : [];
  const snapFile = path.join(dir, 'snapshot-after.json');
  const snap = fs.existsSync(snapFile) ? JSON.parse(read(snapFile)) : [];
  const changedSeeded = snap.filter(s => s.order?.status !== 'COMPLETED').length;
  if (mode.startsWith('st01')) {
    // known state per id: seeded ids start COMPLETED; otherwise last state seen in a 2xx body
    const state = new Map(seeded.map(id => [id, 'COMPLETED']));
    let n = 0;
    for (const e of entries) {
      const m = e.url.match(/^\/api\/orders\/(\d+)\/status$/);
      if (e.method === 'PATCH' && m && state.get(m[1]) === 'COMPLETED' && e.req?.status === 'CANCELLED' && e.status === 200) n++;
      if (e.status >= 200 && e.status < 300) for (const o of Array.isArray(e.body) ? e.body : [e.body]) if (o?.id !== undefined && o?.status) state.set(String(o.id), o.status);
    }
    return { exercised: n > 0, note: `${n} accepted COMPLETED -> CANCELLED requests; seeded orders changed: ${changedSeeded}/${seeded.length}` };
  }
  if (mode.startsWith('db02')) {
    const n409 = entries.filter(e => e.method === 'PATCH' && /^\/api\/orders\/\d+\/status$/.test(e.url) && e.status === 409).length;
    return { exercised: n409 > 0, note: `${n409} PATCH 409 responses (each on an existing order, so each saved a forbidden status); seeded orders changed: ${changedSeeded}/${seeded.length}` };
  }
  return { exercised: null, note: '' };
}

function exercisedFromScenario(mode, r) {
  if (mode.startsWith('sv01')) {
    const n = r.filter(x => x.method === 'POST' && x.status === 201 && typeof x.body?.total === 'string').length;
    return { exercised: n > 0, note: `${n} POST 201 responses with total as a string` };
  }
  if (mode.startsWith('st01')) return { exercised: r[5].status === 200, note: `step 6 returned ${r[5].status}; step 7 read-back status ${r[6].body?.status}` };
  if (mode.startsWith('db02')) return { exercised: r[5].status === 409 && r[6].body?.status !== 'COMPLETED', note: `step 6 ${r[5].status}, read-back ${r[6].body?.status}; step 9 ${r[8].status}, read-back ${r[9].body?.status}` };
  return { exercised: null, note: '' };
}

function verdict(tool, dir) {
  if (tool === 'ajv') {
    const t = read(path.join(dir, 'result.txt'));
    const fails = t.split(/\r?\n/).filter(l => l.startsWith('FAIL')).map(l => l.replace(/\s+/g, ' '));
    return { failures: fails.length, details: fails };
  }
  if (tool === 'postman') {
    const t = read(path.join(dir, 'result.txt'));
    const m = t.match(/assertions \|\s+(\d+) \|\s+(\d+)/);
    const names = [...t.matchAll(/^\s+\d+\.\s+(AssertionError|Error)\s+(.+?)\s*$/gm)].map(x => `${x[1]}: ${x[2]}`);
    return { failures: m ? Number(m[2]) : null, assertions: m ? Number(m[1]) : null, details: names };
  }
  if (tool === 'semval') {
    const r = JSON.parse(read(path.join(dir, 'report.json')));
    return { failures: r.totals.FAIL, totals: r.totals, details: r.failures.map(f => `${f.ruleId} #${f.exchangeIndex}: ${f.message}`) };
  }
  const f = schemathesisFailures(dir);
  // passive rule reports over the run's HAR live in a sibling directory so the matrix output itself is never modified
  const passiveFile = path.join(root + '-passive', path.relative(root, dir), 'semval-passive.json');
  const passive = fs.existsSync(passiveFile) ? JSON.parse(read(passiveFile)) : null;
  return {
    failures: f.length, details: f,
    passiveRules: passive ? { totals: passive.totals, byRule: passive.byRule, failures: passive.failures.slice(0, 5).map(x => `${x.ruleId} #${x.exchangeIndex}: ${x.message}`) } : null,
  };
}

const results = runs.map(run => {
  const dir = path.join(root, run.dir);
  return { ...run, ...verdict(run.tool, dir), ...exercised(run.mode, run.tool, dir) };
});

// Schemathesis: separate findings already present in clean mode (same tool configuration) from new ones.
for (const tool of ['schemathesis-baseline', 'schemathesis-seeded']) {
  const clean = new Set(results.filter(r => r.tool === tool && r.mode === 'clean').flatMap(r => r.details));
  for (const r of results.filter(x => x.tool === tool)) {
    r.baselineFindings = r.details.filter(d => clean.has(d));
    r.newFindings = r.details.filter(d => !clean.has(d));
  }
}

fs.writeFileSync(outJson, JSON.stringify(results, null, 2));
const md = ['| Mode | Tool | Run | Time (s) | Defect exercised | Result | Details |', '|---|---|---|---|---|---|---|'];
for (const r of results) {
  const result = r.tool.startsWith('schemathesis')
    ? `${r.newFindings.length} new / ${r.baselineFindings.length} baseline findings`
    : r.tool === 'postman' ? `${r.failures} of ${r.assertions} assertions failed` : `${r.failures} failures`;
  const details = r.tool.startsWith('schemathesis')
    ? [...r.newFindings, r.passiveRules ? `passive rules: FAIL=${r.passiveRules.totals.FAIL} ${JSON.stringify(Object.fromEntries(Object.entries(r.passiveRules.byRule).map(([k, v]) => [k, v.FAIL])))}` : ''].filter(Boolean).join('<br>')
    : r.details.slice(0, 4).join('<br>');
  md.push(`| ${r.mode} | ${r.tool} | r${r.repeat} | ${r.seconds} | ${r.exercised === null ? '—' : r.exercised ? 'yes' : '**no**'}${r.note ? ` (${r.note})` : ''} | ${result} | ${details.replace(/\|/g, '\\|')} |`);
}
fs.writeFileSync(outMd, md.join('\n') + '\n');
console.log(md.join('\n'));
