// Report-content analysis (not a human study): for each defect mode, extracts the matched failure reports of the
// semantic validator (engine 0.2, active, JDBC) and of Postman (same scenario, same API build, repeat 1), and scores
// what each report contains against a fixed rubric. Also writes the diagnosis task and its answer key.
// Usage: node diagnosis.js <matrix dir> <out dir>
const fs = require('fs');
const path = require('path');
const [root, out] = process.argv.slice(2);
const readText = f => { const b = fs.readFileSync(f); return (b[0] === 0xff && b[1] === 0xfe ? b.toString('utf16le') : b.toString('utf8')).replace(/^﻿/, ''); };
const read = f => JSON.parse(readText(f));
const mask = s => s.replace(/REG-[A-Z0-9]{8}/g, 'REG-xxxxxxxx');

// Answer key: what a correct diagnosis names (from defects.md, written before the runs).
const KEY = {
  'evt-st01': { operation: 'transitionRegistration (POST /v1/registrations/{id}/transitions), step 8 R2 WAITLISTED -> CONFIRMED', rule: 'allowed transition rejected (lifecycle / EVT-ST-001)', evidence: 'HTTP 422 for an allowed WAITLISTED -> CONFIRMED; previous state WAITLISTED' },
  'evt-db01': { operation: 'transitionRegistration, steps 4 (R1 -> CANCELLED) and 13 (R3 -> CHECKED_IN)', rule: 'rejected change stored (database consistency / EVT-DB-001)', evidence: 'HTTP 422 while the stored state changed (before/after state)' },
  'evt-db02': { operation: 'transitionRegistration, step 9 R2 -> CANCELLED', rule: 'successful response not stored (database consistency / EVT-DB-001)', evidence: 'HTTP 200 CANCELLED while the stored state is still CONFIRMED' },
  'evt-ri01': { operation: 'transitionRegistration, step 2 R1 -> CHECKED_IN', rule: 'checkedInAt set iff CHECKED_IN (EVT-INV-001)', evidence: 'state CHECKED_IN with checkedInAt null' },
  'evt-cf01': { operation: 'transitionRegistration, step 2 R1 -> CHECKED_IN', rule: 'time order requestedAt <= confirmedAt <= checkedInAt (EVT-INV-005)', evidence: 'checkedInAt earlier than confirmedAt' },
};
const RUBRIC = [
  ['R1', 'Names the faulty request (method and URL of the step where the defect acts)'],
  ['R2', 'Names the violated rule or expectation'],
  ['R3', 'Shows the observed values that violate it'],
  ['R4', 'States what was expected or allowed'],
  ['R5', 'Includes the response body'],
  ['R6', 'Includes the state before the request'],
  ['R7', 'Includes the stored state after the request (database row; HTTP read-back counted as "partial")'],
  ['R8', 'First failure reported is at the cause step (not only downstream)'],
];

