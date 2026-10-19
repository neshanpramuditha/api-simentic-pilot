# Preliminary defect register

**Status:** Proposed. **SV-01** and **ST-01** have been implemented behind disabled-by-default switches and measured (results recorded under each). The other 15 defects have not been injected, and their detection expectations remain untested hypotheses.
**Baseline:** `docs/openapi-baseline.json`, exported from the clean API (OpenAPI 3.1.0, 4 operations). Regenerated on 2026-10-18 after correcting baseline issues BL-01 to BL-03 (see the end of this document).
**Date:** 2026-10-18

## How to read this register

Every detection entry is a **hypothesis (H)**, written before any tool has been run. Each has the form *outcome — reasoning*, so the reasoning can be checked against the measured result. Do not replace an H entry with a result until the experiment has actually been run; record the measured outcome next to it.

| Outcome label | Meaning |
|---|---|
| H: Likely | Expected to be detected, given the stated reasoning |
| H: Unlikely | Expected not to be detected |
| H: Conditional | Detection depends on the stated condition (test data, tool mode, spec content) |

The three detection approaches are defined as:

| Approach | Definition used in this register |
|---|---|
| **Schema-only** | Validating each HTTP response (status code, content type, body) against `openapi-baseline.json` in isolation, with no knowledge of earlier requests. |
| **Postman (written test)** | A Postman collection whose test scripts were written specifically to check the expected behaviour, including multi-request flows where needed. This is an upper bound: it assumes the tester anticipated the defect. |
| **Schemathesis (standard checks)** | Schemathesis run against `openapi-baseline.json` with its built-in checks only (e.g. `not_a_server_error`, `status_code_conformance`, `content_type_conformance`, `response_schema_conformance`, `negative_data_rejection`, `positive_data_acceptance`, `use_after_free`, `ensure_resource_availability`), no custom checks, no hand-written links, and an empty database at start. The exact version, check list and mode must be recorded when the experiment is run. |

Two facts about the baseline drove many of the Schemathesis hypotheses below, which were written before any Schemathesis run:

- The baseline contains **no OpenAPI `links`** and `POST /api/orders` returns **no `Location` header**, so it was assumed that Schemathesis has no declared way to chain "create an order, then read or update it", and that requests with generated IDs against an empty database would mostly receive `404`.
- `GET /api/users/{userId}/orders` returns `[]` (schema-valid) for any user with no orders.

> **Observed on the clean API (2026-10-18, Schemathesis 4.29.4, configuration in `docs/experiments/schemathesis/README.md`):** the first assumption did not hold. Schemathesis inferred 9 links by itself and covered 5; `GET /api/orders/{id}` returned 72 existing orders (`CREATED`, `PAID`, `CANCELLED`) and `GET /api/users/{userId}/orders` returned 55 `200` responses. No order reached `SHIPPED`, `DELIVERED` or `COMPLETED` in that configuration; with `--max-time 300` (EXT-1, `docs/experiments/st-01/results.md`) the deepest state was `SHIPPED`. The individual Schemathesis hypotheses below have **not** been revised or tested yet; those that rely on "no links" (SV-02, SV-03, DB-03 and others) should be read with this observation in mind and confirmed per defect.

## Summary

| ID | Category | Endpoint | Defect in one line |
|---|---|---|---|
| SV-01 | Schema violation | `POST /api/orders` | `total` returned as a JSON string |
| SV-02 | Schema violation | `GET /api/orders/{id}` | Required field `status` omitted |
| SV-03 | Schema violation | `GET /api/users/{userId}/orders` | `status` returned in lowercase (not an enum value) |
| SV-04 | Schema violation (request) | `POST /api/orders` | Zero or negative `total` accepted |
| CF-01 | Cross-field semantic | `PATCH /api/orders/{id}/status` | `completedAt` set on a non-`COMPLETED` status |
| CF-02 | Cross-field semantic | `PATCH /api/orders/{id}/status` | `completedAt` left `null` on `COMPLETED` |
| CF-03 | Cross-field semantic | `POST /api/orders` | Response `total` differs from request `total` |
| ST-01 | Invalid state transition | `PATCH /api/orders/{id}/status` | `COMPLETED → CANCELLED` accepted |
| ST-02 | Invalid state transition | `PATCH /api/orders/{id}/status` | `CREATED → DELIVERED` accepted |
| ST-03 | Invalid state transition | `PATCH /api/orders/{id}/status` | Same-status transition (`PAID → PAID`) accepted |
| ST-04 | Invalid state transition | `PATCH /api/orders/{id}/status` | Valid `CREATED → PAID` rejected with `409` |
| RO-01 | Resource ownership | `GET /api/users/{userId}/orders` | Other users' orders returned |
| RO-02 | Resource ownership | `POST /api/orders` | Order stored under a different `ownerUserId` |
| RO-03 | Resource ownership | `PATCH /api/orders/{id}/status` | Status change also changes `ownerUserId` |
| DB-01 | Database-state inconsistency | `PATCH /api/orders/{id}/status` | `200` with new status, but the change is not saved |
| DB-02 | Database-state inconsistency | `PATCH /api/orders/{id}/status` | `409` returned, but the change is saved anyway |
| DB-03 | Database-state inconsistency | `POST /api/orders` | `201` with an `id`, but the order is not saved |

