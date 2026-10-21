// Builds evaluation.postman_collection.json from scenario.json. Each request carries explicit assertions derived from
// the step's "expect" block (status code, returned order status, list statuses) plus the baseline Order schema on every
// 2xx order body. No extra requests are sent: Postman sees exactly the scenario's 11 requests.
// Usage (from project root): node docs/experiments/evaluation/build-postman-collection.js
const fs = require('fs');
const path = require('path');

const scenario = JSON.parse(fs.readFileSync('docs/experiments/evaluation/scenario.json', 'utf8'));
const spec = JSON.parse(fs.readFileSync('docs/openapi-baseline.json', 'utf8'));
const dropped = (k, x) => ['description', 'example', 'readOnly'].includes(k) || (k === 'format' && x === 'int64');
const strip = v => Array.isArray(v) ? v.map(strip) : (v && typeof v === 'object')
  ? Object.fromEntries(Object.entries(v).filter(([k, x]) => !dropped(k, x)).map(([k, x]) => [k, strip(x)])) : v;
const orderSchema = strip(spec.components.schemas.Order);
const ops = {};
for (const [template, item] of Object.entries(spec.paths)) {
  for (const [method, op] of Object.entries(item)) ops[op.operationId] = { method: method.toUpperCase(), template };
}

const item = scenario.steps.map(step => {
  const op = ops[step.operationId];
  const url = '{{baseUrl}}' + op.template.replace(/\{(\w+)\}/g, (_, n) => {
    const v = (step.pathParams || {})[n];
    return v.replace(/\$\{(\w+)\}/g, (__, name) => `{{${name}}}`);
  });
  const e = step.expect;
  const tests = [`const orderSchema = ${JSON.stringify(orderSchema)};`,
    `pm.test("status is ${e.status}", () => pm.response.to.have.status(${e.status}));`];
  if (e.orderStatus) {
    tests.push(`pm.test("order status is ${e.orderStatus}", () => pm.expect(pm.response.json().status).to.eql("${e.orderStatus}"));`);
    tests.push(`pm.test("body matches the baseline Order schema", () => pm.response.to.have.jsonSchema(orderSchema));`);
  }
  if (e.listStatuses) {
    tests.push(`pm.test("list statuses are ${e.listStatuses.join(', ')}", () => pm.expect(pm.response.json().map(o => o.status).sort()).to.eql(${JSON.stringify([...e.listStatuses].sort())}));`);
    tests.push(`pm.test("every item matches the baseline Order schema", () => pm.response.to.have.jsonSchema({ type: "array", items: orderSchema }));`);
  }
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

const collection = {
  info: { name: 'Evaluation scenario EVAL-SC-1', description: 'Generated from docs/experiments/evaluation/scenario.json by build-postman-collection.js; do not edit by hand.', schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json' },
  variable: [{ key: 'baseUrl', value: 'http://localhost:8080' }, { key: 'a', value: '' }, { key: 'b', value: '' }],
  item,
};
const out = path.join(__dirname, 'evaluation.postman_collection.json');
fs.writeFileSync(out, JSON.stringify(collection, null, 2) + '\n');
console.log(`wrote ${out}: ${item.length} requests, ${item.reduce((n, i) => n + i.event[0].script.exec.filter(l => l.startsWith('pm.test')).length, 0)} assertions`);
