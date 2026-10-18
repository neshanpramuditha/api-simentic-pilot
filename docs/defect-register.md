# Preliminary defect register

**Status:** Proposed. None of these defects has been injected into the API, and none of the detection expectations has been tested.
**Baseline:** `docs/openapi-baseline.json`, exported from the clean API (OpenAPI 3.1.0, 4 operations).
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

Two facts about the baseline drive many of the Schemathesis hypotheses:

- The baseline contains **no OpenAPI `links`** and `POST /api/orders` returns **no `Location` header**, so Schemathesis has no declared way to chain "create an order, then read or update it". Requests with generated IDs against an empty database will mostly receive `404`.
- `GET /api/users/{userId}/orders` returns `[]` (schema-valid) for any user with no orders.

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
- **Schema-only:** H: Conditional — a strict validator would flag `null` against `type: string`, but see **BL-01**: it would flag the same `null` on every clean non-completed order, so the signal does not distinguish this defect.
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

## Known baseline issues (found during baseline export, not injected)

These are properties of the clean API's documentation that may cause tools to report problems against the **clean** API. They should be measured on the clean baseline before any defect is injected, so they are not mistaken for injected-defect detections.

| ID | Issue | Evidence | Possible effect (H) |
|---|---|---|---|
| BL-01 | `Order.completedAt` is documented as `type: string` only, but the clean API returns `null` for every order that is not `COMPLETED`. | `openapi-baseline.json` → `components.schemas.Order.properties.completedAt`; clean responses observed earlier with `"completedAt": null`. | Strict response-schema validation (schema-only, Schemathesis `response_schema_conformance`) may flag every clean `Order` response. |
| BL-02 | `completedAt` is documented as `format: date-time` (RFC 3339, which requires a timezone offset), but the clean API returns a local timestamp without offset, e.g. `2026-10-18T20:11:35.9973185`. | `entity/Order.java` uses `LocalDateTime`. | Flagged only by validators that assert `format`; many do not by default. |
| BL-03 | `GET /api/orders/{id}` and `GET /api/users/{userId}/orders` do not document `400`, but a non-integer path value (e.g. `/api/orders/abc`) returns `400` from Spring. | Response lists in `openapi-baseline.json`; observed on the clean API on 2026-10-18: `GET /api/orders/abc` → `400`, `GET /api/users/abc/orders` → `400`. | Schemathesis `status_code_conformance` may flag negative-mode requests against the clean API. |
| BL-04 | Error responses (`400`, `404`, `409`) are documented without a body schema. | Response objects have `description` only. | Error bodies cannot be schema-validated; tools may skip or flag them depending on how they treat undocumented content. |

Fixing BL-01 to BL-03 would change the baseline. If they are fixed, re-export the baseline (see `docs/README.md`) before injecting defects.

## Existing JUnit suite versus the proposed defects (H)

The current suite (`OrderControllerTests`) already asserts several of these behaviours, so injecting the corresponding defect is expected to make `mvnw test` fail. Plan for this (for example, inject defects behind a switch so the clean build stays green).

| Expected to fail the current suite (H) | Not covered by the current suite (H) |
|---|---|
| SV-01, SV-02, SV-04, CF-01, CF-02, CF-03, ST-01, ST-02, ST-04, RO-01, RO-02, DB-01, DB-02, DB-03 | SV-03 (status not asserted in the list test), ST-03 (no same-status test), RO-03 (owner not re-checked after `PATCH`) |
