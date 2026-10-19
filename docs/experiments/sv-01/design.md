# SV-01 experiment design — `total` returned as a JSON string

**Status:** Approved 2026-10-18 and implemented as written (Postman runner: Postman CLI). Results: `results.md`.

## 1. Intended violation

| | Clean mode (default) | Defect mode (SV-01 enabled) |
|---|---|---|
| Request | `POST /api/orders`, `Content-Type: application/json`, body `{"ownerUserId": 7, "total": 49.99}` | same |
| Status | `201` | `201` |
| Body | `{"ownerUserId":7,"total":49.99,"completedAt":null,"id":<n>,"status":"CREATED"}` | `{"ownerUserId":7,"total":"49.99","completedAt":null,"id":<n>,"status":"CREATED"}` |
| Saved order | `total = 49.99` | `total = 49.99` (unchanged) |
| `GET /api/orders/{id}` afterwards | `"total": 49.99` | `"total": 49.99` (unchanged; defect is limited to the `POST` response) |

Only the JSON type of one property in one operation's response changes. Status code, headers, other properties, property order, persistence and every other endpoint are unchanged. The string value is `BigDecimal.toPlainString()` of the saved total, so the value itself stays correct and only the type is wrong.

## 2. Is it a schema violation under the corrected baseline?

Yes. In `docs/openapi-baseline.json` (commit `32c7dc2`):

- `paths./api/orders.post.responses.201.content.application/json.schema` is `$ref: #/components/schemas/Order`.
- `components.schemas.Order.properties.total` is `{"type": "number", ...}` and `total` is in `Order.required`.

A JSON string is not a JSON number in JSON Schema 2020-12, so the `201` body violates the documented response schema. No other part of the contract is involved, and the contract will not be changed.

## 3. Feature switch

| Item | Design |
|---|---|
| Property | `pilot.defects.sv01-total-as-string.enabled` |
| Default | `false`; the property is **not** added to `application.properties`, so clean mode needs no configuration |
| Mechanism | New class `com.example.apipilot.defects.Sv01TotalAsStringAdvice`, a `@RestControllerAdvice` implementing `ResponseBodyAdvice<Object>`, annotated `@ConditionalOnProperty(name = "pilot.defects.sv01-total-as-string.enabled", havingValue = "true")` |
| Clean mode | The bean is **not created** at all; no code from the defect package runs |
| Defect mode | `supports()` matches only `OrderController.createOrder`. `beforeBodyWrite()` serializes the `Order` with the application's own JSON mapper to a JSON tree, replaces `total` with a string node, and returns the tree. Nothing else is touched |
| Visibility | A `WARN` log line at startup in defect mode: `DEFECT SV-01 ENABLED: POST /api/orders returns total as a JSON string` |
| Enable | `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--pilot.defects.sv01-total-as-string.enabled=true"` |
| Disable | Start without the argument (default) |
| Reversibility | Remove the argument; or delete the single `defects` class |

Why this mechanism: the controller method keeps its `Order createOrder(...)` signature, so Springdoc generates exactly the same OpenAPI document in both modes (re-exporting the baseline from clean mode stays byte-identical), and no business logic, entity, DTO, service or repository code changes.

## 4. Repeatable request and expected outcome per tool

| Tool | Procedure | Clean mode (observed baseline) | Defect mode — expected (H) |
|---|---|---|---|
| Schema-only (Ajv 2020-12) | Same 16-request capture as the baseline measurement, validated against `openapi-baseline.json` | 0 of 16 failures (measured) | H: the 2 `POST 201` responses fail with `/total must be number`; the other 14 pass |
| Schemathesis | `run-schemathesis.ps1` with the recorded configuration, fresh API | SB-01 to SB-04 (measured) | H: SB-01 to SB-04 plus a `response_schema_conformance` failure on `POST /api/orders` |
| Postman | One request (the one in §1) with test assertions `status 201`, `typeof total === "number"`, and a JSON-schema check using the `Order` schema from the baseline; run with the Postman CLI or Newman | to be measured | H: `typeof total` and schema assertions fail in defect mode; all pass in clean mode |

All three are run against the same freshly started API in each mode, and recorded separately per tool.

## 5. Tests

- **Existing clean-mode tests are unchanged and keep running in clean mode.** The normal suite never enables the switch, so all 22 tests are expected to stay green.
- Tests that *would* fail if run with SV-01 enabled (not done): `OrderControllerTests.createValidOrderReturns201WithStatusCreated` (`$.total` is `49.99`). Other tests do not inspect the `POST` response `total`.
- New: `defects/Sv01TotalAsStringTests` with `@SpringBootTest(properties = "pilot.defects.sv01-total-as-string.enabled=true")`, asserting that in defect mode `POST` returns `total` as the string `"49.99"` with everything else unchanged, and that `GET /api/orders/{id}` still returns a number. This proves the switch works; it does not change any clean-mode expectation.
- New assertion that the generated OpenAPI document is identical in both modes (guards against the defect leaking into the contract).
