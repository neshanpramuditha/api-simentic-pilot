# Third-API experiment: Event Registration API (2026-10-19)

Questions:
1. Does the unchanged semantic-validator work on an API whose contract was designed and frozen before any rules were written?
2. If it doesn't, what is the minimal general fix?
3. How does it compare with an equally careful Postman collection (and Ajv), on the same contract, scenario, defects and ground truth?

Conclusion: `../evaluation/final-project-feasibility.md`. Evidence: `results/`.

## Order of work (timestamps in `results/timing.log`)

1. Contract designed (`contract-design.md`), API implemented (`../../../event-api/`, port 8082, `jdbc:h2:mem:eventsdb`), clean tests 38/38. Contract exported to `event-api/openapi-baseline.json` and frozen (`results/event-contract-frozen.sha256`).
2. Rules v1 (`semantic-validator/rules/event-api.rules.json`) for the **original engine 0.1** (jar kept aside, hashes `results/engine-hashes-v0.1.txt`). First run recorded (`results/first-attempt/`): it runs without source changes, but two problems showed up (see the conclusion, §3).
3. Five defects added (`defects.md`), tests 52/52; clean sources and contract unchanged (hash-checked).
4. Minimal general engine fix **0.2** (`results/engine-v0.2.patch`, hashes `results/engine-hashes-v0.2.txt`), unit tests 5/5. Rules v2 (`event-api.rules.v2.json`) uses it. Regression check: v0.2 gives the same findings as v0.1 on the Order API (4 modes) and the Library API (12 runs) (`results/regression-v02/`).
5. Postman collection (`build-postman-collection.js` → `event.postman_collection.json`); first version `results/build-postman-collection.v1.js`.
6. Matrix (`run-event-matrix.ps1`): 6 modes × 2 repeats × 4 run types = 48 runs, each with a new API process and a new database. A first matrix attempt was **aborted after 5 clean runs** because the packaged jar predated the defect classes; it was discarded and not used. The runner now refuses to continue if the startup log lacks the expected `DEFECT` line.
7. `verify-event-run.js` (ground truth), `summarize-event.js` (`results/summary.md`), `diagnosis.js` (`results/diagnosis/`).

## Files

| File | Role |
|---|---|
| `contract-design.md` | Frozen contract and how its conventions differ from the Order and Library APIs |
| `scenario.json` | EVT-SC-1: 19 requests, 3 registrations, 2 forbidden transitions, 404, two 400s |
| `defects.md` | EVT-ST-01, EVT-DB-01, EVT-DB-02, EVT-RI-01, EVT-CF-01 |
| `start-event-api.ps1`, `stop-event-api.ps1` | Start the jar on 8082 (optionally one defect, optionally `-DatasourceUrl`) / stop |
| `build-postman-collection.js` | Postman collection: schema + the same five invariants on every registration (envelope items too) + per-step expectations + read-back GET after each transition |
| `ajv-validate-har.js` | Schema-only baseline over the recorded traffic |
| `DumpRegistrations.java` | Final database state, independent of the validator |
| `run-event-matrix.ps1`, `verify-event-run.js`, `summarize-event.js`, `diagnosis.js` | Matrix, ground truth, summary, report-content analysis |

Also used: `../library-reuse/record-proxy.js`, `../library-reuse/capture-har.js`, `../library-reuse/save-evidence.js`.

## Commands (PowerShell, project root)

```
.\mvnw.cmd -f event-api\pom.xml test
.\mvnw.cmd -f event-api\pom.xml package -DskipTests        # after any change to event-api (see step 6)
.\mvnw.cmd -f semantic-validator\pom.xml package
node docs\experiments\event-reuse\build-postman-collection.js
.\docs\experiments\event-reuse\run-event-matrix.ps1 -OutRoot <new dir, no spaces> -EngineV01 <dir with 0.1 jar + classpath.txt> -AjvModules <node_modules with ajv, ajv-formats> -Repeats 2
# then for each run dir: node docs\experiments\event-reuse\verify-event-run.js <run> docs\experiments\event-reuse\scenario.json event-api\openapi-baseline.json
node docs\experiments\event-reuse\summarize-event.js <out root> <summary.json> <summary.md>
node docs\experiments\event-reuse\diagnosis.js <out root> <diagnosis dir>
```

Postman CLI 1.74.0 was used without login: `--output` and cloud reporting need a login, so the CLI text report is the Postman evidence.