---

## 1. Schema violations

### SV-01 — `total` returned as a string
- **Category:** Schema violation
- **Endpoint:** `POST /api/orders`
- **Expected behaviour:** `201` with `total` as a JSON number, e.g. `"total": 49.99`.
- **Intentionally defective behaviour:** `201` with `total` as a JSON string, e.g. `"total": "49.99"`.
- **Schema-only:** H: Likely — `Order.total` is `type: number`; a string violates it.
- **Postman (written test):** H: Likely — a JSON-schema assertion or `typeof total === "number"` check fails.
- **Schemathesis (standard checks):** H: Likely — Schemathesis generates schema-valid `POST` bodies that reach `201`, and `response_schema_conformance` validates the body.
- **Proposed semantic rule:** Not applicable (pure schema defect).
- **Measured (2026-10-18, `docs/experiments/sv-01/results.md`):** implemented behind `pilot.defects.sv01-total-as-string.enabled` (default off). Schema-only: **detected** (2 of 16 responses fail, `/total must be number`). Schemathesis 4.29.4: **detected** (new unique failure `"49.99" is not of type "number"`, `response_schema_conformance`; SB-01 to SB-04 also present, as in clean mode). Postman CLI 1.74.0: **detected** (2 of 3 assertions fail; first attempt had a test execution error, see results). All three hypotheses confirmed.

### SV-02 — required `status` omitted
- **Category:** Schema violation
- **Endpoint:** `GET /api/orders/{id}`
- **Expected behaviour:** `200` with all of `id`, `ownerUserId`, `status`, `total`.
- **Intentionally defective behaviour:** `200` with the `status` property missing from the body.
- **Schema-only:** H: Likely — `status` is listed in `Order.required`.
- **Postman (written test):** H: Likely — a schema or `has status` assertion fails.
- **Schemathesis (standard checks):** H: Conditional — only if a generated `id` matches an existing order. With an empty database and no `POST → GET` link, most requests return `404`, so the defective `200` body may never be validated.
- **Proposed semantic rule:** Not applicable.

### SV-03 — `status` not an enum value
- **Category:** Schema violation
- **Endpoint:** `GET /api/users/{userId}/orders`
- **Expected behaviour:** Each item's `status` is one of `CREATED, PAID, SHIPPED, DELIVERED, COMPLETED, CANCELLED`.
- **Intentionally defective behaviour:** `status` returned in lowercase, e.g. `"paid"`.
- **Schema-only:** H: Likely — but only when the list is non-empty; an empty array is valid.
- **Postman (written test):** H: Likely — if the test first creates an order for the user and then checks the enum.
- **Schemathesis (standard checks):** H: Conditional — only if a generated `userId` owns at least one order. With an empty database every response is `[]`.
- **Proposed semantic rule:** Not applicable.

### SV-04 — non-positive `total` accepted
- **Category:** Schema violation (request side)
- **Endpoint:** `POST /api/orders`
- **Expected behaviour:** `400` for `total` ≤ 0, as declared by `exclusiveMinimum: 0` on `CreateOrderRequest.total`.
- **Intentionally defective behaviour:** Request validation removed; `{"ownerUserId": 7, "total": 0}` returns `201` and is saved.
- **Schema-only:** H: Unlikely — the `201` response body is itself schema-valid; the response schema `Order.total` has no minimum. Only validating the *request* would reveal the problem.
- **Postman (written test):** H: Likely — a negative test sending `total: 0` and expecting `400` fails.
- **Schemathesis (standard checks):** H: Conditional — likely via `negative_data_rejection` **if negative test generation is enabled** in the mode used; not detected in positive-only mode.
- **Proposed semantic rule:** For any `2xx` response to `POST /api/orders`, the request body must have satisfied `CreateOrderRequest` (server must reject what the contract forbids). Response-side: `Order.total > 0`.

