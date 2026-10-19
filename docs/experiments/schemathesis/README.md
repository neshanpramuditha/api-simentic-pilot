# Schemathesis measurements

All results in this folder are **observed**, from the runs whose raw output is stored here. Hypotheses live in `docs/defect-register.md`.

## Recorded baseline configuration

| Item | Value |
|---|---|
| Schemathesis | 4.29.4 (released 2026-10-06; 4.32.0 was current on the run date but released the same day, so not used) |
| Python | 3.13.6, isolated virtual environment; full pin list in `requirements.txt` (`hypothesis==6.170.1`, `jsonschema_rs==0.58.6`, …) |
| Schema | `docs/openapi-baseline.json` at commit `32c7dc2` (local file, not the live `/v3/api-docs`) |
| Target | `http://localhost:8080`, application started fresh (empty H2 `semanticdb`) immediately before each run |
| Checks | `--checks all` → 14 checks executed (see results) |
| Generation mode | `--mode all` (positive and negative data) |
| Phases | `examples, coverage, fuzzing, stateful` |
| Examples per operation | `--max-examples 50` |
| Seed | `--seed 42` |
| Workers | `--workers 1` (serial, so database state evolves in a fixed order) |
| Example database | `--generation-database none` (no reuse of examples between runs) |
| Links | None in the spec; Schemathesis inferred them itself (see results) |
| Reports | JUnit, NDJSON, HAR |
| Crash cache | Schemathesis stores failing cases in `.schemathesis/` (git-ignored). Run 1 started without it, run 2 with run 1's cache; results were identical. `run-schemathesis.ps1` deletes it before every run so all runs start like run 1. |

Command (as run by `run-schemathesis.ps1`):

```
schemathesis run docs/openapi-baseline.json --url http://localhost:8080 --checks all --mode all \
  --phases examples,coverage,fuzzing,stateful --max-examples 50 --seed 42 --workers 1 \
  --generation-database none --no-color --report junit,ndjson,har --report-dir <out>
```

To reproduce: install `requirements.txt` into a fresh virtual environment, start the API fresh (`.\mvnw.cmd spring-boot:run`), then from the project root run `.\docs\experiments\schemathesis\run-schemathesis.ps1 -OutDir <out> -Schemathesis <path to schemathesis.exe>`. Summarise with `node docs\experiments\schemathesis\analyze-run.js <out>`.

## Clean-API baseline (2026-10-18, commit `32c7dc2`, no defects injected)

Raw output: `clean-baseline/run1/` and `clean-baseline/run2/` (`stdout.txt`, `junit.xml`, `events.ndjson`, `requests.har.json`). Report files were renamed from Schemathesis's timestamped names. The absolute local report directory (which contains the Windows user name) was replaced by `<report-dir>\` on the "Reports:" lines of every `stdout.txt` and in the recorded command line in the first event of every `events.ndjson`; Maven logs had the user home, project path and user name replaced by `<user-home>`, `<project>` and `<user>`. No other edits. **Correction:** the NDJSON redaction was initially missed; the unpushed SV-01 commit was amended (`db978dc` → `73bae48`, NDJSON files only) before anything was pushed, so the branch history contains only the redacted files. A scan of all evidence found no credentials, cookies, tokens or authorization headers (requests carry only `Accept`, `Accept-Encoding`, `Connection`, `Content-Length`, `Content-Type`, `User-Agent`, `X-Schemathesis-TestCaseId`).

### Reproducibility

Two runs with the configuration above, each against a freshly started API, produced identical phase results, identical failure list, identical request count (581) and identical status-code counts per operation. The only differences in `stdout.txt` are the randomly assigned test-case IDs and timestamps.

### Summary

| Phase | Result |
|---|---|
| Examples | 4 passed |
| Coverage | 4 failed |
| Fuzzing | 3 passed, 1 failed |
| Stateful | 57 scenarios, 56 passed, 1 failed; API links: 5 covered / 9 selected / 9 total (**9 inferred**) |
| Total | 581 test cases generated, 4 unique failures, 3 skipped; exit code 1 |

### Check executions (run 1, from `events.ndjson`)

| Check | Successes | Failures |
|---|---|---|
| `not_a_server_error` | 575 | 3 |
| `positive_data_acceptance` | 373 | 3 (reported as 1 unique failure) |
| `response_schema_conformance` | 546 | 0 |
| `status_code_conformance` | 546 | 0 |
| `content_type_conformance` | 546 | 0 |
| `response_headers_conformance` | 546 | 0 |
| `negative_data_rejection` | 546 | 0 |
| `use_after_free` | 546 | 0 |
| `ensure_resource_availability` | 546 | 0 |
| `ignored_auth` | 546 | 0 |
| `allow_header_conformance`, `missing_required_header`, `unsupported_method`, `max_response_time` | 578 each | 0 |

