// Aggregates verify.json of every Event API run into summary.json / summary.md: one row per mode x tool x repeat,
// reproducibility per mode x tool (failure keys identical across repeats; oracle and final database identical with
// ids and timestamps masked), and incomplete runs (run directories without verify.json).
// Usage: node summarize-event.js <matrix dir> <out summary.json> <out summary.md>
const fs = require('fs');
const path = require('path');
const [root, outJson, outMd] = process.argv.slice(2);
const readText = f => { const b = fs.readFileSync(f); return (b[0] === 0xff && b[1] === 0xfe ? b.toString('utf16le') : b.toString('utf8')).replace(/^﻿/, ''); };
const read = f => JSON.parse(readText(f));
const mask = s => s.replace(/REG-[A-Z0-9]{8}/g, '<id>').replace(/\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[+-]\d{2}(:\d{2})?)?/g, '<ts>');
const modes = ['clean', 'evt-st01', 'evt-db01', 'evt-db02', 'evt-ri01', 'evt-cf01'];
const toolOrder = ['semval-v01-active', 'semval-v02-active', 'semval-v01-passive', 'semval-v02-passive', 'postman', 'ajv'];

const runs = [], incomplete = [];
for (const mode of fs.readdirSync(root).filter(d => fs.statSync(path.join(root, d)).isDirectory())) {
  for (const dir of fs.readdirSync(path.join(root, mode)).sort()) {
    const runDir = path.join(root, mode, dir);
    if (!fs.existsSync(path.join(runDir, 'verify.json'))) { incomplete.push(`${mode}/${dir}`); continue; }
    const v = read(path.join(runDir, 'verify.json'));
    v.dbSignature = read(path.join(runDir, 'db-final.json')).map(r => `${r.attendee_ref}/${r.event_code}/${r.state}/${r.confirmed_at != null}/${r.checked_in_at != null}/${r.cancelled_at != null}/${r.cancellation_reason}`);
    runs.push(v);
  }
}
const rows = [];
for (const v of runs) {
  for (const [tool, t] of Object.entries(v.tools)) {
    rows.push({ mode: v.run.mode, tool, repeat: v.run.repeat, runType: v.run.type, switchOk: v.run.defectSwitchOk, trafficOk: v.traffic.problems.length === 0,
      exercised: v.oracle.exercised, oracleViolations: v.oracle.violations.length, causeSteps: v.oracle.causeSteps, affectedSteps: v.oracle.affectedSteps,
      hiddenStateChanges: v.oracle.hiddenStateChanges.length, exitCode: t.exitCode, durationMs: t.durationMs,
      failSteps: t.failSteps, fpSteps: t.falsePositiveSteps, detectedAtCause: t.detectedAtCause,
      keys: t.failKeys || t.fails || [], tp: t.tp, fn: t.fn || null, extraKeys: t.extraKeys || null, evidenceProblems: t.evidenceProblems || null,
      failsByRule: t.failsByRule || t.failsByTag || null, ajvViolations: t.violations, postmanAssertions: t.assertions, postmanFailed: t.failedAssertions, invariantKeysMatchOracle: t.invariantKeysMatchOracle,
      oracleSig: v.oracle.violations.map(x => mask(x.key + '|' + x.detail)), dbSig: v.dbSignature });
  }
}
rows.sort((a, b) => modes.indexOf(a.mode) - modes.indexOf(b.mode) || toolOrder.indexOf(a.tool) - toolOrder.indexOf(b.tool) || a.repeat - b.repeat);
const groups = {};
rows.forEach(r => (groups[`${r.mode}/${r.tool}`] ||= []).push(r));
const reproducibility = Object.entries(groups).map(([g, rs]) => ({ group: g, repeats: rs.length,
  failKeysIdentical: rs.every(r => JSON.stringify(r.keys) === JSON.stringify(rs[0].keys)),
  oracleIdentical: rs.every(r => JSON.stringify(r.oracleSig) === JSON.stringify(rs[0].oracleSig)),
  finalDbIdentical: rs.every(r => JSON.stringify(r.dbSig) === JSON.stringify(rs[0].dbSig)) }));
fs.writeFileSync(outJson, JSON.stringify({ rows: rows.map(({ oracleSig, dbSig, ...r }) => r), reproducibility, incomplete }, null, 2) + '\n');

const md = ['# Event API matrix — summary', '', `Runs with verify.json: ${runs.length}; tool results: ${rows.length}; incomplete run directories: ${incomplete.length ? incomplete.join(', ') : 'none'}.`, ''];
md.push('| Mode | Tool | Rep | Switch | Traffic | Exercised | Cause steps | Affected steps | Tool failure steps | FP steps | Detected at cause | Key TP / FN (semval) | Exit | ms |', '|---|---|---|---|---|---|---|---|---|---|---|---|---|---|');
for (const r of rows) {
  md.push(`| ${r.mode} | ${r.tool} | ${r.repeat} | ${r.switchOk ? 'ok' : 'BAD'} | ${r.trafficOk ? 'ok' : 'BAD'} | ${r.mode === 'clean' ? '-' : r.exercised ? 'yes' : 'NO'} | ${r.causeSteps.join(',') || '-'} | ${r.affectedSteps.join(',') || '-'} | ${r.failSteps.join(',') || '-'} | ${r.fpSteps.join(',') || '-'} | ${r.mode === 'clean' ? '-' : r.detectedAtCause ? 'yes' : 'no'} | ${r.fn ? `${r.tp} / ${r.fn.length}` : '-'} | ${r.exitCode ?? '-'} | ${r.durationMs ?? '-'} |`);
}
md.push('', '## Reproducibility', '', '| Mode/tool | Repeats | Failure keys identical | Oracle identical | Final DB identical |', '|---|---|---|---|---|');
reproducibility.forEach(g => md.push(`| ${g.group} | ${g.repeats} | ${g.failKeysIdentical} | ${g.oracleIdentical} | ${g.finalDbIdentical} |`));
md.push('', '## Semantic-validator false negatives and extra failures (repeat 1)', '');
rows.filter(r => r.repeat === 1 && r.fn && (r.fn.length || r.extraKeys.length)).forEach(r => {
  md.push(`- **${r.mode} / ${r.tool}**: ${r.fn.map(x => `${x.key} (${x.reason})`).join('; ') || 'no FN'}${r.extraKeys.length ? `; extra FAIL keys: ${r.extraKeys.map(x => `${x.key}${x.onAffectedStep ? ' (affected step)' : ' (UNAFFECTED step)'}`).join(', ')}` : ''}`);
});
md.push('', '## Postman failures by tag (repeat 1)', '');
rows.filter(r => r.repeat === 1 && r.tool === 'postman').forEach(r => md.push(`- ${r.mode}: ${r.postmanFailed}/${r.postmanAssertions} assertions failed; ${JSON.stringify(r.failsByRule)}; keys ${r.keys.join(', ') || '-'}; invariant failures match oracle: ${r.invariantKeysMatchOracle}`));
fs.writeFileSync(outMd, md.join('\n') + '\n');
console.log(`summarized ${runs.length} runs / ${rows.length} tool results; incomplete: ${incomplete.length}`);
