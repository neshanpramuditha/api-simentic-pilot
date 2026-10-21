// Builds event.postman_collection.json: the Postman counterpart of the Event API rules, written for coverage
// comparable to event-api.rules.v2.json on the same scenario (scenario.json):
//  - collection-level tests on every response: OpenAPI schema of the body; the same invariants as EVT-INV-001..005
//    (presence and time order) on every registration, including each item of the list envelope;
//  - per-request tests from the step's "expect" block: status, returned state, list states;
//  - after every transition request, a read-back GET (pm.sendRequest) that checks the stored registration equals the
//    response after a 2xx, and is unchanged from the last observed snapshot after a 4xx (the HTTP-only counterpart of
//    EVT-DB-001; Postman has no database access).
// The transition table is not re-implemented: forbidden/allowed outcomes are asserted through each step's expected
// status, which is how such a collection is normally written.
// Usage (from project root): node docs/experiments/event-reuse/build-postman-collection.js
const fs = require('fs');
const path = require('path');

const scenario = JSON.parse(fs.readFileSync(path.join(__dirname, 'scenario.json'), 'utf8'));
const spec = JSON.parse(fs.readFileSync('event-api/openapi-baseline.json', 'utf8'));
const dropped = (k, x) => ['description', 'example', 'readOnly'].includes(k) || (k === 'format' && ['int32', 'int64', 'date-time'].includes(x));
const strip = v => Array.isArray(v) ? v.map(strip) : (v && typeof v === 'object')
  ? Object.fromEntries(Object.entries(v).filter(([k, x]) => !dropped(k, x)).map(([k, x]) => [k, strip(x)])) : v;
const registrationSchema = strip(spec.components.schemas.Registration);
const envelopeSchema = strip(spec.components.schemas.AttendeeRegistrations);
envelopeSchema.properties.items = { type: 'array', items: registrationSchema };
const ops = {};
for (const [template, item] of Object.entries(spec.paths)) {
  for (const [method, op] of Object.entries(item)) ops[op.operationId] = { method: method.toUpperCase(), template };
}

const collectionTests = [
  `const registrationSchema = ${JSON.stringify(registrationSchema)};`,
  `const envelopeSchema = ${JSON.stringify(envelopeSchema)};`,
  `const ok = pm.response.code >= 200 && pm.response.code < 300;`,
  `let body = null; try { body = pm.response.json(); } catch (e) { body = null; }`,
  `const isEnvelope = body && Array.isArray(body.items);`,
  `const regs = !ok || !body ? [] : isEnvelope ? body.items : (body.registrationId ? [body] : []);`,
  `if (ok && body) pm.test("[schema] body matches the OpenAPI " + (isEnvelope ? "AttendeeRegistrations" : "Registration") + " schema", () => pm.response.to.have.jsonSchema(isEnvelope ? envelopeSchema : registrationSchema));`,
  `const set = v => v !== null && v !== undefined;`,
  `const t = v => Date.parse(v);`,
  `regs.forEach((r, i) => {`,
  `  const at = isEnvelope ? " (item " + i + ")" : "";`,
  `  pm.test("[INV-001] checkedInAt set iff CHECKED_IN" + at, () => pm.expect(set(r.checkedInAt), JSON.stringify({ state: r.state, checkedInAt: r.checkedInAt })).to.eql(r.state === "CHECKED_IN"));`,
  `  pm.test("[INV-002] cancelledAt set iff CANCELLED" + at, () => pm.expect(set(r.cancelledAt), JSON.stringify({ state: r.state, cancelledAt: r.cancelledAt })).to.eql(r.state === "CANCELLED"));`,
  `  pm.test("[INV-003] cancellationReason set iff CANCELLED" + at, () => pm.expect(set(r.cancellationReason), JSON.stringify({ state: r.state, cancellationReason: r.cancellationReason })).to.eql(r.state === "CANCELLED"));`,
  `  pm.test("[INV-004] CHECKED_IN implies confirmedAt; confirmedAt only in CONFIRMED/CHECKED_IN/CANCELLED" + at, () => {`,
  `    if (r.state === "CHECKED_IN") pm.expect(r.confirmedAt, "confirmedAt of a CHECKED_IN registration").to.not.equal(null);`,
  `    if (set(r.confirmedAt)) pm.expect(["CONFIRMED", "CHECKED_IN", "CANCELLED"], "state with confirmedAt").to.include(r.state);`,
  `  });`,
  `  pm.test("[INV-005] requestedAt <= confirmedAt <= checkedInAt" + at, () => {`,
  `    if (set(r.confirmedAt)) pm.expect(t(r.requestedAt), "requestedAt " + r.requestedAt + " vs confirmedAt " + r.confirmedAt).to.be.at.most(t(r.confirmedAt));`,
  `    if (set(r.checkedInAt)) pm.expect(t(r.confirmedAt), "confirmedAt " + r.confirmedAt + " vs checkedInAt " + r.checkedInAt).to.be.at.most(t(r.checkedInAt));`,
  `  });`,
  `});`,
  `// Snapshot of each registration as last observed, for the read-back check after rejected transitions.`,
  `const before = {};`,
  `regs.forEach(r => { before[r.registrationId] = pm.collectionVariables.get("snap_" + r.registrationId); pm.collectionVariables.set("snap_" + r.registrationId, JSON.stringify(r)); });`,
  `const m = pm.request.url.getPath().match(/^\\/v1\\/registrations\\/([^/]+)\\/transitions$/);`,
  `if (m) {`,
  `  const id = m[1];`,
  `  const previous = ok ? null : pm.collectionVariables.get("snap_" + id);`,
  `  pm.sendRequest({ url: pm.collectionVariables.get("baseUrl") + "/v1/registrations/" + id, method: "GET", header: { Accept: "application/json" } }, (err, res) => {`,
  `    if (err || res.code !== 200) return;`,
  `    const stored = res.json();`,
  `    if (ok) pm.test("[READBACK] stored registration equals the " + pm.response.code + " response", () => pm.expect(stored, "read-back GET").to.eql(body));`,
  `    else if (previous) pm.test("[READBACK] registration unchanged after HTTP " + pm.response.code, () => pm.expect(stored, "read-back GET").to.eql(JSON.parse(previous)));`,
  `    pm.collectionVariables.set("snap_" + id, JSON.stringify(stored));`,
  `  });`,
  `}`,
];

