// Schema-only baseline: validates every response body in a HAR against the response schema the OpenAPI document
// declares for that operation and status (JSON Schema 2020-12, Ajv + ajv-formats). Writes {checked, violations[]}.
// Usage: node ajv-validate-har.js <ajv node_modules dir> <openapi.json> <traffic.har.json> <out ajv.json>
const fs = require('fs');
const path = require('path');
const [modules, specFile, harFile, outFile] = process.argv.slice(2);
const Ajv2020 = require(path.join(modules, 'ajv/dist/2020')).default;
const addFormats = require(path.join(modules, 'ajv-formats')).default;
const spec = JSON.parse(fs.readFileSync(specFile, 'utf8'));
const readText = f => { const b = fs.readFileSync(f); return (b[0] === 0xff && b[1] === 0xfe ? b.toString('utf16le') : b.toString('utf8')).replace(/^﻿/, ''); };
const har = JSON.parse(readText(harFile));

const ajv = new Ajv2020({ strict: false, allErrors: true });
addFormats(ajv);
const strip = v => Array.isArray(v) ? v.map(strip) : (v && typeof v === 'object')
  ? Object.fromEntries(Object.entries(v).filter(([k]) => k !== 'example').map(([k, x]) => [k, k === '$ref' ? x.replace('#/components/schemas/', 'openapi#/$defs/') : strip(x)])) : v;
ajv.addSchema({ $id: 'openapi', $defs: strip(spec.components.schemas) });

const ops = [];
for (const [template, item] of Object.entries(spec.paths)) {
  for (const [method, op] of Object.entries(item)) ops.push({ method: method.toUpperCase(), regex: new RegExp('^' + template.replace(/\{[^}]+\}/g, '[^/]+') + '$'), op });
}
let checked = 0;
const violations = [];
har.log.entries.forEach((e, i) => {
  const u = new URL(e.request.url);
  const m = ops.find(o => o.method === e.request.method && o.regex.test(u.pathname));
  const schema = m && m.op.responses[String(e.response.status)] && m.op.responses[String(e.response.status)].content
    && m.op.responses[String(e.response.status)].content['application/json'].schema;
  if (!schema) return;
  checked++;
  const validate = ajv.compile(strip(schema));
  if (!validate(JSON.parse(e.response.content.text))) {
    violations.push({ entry: i, operationId: m.op.operationId, status: e.response.status, errors: validate.errors.map(x => `${x.instancePath} ${x.message}`) });
  }
});
fs.writeFileSync(outFile, JSON.stringify({ tool: 'ajv 2020 + ajv-formats', checked, violations }, null, 2) + '\n');
console.log(`ajv: ${checked} responses checked, ${violations.length} schema violations`);
