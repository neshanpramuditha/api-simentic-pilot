// Sends the shared scenario (scenario.json) to the running API and records the responses in the format used by
// docs/experiments/schema-only/validate.js. Usage: node capture-scenario.js <scenario.json> <openapi.json> <out responses.json>
const fs = require('fs');
const [scenarioFile, specFile, outFile] = process.argv.slice(2);
const scenario = JSON.parse(fs.readFileSync(scenarioFile, 'utf8'));
const spec = JSON.parse(fs.readFileSync(specFile, 'utf8'));
const ops = {};
for (const [template, item] of Object.entries(spec.paths)) {
  for (const [method, op] of Object.entries(item)) ops[op.operationId] = { method: method.toUpperCase(), template };
}

(async () => {
  const vars = {};
  const out = [];
  for (const step of scenario.steps) {
    const op = ops[step.operationId];
    const params = Object.fromEntries(Object.entries(step.pathParams || {}).map(([k, v]) => [k, v.replace(/\$\{(\w+)\}/g, (_, n) => vars[n])]));
    const url = op.template.replace(/\{(\w+)\}/g, (_, n) => params[n]);
    const res = await fetch('http://localhost:8080' + url, {
      method: op.method,
      headers: { Accept: 'application/json', ...(step.body ? { 'Content-Type': 'application/json' } : {}) },
      body: step.body ? JSON.stringify(step.body) : undefined,
    });
    const text = await res.text();
    const body = text ? JSON.parse(text) : null;
    out.push({ step: step.name, method: op.method, template: op.template, url, status: res.status, body });
    for (const [k, path] of Object.entries(step.save || {})) vars[k] = body[path.replace(/^\$\./, '')];
  }
  fs.writeFileSync(outFile, JSON.stringify(out, null, 2));
  console.log(`captured ${out.length} responses`);
})().catch(e => { console.error(e.message); process.exit(1); });
