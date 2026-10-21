// Copies a run-matrix output (plus passive reports and DB dumps) into docs/experiments/evaluation/results with:
// local paths redacted, API logs and raw database files excluded, Schemathesis HAR/NDJSON gzip-compressed.
// Fails if a user-profile path survives. Usage: node save-matrix-evidence.js <matrix> <matrix-passive> <db-copies> <dest> <redact-prefix>
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const [matrix, passive, dbCopies, dest, prefix] = process.argv.slice(2);
const escaped = prefix.replace(/\\/g, '\\\\');
const userHome = process.env.USERPROFILE;
const redact = t => t.split(prefix).join('<report-dir>\\').split(escaped).join('<report-dir>\\\\');
let files = 0;

function copyTree(src, dst, renames = {}) {
  for (const e of fs.readdirSync(src, { withFileTypes: true })) {
    const s = path.join(src, e.name);
    if (e.isDirectory()) { if (e.name !== 'db') copyTree(s, path.join(dst, e.name), renames); continue; }
    if (e.name === 'api.log' || e.name.endsWith('.mv.db') || e.name.endsWith('.lock.db')) continue;
    let name = e.name.replace(/^har-.*\.json$/, 'requests.har.json').replace(/^ndjson-.*\.ndjson$/, 'events.ndjson').replace(/^junit-.*\.xml$/, 'junit.xml');
    let data = redact(fs.readFileSync(s, 'utf8'));
    if (data.includes(userHome) || data.includes(userHome.replace(/\\/g, '\\\\'))) throw new Error(`user path remains in ${s}`);
    fs.mkdirSync(dst, { recursive: true });
    if (name === 'requests.har.json' || name === 'events.ndjson' || (name.endsWith('.json') && data.length > 1_000_000)) {
      fs.writeFileSync(path.join(dst, name + '.gz'), zlib.gzipSync(Buffer.from(data, 'utf8')));
    } else {
      fs.writeFileSync(path.join(dst, name), data);
    }
    files++;
  }
}
copyTree(matrix, path.join(dest, 'matrix'));
copyTree(passive, path.join(dest, 'passive-rules-on-schemathesis'));
copyTree(dbCopies, path.join(dest, 'final-db-state'));
console.log(`saved ${files} files to ${dest}`);
