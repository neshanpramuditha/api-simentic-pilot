# Final project feasibility evaluation (2026-10-19)

**Decision: NARROW.** Proceed to the proposal, but with a narrowed contribution. The prototype works, and it can be reused through configuration with small, general engine extensions. Its findings are correct and reproducible, and its reports carry more diagnostic content than Postman's.

It does **not** detect more defects than a carefully written Postman collection. On the third API, Postman detected all 5 defects at the cause step with 0 false positives, the same as the validator.

This rules out a GO on "better detection". A PIVOT is not warranted, because the remaining contribution is real and was measured: declarative, contract-bound rules; database-aware, evidence-linked reports; and a passive mode.

**Evidence base:**
- Order API: `feasibility-conclusion.md`.
- Library API: `second-api-reuse-report.md`.
- Event Registration API: `../event-reuse/` and its `results/`.

All numbers below are measured. Where something was not measured, the report says so.

## 1. Research problem and narrowed contribution

**Problem.** Schema validation and property-based tools check the shape of each response. They miss violations of business state and of stored data even when their requests trigger them:
- Ajv detected 0 of the 5 Event defects.
- Schemathesis exercised ST-01 and DB-02 on the Order API without detecting them.

Hand-written assertions (Postman) catch these violations, but only through expectations written for each scenario plus extra read-back requests. Their failure reports contain neither the stored data nor the full response.

**Narrowed contribution:**
1. A declarative rule format (response invariants, state transitions, database consistency) bound to OpenAPI operationIds and checked against the contract.
2. One engine that applies the rules to scripted scenarios (active mode, with read-backs and JDBC) and to recorded traffic (passive mode).
3. Failure reports that link each finding to the request, the response, the prior state and the stored row.
4. A controlled evaluation of portability, detection, maintenance effort and report content against Postman and Ajv.

**Not part of the contribution:** superior defect detection, novelty of runtime or contract checking, and rule inference.

## 2. Third API: Event Registration

Contract: `../event-reuse/contract-design.md`. It was written and frozen (contract SHA-256 `847a10da…0bc3a`, clean tests 38 of 38) **before** any rules existed. It was re-checked unchanged at the end.

| Aspect | Order / Library | Event Registration |
|---|---|---|
| Base path | `/api` | `/v1` |
| Identifier | `id` (Long, UUID) | `registrationId` (string `REG-XXXXXXXX`), path `{registrationId}` |
| Owner fields | `ownerUserId` / `memberId` | `attendeeRef`, `eventCode` (strings) |
| State field and change | `status`, `PATCH …/status {"status"}` | `state`, **action endpoint** `POST …/transitions {"to","reason"}` |
| Forbidden transition | 409 | **422** |
| Collection response | bare array | **envelope** `{attendeeRef, count, items[]}` |
| Lifecycle | linear / one branch | 5 states, wait-list branch, cancellation from 3 states, reason required |
| Cross-field properties | presence only | presence **and time order** (`requestedAt ≤ confirmedAt ≤ checkedInAt`) |

**Honesty:** the Event API was built in this project by the same author (an AI assistant), who knew the engine. It runs on the same stack: Spring Boot 4.1.1, H2, Spring's default error body. It is independent in intent, not a third-party API.

## 3. Configuration-only portability, then engine changes

### First attempt: original engine 0.1 with rules v1, configuration only

| Item | Result |
|---|---|
| Engine source changed | **No.** The jar was kept aside and the hashes are in `results/engine-hashes-v0.1.txt` |
| Runs end to end | Yes: 19 exchanges, 0 FAIL on the clean API (`results/first-attempt/`) |
| Rules file | 62 lines, 6 rules. Diff against the Library rules: 43 lines added, 36 removed |
| What had to change | operationIds; `idParam: registrationId`; `stateField: $.state`; `requestedState: $request.body.to`; `rejectedStatuses: [422]`; a 5-state table; table `event_registration`; key `registration_id` |
| Elapsed time, measured (AI-assistant wall-clock, includes writing the scenario and fixing one script bug) | 9 min 15 s (07:01:01 → 07:10:16 UTC) |

Two problems blocked full reuse:

| # | Problem | Classification | Effect |
|---|---|---|---|
| P1 | The list envelope `{count, items[]}`: invariants were evaluated against the envelope object, where `$.state` and the timestamps are missing, so every `iff` held trivially. **The result was a silent vacuous PASS.** Paths cannot address `items[]` | **Generic engine limitation** | Violations in list items were missed (one FN each in DB-01 and RI-01) |
| P2 | Time order (`confirmedAt ≤ checkedInAt`) cannot be expressed: conditions compare a field only with constants | **Generic engine limitation** (rule language) | EVT-CF-01 missed entirely: 0 of 4 violations |

Neither problem is a database-adapter limitation. The JDBC rule worked unchanged with a string primary key and a different table and column naming.

### Minimal general fix: engine 0.2

`results/engine-v0.2.patch`: 3 main files, **+50 / −3 lines**, plus 30 lines of tests. Unit tests: 5 of 5 pass. Elapsed time: 3 min 28 s.
- An optional per-operation `items` path for invariants (fixes P1).
- A `notAfter` condition comparing two fields as instants or numbers (fixes P2).
- A guard: a subject in which **none** of the referenced fields is present becomes NOT_APPLICABLE instead of a vacuous PASS.

Rules v2 adds 16 lines and changes 1 (77 lines, 7 rules).

**Regression check:** engine 0.2 produced the same findings as 0.1 on the Order API (4 modes) and the Library API (12 runs) (`results/regression-v02/`).

## 4. Defect inventory across all three APIs

| API | ID | Distinct type | Category |
|---|---|---|---|
| Order | SV-01 | (schema: `total` as string) | not semantic, excluded |
| Order | ST-01 | T1 forbidden transition accepted (exit from a final state) | state |
| Library | LIB-ST-01 | T1 forbidden transition accepted (skipped step) | state |
| Event | EVT-ST-01 | **T2 allowed transition rejected** | state |
| Order | DB-02 | T3 rejected change stored anyway | stored data |
| Library | LIB-DB-01 | T3 | stored data |
| Event | EVT-DB-01 | T3 | stored data |
| Event | EVT-DB-02 | **T4 successful change not stored (lost write)** | stored data |
| Library | LIB-RI-01 | T5 conditional timestamp missing | response invariant |
| Event | EVT-RI-01 | T5 | response invariant |
| Event | EVT-CF-01 | **T6 time-order cross-field violation** | cross-field invariant |

**Exact counts:**
- 11 injected defects, of which **10 are semantic**; this meets the target of 10.
- After merging duplicates, **6 distinct semantic types**. T3 appears three times and T1 and T5 twice each.
- All 5 Event defects were exercised in every run of every mode, as confirmed by the oracle.

## 5. Fair comparison on the Event API

**Setup:**
- Same frozen contract, same scenario EVT-SC-1 (19 requests), same API build and same ground truth.
- 6 modes × 2 repeats × 4 run types = **48 runs**, each with a new API process and a new database file, giving 72 tool results.
- **Incomplete runs:** none. A first attempt was aborted after 5 clean runs because the jar was stale, and was discarded unused.
- **Ground truth** (`verify-event-run.js`):
  - recorded traffic, including the read-backs of both tools;
  - an independent dump of the database;
  - the transition table parsed from the OpenAPI text;
  - affected steps, meaning an oracle violation or a deviation from the clean expectation. A tool failure on any other step counts as a false positive.

**Postman collection** (`build-postman-collection.js`, 104 lines of generator JavaScript):
- The same 5 invariants on every registration, including envelope items.
- OpenAPI schema validation.
- Per-step status and state expectations taken from the scenario.
- A read-back GET after every transition, which checks the stored state over HTTP.
- 136 assertions per clean run.

| Tool (per mode: repeat 1 = repeat 2) | ST-01 | DB-01 | DB-02 | RI-01 | CF-01 | Clean false-positive steps | Steps flagged in defect modes outside the affected steps |
|---|---|---|---|---|---|---|---|
| Validator 0.1, active + JDBC, rules v1 | cause | cause (2 list-item FN) | cause | cause (1 list-item FN) | **missed** | 0 | 0 |
| **Validator 0.2, active + JDBC, rules v2** | **cause** | **cause** | **cause** | **cause** | **cause** | **0** | **0** |
| Validator 0.1, passive | cause | downstream only | **missed** | cause | missed | 0 | 0 |
| Validator 0.2, passive | cause | downstream only | **missed** (database evidence N/A) | cause | cause | 0 | 0 |
| **Postman** | **cause** | **cause** | **cause** | **cause** | **cause** | **0** | **0** |
| Ajv (schema) | missed | missed | missed | missed | missed | 0 | 0 |