## 2. Cross-field semantic violations

### CF-01 — `completedAt` set too early
- **Category:** Cross-field semantic violation
- **Endpoint:** `PATCH /api/orders/{id}/status`
- **Expected behaviour:** `completedAt` is `null` unless `status` is `COMPLETED`.
- **Intentionally defective behaviour:** Moving to `PAID` (or any non-final status) sets `completedAt` to the current time.
- **Schema-only:** H: Unlikely — a timestamp string is valid for `completedAt` regardless of `status`; the schema has no cross-field condition.
- **Postman (written test):** H: Likely — after `CREATED → PAID`, assert `completedAt === null`.
- **Schemathesis (standard checks):** H: Unlikely — needs an existing order, and no standard check relates two fields.
- **Proposed semantic rule:** `completedAt != null ⇔ status == COMPLETED`.

### CF-02 — `completedAt` missing on completion
- **Category:** Cross-field semantic violation
- **Endpoint:** `PATCH /api/orders/{id}/status`
- **Expected behaviour:** `DELIVERED → COMPLETED` sets `completedAt` to the completion time.
- **Intentionally defective behaviour:** Status becomes `COMPLETED` but `completedAt` stays `null`.
- **Schema-only:** H: Unlikely — since the BL-01 correction, `completedAt` is documented as `["string", "null"]`, so `null` is schema-valid on any status. (Against the original baseline, `null` was flagged on every clean non-completed order, so it never distinguished this defect either.)
- **Postman (written test):** H: Likely — after the four-step flow to `COMPLETED`, assert `completedAt` is non-null.
- **Schemathesis (standard checks):** H: Unlikely — reaching `COMPLETED` requires four chained `PATCH` calls on one order, which the baseline gives no links for.
- **Proposed semantic rule:** Same as CF-01: `completedAt != null ⇔ status == COMPLETED`.

### CF-03 — response `total` differs from request
- **Category:** Cross-field semantic violation (request ↔ response)
- **Endpoint:** `POST /api/orders`
- **Expected behaviour:** Response `total` and `ownerUserId` equal the request values.
- **Intentionally defective behaviour:** `total` rounded to a whole number before saving, e.g. `49.99 → 50.00`.
- **Schema-only:** H: Unlikely — `50.00` is a valid number; the response is not compared with the request.
- **Postman (written test):** H: Likely — assert `response.total === request.total`.
- **Schemathesis (standard checks):** H: Unlikely — no standard check compares request and response values.
- **Proposed semantic rule:** For `POST /api/orders` `201`: `response.total == request.total` and `response.ownerUserId == request.ownerUserId`.

## 3. Invalid state transitions

Allowed transitions in the clean API: `CREATED → PAID | CANCELLED`, `PAID → SHIPPED | CANCELLED`, `SHIPPED → DELIVERED`, `DELIVERED → COMPLETED`. `COMPLETED` and `CANCELLED` are final. Anything else returns `409`.

### ST-01 — leaving a final state
- **Category:** Invalid state transition
- **Endpoint:** `PATCH /api/orders/{id}/status`
- **Expected behaviour:** `COMPLETED → CANCELLED` returns `409`; status stays `COMPLETED`.
- **Intentionally defective behaviour:** Returns `200` and the order becomes `CANCELLED`.
- **Schema-only:** H: Unlikely — `200` is documented and the body is schema-valid. Detecting it requires knowing the previous status.
- **Postman (written test):** H: Likely — a flow that drives an order to `COMPLETED`, then tries `CANCELLED` and expects `409`.
- **Schemathesis (standard checks):** H: Unlikely — `200` is a documented status code, and reaching `COMPLETED` needs a chained flow.
- **Proposed semantic rule:** For `PATCH …/status` `2xx`: `(previousStatus, response.status)` must be in the allowed-transition table. `previousStatus` comes from a prior `GET` or the last observed response for that `id`.
- **Measured (2026-10-19, `docs/experiments/st-01/results.md`):** implemented behind `pilot.defects.st01-completed-to-cancelled.enabled` (default off; SV-01 off). In ST-01 mode the forbidden request returns `200` and the stored order changes from `COMPLETED` to `CANCELLED` (with `completedAt` still set). Schema-only: **missed** — the defect was exercised (200, schema-valid body), 0 of 16 failures. Postman CLI 1.74.0: **detected** (2 of 18 assertions fail at step 6: status and persisted state). JUnit suite: **detected** (2 of 29 fail). Schemathesis 4.29.4, baseline configuration: **not exercised** — no order reached `COMPLETED`; traffic identical to clean mode. Separate experiment EXT-1 (`--max-time 300`): **not exercised** — deepest state `SHIPPED`. Schema-only and Postman hypotheses confirmed; the Schemathesis hypothesis ("Unlikely") is neither confirmed nor refuted, because the defect was never reached.

