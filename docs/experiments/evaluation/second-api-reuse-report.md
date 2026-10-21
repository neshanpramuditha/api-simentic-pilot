# Second-API reuse report: Library Lending API (2026-10-19)

**Question.** Can the existing semantic-validator engine validate a second, independently written REST API using only a new rules file, with no change to the engine's source?

**Short answer.** Yes, for this API.
- **Engine unchanged:** the engine source was not modified. SHA-256 hashes of every source file, the pom and the jar were the same before and after.
- **Clean mode:** 0 false failures in 6 clean runs.
- **Detection:** each injected defect was detected in every run of the configurations that can observe it, and 0 false positives in 24 runs.
- **Evidence:** every FAIL's evidence matched the recorded traffic and database.
- **Reproducibility:** all 12 mode/configuration pairs gave identical results on repeat.

Two limits apply:
- **One API only:** this is one more API, written by the same author on the same stack, so it supports "reusable across two similar Spring/H2 APIs", not general reusability.
- **Configuration-bound detection:** the database-state defect is only caught directly when JDBC access is configured.

Experiment files: `../library-reuse/` (README, scripts, `defects.md`). Evidence: `../library-reuse/results/`. Order API comparison: `feasibility-conclusion.md`.

## 1. API design and OpenAPI

`library-api/` is a standalone Maven project: Spring Boot 4.1.1, Java 21, H2 (`jdbc:h2:mem:librarydb`), port 8081. It shares no code with the Order API or the validator.

| Operation (operationId) | Success | Errors |
|---|---|---|
| `POST /api/loans` (`createLoan`) | 201, loan in `REQUESTED` | 400 validation (`memberId`, `bookId` required, positive) |
| `GET /api/loans/{id}` (`getLoan`) | 200 | 400 malformed UUID, 404 |
| `GET /api/members/{memberId}/loans` (`listMemberLoans`) | 200, oldest request first | 400 |
| `PATCH /api/loans/{id}/status` (`updateLoanStatus`) | 200 | 400 (bad id or body, e.g. `"LOST"`), 404, **409 forbidden transition** |

`Loan` fields:
- `id` is a UUID.
- `memberId` and `bookId` are integers.
- `status` is an enum.
- `requestedAt` is a timestamp and is always set.
- `issuedAt` and `returnedAt` are nullable timestamps.

All timestamps are RFC 3339 strings in UTC.

Lifecycle:
- `REQUESTED → APPROVED | REJECTED`
- `APPROVED → ISSUED`, which sets `issuedAt`
- `ISSUED → RETURNED`, which sets `returnedAt`
- `REJECTED` and `RETURNED` are final.

The OpenAPI 3.1 baseline is `library-api/openapi-baseline.json`, generated from the clean code by an opt-in export test. It contains:
- explicit operationIds;
- a `Loan` response schema with `["string","null"]` timestamp types;
- the transition table in the `updateLoanStatus` description.

**Tests: 52 of 52 pass.**
- 39 tests for clean behaviour, including all 20 forbidden pairs (409, database unchanged).
- 3 contract tests: operationIds, schema types, and generated contract equal to the baseline.
- 4 + 3 + 3 tests for the defect switches.

**Defects.** All are off by default, log one `DEFECT LIB-…` warning when enabled, and only one can be enabled at a time; enabling two fails at startup. Details are in `../library-reuse/defects.md`.

| ID | Category | Effect (HTTP and database) |
|---|---|---|
| LIB-ST-01 | State transition | `REQUESTED → ISSUED` (approval skipped) is accepted with 200 and saved |
| LIB-DB-01 | Database consistency | Forbidden change returns **409** but is **saved**. Side effect: a re-issued RETURNED loan keeps its `returnedAt` |
| LIB-RI-01 | Response invariant | `ISSUED → RETURNED` is accepted, but `returnedAt` stays null in the response and the database |

## 2. Rule configuration and adaptations

New file: `semantic-validator/rules/library-api.rules.json`. It has 55 lines; the Order rules file has 44.
- **Unchanged:** 24 lines are identical to the Order file.
- **Edited:** 19 lines changed in place.
- **Added:** 12 lines form a second invariant rule (`git diff --no-index --numstat`: 31 added, 20 removed).

| Rule | Kind | Fields changed from the Order rule | Fields kept unchanged |
|---|---|---|---|
| `LIB-INV-001` `returnedAt` set iff `RETURNED` | response-invariant | id, description, `operations` (4 ids), path `$.returnedAt`, value `RETURNED` | `iff` / `notNull` / `equals` structure |
| `LIB-INV-002` `issuedAt` set iff `ISSUED` or `RETURNED` | response-invariant | **New rule**, using the `in` condition (not used by any Order rule) | n/a |
| `LIB-ST-001` lending lifecycle | state-transition | id, description, `operations`, `readOperation` (`getLoan`), `transitions` (5 states, branching) | `idParam`, `idField`, `stateField`, `requestedState`, `rejectedStatuses: [409]` |
| `LIB-DB-001` row matches response, unchanged after rejection | db-consistency | id, description, `operations`, `query` (table `loans`, columns `status, issued_at, returned_at`) | `params: ["$path.id"]`, `columnsMatchResponse: {status}`, `onError: rowUnchanged` |

