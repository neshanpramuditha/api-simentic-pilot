# Feasibility conclusion — OpenAPI-driven semantic validation for REST APIs

**Date:** 2026-10-19. **Code:** commits `32c7dc2` (clean baseline, pushed), `73bae48` (SV-01), `4d9e7d8` (ST-01), plus uncommitted DB-02, prototype and evaluation work. **Contract:** `docs/openapi-baseline.json` (unchanged throughout, SHA-256 `0ADA1DCB…05B3B`).
Every number below was measured; raw evidence is under `results/` unless another path is given. Method and commands: `README.md`.

## 1. Research question

Can explicit, developer-defined semantic rules bound to OpenAPI operations detect defects that schema-only validation and Schemathesis's standard checks do not detect — cross-field invariants, state transitions and database-state consistency — with no false failures on a clean API and with failure reports that point to the cause? And is the gain over hand-written Postman assertions more than a matter of who writes the checks?

## 2. What was tested

**Target:** the Order Management pilot API (4 operations). **Injected defects** (one at a time, each behind a disabled-by-default switch, contract unchanged):

| ID | Category | Defect |
|---|---|---|
| SV-01 | Schema violation | `POST /api/orders` returns `total` as a JSON string |
| ST-01 | Invalid state transition | `COMPLETED → CANCELLED` accepted with `200` and saved |
| DB-02 | Database-state inconsistency | forbidden change answered with `409` but saved anyway |

**Tools and what each can observe:**

| Tool | Input | Sees responses | Sees earlier state | Sees database |
|---|---|---|---|---|
| Ajv (schema-only) | 11 scenario responses vs baseline | yes, one at a time | no | no |
| Postman CLI 1.74.0 | same 11 requests, 29 explicit assertions written per step | yes | only through the scenario's read-back requests | no |
| Schemathesis 4.29.4 | generated traffic, standard checks, recorded configuration; empty or seeded database | yes | its own stateful sequences | no |
| semantic-validator 0.1.0 (prototype) | same 11 requests, 3 rules (active, JDBC) | yes | read-back `GET` before each `PATCH` | **yes**, direct JDBC to the API's H2 database |
| semantic-validator, passive | Schemathesis HAR files, same 3 rules | yes | last state seen in earlier responses | no |

Matrix: 4 modes × 5 configurations × 2 repeats = 40 runs, each on a freshly started API with its own empty database (`results/matrix/runs.json`).

## 3. Results

### 3.1 Clean mode (false positives)

| Tool | Result in both repeats | Evidence |
|---|---|---|
| Ajv | 0 of 11 responses fail | `results/matrix/clean/ajv-r*/result.txt` |
| Postman | 0 of 29 assertions fail | `…/clean/postman-r*/result.txt` |
| Semantic validator (active) | 0 FAIL, 22 PASS, 2 NOT_APPLICABLE (the two `409` bodies) | `…/clean/semval-r*/report.md` |
| Schemathesis, both configurations | only the 4 known clean-API findings SB-01..SB-04 (multipart `500` ×3, `@Digits` under-specified ×1; `../schemathesis/README.md`) | `…/clean/schemathesis-*/stdout.txt` |
| Semantic validator (passive, Schemathesis traffic) | **v1: 2 false FAIL** per run; after a one-line scope fix: 0 FAIL | `results/passive-rules-v1-before-fix/`, `results/passive-rules-on-schemathesis/clean/` |

The v1 false positives were `ORD-ST-001` failing a forbidden transition that the API answered with `415` and `500` (requests refused before the state machine ran). The rule now treats status codes other than 2xx and its configured rejection status as outside its scope (NOT_APPLICABLE); a regression test was added. None of the 8 active reports contains such a status, so the active results are unaffected (checked).

### 3.2 Injected defects — was it exercised, was it detected?

"Exercised" is judged from the traffic and, for DB-02, from the final database state (`results/final-db-state/`), never from the tool's own verdict.