### ST-02 — skipping lifecycle steps
- **Category:** Invalid state transition
- **Endpoint:** `PATCH /api/orders/{id}/status`
- **Expected behaviour:** `CREATED → DELIVERED` returns `409`.
- **Intentionally defective behaviour:** Returns `200` and the order becomes `DELIVERED`.
- **Schema-only:** H: Unlikely — same reasoning as ST-01.
- **Postman (written test):** H: Likely — create an order, `PATCH` to `DELIVERED`, expect `409`.
- **Schemathesis (standard checks):** H: Unlikely — a `200` here is documented; even if a generated `id` hits a `CREATED` order, no standard check knows the transition table.
- **Proposed semantic rule:** Same transition-table rule as ST-01.

### ST-03 — same-status transition accepted
- **Category:** Invalid state transition
- **Endpoint:** `PATCH /api/orders/{id}/status`
- **Expected behaviour:** `PAID → PAID` returns `409` (not in the table).
- **Intentionally defective behaviour:** Returns `200` and leaves the order unchanged.
- **Schema-only:** H: Unlikely.
- **Postman (written test):** H: Likely — but only if the tester thinks to test a no-op transition.
- **Schemathesis (standard checks):** H: Unlikely.
- **Proposed semantic rule:** Same transition-table rule as ST-01 (which excludes `from == to`).

### ST-04 — valid transition rejected
- **Category:** Invalid state transition (over-restrictive)
- **Endpoint:** `PATCH /api/orders/{id}/status`
- **Expected behaviour:** `CREATED → PAID` returns `200`.
- **Intentionally defective behaviour:** Returns `409`; status stays `CREATED`.
- **Schema-only:** H: Unlikely — `409` is a documented response for this operation.
- **Postman (written test):** H: Likely — the happy-path test expects `200`.
- **Schemathesis (standard checks):** H: Conditional — `positive_data_acceptance` may flag a `4xx` for schema-valid input, but the clean API already returns `404`/`409` for many schema-valid inputs (unknown ids, disallowed transitions). How that check treats those must be measured on the clean baseline first.
- **Proposed semantic rule:** For `PATCH …/status`: if `(previousStatus, requestedStatus)` is in the allowed table and the order exists, the response must be `200`.

## 4. Resource-ownership violations

The pilot has no authentication, so "ownership" here means consistency of `ownerUserId` across operations, not access control.

### RO-01 — other users' orders returned
- **Category:** Resource-ownership violation
- **Endpoint:** `GET /api/users/{userId}/orders`
- **Expected behaviour:** Only orders whose `ownerUserId` equals the path `userId`.
- **Intentionally defective behaviour:** The filter is ignored and every order is returned.
- **Schema-only:** H: Unlikely — every item is a valid `Order`; the schema does not relate items to the path parameter.
- **Postman (written test):** H: Likely — create orders for two users and assert every item's `ownerUserId` equals the path `userId`.
- **Schemathesis (standard checks):** H: Unlikely — needs orders for at least two users, and no standard check compares body fields with path parameters.
- **Proposed semantic rule:** For `GET /api/users/{userId}/orders` `200`: `∀ item: item.ownerUserId == path.userId`.

### RO-02 — order stored under a different owner
- **Category:** Resource-ownership violation
- **Endpoint:** `POST /api/orders`
- **Expected behaviour:** The saved order's `ownerUserId` equals the request's `ownerUserId`.
- **Intentionally defective behaviour:** `ownerUserId` is replaced with a fixed value (e.g. `1`) before saving; the response shows the replaced value.
- **Schema-only:** H: Unlikely — `1` is a valid integer.
- **Postman (written test):** H: Likely — assert `response.ownerUserId === request.ownerUserId`, and/or that the order appears in `GET /api/users/{requested}/orders`.
- **Schemathesis (standard checks):** H: Unlikely — no request-to-response comparison.
- **Proposed semantic rule:** For `POST /api/orders` `201`: `response.ownerUserId == request.ownerUserId` (shared with CF-03).

