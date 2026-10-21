// Sends the scenario to the running API (no read-backs, no database access) and records the traffic as a HAR file,
// the input of the validator's passive mode (check-har).
// Usage: node capture-har.js <scenario.json> <openapi.json> <base url> <out traffic.har.json>
const fs = require('fs');
const [scenarioFile, specFile, baseUrl, outFile] = process.argv.slice(2);
const scenario = JSON.parse(fs.readFileSync(scenarioFile, 'utf8'));
const spec = JSON.parse(fs.readFileSync(specFile, 'utf8'));
const ops = {};
for (const [template, item] of Object.entries(spec.paths)) {
  for (const [method, op] of Object.entries(item)) ops[op.operationId] = { method: method.toUpperCase(), template };
}

(async () => {
  const vars = {};
  const entries = [];
  for (const step of scenario.steps) {
    const op = ops[step.operationId];
    const params = Object.fromEntries(Object.entries(step.pathParams || {}).map(([k, v]) => [k, v.replace(/\$\{(\w+)\}/g, (_, n) => vars[n])]));
    const url = baseUrl + op.template.replace(/\{(\w+)\}/g, (_, n) => params[n]);
    const headers = { Accept: 'application/json', ...(step.body ? { 'Content-Type': 'application/json' } : {}) };
    const body = step.body ? JSON.stringify(step.body) : undefined;
    const started = new Date();
    const res = await fetch(url, { method: op.method, headers, body });
    const text = await res.text();
    const type = res.headers.get('content-type') || '';
    entries.push({
      startedDateTime: started.toISOString(),
      time: Date.now() - started.getTime(),
      request: { method: op.method, url, httpVersion: 'HTTP/1.1', headers: Object.entries(headers).map(([name, value]) => ({ name, value })),
        ...(body ? { postData: { mimeType: 'application/json', text: body } } : {}) },
      response: { status: res.status, statusText: res.statusText, httpVersion: 'HTTP/1.1',
        headers: type ? [{ name: 'content-type', value: type }] : [], content: { size: text.length, mimeType: type, text } },
    });
    const parsed = text ? JSON.parse(text) : null;
    for (const [k, path] of Object.entries(step.save || {})) vars[k] = parsed[path.replace(/^\$\./, '')];
  }
  fs.writeFileSync(outFile, JSON.stringify({ log: { version: '1.2', creator: { name: 'capture-har', version: '1' }, entries } }, null, 2));
  console.log(`captured ${entries.length} exchanges`);
})().catch(e => { console.error(e.message); process.exit(1); });