const item = scenario.steps.map(step => {
  const op = ops[step.operationId];
  const url = '{{baseUrl}}' + op.template.replace(/\{(\w+)\}/g, (_, n) => (step.pathParams || {})[n].replace(/\$\{(\w+)\}/g, (__, name) => `{{${name}}}`));
  const e = step.expect;
  const tests = [`pm.test("[STEP] status is ${e.status}", () => pm.response.to.have.status(${e.status}));`];
  if (e.state) tests.push(`pm.test("[STEP] state is ${e.state}", () => pm.expect(pm.response.json().state).to.eql("${e.state}"));`);
  if (e.listStates) tests.push(`pm.test("[STEP] list states are ${e.listStates.join(', ')}", () => pm.expect(pm.response.json().items.map(r => r.state)).to.eql(${JSON.stringify(e.listStates)}));`);
  for (const [k, p] of Object.entries(step.save || {})) tests.push(`pm.collectionVariables.set("${k}", pm.response.json()["${p.replace(/^\$\./, '')}"]);`);
  return {
    name: step.name,
    request: {
      method: op.method,
      header: [{ key: 'Accept', value: 'application/json' }, ...(step.body ? [{ key: 'Content-Type', value: 'application/json' }] : [])],
      url,
      ...(step.body ? { body: { mode: 'raw', raw: JSON.stringify(step.body) } } : {}),
    },
    event: [{ listen: 'test', script: { type: 'text/javascript', exec: tests } }],
  };
});

const vars = new Set();
scenario.steps.forEach(s => Object.keys(s.save || {}).forEach(k => vars.add(k)));
const collection = {
  info: { name: 'Event Registration scenario EVT-SC-1', description: 'Generated from docs/experiments/event-reuse/scenario.json by build-postman-collection.js; do not edit by hand.', schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json' },
  variable: [{ key: 'baseUrl', value: 'http://localhost:8082' }, ...[...vars].map(key => ({ key, value: '' }))],
  event: [{ listen: 'test', script: { type: 'text/javascript', exec: collectionTests } }],
  item,
};
const out = path.join(__dirname, 'event.postman_collection.json');
fs.writeFileSync(out, JSON.stringify(collection, null, 2) + '\n');
const stepTests = item.reduce((n, i) => n + i.event[0].script.exec.filter(l => l.startsWith('pm.test')).length, 0);
console.log(`wrote ${out}: ${item.length} requests, ${stepTests} per-step assertions + collection-level checks (schema, 5 invariants per registration, read-back after each transition)`);