Key-level checks for validator 0.2 active:
- Every oracle violation was a FAIL (0 false negatives).
- 2 extra FAILs in DB-01 mode: EVT-INV-005 on R3, stored as CHECKED_IN without `confirmedAt`. My time-order rule treats a missing `confirmedAt` as a violation, while the oracle attributes it only to INV-004.
- Postman's time-order check behaves the same way, so this is a shared rule-authoring choice on already-affected steps, not a false positive.

**Passive mode:** DB-02 (a lost write) is invisible from traffic alone. The later GET shows CONFIRMED after a 200 CANCELLED, and no rule checks that state stays consistent between observations. Postman catches this through its step expectation and its read-back.

**Changes and effort** (measured wall-clock time of an AI assistant; **not human effort, and no human was timed**):

| Artifact | Built from | Changes after the first version | Time |
|---|---|---|---|
| Rules v1 | Library rules | — | 9 min 15 s (includes the scenario) |
| Engine 0.2 + rules v2 | — | +50/−3 engine, +16/−1 rules | 3 min 28 s + under 1 min |
| Postman generator | Order generator (+74/−26) | Revision 1: **3 false positives in a trial run** led to a timestamp-tolerant read-back comparison (+7/−3). Revision 2: harness only, 1 line | 2 min 28 s for v1 including trials; seconds for each revision |

The revision-1 false positives came from a **real quirk of the clean API**: responses carry 100 ns timestamps but H2 stores microseconds, so a strict read-back comparison fails. The validator's database rule compares only `state` and `reason`, so it never hit this. That reflects a choice I made when writing the rule, not a property of the tool.

**Reproducibility:** for all 36 mode × tool groups, failure keys, oracle results and final databases were identical across repeats.

**Duration per run:**
- validator active: 2.5–6.0 s;
- validator passive: 0.4–2.8 s;
- Postman: 4.9–13.0 s;
- Ajv: 0.2–1.3 s.

## 6. Diagnosis evidence (report-content analysis, not a human study)

Files are in `../event-reuse/results/diagnosis/`. They hold matched, masked failure reports (validator 0.2 active versus Postman, same runs), a repeatable task (name the faulty operation and step, the violated rule, and the evidence) with an answer key, and an automatic rubric. **No participants took part; no time savings or usability claims follow from this.**

| Rubric item | Validator 0.2 | Postman |
|---|---|---|
| R1 faulty request (method + URL) | 5 of 5 | 5 of 5 (in the run log) |
| R2 violated rule or expectation named | 5 of 5 | 5 of 5 |
| R3/R4 observed and expected values | 5 of 5 | 5 of 5 (helped by my custom assertion messages) |
| R5 response body | 5 of 5 | 0 of 5 (the CLI never prints bodies) |
| R6 prior state | 3 of 5 (ST, DB-01, DB-02) | 1 of 5 (DB-01, from my read-back message) |
| R7 stored state | 2 of 5 as a database row (DB-01, DB-02) | 2 of 5 partial (HTTP read-back) |
| R8 first failure at the cause step | 5 of 5 | 5 of 5 |

For EVT-ST-01, Postman reports "expected status 200 but got 422" and nothing says *why* 200 was expected. The validator reports "allowed transition WAITLISTED → CONFIRMED rejected", with the read-back state and the allowed targets.

## 7. Limitations, threats to validity, remaining work

1. **The APIs are not independent.** All three were written in this project, by an author who knew the engine, on one stack (Spring Boot/H2/default error format). External validity is the main threat.
2. **Configuration-only reuse held for 2 of 3 APIs.** The third needed 2 general engine extensions. These were small and caused no regressions, but they show the rule language is still incomplete; for example, there is no passive state-continuity rule (the DB-02 gap).
3. **Same author for both sides.** The validator rules, the Postman collection and the oracle were all written by the same author with the same knowledge. The oracle uses the same kinds of observation as the tools, although in independent code.
4. **Effort and diagnosis.** Timings are AI-assistant wall-clock and only indicative. There is no human effort or diagnosis study, and no API-evolution maintenance study.
5. **Scenarios.** One scenario per API, and defects injected one at a time, deliberately. No test generation.
6. **Database support.** H2 only, the same instance via `AUTO_SERVER`, default credentials hardcoded, and the exit code ambiguous on database errors (from the Library report).

