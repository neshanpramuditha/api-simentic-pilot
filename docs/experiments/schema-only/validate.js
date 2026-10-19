// Schema-only validation: each captured response is checked in isolation against one OpenAPI 3.1 document.
// Checks: status code documented, and (when a JSON schema is documented) body valid against it, formats asserted.
// Usage: node validate.js <openapi.json> <responses.json>
const fs = require('fs');
const Ajv2020 = require('ajv/dist/2020').default;
const addFormats = require('ajv-formats');

const read = f => JSON.parse(fs.readFileSync(f, 'utf8').replace(/^﻿/, ''));
const [specFile, responsesFile] = process.argv.slice(2);
const spec = read(specFile);
const responses = read(responsesFile);

const ajv = new Ajv2020({ strict: false, allErrors: true });
addFormats(ajv);
ajv.addSchema({ $id: 'spec', components: spec.components });

let failures = 0;
for (const r of responses) {
  const op = spec.paths[r.template]?.[r.method.toLowerCase()];
  const label = `${r.method} ${r.url} -> ${r.status}`;
  const problems = [];
  const documented = op?.responses?.[String(r.status)];
  if (!documented) {
    problems.push(`status ${r.status} not documented`);
  } else {
    const media = documented.content?.['application/json'];
    if (media?.schema) {
      const schema = JSON.parse(JSON.stringify(media.schema).replaceAll('"#/components/', '"spec#/components/'));
      const validate = ajv.compile(schema);
      if (!validate(r.body)) {
        for (const e of validate.errors) problems.push(`${e.instancePath || '(root)'} ${e.message}`);
      }
    } else {
      problems.push('(no body schema documented; body not checked)');
    }
  }
  const failed = problems.some(p => !p.startsWith('('));
  if (failed) failures++;
  console.log(`${failed ? 'FAIL' : 'pass'}  ${label}${problems.length ? '  ' + [...new Set(problems)].join('; ') : ''}`);
}
console.log(`\n${responses.length} responses, ${failures} schema-only failures`);
