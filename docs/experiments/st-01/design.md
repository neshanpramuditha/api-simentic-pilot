# ST-01 experiment design — `COMPLETED → CANCELLED` accepted

**Status:** Implemented 2026-10-19. Results: `results.md`.

## 1. Clean behaviour (inspected in `service/OrderService.java`)

`updateStatus` loads the order, looks the target up in `ALLOWED_TRANSITIONS`, and throws `ResponseStatusException(CONFLICT)` **before** `setStatus`/`save` when the pair is not allowed. `COMPLETED` maps to an empty set, so every transition out of `COMPLETED` is rejected and nothing is written.

| | Clean mode | ST-01 mode |
|---|---|---|
| Request | `PATCH /api/orders/{id}/status` `{"status":"CANCELLED"}` on an order in `COMPLETED` | same |
| Status | `409` | `200` |
| Body (running server, `Accept: application/json`) | Spring Boot default error body `{"timestamp","status":409,"error":"Conflict","path"}` | the order: `{"ownerUserId", "total", "completedAt": "<completion time>", "id", "status": "CANCELLED"}` |
| Persisted status | stays `COMPLETED` | becomes `CANCELLED` |
| Persisted `completedAt` | unchanged (set) | **unchanged (still set)** — see §3 |

Existing clean-mode test: `OrderControllerTests.completedToCancelledIsRejectedWith409` (asserts `409` and persisted `COMPLETED`). `createdToDeliveredIsRejectedWith409` covers another rejected transition.

## 2. Feature switch

| Item | Design |
|---|---|
| Property | `pilot.defects.st01-completed-to-cancelled.enabled` (default `false`, not set anywhere) |
| Mechanism | `com.example.apipilot.defects.St01CompletedToCancelledOrderService extends OrderService`, annotated `@Service @Primary @ConditionalOnProperty(..., havingValue = "true")`. It overrides `updateStatus`: if the stored status is `COMPLETED` and the target is `CANCELLED`, it sets the status and saves; every other request is delegated to `super.updateStatus` |
| Clean mode | The subclass bean does not exist; `OrderController` gets the unchanged `OrderService` |
| ST-01 mode | `@Primary` makes `OrderController` use the subclass. Only the one pair `COMPLETED → CANCELLED` is accepted; all other disallowed transitions still return `409` |
| Visibility | Startup `WARN`: `DEFECT ST-01 ENABLED: PATCH /api/orders/{id}/status accepts COMPLETED -> CANCELLED` |
| Enable | `.\docs\experiments\start-api.ps1 -Defect st01-completed-to-cancelled` (or `--pilot.defects.st01-completed-to-cancelled.enabled=true`) |
| SV-01 | Disabled throughout (its switch is never set) |

No clean-mode class (`OrderService`, controller, entity, DTOs) is modified, and the generated OpenAPI document is unchanged in both modes (asserted by a test).

## 3. Side effect of the injection

The clean service only ever *sets* `completedAt` (on `→ COMPLETED`) and never clears it. The defect reuses that behaviour, so after `COMPLETED → CANCELLED` the order is `CANCELLED` **with** a non-null `completedAt`. That additionally violates the cross-field rule of CF-01/CF-02 (`completedAt != null ⇔ status == COMPLETED`). It is recorded, not hidden: a tool that flags this order may be detecting the cross-field symptom rather than the transition itself.

## 4. Six-step sequence (JUnit and Postman)

| Step | Request | Clean: status / response `status` / persisted | ST-01: status / response `status` / persisted |
|---|---|---|---|
| 1 | `POST /api/orders` `{"ownerUserId": 7, "total": 49.99}` | 201 / `CREATED` / `CREATED` | same |
| 2 | `PATCH …/status` `PAID` | 200 / `PAID` / `PAID` | same |
| 3 | `PATCH …/status` `SHIPPED` | 200 / `SHIPPED` / `SHIPPED` | same |
| 4 | `PATCH …/status` `DELIVERED` | 200 / `DELIVERED` / `DELIVERED` | same |
| 5 | `PATCH …/status` `COMPLETED` | 200 / `COMPLETED`, `completedAt` set / `COMPLETED` | same |
| 6 | `PATCH …/status` `CANCELLED` | **409** / — / **`COMPLETED`** | **200** / `CANCELLED` / **`CANCELLED`** |

"Persisted" is checked with the repository in JUnit, and with a fresh `GET /api/orders/{id}` (sent from the test script with `pm.sendRequest`) in Postman. The Postman assertions encode the **clean** contract, so in ST-01 mode a failure at step 6 is a detection.