| Defect | Ajv | Postman (29 assertions) | Schemathesis, empty DB | Schemathesis, 30 seeded `COMPLETED` orders | Semantic validator (active) | Semantic validator (passive, on Schemathesis traffic) |
|---|---|---|---|---|---|---|
| SV-01 | exercised, **detected** (2 responses, `/total must be number`) | exercised, **detected** (2 schema assertions) | exercised (29 bad 201s), **detected** (`Response violates schema`) | exercised (18), **detected** | exercised, **not detected — out of scope** (no schema rule) | out of scope |
| ST-01 | exercised, **missed** (0 failures) | exercised, **detected** (status 409 at step 6; read-back and list show `CANCELLED`) | **not exercised** (no order reached `COMPLETED`) | exercised (2 accepted cancellations, 2/30 seeded orders changed), **missed** (0 new findings) | exercised, **detected** (`ORD-ST-001` at the faulty request: "forbidden transition COMPLETED -> CANCELLED was accepted with HTTP 200"; plus `ORD-INV-001` ×3 on the resulting `CANCELLED` + `completedAt` bodies) | empty DB: not exercised, 0 FAIL; seeded: **detected** (`ORD-ST-001` ×1 — the second attempt was NOT_APPLICABLE because that order's prior state was never observed; `ORD-INV-001` ×6) |
| DB-02 | exercised, **missed** (both `409`s are schema-valid) | exercised, **detected** — but only by the scenario's later read-back requests (step 7 expected `COMPLETED`, step 10 expected `CREATED`, list) | exercised (1 order's final DB state differs from what the client last saw), **missed** | exercised (2 hidden state changes), **missed** | exercised, **detected at the faulty request** (`ORD-DB-001` ×2: "rejected with HTTP 409 but the row changed: before {status: CREATED…}, after {status: DELIVERED…}"); plus `ORD-INV-001` ×2 on the corrupted completed order | **detected indirectly**: `ORD-INV-001` on corrupted state (e.g. `PAID` with `completedAt`); `ORD-ST-001` failures whose diagnosis is wrong ("forbidden CANCELLED -> PAID accepted" — the real cause was an earlier hidden change); DB rule NOT_APPLICABLE without database access |

All 20 mode/tool pairs gave identical results in both repeats (normalised for timestamps; `results/matrix-summary.json`).

### 3.3 Counts (exercised defects only; SV-01 excluded from the prototype as out of scope)

| Tool | Exercised | Detected | Missed | Not exercised | Clean false positives |
|---|---|---|---|---|---|
| Ajv (shared scenario) | 3 | 1 (SV-01) | 2 (ST-01, DB-02) | 0 | 0 |
| Postman (shared scenario, 29 assertions) | 3 | 3 | 0 | 0 | 0 |
| Schemathesis, empty DB | 2 | 1 (SV-01) | 1 (DB-02) | 1 (ST-01) | 0 beyond SB-01..SB-04 |
| Schemathesis, seeded | 3 | 1 (SV-01) | 2 (ST-01, DB-02) | 0 | 0 beyond SB-01..SB-04 |
| Semantic validator, active (2 in-scope defects) | 2 | 2 (ST-01, DB-02) | 0 | 0 | 0 |
| Semantic validator, passive on seeded Schemathesis traffic (2 in-scope) | 2 | 2 (ST-01 direct; DB-02 indirect, partly misdiagnosed) | 0 | 0 | 0 after fix (2 before) |

Earlier single experiments agree: JUnit detected ST-01 (`../st-01/results.md`).

### 3.4 Execution time (wall clock per run, one machine, includes tool start-up; indicative only)

Ajv 0.9–1.1 s; semantic validator (active, JVM start + 11 requests + read-backs + JDBC) 1.8–2.1 s; Postman CLI 3.5–4.6 s; Schemathesis 12–19 s (500–580 generated requests). Passive rule evaluation over ~550 Schemathesis exchanges: 0.17–0.23 s inside the engine (JVM start excluded).

### 3.5 Effort

