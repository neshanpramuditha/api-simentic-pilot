# Comparative evaluation (2026-10-19)

Conclusion: `feasibility-conclusion.md`. Raw evidence: `results/`. Everything below was run from the project root on Windows 11 (PowerShell 5.1).

## Inputs

| File | Role |
|---|---|
| `scenario.json` | Shared 11-request business scenario (EVAL-SC-1) with per-step expected outcomes, used by Ajv, Postman and the semantic validator |
| `capture-scenario.js` | Sends the scenario and records responses for the schema-only (Ajv) check |
| `build-postman-collection.js` → `evaluation.postman_collection.json` | Postman collection: the same 11 requests, 29 explicit assertions (status code, returned status, list statuses, baseline `Order` schema) derived from the scenario's `expect` blocks |
| `../../../semantic-validator/rules/order-api.rules.json` | The 3 semantic rules (`ORD-INV-001`, `ORD-ST-001`, `ORD-DB-001`) |
| `run-semval.ps1` | Runs the validator actively (scenario + JDBC) or passively over a HAR |
| `run-matrix.ps1` | The matrix: 4 modes × 5 tool configurations × 2 repeats, fresh API and fresh database per run |
| `analyze-matrix.js` | Per-run verdicts, defect-exercise check from traffic, Schemathesis findings split into baseline vs new |
| `db02-effect.js`, `hidden-state-changes.js`, `DumpOrders.java` | DB-02 ground truth: final database state of each Schemathesis run vs the last state the client observed |
| `save-matrix-evidence.js` | Copies evidence into `results/` with paths redacted, logs and raw DB files excluded, large files gzipped |

## Environment and versions

JDK 21.0.12 (Temurin), Spring Boot 4.1.1, H2 2.4.240; Node 22.21.1 with Ajv 8.17.1 + ajv-formats 3.0.1; Postman CLI 1.74.0; Schemathesis 4.29.4 (Python 3.13.6); semantic-validator 0.1.0 (Jackson 2.21.5, H2 2.4.240 JDBC).

**Deviation from earlier experiments:** every matrix run started the API with `SPRING_DATASOURCE_URL=jdbc:h2:file:<run dir>/db/semanticdb;AUTO_SERVER=TRUE` (via `start-api.ps1 -DatasourceUrl`) instead of the in-memory database, so the validator can open the same database over H2's auto-server TCP connection. HTTP behaviour was unchanged: clean-mode Schemathesis findings were the same four (SB-01..SB-04) as with the in-memory database.

## Commands

```
.\mvnw.cmd -f semantic-validator\pom.xml package
.\mvnw.cmd -f semantic-validator\pom.xml dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt" "-Dmdep.includeScope=runtime"
node docs\experiments\evaluation\build-postman-collection.js
.\docs\experiments\evaluation\run-matrix.ps1 -OutRoot <out> -Schemathesis <venv>\Scripts\schemathesis.exe -AjvModules <dir with ajv node_modules> -Repeats 2
# passive rules over each Schemathesis HAR (written to a separate directory):
.\docs\experiments\evaluation\run-semval.ps1 -Out <out>-passive\<mode>\<run>\semval-passive.json -Har <out>\<mode>\<run>\har-*.json
node docs\experiments\evaluation\analyze-matrix.js <out> <summary.json> <summary.md>
```

Modes: `clean`, `sv01-total-as-string`, `st01-completed-to-cancelled`, `db02-rejected-change-saved` (at most one switch per run; the startup log was checked for exactly the expected `DEFECT … ENABLED` line in every run — `results/matrix/runs.json`).

Tool configurations: `ajv` (scenario responses vs baseline), `postman` (collection), `semval` (scenario, active, with JDBC), `schemathesis-baseline` (recorded configuration, empty database), `schemathesis-seeded` (recorded configuration after seeding 30 `COMPLETED` orders through the API with `../seed-orders.js`; EXT-2 configuration).
