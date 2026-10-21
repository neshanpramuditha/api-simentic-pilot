// Copies the evidence of a matrix run into results/, re-encoded as UTF-8 and with local paths redacted.
// API logs (they contain the local path and user name) and raw H2 database files are not copied; the final database
// state is kept as db-final.json. Fails if anything that looks like a local path or user name survives.
// Usage: node save-evidence.js <matrix dir> <results dir> [comma-separated extra file names to keep]
const fs = require('fs');
const path = require('path');
const os = require('os');
const [root, dest, extra] = process.argv.slice(2);
const keep = ['run.json', 'semval.json', 'semval.md', 'semval.out.txt', 'traffic.har.json', 'db-final.json', 'verify.json', ...(extra ? extra.split(',') : [])];
const readText = file => { const b = fs.readFileSync(file); return (b[0] === 0xff && b[1] === 0xfe ? b.toString('utf16le') : b.toString('utf8')).replace(/^﻿/, ''); };
const user = os.userInfo().username;
const absRoot = path.resolve(root);
const variants = p => [p, p.replace(/\\/g, '/'), p.replace(/\\/g, '\\\\'), p.replace(/\\/g, '/').replace(/ /g, '%20')];
const secrets = [...variants(absRoot), ...variants(os.homedir()), ...variants(process.cwd())].sort((a, b) => b.length - a.length);
const redact = text => {
  for (const s of secrets) text = text.split(s).join('<run-root>');
  return text;
};
// A drive letter not preceded by a letter (so "http://" does not match), the user name, or typical profile folders.
// The user name is matched case-insensitively; path patterns case-sensitively (the Order API has "/api/users/...").
const forbiddenUser = new RegExp(user, 'i');
const forbiddenPath = /(^|[^A-Za-z])[A-Za-z]:[\\/]|\/Users\/|AppData/;
const forbidden = { exec: text => forbiddenUser.exec(text) || forbiddenPath.exec(text) };
let files = 0;
for (const mode of fs.readdirSync(root)) {
  if (!fs.statSync(path.join(root, mode)).isDirectory()) continue;
  for (const run of fs.readdirSync(path.join(root, mode))) {
    const from = path.join(root, mode, run);
    const to = path.join(dest, mode, run);
    fs.mkdirSync(to, { recursive: true });
    for (const f of keep) {
      if (!fs.existsSync(path.join(from, f))) continue;
      const text = redact(readText(path.join(from, f))).replace(/\r\n/g, '\n');
      const hit = forbidden.exec(text);
      if (hit) throw new Error(`unredacted value in ${mode}/${run}/${f}: ...${text.substr(Math.max(0, hit.index - 20), 60)}...`);
      fs.writeFileSync(path.join(to, f), text);
      files++;
    }
  }
}
console.log(`saved ${files} files to ${dest}`);
