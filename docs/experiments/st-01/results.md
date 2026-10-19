# ST-01 results — `COMPLETED → CANCELLED` accepted with 200

**Date:** 2026-10-19. **Code:** commit `db978dc` plus the ST-01 switch (`defects/St01CompletedToCancelledOrderService`) and tests. **Contract:** `docs/openapi-baseline.json`, unchanged (SHA-256 `0ADA1DCB…05B3B`). Design: `design.md`. All results are observed; raw output is in the subfolders.

## Modes and switch values

| Mode | `pilot.defects.st01-completed-to-cancelled.enabled` | `pilot.defects.sv01-total-as-string.enabled` | How set |
|---|---|---|---|
| A — clean | not set (`false`) | not set (`false`) | `.\docs\experiments\start-api.ps1` |
| B — ST-01 | `true` | not set (`false`) | `.\docs\experiments\start-api.ps1 -Defect st01-completed-to-cancelled` |

Every API start was checked for the startup warnings: mode A runs logged neither `DEFECT ST-01 ENABLED` nor `DEFECT SV-01 ENABLED`; mode B runs logged `DEFECT ST-01 ENABLED` once and never `DEFECT SV-01 ENABLED`.

## Tool versions

JDK 21.0.12 (Temurin), Spring Boot 4.1.1, Maven 3.10.0 (wrapper); Schemathesis 4.29.4 (Python 3.13.6, `docs/experiments/schemathesis/requirements.txt`); Postman CLI 1.74.0; Node 22.21.1 with Ajv 8.17.1 + ajv-formats 3.0.1.

## Summary

| Tool | Mode A (clean) | Mode B (ST-01) | Defect exercised? | Detected? |
|---|---|---|---|---|
| JUnit suite | 29 run, 0 failures | 29 run, **2 failures** (`Status expected:<409> but was:<200>`) | yes | **Detected** |
| Schema-only (16 responses) | 0 failures | 0 failures | **yes** (request 16 is `COMPLETED → CANCELLED` on a completed order; mode B returned 200 `CANCELLED`) | **Missed** |
| Schemathesis, baseline config | SB-01..SB-04 only | SB-01..SB-04 only; request/response sequence **identical** to mode A | **no** — no order reached `COMPLETED` | **Not exercised** |
| Schemathesis, EXT-1 (`--max-time 300`) | SB-01..SB-04 only | SB-01..SB-04 only | **no** — highest state reached `SHIPPED` | **Not exercised** |
| Postman (six-step sequence, 18 assertions) | 18 pass, exit 0 | **2 fail**, exit 1 (step 6 status, step 6 persisted state) | yes | **Detected** |

Database state in mode B: the forbidden request **changed the stored order** from `COMPLETED` to `CANCELLED` (repository check in `St01CompletedToCancelledTests`, fresh `GET` in Postman and in the schema-only capture). `completedAt` stayed set, so the cancelled order also carries a completion timestamp (side effect described in `design.md` §3). In mode A the stored order stayed `COMPLETED` with its `completedAt` unchanged.

## JUnit (`junit/`)

Commands: `.\mvnw.cmd -B clean test` (mode A) and `.\mvnw.cmd -B clean test "-Dpilot.defects.st01-completed-to-cancelled.enabled=true"` (mode B; the Maven property reaches the forked test JVM as a system property and therefore applies to every Spring test context — the log shows the ST-01 warning in all 4 contexts).

- Mode A: `Tests run: 29, Failures: 0`. The ST-01 and SV-01 warnings each appear once, from their own switch-test classes.
- Mode B: `Tests run: 29, Failures: 2`:
  - `OrderControllerTests.completedToCancelledIsRejectedWith409:181 Status expected:<409> but was:<200>`
  - `TransitionSequenceTests.fullLifecycleThenCancellingACompletedOrderIsRejected:72 Status expected:<409> but was:<200>`
- No other test failed in mode B (all other transitions are still enforced; `St01CompletedToCancelledTests.otherForbiddenTransitionsAreStillRejected` passes in both modes).