The database mapping (table, columns and key parameter) lives only in the rule's `query` and `columnsMatchResponse`; nothing database-specific was added to the rule-evaluation code.

**Other new files:**
- A scenario (`LIB-SC-1`, 15 steps, same format as the Order scenario).
- Start and stop scripts for port 8081.
- Ground-truth tooling.

None of these is part of the engine.

**Adaptation effort:**
- The rules file was written in one pass and passed its first run (clean smoke run: 0 FAIL). It was not edited afterwards.
- It was written by an AI assistant that already knew the engine. **Human adaptation time was not measured.** A number here would mean little.

## 3. Engine source

Unchanged. `../library-reuse/results/engine-hashes-before.txt` and `…-after.txt` are identical. They cover all 16 Java files, the pom, the jar and `order-api.rules.json`. The engine was run through its existing command line (`run` and `check-har`).

The engine contains no Order-specific code, but it makes these assumptions. The Library API met them; one was met by design (see §6):

| # | Assumption | How it was met here |
|---|---|---|
| A1 | `Main` hardcodes the JDBC user and password as `sa` / `""` | The Library API uses the H2 defaults |
| A2 | The state-transition read-back reuses the write operation's path-parameter name (`Map.of(idParam, id)`) | Both endpoints use `{id}`. **The API was designed knowing this** |
| A3 | The last-observed-state map is keyed by id alone, across all operations | Only loans carry a `status` |
| A4 | Database values are compared with the response as text | Only `status` is compared |
| A5 | Database parameters come only from `$path.*` and are bound with `setString` | It works for an H2 `uuid` key column (verified) |
| A6 | Scenarios have no query parameters or headers | Not needed |
| A7 | A database that can't be reached or queried is an uncaught exception | See §5 |

## 4. Results (24 runs, each with a fresh API process and a fresh H2 file database)

**Modes:** `clean`, `lib-st01`, `lib-db01`, `lib-ri01`.

**Validator configurations:**
- `semval-jdbc`: active scenario with database access.
- `semval-nojdbc`: active scenario without database access.
- `semval-passive`: `check-har` over traffic recorded without read-backs.

Each configuration ran twice in each mode.

**Ground truth** (`verify-run.js`, independent of the engine code) comes from three sources:
1. The real traffic. In active runs this is a recording proxy between the validator and the API, which also captures the validator's 9 read-backs.
2. The final database, dumped by `DumpLoans.java`.
3. An oracle that:
   - takes the transition table from the OpenAPI description (not from the rules file);
   - checks the invariants itself;
   - works out what each request saved from later reads or from the final database.

Each run also checked the expected startup warning line (OK in 24 of 24) and that the traffic matched the scenario (OK in 24 of 24).

| Mode | Oracle: violations at the cause → later effects | `semval-jdbc` | `semval-nojdbc` | `semval-passive` |
|---|---|---|---|---|
| clean | none (and no defect signature) | 0 FAIL | 0 FAIL | 0 FAIL |
| lib-st01 | `ST@7` (B: REQUESTED→ISSUED accepted) | **ST@7** | **ST@7** | **ST@7** |
| lib-db01 | `DB@4, DB@7, DB@11` (A, B, C changed after 409; 3 hidden state changes in the final DB) → `INV-001@5`, `INV-001@12#0` (A: ISSUED with `returnedAt`) | **DB@4, DB@7, DB@11** + INV-001@5, @12 | INV-001@5, @12 only; DB@4/7/11 not evaluable | INV-001@5, @12 only; DB@4/7/11 not evaluable |
| lib-ri01 | `INV-001@3` (RETURNED, `returnedAt` null; also in the DB) → `@5`, `@12#0` | **INV-001@3**, @5, @12 | same | same |

Each cell lists the steps where the validator reported FAIL, identical in both repeats. Notation:
- `@n` is the scenario step; `#i` is the item index within a list response.
- **Bold** means the validator flagged the cause step, i.e. the request where the defect acted.

Totals:
- **True positives:** every oracle violation the configuration could evaluate was a validator FAIL.
- **False positives:** 0.
- **Missed:** 0.
- **Not evaluable:** 12, all `LIB-DB-001` findings in `lib-db01` without JDBC.

In clean `semval-jdbc`, the `LIB-DB-001` outcomes were 8 PASS and 1 NOT_APPLICABLE. The NOT_APPLICABLE is the 404 step ("no row before the request"). The 400 step (`"LOST"`) correctly ended as PASS ("row unchanged") for the database rule and NOT_APPLICABLE for the transition rule.