### RO-03 — owner changed by a status update
- **Category:** Resource-ownership violation
- **Endpoint:** `PATCH /api/orders/{id}/status`
- **Expected behaviour:** A status change never alters `ownerUserId`.
- **Intentionally defective behaviour:** The update also sets `ownerUserId` to a different value (e.g. `0`).
- **Schema-only:** H: Unlikely — `Order.ownerUserId` in the response schema has no minimum, so `0` is valid.
- **Postman (written test):** H: Likely — compare `ownerUserId` before and after the `PATCH`.
- **Schemathesis (standard checks):** H: Unlikely.
- **Proposed semantic rule:** `ownerUserId` is immutable: for any response about order `id`, `ownerUserId` equals the value at creation.

## 5. Database-state inconsistencies

### DB-01 — status change not saved
- **Category:** Database-state inconsistency
- **Endpoint:** `PATCH /api/orders/{id}/status`
- **Expected behaviour:** After `200`, a `GET /api/orders/{id}` returns the new status.
- **Intentionally defective behaviour:** The response shows the new status, but the change is never written; `GET` returns the old status.
- **Schema-only:** H: Unlikely — each response is valid on its own.
- **Postman (written test):** H: Likely — `PATCH` then `GET`, compare status.
- **Schemathesis (standard checks):** H: Unlikely — requires a `PATCH → GET` chain on the same id.
- **Proposed semantic rule:** Read-after-write: after a `2xx` `PATCH`, the next `GET` of the same `id` returns the same `status` and `completedAt`.

### DB-02 — rejected change saved anyway
- **Category:** Database-state inconsistency
- **Endpoint:** `PATCH /api/orders/{id}/status`
- **Expected behaviour:** After `409`, the order is unchanged.
- **Intentionally defective behaviour:** The new status is saved *before* the transition check, so `409` is returned but `GET` shows the forbidden status.
- **Schema-only:** H: Unlikely.
- **Postman (written test):** H: Likely — record status, send a forbidden transition, expect `409`, then `GET` and compare.
- **Schemathesis (standard checks):** H: Unlikely.
- **Proposed semantic rule:** After a `4xx` `PATCH`, the next `GET` of the same `id` returns the same state as before the request.

### DB-03 — created order not saved
- **Category:** Database-state inconsistency
- **Endpoint:** `POST /api/orders`
- **Expected behaviour:** After `201` with `id = N`, `GET /api/orders/N` returns `200`.
- **Intentionally defective behaviour:** `201` with a plausible `id`, but nothing is saved; `GET` returns `404`.
- **Schema-only:** H: Unlikely — both responses are documented and valid.
- **Postman (written test):** H: Likely — `POST` then `GET` the returned `id`.
- **Schemathesis (standard checks):** H: Conditional — this is what `ensure_resource_availability` targets, but that check relies on stateful links from `POST` to `GET`. The baseline has none, so it may not be exercised unless the installed version infers them.
- **Proposed semantic rule:** Read-after-create: after `201` with body `id = N`, `GET /api/orders/N` returns `200` with the same body.

---

## Baseline issues (found in the clean API's documentation, not injected)

These were mismatches between the clean API and its generated contract. They were investigated against actual serialized responses on 2026-10-18. Every correction is documentation-only (OpenAPI annotations); no business logic, endpoint behaviour, serialization or database behaviour was changed.