## Schema-only (`schema-only/`)

Same capture and validator as the baseline (`docs/experiments/schema-only/`), fresh API per mode.

| Request 16: `PATCH /api/orders/1/status {"status":"CANCELLED"}` (order 1 is `COMPLETED`) | Mode A | Mode B |
|---|---|---|
| HTTP status | 409 | 200 |
| Body | `{"timestamp":…,"status":409,"error":"Conflict","path":"/api/orders/1/status"}` | `{"ownerUserId":7,"total":49.99,"completedAt":"2026-10-19T08:29:07.977123","id":1,"status":"CANCELLED"}` |
| Schema-only verdict | pass (409 documented, no body schema) | pass (200 documented, body valid against `Order`) |

0 of 16 failures in both modes. The defect was exercised and produced a schema-valid response; schema-only validation has no notion of the previous state, so it cannot see that the transition was forbidden. The cross-field side effect (`CANCELLED` with non-null `completedAt`) is also schema-valid.

## Schemathesis

### Baseline configuration (`schemathesis/baseline-config/`)

Exactly the recorded configuration (`docs/experiments/schemathesis/README.md`, `run-schemathesis.ps1` without extra arguments), fresh API per mode.

| | Mode A | Mode B |
|---|---|---|
| Test cases / requests | 581 / 581 | 581 / 581 |
| Unique failures | 4: SB-01, SB-02, SB-03 (`not_a_server_error`), SB-04 (`positive_data_acceptance`) | the same 4 |
| Stateful | 57 scenarios, 5/9 inferred links covered | same |
| `PATCH …/status` responses | 200 × 9, 400 × 21, 404 × 8, 409 × 69, 415 × 1, 500 × 1 | identical |
| Order statuses seen in 2xx bodies | `CREATED`, `PAID`, `CANCELLED` | identical |
| Reached `COMPLETED`? | no | no |
| `COMPLETED → CANCELLED` attempted on a known-completed order | 0 | 0 |
| Request/response sequence vs mode A | — | **identical** (all 581 method/URL/body/status tuples; `mode-b-vs-mode-a.txt`) |

Classification: **unexercised defect in this run.** The defective code path was never executed (no order ever reached `COMPLETED`), which is why mode B's traffic is byte-for-byte the same as mode A's. This is not evidence about whether Schemathesis can detect state-transition defects.

### EXT-1: separate experiment with a larger time budget (`schemathesis/ext1-max-time-300/`)

Configuration: the recorded baseline configuration **plus `--max-time 300`** (`run-schemathesis.ps1 -ExtraArgs "--max-time","300"`), fresh API per mode. A time budget makes the amount of testing depend on machine speed, so EXT-1 runs are not expected to be request-for-request reproducible.

| | Mode A | Mode B |
|---|---|---|
| Duration | 302 s | 302 s |
| Test cases / requests | 15 325 / 15 316 | 15 750 / 15 740 |
| Stateful | 1 895 scenarios (1 861 passed, 34 failed), 6/9 links covered | 1 954 scenarios (1 919 passed, 35 failed), 6/9 links covered |
| Unique failures | the same 4 (SB-01..SB-04) | the same 4 |
| Order statuses seen in 2xx bodies | `CREATED`, `PAID`, `SHIPPED`, `CANCELLED` | same |
| Reached `COMPLETED`? | no (`DELIVERED` not reached either) | no |
| `COMPLETED → CANCELLED` attempted on a known-completed order | 0 | 0 |
| Summary line also reported | `9 errored` | `10 errored` |
| Warnings | "Missing test data" and "Schema validation mismatch", both for `PATCH /api/orders/{id}/status` | same |

Classification: **unexercised defect in this run too.** More budget moved the deepest state from `PAID` to `SHIPPED`, still two transitions short of `COMPLETED`.