fs.mkdirSync(out, { recursive: true });
const table = [];
for (const mode of Object.keys(KEY)) {
  const v = read(path.join(root, mode, 'semval-v02-jdbc-r1', 'verify.json'));
  const cause = v.oracle.causeSteps;
  // --- semantic validator ---
  const rep = read(path.join(root, mode, 'semval-v02-jdbc-r1', 'semval.json'));
  const fails = rep.findings.filter(f => f.outcome === 'FAIL');
  const atCause = fails.filter(f => cause.includes(f.exchangeIndex));
  const sv = atCause[0];
  const ev = sv ? sv.evidence : {};
  const s = {
    R1: !!(sv && ev.request && ev.request.method && ev.request.url),
    R2: !!sv,
    R3: !!(sv && (ev.values || ev.state || ev.database)),
    R4: !!(sv && (ev.assertion || (ev.state && ev.state.allowedFromPrevious) || (ev.database && ev.database.rowBefore))),
    R5: !!(sv && ev.response && ev.response.body !== undefined),
    R6: !!(sv && ((ev.state && ev.state.previous) || (ev.database && ev.database.rowBefore))),
    R7: sv && ev.database && ev.database.rowAfter ? 'yes' : 'no',
    R8: fails.length > 0 && cause.includes(Math.min(...fails.map(f => f.exchangeIndex))),
  };
  fs.writeFileSync(path.join(out, `${mode}-semantic-validator.md`), mask([`# ${mode}: semantic validator 0.2 report (failures only, ids masked)`, '',
    `Rule set: ${rep.ruleSet}. Totals: ${JSON.stringify(rep.totals)}.`, '',
    ...fails.map(f => `## ${f.ruleId} — exchange #${f.exchangeIndex} (${f.operationId})\n\n${f.message}\n\n\`\`\`json\n${JSON.stringify(f.evidence, null, 2)}\n\`\`\`\n`)].join('\n')) + '\n');
  // --- Postman ---
  const pmText = readText(path.join(root, mode, 'postman-r1', 'postman.txt'));
  const sections = pmText.split(/\r?\n(?=Root \d+ )/);
  const pmV = read(path.join(root, mode, 'postman-r1', 'verify.json')).tools.postman;
  const failSections = sections.filter(sec => /^Root (\d+) /.test(sec) && /^\s+\d+\.\s+\[/m.test(sec));
  const table0 = pmText.slice(pmText.indexOf('#  failure'));
  const details = table0.split(/\r?\n(?=[ \t]*\d+\.[ \t]{2})/).slice(1).map(x => x.replace(/[ \t]+$/gm, ''));
  const causeSections = failSections.filter(sec => cause.includes(Number(/^Root (\d+)/.exec(sec)[1])));
  const causeNames = causeSections.map(sec => /^Root (.*)$/m.exec(sec)[1].trim()); // step names start with their number
  const causeDetails = details.filter(d => causeNames.some(n => d.includes(`inside "${n}"`)));
  const joined = causeDetails.join('\n');
  const p = {
    R1: causeSections.some(sec => /^\s+(GET|POST) http\S+\/v1\//m.test(sec)),
    R2: causeDetails.length > 0,
    R3: /\{"state"|status code|expected/.test(joined),
    R4: /expected/.test(joined),
    R5: false,
    R6: /vs before \{/.test(joined),
    R7: /read-back GET \{/.test(joined) ? 'partial' : 'no',
    R8: pmV.failSteps.length > 0 && cause.includes(Math.min(...pmV.failSteps)),
  };
  fs.writeFileSync(path.join(out, `${mode}-postman.txt`), mask([`# ${mode}: Postman CLI output (sections with failures, then the failure table; ids masked)`, '',
    ...failSections, '', table0].join('\n')) + '\n');
  table.push({ mode, cause, semval: s, postman: p, semvalFailSteps: [...new Set(fails.map(f => f.exchangeIndex))], postmanFailSteps: pmV.failSteps });
}
const mark = x => x === true || x === 'yes' ? 'yes' : x === 'partial' ? 'partial' : 'no';
const md = ['# Diagnosis comparison — report-content analysis', '',
  '**This is an analysis of what each failure report contains, not a measurement of human diagnosis time or accuracy. No participants took part.**', '',
  'Task (repeatable, for a future human study): given only one tool\'s failure report for a run, identify (a) the faulty operation and step, (b) the violated rule, (c) the supporting evidence. The answer key is below; the reports are in this directory (`<mode>-semantic-validator.md`, `<mode>-postman.txt`), both from repeat 1 of the same mode, ids masked.', '',
  '## Answer key', '', '| Mode | Faulty operation / step | Violated rule | Evidence |', '|---|---|---|---|',
  ...Object.entries(KEY).map(([m, k]) => `| ${m} | ${k.operation} | ${k.rule} | ${k.evidence} |`), '',
  '## Rubric', '', ...RUBRIC.map(([id, d]) => `- **${id}** ${d}`), '',
  '## Scores (SV = semantic validator 0.2 active with JDBC; PM = Postman collection)', '',
  `| Mode | Cause steps | SV fail steps | PM fail steps | ${RUBRIC.map(([id]) => id + ' SV/PM').join(' | ')} |`, `|---|---|---|---|${RUBRIC.map(() => '---').join('|')}|`,
  ...table.map(t => `| ${t.mode} | ${t.cause.join(',')} | ${t.semvalFailSteps.join(',') || '-'} | ${t.postmanFailSteps.join(',') || '-'} | ${RUBRIC.map(([id]) => `${mark(t.semval[id])}/${mark(t.postman[id])}`).join(' | ')} |`), '',
  'Scored automatically by diagnosis.js from the report files: R1 needs method + URL in the report; R3/R4 look for observed and expected values in the failure text; R5-R7 for the response body, the prior state and the stored state. Postman CLI output never contains response bodies (R5) and has no database access (R7 at most a read-back over HTTP).'];
fs.writeFileSync(path.join(out, 'README.md'), md.join('\n') + '\n');
fs.writeFileSync(path.join(out, 'scores.json'), JSON.stringify(table, null, 2) + '\n');
console.log(`diagnosis: ${table.length} modes -> ${out}`);
