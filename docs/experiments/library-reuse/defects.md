# Library Lending API — injected defects

Three switches in `library-api/src/main/java/com/example/library/defects/`, all **off by default** (no property sets them anywhere). Each is a `@Service @Primary @ConditionalOnProperty` subclass of the unchanged `LoanService` and logs one `WARN` line at startup. Because each replaces `LoanService` with a `@Primary` bean, **enable at most one at a time** (two would fail application startup with a duplicate-primary error, never silently combine).

Enable with `.\docs\experiments\library-reuse\start-library-api.ps1 -Defect <name>`, i.e. `--library.defects.<name>.enabled=true`.

| ID | Switch `<name>` | Category | Effect | Clean behaviour it replaces | Startup warning |
|---|---|---|---|---|---|
| LIB-ST-01 | `st01-requested-to-issued` | State transition | `REQUESTED -> ISSUED` (approval skipped) is accepted with `200`, saved, `issuedAt` set | `409`, nothing saved | `DEFECT LIB-ST-01 ENABLED: PATCH /api/loans/{id}/status accepts REQUESTED -> ISSUED` |
| LIB-DB-01 | `db01-rejected-change-saved` | Database-state consistency | Any transition the clean service rejects still returns `409`, but the requested status is saved (with `issuedAt` / `returnedAt` set when the target is ISSUED / RETURNED). `@Transactional(noRollbackFor = ResponseStatusException.class)` lets the save commit | `409`, nothing saved | `DEFECT LIB-DB-01 ENABLED: PATCH /api/loans/{id}/status saves rejected (409) status changes` |
| LIB-RI-01 | `ri01-returned-without-timestamp` | Response invariant | `ISSUED -> RETURNED` is accepted as in clean mode, but saved and returned with `returnedAt = null` | `returnedAt` set to the return time | `DEFECT LIB-RI-01 ENABLED: PATCH /api/loans/{id}/status saves RETURNED loans without returnedAt` |

Side effect of LIB-DB-01 (inherited from the clean service, which never clears timestamps): a RETURNED loan whose forbidden re-issue is saved keeps its `returnedAt`, so it is ISSUED with a return time and also violates the `returnedAt` invariant. This mirrors DB-02 in the Order API.

LIB-RI-01 is a persisted defect, not a serialization-only one: the database row has `returned_at = NULL` too. Response and database therefore agree, so it is not a database-consistency violation; it is meant for the response-invariant rule.

## Tests (`library-api/src/test/java/com/example/library/`)

| Class | Switch | Proves |
|---|---|---|
| `controller.LoanControllerTests` (39) | none | Clean behaviour: create/validation (400), get/404/malformed UUID (400), list order and filtering, full lifecycle with timestamps, **every** forbidden pair (20) returns `409` with the database unchanged, 404 and invalid status (400) on PATCH; no defect bean is active by default |
| `defects.LibSt01RequestedToIssuedTests` (4) | LIB-ST-01 | `REQUESTED -> ISSUED` accepted and persisted; other forbidden transitions still `409`; allowed path unchanged; generated contract identical to the clean baseline |
| `defects.LibDb01RejectedChangeSavedTests` (3) | LIB-DB-01 | Rejected transitions return `409` and are persisted; allowed transitions and 404 unchanged; contract identical |
| `defects.LibRi01ReturnedWithoutTimestampTests` (3) | LIB-RI-01 | RETURNED with null `returnedAt` in the PATCH response, a later GET and the database; other transitions unchanged; contract identical |
| `OpenApiDocumentationTests` (3) | none | operationIds the rules bind to; nullable timestamp types; generated contract identical to `library-api/openapi-baseline.json` |
