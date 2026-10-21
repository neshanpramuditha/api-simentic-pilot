# DB-02 — rejected status change saved anyway

**Status:** Implemented 2026-10-19 and measured in the comparative evaluation (`docs/experiments/evaluation/`).

## Why it is a database-state consistency violation

The HTTP exchange is identical to clean mode: `409 Conflict` with Spring Boot's default error body. The response therefore tells the client that nothing changed, but the database now holds the forbidden status. The inconsistency exists only between the response and the persisted state; it is visible to a client only if it reads the resource again, and to a database check immediately.

| | Clean mode | DB-02 mode |
|---|---|---|
| `PATCH /api/orders/{id}/status` with a forbidden target (e.g. `COMPLETED → CANCELLED`, `CREATED → DELIVERED`) | `409` | `409` (same) |
| Persisted status afterwards | unchanged | **the forbidden target** |
| Allowed transitions, unknown ids (`404`), validation errors (`400`) | unchanged behaviour | unchanged behaviour |

## Switch

| Item | Value |
|---|---|
| Property | `pilot.defects.db02-rejected-change-saved.enabled` (default `false`, not set anywhere) |
| Class | `com.example.apipilot.defects.Db02RejectedChangeSavedOrderService` (`@Service @Primary @ConditionalOnProperty`, extends `OrderService`) |
| Mechanism | Calls the unchanged `OrderService.updateStatus`; when it throws `409`, saves the requested status (and `completedAt` if the target is `COMPLETED`) and rethrows the same exception. `@Transactional(noRollbackFor = ResponseStatusException.class)` lets the save commit. The accept/reject decision is the clean service's |
| Startup warning | `DEFECT DB-02 ENABLED: PATCH /api/orders/{id}/status saves rejected (409) status changes` |
| Enable | `.\docs\experiments\start-api.ps1 -Defect db02-rejected-change-saved` |
| Limitation | Cannot be enabled together with ST-01 (both replace `OrderService` with a `@Primary` bean) |

Side effect (inherited from the clean service, which never clears `completedAt`): a completed order whose forbidden cancellation is saved keeps its `completedAt`, so it also violates the cross-field invariant.

## Tests

`Db02RejectedChangeSavedTests` (switch on): forbidden transitions return `409` and are persisted; allowed transitions and `404` behave as in clean mode; generated contract identical to the baseline. Clean-mode tests are unchanged and never enable the switch.

## Results

See `docs/experiments/evaluation/feasibility-conclusion.md` (summary) and `docs/experiments/evaluation/results/` (raw evidence).