| | Postman | Semantic validator |
|---|---|---|
| Checks | 29 assertions, tied to the 11 steps (generated here from the scenario's `expect` blocks) | 3 rules, 44 lines of JSON, bound to operationIds |
| Reuse | none beyond this scenario | the same 3 rules ran unchanged on the 11-step scenario and on 16 Schemathesis traffic sets (536–553 exchanges each) |
| Engine | — | 984 non-blank lines of Java (15 files), 3 unit tests |

## 4. What the prototype demonstrated

1. **Coverage beyond schema-only validation and Schemathesis's standard checks, on this API:** ST-01 and DB-02 were exercised and missed by Ajv and by Schemathesis (including when ST-01 was made reachable), and detected by the rules.
2. **Diagnosis at the faulty request:** for DB-02 the database rule fails on the `PATCH` that caused the corruption, with the row before and after; Postman detected the same defect only later, through read-back assertions that the scenario happened to contain.
3. **Rules are separable from scenarios:** the same rule file judged hand-written and generated traffic. Combined with Schemathesis traffic it found ST-01 where Schemathesis's own checks did not.
4. **Actionable evidence:** every FAIL names a stable rule ID, the exchange, the request and response, and the rule-specific evidence (previous state and its source; database row before/after; the values that broke an invariant) — `results/matrix/*/semval-r1/report.md`.

## 5. Limitations and fairness caveats

- **Postman matched the prototype's detections** on the shared scenario. The difference demonstrated is *where* checks live (rules reusable across scenarios and traffic vs per-step assertions) and *where* the failure is reported, not that the rules can detect something a well-written test cannot.
- All rules and assertions were written knowing the injected defects (an upper bound for every tool). Rules for unknown defects were not evaluated.
- **One API, three rules, three defects.** Reusability across APIs was not demonstrated.
- **Database access is direct H2 JDBC** (H2 `AUTO_SERVER`, same H2 version as the API, table and column names in the rule). It required overriding the API's datasource for the run. The state-transition rule uses an API read-back (`GET`) instead, so it needs no database. Production databases, other engines, transactions and concurrency were not tested.
- **Passive mode has no database and only observed state:** a stale previous state produced misleading `ORD-ST-001` diagnoses on DB-02 traffic, and an order never observed before is NOT_APPLICABLE.
- The validator does not itself say whether a *defect* was exercised; it reports which rules were evaluated (PASS/FAIL) or not applicable for each exchange. Separating "not exercised" from "exercised but missed" was done by the evaluation harness from traffic and database ground truth.
- Schemathesis needed seeded data to reach `COMPLETED`; with an empty database or a 300 s budget it never did. Its results depend on configuration; other settings (explicit links, stateful-only) were not tried.
- The two DB-02-style side effects (`completedAt` not cleared) mean some invariant failures are symptoms of the injected defect rather than separate defects.
- Timings are single-machine and include process start-up.

## 6. What the evidence supports

| Supported | Uncertain | Not supported |
|---|---|---|
| Schema-only validation and Schemathesis's standard checks missed an exercised state-transition defect and an exercised database-state defect on this API | Whether the rule format stays small and expressive for other APIs, other invariants and other databases | Any claim of novelty: explicit Postman assertions caught all three defects |
| Explicit semantic rules detected both, with 0 clean false failures after one scope fix | Effort and false-positive rate when rules are written without knowing the defects | Superiority over well-written integration tests |
| The same rules work on scenario traffic and on generated traffic; together with Schemathesis traffic they detected ST-01 | Value of passive mode where state is often unobserved | Database consistency checking beyond H2 with direct access |
| Database rules localise the failing request and show the row before/after | Developer usability of authoring rules (not measured with users) | |

## 7. Recommendation: **narrow, then proceed**

Proceed, but with the claim narrowed from "detects what existing tools cannot" to: **a reusable, OpenAPI-bound rule layer that adds state-aware and database-aware checks to existing API testing, and produces evidence-linked failure reports.** The value measured is complementarity (with Schemathesis traffic and schema validation), rule reuse across scenarios, and better diagnosis — not detection power beyond hand-written tests. Do not pivot: the core mechanism works and the gaps it fills were observed, not assumed. Do not build the full application before the MVP evaluation below can be run on at least one more API.

## 8. Realistic individual-project MVP and evaluation plan

**MVP (in scope):**
1. Rule engine (from this prototype): the three rule kinds, operationId binding checked against the OpenAPI document, JSON rule files with stable IDs, NOT_APPLICABLE semantics, and JSON + Markdown evidence reports.
2. Integrated schema validation of responses (Ajv-equivalent), so one report covers schema and semantic failures.
3. Two inputs: run a scenario actively, or check recorded traffic (HAR) passively.
4. Database rules for one engine (PostgreSQL or H2) via a read-only JDBC connection.
5. A small web UI: upload OpenAPI + rules, run, browse findings with evidence. React only after the engine and CLI are stable.

**Out of scope:** automatic rule inference, multiple database engines, authentication flows, production monitoring, arbitrary-API reuse claims beyond the evaluated APIs.

**Evaluation plan:** (a) a second, independently designed API (different domain) with its own rules, to test reuse of the rule kinds; (b) at least 10 injected defects across the three semantic categories, with ground truth recorded as in this pilot (exercised vs detected, clean false positives, repeats); (c) the same baselines: schema-only validation, Schemathesis with documented settings and seeded data, and Postman assertions of comparable effort; (d) report effort (rule lines vs assertions), time, and a small usability check of failure reports with 3–5 developers if feasible.
