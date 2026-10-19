# SV-01 results — `total` returned as a JSON string

**Date:** 2026-10-18. **Code:** commit `32c7dc2` plus the SV-01 switch (`defects/Sv01TotalAsStringAdvice`). **Contract:** `docs/openapi-baseline.json` (unchanged, SHA-256 `0ADA1DCB…05B3B`).
Design: `design.md` (approved before implementation). All results below are observed; raw output is in the subfolders.

## Switch verification

- Clean mode: the advice bean is not created (no `DEFECT SV-01 ENABLED` log line in any clean run or clean test context).
- Defect mode: the startup log contains `DEFECT SV-01 ENABLED: POST /api/orders returns total as a JSON string` in every defect run.
- `Sv01TotalAsStringTests` (3 tests, pass): defect-mode `POST` returns `"total":"49.99"` with all other fields unchanged and the order saved with `49.99`; `GET /api/orders/{id}` still returns a number; the generated OpenAPI document in defect mode equals `docs/openapi-baseline.json`.
- Clean-mode re-export of the baseline is byte-identical to the committed file.

## Per-tool results

Each tool was run separately, against its own freshly started API, in both modes.

| Tool | Clean mode | SV-01 mode | Outcome | Hypothesis in register |
|---|---|---|---|---|
| **Schema-only** (Ajv 8.17.1, 16-response capture) | 0 of 16 failures | **2 of 16 failures**: both `POST /api/orders → 201`, `/total must be number`; the other 14 responses pass | **Detected** | H: Likely — confirmed |
| **Schemathesis** 4.29.4, recorded baseline config, 2 runs per mode | 4 unique failures (SB-01 to SB-04) | **5 unique failures**: SB-01 to SB-04 plus `POST /api/orders` *Response violates schema* — `"49.99" is not of type "number"`, validated against the `201` schema at `/properties/total` (`response_schema_conformance`: 510 successes, 3 failures) | **Detected** (1 new unique failure) | H: Likely — confirmed |
| **Postman** CLI 1.74.0, 1 request, 3 assertions | 3 of 3 pass, exit 0 | **2 of 3 fail**, exit 1: `total is a JSON number` (`expected 'string' to deeply equal 'number'`), `body matches the baseline Order schema` (`data.total should be number`); `status is 201` passes | **Detected** | H: Likely — confirmed |

### Schemathesis details

- The defect was first hit in the **examples** phase (the spec's own example request `{"ownerUserId": 7, "total": 49.99}`); that phase passed in clean mode and failed in SV-01 mode. Phases failing in SV-01 mode: Examples, Coverage, Fuzzing, Stateful.
- The two SV-01-mode runs were identical apart from test-case IDs and timestamps (same failures, same 574 requests, same per-check counts).
- The request mix differs from the clean baseline (574 vs 581 requests; e.g. `POST 201` × 29 vs × 23). Data generation and stateful transitions depend on response content, so runs on a changed API are not request-for-request comparable to the clean baseline; only the failure sets are compared.
- SB-01 to SB-04 (pre-existing, see `docs/experiments/schemathesis/README.md`) appeared unchanged and are **not** counted as SV-01 detections.

### Execution failures

- **Postman attempt 1** (`postman/attempt1-run-*.txt`): the schema assertion errored in **both** modes with `unknown format "int64" is used in schema at path "#/properties/id"`. Postman's validator rejects the OpenAPI-specific `int64` format. This is an execution failure of the test, not a detection. The collection generator was changed to drop `format: int64` (the schema-only Ajv run ignores unknown formats, so both tools now apply the same schema); attempt 2 is the result reported above. In attempt 1 the `typeof` assertion already passed in clean mode and failed in SV-01 mode.
- Postman CLI printed `No authorization data found` and `Postman credentials are required for publishing run details`; the runs executed locally without login, and `--no-report-events` was set so no run data was sent.

## Reproduce

From the project root (Windows PowerShell):

```
.\docs\experiments\start-api.ps1 -Defect sv01-total-as-string     # omit -Defect for clean mode
node docs\experiments\schema-only\capture.js <out>\responses.json
node docs\experiments\schema-only\validate.js docs\openapi-baseline.json <out>\responses.json
.\docs\experiments\stop-api.ps1

.\docs\experiments\start-api.ps1 -Defect sv01-total-as-string
.\docs\experiments\schemathesis\run-schemathesis.ps1 -OutDir <out> -Schemathesis <venv>\Scripts\schemathesis.exe
.\docs\experiments\stop-api.ps1

.\docs\experiments\start-api.ps1 -Defect sv01-total-as-string
postman collection run docs\experiments\sv-01\postman\sv-01.postman_collection.json --no-report-events
.\docs\experiments\stop-api.ps1
```

`validate.js` needs `ajv@8.17.1` and `ajv-formats@3.0.1` (`docs/experiments/schema-only/package.json`); Schemathesis pins are in `docs/experiments/schemathesis/requirements.txt`.

## Limits of this result

- SV-01 is the easiest kind of defect for all three tools: a type error on a documented, always-reached `201` response. It says nothing yet about cross-field, state-transition, ownership or database-state defects.
- The Postman test was written knowing the defect (an upper bound, as defined in the register).
- One request per tool for schema-only and Postman; Schemathesis generated several hundred.