Not explained: the "errored" counts appear only in the summary line; the NDJSON report contains no error events and no scenario with status `error`, and `stdout.txt` has no ERRORS section. Their cause was not determined. The "Schema validation mismatch" warning is consistent with most generated `PATCH` bodies being schema-valid but rejected with 409 (69 of 109 in the baseline run), but that interpretation was not verified.

EXT-1 HAR and NDJSON reports are stored gzip-compressed (`*.gz`, ~30–40 MB each uncompressed).

## Postman (`postman/`)

Collection `st-01.postman_collection.json` (generated by `build-collection.js`), run with `postman collection run docs\experiments\st-01\postman\st-01.postman_collection.json --no-report-events`, fresh API per mode. Each of the 6 steps asserts the response status code and returned `status`, then sends a fresh `GET /api/orders/{id}` (`pm.sendRequest`) and asserts the persisted status: 6 collection requests + 6 verification GETs, 18 assertions.

| Step | Mode A | Mode B |
|---|---|---|
| 1 `POST` | 201, `CREATED`, persisted `CREATED` | same |
| 2–4 `PAID`, `SHIPPED`, `DELIVERED` | 200, each status returned and persisted | same |
| 5 `COMPLETED` | 200, `COMPLETED`, `completedAt` set, persisted `COMPLETED` | same |
| 6 `CANCELLED` | **409**; persisted `COMPLETED` | **200**; persisted **`CANCELLED`** |
| Assertions | 18 pass, exit 0 | 16 pass, **2 fail**, exit 1 |

Mode B failures: `COMPLETED -> CANCELLED is rejected with 409` — `expected response to have status code 409 but got 200`; `persisted status is COMPLETED (fresh GET)` — `expected 'CANCELLED' to deeply equal 'COMPLETED'`.

No execution errors. The CLI printed its usual "No authorization data found" notice; runs were local and `--no-report-events` was set.

## Baseline findings that also appeared

SB-01 to SB-04 appeared unchanged in every Schemathesis run (both configurations, both modes) and are not counted as ST-01 detections. Schema-only and Postman showed no baseline findings.

## Deviations and limitations

- The planned Schemathesis comparison could not test detection, because the defect was never reached in either configuration. Reaching `COMPLETED` needs four consecutive successful `PATCH` calls on the same order; with the recorded configuration and with a 300 s budget, Schemathesis did not produce such a sequence. Not tried: explicit OpenAPI `links`, a stateful-only run, or other generation settings.
- `completedAt` is not cleared by the defect (inherited from the clean service). A tool that flagged the cancelled order could be reacting to the cross-field symptom rather than to the transition; none of the tools here reported it.
- The Postman collection and JUnit tests were written knowing the defect (an upper bound, as defined in the register).
- Schema-only and Postman each ran one sequence; Schemathesis generated hundreds (baseline) or thousands (EXT-1) of cases.
- `run-schemathesis.ps1` gained an optional `-ExtraArgs` parameter for EXT-1; with no extra arguments it runs exactly the recorded baseline command.

## Reproduce

```
.\docs\experiments\start-api.ps1 [-Defect st01-completed-to-cancelled]
node docs\experiments\schema-only\capture.js <out>\responses.json
node docs\experiments\schema-only\validate.js docs\openapi-baseline.json <out>\responses.json
.\docs\experiments\stop-api.ps1

.\docs\experiments\start-api.ps1 [-Defect st01-completed-to-cancelled]
.\docs\experiments\schemathesis\run-schemathesis.ps1 -OutDir <out> -Schemathesis <venv>\Scripts\schemathesis.exe [-ExtraArgs "--max-time","300"]
.\docs\experiments\stop-api.ps1
node docs\experiments\st-01\reachability.js <out>\har-*.json [<other run>\har-*.json]

.\docs\experiments\start-api.ps1 [-Defect st01-completed-to-cancelled]
postman collection run docs\experiments\st-01\postman\st-01.postman_collection.json --no-report-events
.\docs\experiments\stop-api.ps1

.\mvnw.cmd -B clean test ["-Dpilot.defects.st01-completed-to-cancelled.enabled=true"]
```