**Mismatch versus unavailable database evidence:**
- **With JDBC:** `LIB-DB-001` gave PASS or FAIL backed by row evidence. The only NOT_APPLICABLE was the 404 step, where no row exists.
- **Without JDBC (`nojdbc`, passive):** all 9 `LIB-DB-001` findings in every run were NOT_APPLICABLE with "no database access". None was PASS, so missing evidence was never reported as consistency.
- **Unusable JDBC URL** (missing database, or a wrong empty database): the run aborts with an exception and no report. See §5.

**Duration.** The validator process, including JVM start-up, took:
- active: 2.2–3.2 s, for 15 requests, 9 read-backs and JDBC;
- passive: 0.4–0.6 s.

## 5. False positives, misses, reproducibility, diagnostics

- **False positives:** 0 of 24 runs, including the 404 and 400 steps.
- **Misses:** 0. The only gaps are in DB-01 without database access. There, 2 of 3 hidden changes (B → ISSUED, C → APPROVED) leave no trace at all over HTTP, because the illegal states have consistent timestamps.
  - A's change shows up only as an invariant FAIL at the *later read* (`@5`, `@12`). The message ("`returnedAt` set while ISSUED") does not point to the rejected request at `@4`.
  - This is the same indirect, misleading passive signal seen with DB-02 on the Order API.
- **Reproducibility:** for all 12 mode/configuration pairs, repeats gave identical findings (rule, outcome, step, message), identical oracle results and identical final databases, once UUIDs and timestamps were masked.
- **Diagnostic evidence:**
  - All 42 FAIL findings carry the request (method, URL, body) and the response (status, body).
  - All transition FAILs also carry the previous state with its source. In active mode that includes the read-back response.
  - All database FAILs also carry the query, parameters, `rowBefore` and `rowAfter`. For example, DB@4: `RETURNED → ISSUED` with new `issued_at` and old `returned_at`, alongside the 409 body.
  - `verify-run.js` matched every one against the recorded traffic: request, response and read-back are identical. Each `rowAfter` matched the state seen by the next read or in the final database. 0 evidence problems.
  - PASS and NOT_APPLICABLE findings carry no evidence, by design.
- **Verifier checked:** feeding it altered reports produced the expected missed, false-positive and evidence-mismatch verdicts (`results/README.md`). That check exposed one crash in the verifier, which was fixed; all runs were re-verified with the same results.

## 6. Limitations and API-specific assumptions

1. **One more API, not general reuse.** The second API uses the same framework, the same database, the same error format, the same `{"status": …}` PATCH body and the same 409 convention. The same person wrote it and **knew the engine's assumptions**: A2 was met by design. A third-party API, a different stack, or a different state representation (sub-resources, action endpoints like `POST /loans/{id}/return`, ETags) is untested and may fail A2, A3 or A6.
2. **Small defect set.** 3 new defects (5 semantic defects in total across both APIs), each injected deliberately and one at a time, with one scenario per API. The MVP target in `feasibility-conclusion.md` is at least 10 semantic defects across 2 or more APIs, so that target is **not yet met**.
3. **Database rules need JDBC on the same H2 instance** (`AUTO_SERVER`, file mode) and the default credentials (A1). Without database access, database-state defects are mostly invisible.
4. **Error handling (A7):**
   - A database that doesn't exist, or one without the table, aborts the run with no report and **exit code 1**, the same code as "a rule failed". `Main`'s doc comment says execution errors exit with 2.
   - No false verdict results, but a pipeline cannot tell the two apart by exit code. This would need an engine change, so it was recorded rather than fixed.
5. **Comparison scope.** Comparisons are text-based (A4) and state is tracked by id only (A3). Neither caused a problem here, but timestamp columns and multi-resource APIs remain untested.
6. **Ground truth.** The oracle uses the same kinds of observation as the validator: traffic, database and documented transitions. It is independent code with its own sources (the OpenAPI text and an all-column dump), but it is not an independent human judgement.
7. **No comparison tools.** No Ajv, Postman or Schemathesis runs on this API; the experiment tested reuse, not comparative detection.

## 7. Recommendation

**Proceed to the proposal with the narrowed contribution already recommended.** The engine is reusable by configuration, it gives complementary detection when it has state and database access, and its evidence links each finding to its cause.

This experiment adds:
- the first evidence that the rule format and engine carry over to a second API with **no engine changes**;
- a small, measured configuration effort;
- no false positives;
- reproducible, ground-truth-checked results.

It also confirms that the direct-detection advantage for database-state defects depends on JDBC access.

In the proposal:
- **Claim:** reuse across two similar Spring Boot/H2 REST APIs. Do not claim reuse across arbitrary APIs.
- **Present these as planned work:**
  - a third API that is externally written or on a different stack;
  - the defect count of 10 or more;
  - the exit-code / error-reporting fix (A7);
  - making A1 and A2 configurable.
