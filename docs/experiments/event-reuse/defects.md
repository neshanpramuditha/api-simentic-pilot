# Event Registration API — injected defects

Five switches in `event-api/src/main/java/com/example/events/defects/`, added **after** the clean contract and behaviour were frozen (`contract-design.md`; contract SHA-256 `847a10da…0bc3a`, unchanged afterwards). All are **off by default**; each is a `@Service @Primary @ConditionalOnProperty` subclass of the unchanged `RegistrationService` and logs one `WARN DEFECT EVT-…` line. Enable at most one (two fail startup with a duplicate-primary error). Switch: `--events.defects.<name>.enabled=true`, or `start-event-api.ps1 -Defect <name>`.

| ID | `<name>` | Category | Effect | Acts at scenario step | Designed so that… |
|---|---|---|---|---|---|
| EVT-ST-01 | `st01-waitlist-confirm-rejected` | State transition (**over-restrictive**) | The *allowed* `WAITLISTED → CONFIRMED` is refused with `422`; nothing stored | 8 | only the transition rule (and per-step expectations) can see it: the response is a plain 422 |
| EVT-DB-01 | `db01-rejected-change-saved` | Persisted state | A transition rejected with `422` is stored anyway (target state + its timestamp/reason) | 4, 13 | the response is identical to clean mode; the stored row changes |
| EVT-DB-02 | `db02-cancellation-not-saved` | Persisted state (**lost write**) | An allowed cancellation returns `200` with the cancelled registration, but nothing is stored (entity detached) | 9 | the response is internally consistent; only the stored row (or a later read) differs |
| EVT-RI-01 | `ri01-checkin-without-timestamp` | Response invariant (presence) | Check-in accepted and stored with `checkedInAt = null` | 2 | violates only `checkedInAt iff CHECKED_IN` |
| EVT-CF-01 | `cf01-checkin-before-confirmation` | Cross-field invariant (**time order**) | Check-in stored with `checkedInAt = requestedAt`, i.e. before `confirmedAt` | 2 | all presence invariants still hold; only the order `confirmedAt ≤ checkedInAt` is broken |

Each forbidden attempt in the scenario (steps 4, 13) is on a registration that is only read afterwards, so a defect's effect does not cascade into another defect's step. Side effects that remain (inherent to the defect, reported as downstream signals, not as separate defects):

- EVT-DB-01: R1 stored as CANCELLED while keeping `checkedInAt` (later reads violate INV-001); R3 stored as CHECKED_IN without `confirmedAt` (later reads violate INV-004).
- EVT-ST-01: R2 stays WAITLISTED, so its later cancellation is WAITLISTED → CANCELLED (allowed) and the list shows the same final states.

## Tests (`event-api/src/test/java/com/example/events/`), 52 in total

| Class | Switch | Proves |
|---|---|---|
| `RegistrationLifecycleTests` (36) | none | Frozen clean behaviour: validation, 404, envelope list order, timestamps and their order, reason rule, all 18 forbidden pairs are `422` with nothing stored, invalid bodies `400`; no defect bean active by default |
| `ContractTests` (2) | none | operationIds; generated contract identical to `event-api/openapi-baseline.json` |
| `defects.EventDefectTests$St01/Db01/Db02/Ri01/Cf01` (3+3+3+2+3) | one each | the defect's effect (HTTP and database), neighbouring behaviour clean, contract identical to the frozen baseline |