| ID | Issue | Classification | Treatment |
|---|---|---|---|
| BL-01 | `Order.completedAt` was documented as `type: string`, but the clean API returns `"completedAt": null` for every order that is not `COMPLETED`. | Genuine contract mismatch | **Fixed.** Now `type: ["string", "null"]` (not in `required`; Jackson always writes the property). |
| BL-02 | `completedAt` was documented as `format: date-time` (RFC 3339, which requires a UTC offset). The API actually sends `LocalDateTime` as ISO-8601 local date-time in server time with no offset, and a variable number of fractional digits, e.g. `2026-10-18T21:03:39.154171` (server zone UTC+05:30). | Genuine contract mismatch | **Fixed in the schema, not the serialization.** `format` removed; replaced by `pattern: ^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{1,9})?$` and a description stating server local time without offset. Changing to `OffsetDateTime`/`Instant` was rejected for now: it would change the wire format (adds an offset), the meaning (wall-clock → absolute instant) and probably the column type, i.e. API behaviour. |
| BL-03 | `GET /api/orders/{id}` and `GET /api/users/{userId}/orders` did not document `400`, but a non-integer or out-of-range path value (`abc`, `99999999999999999999`) returns `400`. | Genuine contract mismatch | **Fixed.** `400` documented on both operations; the `PATCH` operation's existing `400` description now also names the invalid-ID case. |
| BL-04 | Error responses (`400`, `404`, `409`) have no body schema. Observed body for `Accept: application/json` or `*/*`: `{"timestamp": "…Z", "status": <int>, "error": "<reason phrase>", "path": "<request path>"}`, identical in shape for all three codes. For `Accept: text/html` the body is empty `text/html`. | Optional documentation improvement (an undescribed body is incomplete, not wrong) | **Deferred.** The body is Spring Boot's default error format: it varies with the `Accept` header and with `server.error.*` properties, and is likely to be replaced if a `ProblemDetail` handler is added. Adding a docs-only class now would bake a transient format into the baseline. |

### Measured effect (schema-only validation of the clean API)

16 real responses were captured from the clean API (fresh database, `Accept: application/json`) and validated in isolation with Ajv 8.17.1 (JSON Schema 2020-12, `ajv-formats` 3.0.1, format assertion on): status-code documented, and body valid against the documented schema where one exists. The set was 2 × `POST` 201, 2 × `GET /orders/{id}` 200, 5 × `PATCH` 200 (through to `COMPLETED` and `CANCELLED`), 2 × `GET /users/{userId}/orders` 200 (one item, empty), 404, 2 × 400 for non-integer path IDs, 400 for `total: 0`, 409 for `COMPLETED → CANCELLED`.

| Contract | Schema-only failures on the clean API | Causes |
|---|---|---|
| Original baseline (commit `3b6f459`) | **12 of 16** | `completedAt must be string` (7, BL-01), `completedAt must match format "date-time"` (3, BL-02), `status 400 not documented` (2, BL-03) |
| Corrected baseline | **0 of 16** | — (5 error responses have no body schema, so only their status codes were checked; BL-04) |

### Remaining known limitations (not fixed)

| ID | Limitation | Status |
|---|---|---|
| BL-04 | Error bodies are not schema-checked (see above). | Deferred |
| BL-05 | Inconsistent time semantics: error `timestamp` is UTC with `Z`; `completedAt` is server local time without offset. A `completedAt` value cannot be converted to an instant without knowing the server's time zone. | Recorded; changing either would alter API behaviour |
| BL-06 | Other framework-generated status codes are not documented, e.g. `415` (wrong `Content-Type`), `406` (unsatisfiable `Accept`), `405` (wrong method), `500`. | Not measured; may appear as `status_code_conformance` findings if a tool sends such requests (H) |
| BL-07 | Authentication is not defined, so Redocly's recommended ruleset reports 4 `security-defined` errors; also `info-license` and `no-server-example.com` (localhost) warnings. | By design for the pilot; structural validation passes |
| BL-08 | Any request with `Content-Type: multipart/form-data` and no boundary returns `500` (Tomcat `FileUploadException` not mapped to 4xx), on all four operations. | Observed (Schemathesis SB-01 to SB-03, reproduced manually). Actual API defect, not injected; fixing it changes behaviour, so left as is |
| BL-09 | `CreateOrderRequest.total` is limited by `@Digits(integer = 17, fraction = 2)`, but the baseline expresses this only in prose, so schema-valid values such as `0.001` and `5e-324` are rejected with `400`. | Observed (Schemathesis SB-04, reproduced manually). Specification issue; not fixed yet |

Re-export the baseline (see `docs/README.md`) after any further change to the contract, and before injecting defects.

## Existing JUnit suite versus the proposed defects (H)

The current suite (`OrderControllerTests`, and for CF-02 also `ContractConformanceTests`) already asserts several of these behaviours, so injecting the corresponding defect is expected to make `mvnw test` fail. Plan for this (for example, inject defects behind a switch so the clean build stays green).

| Expected to fail the current suite (H) | Not covered by the current suite (H) |
|---|---|
| SV-01, SV-02, SV-04, CF-01, CF-02, CF-03, ST-01, ST-02, ST-04, RO-01, RO-02, DB-01, DB-02, DB-03 | SV-03 (status not asserted in the list test), ST-03 (no same-status test), RO-03 (owner not re-checked after `PATCH`) |