### What was exercised (run 1, from `requests.har.json`)

| Operation | Requests | Status codes |
|---|---|---|
| `POST /api/orders` | 294 | 201 × 23, 400 × 271 |
| `GET /api/orders/{id}` | 85 | 200 × 72, 400 × 7, 404 × 5, 500 × 1 |
| `GET /api/users/{userId}/orders` | 65 | 200 × 55, 400 × 9, 500 × 1 |
| `PATCH /api/orders/{id}/status` | 109 | 200 × 9, 400 × 21, 404 × 8, 409 × 69, 415 × 1, 500 × 1 |
| Undocumented methods on the 4 paths (`unsupported_method`) | 24 + 4 `OPTIONS` | 405 × 24, `OPTIONS` 200 × 4 |

- `GET /api/orders/{id}` returned 72 existing orders; their statuses were `CREATED` × 39, `PAID` × 8, `CANCELLED` × 25.
- Successful `PATCH` responses produced only `PAID` × 6 and `CANCELLED` × 3. **No order reached `SHIPPED`, `DELIVERED` or `COMPLETED`** in this configuration.
- `response_schema_conformance` ran on every documented response, including the 72 order bodies above, with 0 failures. This agrees with the schema-only measurement in `docs/defect-register.md` (0 of 16).
- One `415` (PATCH) and the `405`s were observed with no `status_code_conformance` failure. Why Schemathesis did not flag them was not investigated.

### Findings and classification

Each finding was reproduced manually with `curl` against a freshly started API and the server log was inspected.

| ID | Finding (Schemathesis check) | Reproduction | Investigation | Classification |
|---|---|---|---|---|
| SB-01 | `GET /api/orders/{id}` → `500` with `Content-Type: multipart/form-data` and no boundary (`not_a_server_error`) | `curl -X GET -H "Content-Type: multipart/form-data" http://localhost:8080/api/orders/1` → `500` | Server log: `FileUploadException: the request was rejected because no multipart boundary was found`, raised while Spring's `DispatcherServlet` checks for a multipart request and not mapped to a 4xx. With `boundary=x` or `Content-Type: text/plain` the same request → `200`. | **Actual API defect** (pre-existing, framework default; not one of the 17 proposed defects). Robustness issue outside the documented contract. |
| SB-02 | Same on `GET /api/users/{userId}/orders` → `500` | as above | same cause | Actual API defect (same root cause as SB-01) |
| SB-03 | Same on `PATCH /api/orders/{id}/status` → `500` | as above, with body `{"status":"PAID"}` | same cause; also reproduced manually on `POST /api/orders` (→ `500`), which Schemathesis did not report in this run | Actual API defect (same root cause as SB-01) |
| SB-04 | `POST /api/orders` with `{"ownerUserId": 7, "total": 5e-324}` → `400` (`positive_data_acceptance`) | reproduced; also `0.001` → 400, `0.01` → 201, `123456789012345678` → 400 | The request is valid under the baseline (`total`: number, `exclusiveMinimum: 0`). The API rejects it because of `@Digits(integer = 17, fraction = 2)` on `CreateOrderRequest.total`, which the baseline expresses only in the field description, not as a machine-readable constraint. | **Specification issue**: the contract under-specifies a real, intended request constraint. API behaviour is as designed. |

No findings were classified as configuration problems or tool limitations.

### Consequences for later runs

- SB-01 to SB-04 are present in the clean API. Any defect experiment using this configuration will report them as well; they must be subtracted, not counted as detections.
- Links: Schemathesis 4.29.4 inferred 9 links from the baseline without any `links` in the spec, and stateful testing reached existing orders. The register's preliminary assumption that Schemathesis would have "no declared way to chain" operations was wrong for this version and configuration; see the note in `docs/defect-register.md`.
- Under this configuration no order reached `SHIPPED`, `DELIVERED` or `COMPLETED`. A separate experiment, EXT-1 (`--max-time 300`, see `docs/experiments/st-01/results.md`), reached `SHIPPED` but not `DELIVERED` or `COMPLETED`. `run-schemathesis.ps1 -ExtraArgs …` runs such variants; without `-ExtraArgs` it runs exactly the configuration above.