**Remaining work:**
- an externally authored API on a different stack;
- a small human diagnosis study;
- a contract-evolution maintenance task;
- a passive state-continuity rule;
- configurable credentials and exit codes.

## 8. Recommendation: NARROW

**For proceeding:**
- Correctness: 0 false positives in 72 tool results on the Event API and in the 24 Library runs. In the Order pilot, the passive transition rule produced 2 false positives (HTTP 415 and 500 on clean Schemathesis traffic); they were fixed, with a regression test, before the later experiments.
- Full detection at the cause step for all 5 Event defects with engine 0.2 active.
- Reproducible results, and ground truth that has itself been checked (verifier self-tests).
- Reuse across 3 APIs, with the engine changes documented and measured.
- Richer failure evidence than Postman.

**Against a GO:**
- Detection matches Postman; it is not better.
- A careful Postman collection with read-backs is a strong baseline.
- Engine changes were needed for the third API.
- All the APIs are self-authored.
- No human-factors evidence.

**Against a PIVOT:** the contribution holds up once narrowed, and the remaining risks can be addressed within the project. The main ones are external APIs and a human diagnosis study.

## 9. Proposal-ready framing

- **Title:** *Declarative, OpenAPI-Bound Semantic Rules for State- and Database-Aware REST API Validation with Evidence-Linked Diagnosis*
- **Research question:** Can declarative rules bound to OpenAPI operations detect state-transition, cross-field and stored-data violations in REST APIs:
  - as reliably as hand-written test assertions;
  - while needing only rule configuration (plus documented general engine extensions) to move to a new API;
  - and producing failure reports with more diagnostic evidence?
- **Aim:** build and evaluate a rule engine and rule format that apply such rules to scripted scenarios and recorded traffic, and report each violation together with its request, response, prior state and stored data.
- **Objectives:**
  1. A rule format with three rule kinds, validated against the OpenAPI document, including array and envelope items and comparisons between fields.
  2. An engine with active mode (read-backs, JDBC) and passive mode (HAR), plus a passive state-continuity rule.
  3. Evidence-linked JSON and Markdown reports, and unambiguous exit codes.
  4. An evaluation on at least 4 APIs, at least 1 of them externally authored or on a different stack, with at least 10 semantic defects of at least 6 distinct types. Baselines: Ajv, Schemathesis, and an equally careful Postman collection. Measure detection at the cause step, false positives, reproducibility, configuration and maintenance changes, and report content.
  5. A small human diagnosis study (consented, timed, with a fixed task and answer key), if ethics approval allows. Otherwise report-content analysis, labelled as such.
- **MVP:** what exists now (engine 0.2, three rule kinds, active and passive modes, H2/JDBC, reports), plus configurable credentials and exit codes, plus the state-continuity rule. A minimal UI only after the external-API evaluation. **Out of scope:** rule inference, multiple database engines, full UI, claims of general reusability.
- **Evaluation method:** the protocol already used:
  - frozen contract first;
  - defects that are off by default, isolated, tested, and switched on one at a time;
  - fresh database per run, and 2 or more repeats;
  - recorded traffic and an independent database dump as ground truth;
  - "affected steps" as the definition of false positives;
  - effort measured as configuration diffs and timestamps;
  - redacted evidence.

## 10. Claims

**Supported by the evidence:**
- On three self-authored Spring Boot/H2 APIs, declarative rules detected all 10 semantic injected defects at the faulty request, in active mode with JDBC. EVT-CF-01 needs engine 0.2. There were no false positives in those runs.
- Reuse needed only new configuration for the second API. The third needed two small general engine extensions (+50/−3 lines) that did not change earlier results.
- With the same scenario and ground truth, a careful Postman collection with read-backs reached the same detection on the Event API. The validator's reports additionally contained the response body, the prior state and the stored database row.
- Results were reproducible across repeated runs with a fresh database.
- Schema-only validation (Ajv) detected none of the Event semantic defects.

**Must not be claimed:**
- That the validator detects more defects than hand-written tests, or than Postman.
- That the rules or engine work on arbitrary APIs, or that configuration-only reuse is generally achievable.
- Any human productivity, diagnosis-time or usability benefit.
- That fewer configuration lines mean less effort or a better approach.
- That passive mode detects stored-data defects. It did not detect DB-02, and it found DB-01 only downstream.
- Novelty of runtime contract checking or of database assertions in themselves.
