# Second-API reuse experiment: Library Lending API (2026-10-19)

Question: can the existing semantic-validator engine (`../../../semantic-validator/`) validate a second, independently written REST API **without changes to its source code**, using only a new rules file? Report: `../evaluation/second-api-reuse-report.md`. Raw evidence: `results/`.

**Engine version:** the evidence here was produced with engine 0.1 (hashes in `results/engine-hashes-*.txt`). The engine was later extended to 0.2 for the Event Registration API (`../event-reuse/results/engine-v0.2.patch`). Re-running this matrix once with 0.2 gave findings identical to these (`../event-reuse/results/regression-v02/library/`).

Nothing in the Order API experiments was changed or re-run for this; Order API code, `docs/openapi-baseline.json`, `order-api.rules.json` and all earlier evidence are untouched.

## Inputs

| File | Role |
|---|---|
| `../../../library-api/` | Subject API (Spring Boot 4.1.1, Java 21, H2, port 8081, `jdbc:h2:mem:librarydb` by default) |
| `../../../library-api/openapi-baseline.json` | OpenAPI 3.1 document generated from the clean API (`OpenApiBaselineExport`, opt-in) |
| `../../../semantic-validator/rules/library-api.rules.json` | The 4 rules: `LIB-INV-001`, `LIB-INV-002` (response invariants), `LIB-ST-001` (state transition), `LIB-DB-001` (database consistency) |
| `scenario.json` | LIB-SC-1: 15 requests (3 loans, 3 forbidden transitions, a 404 and a 400), same format as `../evaluation/scenario.json` |
| `defects.md` | The three defect switches (LIB-ST-01, LIB-DB-01, LIB-RI-01) and their tests |
| `start-library-api.ps1`, `stop-library-api.ps1` | Start the packaged jar on 8081 (optionally one defect, optionally `-DatasourceUrl`) / stop whatever listens on 8081 |
| `record-proxy.js` | Recording proxy on 8091 in front of the API: ground-truth HAR of everything the validator sent in active mode, read-backs included |
| `capture-har.js` | Sends the scenario directly (no read-backs) and writes a HAR, the input of passive mode |
| `DumpLoans.java` | Final database state (all columns) over the API's `AUTO_SERVER` connection, independent of the validator |
| `run-library-matrix.ps1` | The matrix: modes `clean`, `lib-st01`, `lib-db01`, `lib-ri01` × configurations `semval-jdbc`, `semval-nojdbc`, `semval-passive` × 2 repeats; new API process and new file database per run |
| `verify-run.js` | Per-run ground truth: traffic vs scenario, independent oracle (transition table parsed from the OpenAPI description, invariants, persisted state from later reads and the final database), validator FAILs vs oracle (TP/FP/FN), evidence vs traffic, database-rule scoping |
| `summarize.js` | `results/summary.md` / `summary.json`, including reproducibility across repeats |

## Commands (Windows PowerShell 5.1, project root)

```
.\mvnw.cmd -f library-api\pom.xml test                                   # 52 tests (clean + each defect)
.\mvnw.cmd -f library-api\pom.xml test "-Dtest=OpenApiBaselineExport" "-Dopenapi.export=true" "-Dsurefire.failIfNoSpecifiedTests=false"   # only to regenerate the baseline, clean code only
.\mvnw.cmd -f library-api\pom.xml package -DskipTests
.\mvnw.cmd -f semantic-validator\pom.xml package
.\mvnw.cmd -f semantic-validator\pom.xml dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt" "-Dmdep.includeScope=runtime"
.\docs\experiments\library-reuse\run-library-matrix.ps1 -OutRoot <new dir, no spaces> -Repeats 2
node docs\experiments\library-reuse\summarize.js <out root> <summary.json> <summary.md>
```

The run directory must not contain spaces (it becomes part of the H2 file URL). Evidence was copied into `results/` with local paths removed, and API logs and raw database files left out (see `results/README.md`).
