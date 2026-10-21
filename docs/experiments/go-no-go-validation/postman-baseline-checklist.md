# Postman baseline: fairness checklist (2026-10-21)

**What this is:** a non-destructive checklist of the changes needed before Postman can be called a fair baseline. **Nothing here has been implemented or run.**

**Labels.** Every statement has one of these:

| Label | Meaning |
|---|---|
| **[V]** | Verified by reading the file or running a read-only command on 2026-10-20/21 |
| **[I]** | Inference from code reading; not executed |
| **[U]** | Untested or unverified |
| **[P]** | Proposed change; not made |

**Files inspected (read only):**
- `docs/experiments/event-reuse/build-postman-collection.js`, the Event generator (104 lines). The collection `event.postman_collection.json` is generated from it.
- `docs/experiments/evaluation/build-postman-collection.js`, the Order generator (56 lines). The collection `evaluation.postman_collection.json` is generated from it.
- `docs/experiments/event-reuse/run-event-matrix.ps1` and the stored `run.json` and `postman.txt` files.

**Library API:** no Postman collection exists **[V]**.

Rule of this checklist: a script existing is not evidence of equivalence. Each row names the specific check and the line where Postman differs from the validator rule.

## 1. Event collection compared with `event-api.rules.v2.json`

| # | Area | Validator (rules v2 / engine 0.2) | Postman (Event generator) | Status | Required change |
|---|---|---|---|---|---|
| P1 | Request sequencing | 19 steps from `scenario.json` | Same 19 steps, generated from the same file (generator line 73) **[V]** | Equal | none |
| P2 | Variable handling | `save` → `${r1}` and so on, path parameters only | `pm.collectionVariables.set` per `save` (line 80); `{{r1}}` in the URL (line 75) **[V]** | Equal | none |
| P3 | Conditional-field invariants INV-001…003 | `iff` with `notNull` (absent = null) | `set(v) = v !== null && v !== undefined` (line 36), compared to the state (lines 40–42) **[V]** | Equal **[I]** | none |
| P4 | INV-004 | `implies(state = CHECKED_IN, confirmedAt notNull)`; absent counts as null | `pm.expect(r.confirmedAt).to.not.equal(null)` (line 44) **passes when `confirmedAt` is absent (`undefined`)** **[I]** | **Unequal** (an absent field is missed) | **[P]** use `set(r.confirmedAt)` |
| P5 | INV-005 time order | `notAfter`: both set and a ≤ b; a missing side is a violation | `Date.parse` comparison (lines 47–50); a null `confirmedAt` gives NaN, which fails **[I]** | Equal in effect **[I]** (`final-project-feasibility.md` §5 reports the same behaviour) | none; verify in the pilot **[U]** |
| P6 | Envelope items | `items: {listAttendeeRegistrations: "$.items"}` | `isEnvelope ? body.items` (lines 33–34) **[V]** | Equal | none |
| P7 | **Transition legality (C1/C2)** | Transition table, applied to any request; the previous state comes from a read-back | **No transition table.** Per-step `[STEP] status is N` and `state is S` copied from the scenario's `expect` (lines 77–78) **[V]**. The generator comment (lines 9–10) states this choice | **Unequal.** Postman encodes the *expected answer for this scenario*. It does not carry over to another request sequence (Experiment 1, H1.4) and cannot say *why* a status was expected (EVT-ST-01) | **[P]** add a collection-level transition table with the previous state from the probe or from a pre-request GET, mirroring rules v2. Keep the per-step checks as a separate, labelled set |
| P8 | **Prior state for a rejected request (C3)** | Database row read **in-process before** the request | The snapshot is the last *observed response* for that id (lines 53–54, 58), not a read taken just before the request **[V]** | **Unequal** | **[P]** pre-request script: query the probe for the row before the request; store it in a request-scoped variable |
| P9 | **Persisted state (C3/C4)** | JDBC `SELECT state, confirmed_at, checked_in_at, cancelled_at, cancellation_reason …` | HTTP read-back `GET /v1/registrations/{id}` (line 59) **[V]**. It shows the API's view, not storage | **Unequal capability** | **[P]** after the response, query the probe with **the same SQL** as `EVT-DB-001`. Keep the HTTP read-back as a separate labelled check, or remove it, and record which |
| P10 | Compared fields after a 2xx | `state`, `cancellation_reason` only | `state`, `cancellationReason` **plus** three timestamps within 1 ms (lines 63–64) **[V]** | **Unequal** (Postman is stricter) | **[P]** choose one field set for both tools, record it in the check mapping, and do not edit the validator rules. The default is to match the rule |
| P11 | Compared fields after a 4xx | the whole selected row (5 columns) unchanged | the same 5 fields within 1 ms (lines 63–64) **[V]** | Close; timestamp tolerance differs **[I]** | **[P]** compare the probe rows exactly, as the validator does |
| P12 | Schema validation | none (the validator has no schema rules) | `[schema]` on every 2xx (line 35) **[V]** | Postman has an **extra** check | Keep (it is what detects SV-01), but label it, and exclude it from C1–C6 equivalence |
| P13 | Downstream step checks | none | `[STEP]` checks on reads and lists give downstream detection, for example EVT-DB-02 at steps 10 and 15 (`summary.md`) **[V]** | Postman has **extra** checks derived from the answer | Keep, labelled. In scoring, only cause-step items count (Experiment 2 §4.1) |
| P14 | Read-back routing | — | The read-back uses `pm.variables.get("baseUrl")`, the proxy port 8092 (line 59), so the harness records it **[V]** | OK | Probe calls go to a different port and are **not** recorded by the proxy **[I]**. The oracle must be told (see the protocol amendments) |
| P15 | Authorship and knowledge | rules written before the defects (`timing.log`) | the generator was built after the defects and after the validator's misses (`timing.log` 07:19) **[V]** | Knowledge asymmetry | Disclose. For new PM-S work, record the author and the start time |

## 2. Order collection compared with `order-api.rules.json`

Needed only for the secondary instances in Experiment 2 and for task T2 in Experiment 1.

| # | Check | Validator | Postman (Order generator) | Status | Required change |
|---|---|---|---|---|---|
| O1 | ORD-INV-001 `completedAt` iff COMPLETED | yes | **absent.** Only status, expected status, schema and list statuses (lines 27–34) **[V]** | Missing | **[P]** add the invariant on single and list responses |
| O2 | ORD-ST-001 transition table | yes | per-step expected status only (line 27) **[V]** | Unequal (see P7) | **[P]** add a transition table |
| O3 | ORD-DB-001 database consistency | yes (JDBC) | **absent.** No read-back, no database **[V]** | Missing | **[P]** probe queries before and after each status change |
| O4 | Sequencing and variables | `${a}`, `${b}` | `pm.collectionVariables` (line 36); `baseUrl` 8080 (line 51) **[V]** | Equal | none |

**Library:** a collection must be written from scratch to the same checklist **[V: none exists]**.

## 3. Reporting options

| # | Item | Current | Status | Required change |
|---|---|---|---|---|
| R1 | Output captured | `postman collection run … --env-var baseUrl=… --no-report-events --color off`, CLI text only (`run-event-matrix.ps1` line 91) **[V]** | PM-D configuration | Keep it as PM-D |
| R2 | `--output <folder>` | Listed in `postman collection run --help`, Postman CLI 1.74.0 **[V]**. Contents **not inspected** **[U]** | Unknown | **[P]** pilot on the clean API. Record which fields appear (request/response bodies, timestamps, ids) |
| R3 | `--verbose` | Listed in `--help` **[V]**. Effect on the output **[U]** | Unknown | **[P]** pilot |
| R4 | Nothing sent to the cloud | `--no-report-events` used **[V]**. The CLI printed "Postman credentials are required for publishing run details" and ran locally (`sv-01/results.md`) **[V]** | OK for PM-D | **[U]** confirm that `--output` writes locally only. Keep `--no-report-events`, and do not log in |
| R5 | Exit code | recorded in `run.json` **[V]** | OK | none |

## 4. Fresh state per defect run

| # | Item | Status |
|---|---|---|
| F1 | New API process and new H2 file database per run; the `DEFECT` line checked (`run-event-matrix.ps1` lines 49–59) **[V]** | Ready for Event |
| F2 | The run directory must not exist yet (line 50 `throw`), so no run is overwritten **[V]** | Ready |
| F3 | Collection variables (`snap_*`) are runtime-only in a CLI run **[I]** | **[U]** confirm in the pilot that no state carries over between runs |
| F4 | A runner for PM-R / PM-S | **Missing.** The existing runner's run types are fixed, and it needs the engine v0.1 jar directory, which is not in the repository **[V]** | **[P]** write a new runner file (needs separate approval); do not edit the existing one |

## 5. Version and configuration recording

| # | Item | Current | Required change |
|---|---|---|---|
| M1 | Postman CLI version | in the READMEs only, not per run **[V]** | **[P]** write `postman --version` into each `run.json` |
| M2 | Collection identity | `info` has a name and no version **[V]**; there is no hash per run | **[P]** SHA-256 of the collection file in each `run.json` |
| M3 | Timestamps | `run.json` has `durationMs` only **[V]** | **[P]** start and end time (UTC) |
| M4 | API jar hash and defect switch | the switch and `DEFECT` lines are recorded; no jar hash **[V]** | **[P]** jar SHA-256 per run |
| M5 | Probe identity | — | **[P]** probe file hash and query-list hash per run |
| M6 | Evidence lines added | — | **[P]** record `detection_code_lines` and `evidence_code_lines` (operational-definitions.md §4; classification now by the R2 deletion test) |

## 6. Read-only database evidence

All rows are **[P]**. Building the probe needs separate approval.

- A localhost-only helper that runs **only queries from a fixed list**, identical to the validator rules' SQL (`EVT-DB-001`, `ORD-DB-001`, `LIB-DB-001`), with positional parameters. Arbitrary SQL is refused.
- It connects to the run's H2 database with the same URL as the validator (`AUTO_SERVER=TRUE`).
  - Whether H2's read-only modes can be combined with `AUTO_SERVER` is **[U]**. If they cannot, read-only behaviour is enforced by the fixed query list alone.
- It returns the first row as JSON, matching the validator's `queryRow`, for parity.
- Postman calls it with `pm.sendRequest` (pre-request for "before", test script for "after"). Schemathesis uses the same probe.

**Limitation to disclose.** The probe is helper code that the baselines need and the validator does not. It is reported as a collection requirement and overhead, not hidden.

## 6a. Scripting policy and evidence classification (revision 2, 2026-10-21, proposed)

- PM-S is built under the scripting policy in `operational-definitions.md` R3:
  - generic and derived;
  - frozen before any defect run;
  - the rubric given as the evidence specification;
  - the same probe;
  - **a 90-active-minute cap**;
  - active time and code size recorded separately.
- Every element is classified with the deletion test (R2). Custom assertion messages, logs, displayed-only follow-up requests and probe results are **PS** unless the element also appears without that code.
- An HTTP read-back may satisfy **E7a only**, labelled as API-observed state, and **never E7b**.
- PM-S is now the configuration that counts in the **primary** (scripted) decision (R1, R8).
- **Revision 3 (2026-10-21, proposed; R14.6):**
  - The 90-minute cap covers **evidence-authoring code only**.
  - The detection changes P4 and P7–P11 are built first, uncapped, with their effort recorded separately. They must pass the equivalence review (C1–C6 mapped, executed on clean, 0 false positives) before the evidence clock starts.
  - If they cannot pass it, the outcome is INCONCLUSIVE, not a weaker scored PM-S.
  - PM-D and PM-S are run fresh in run set N1 (R14.3).
- **Final conditions (2026-10-21, proposed; R15.2, R15.3):**
  - The equivalence review is completed **before any PM-S unit is scored**, by an independent reviewer if available.
  - A self-review is recorded as such and is not treated as independent verification; until the requirement is satisfied, the Experiment 2 outcome stays INCONCLUSIVE (open U-1).
  - Detection effort is recorded in `notes` as `DETECTION: <minutes> min — <description>`.
- **Freeze decisions (2026-10-21, proposed; R17.1, R17.4):**
  - The PM-S equivalence review and the review of disputed scoring need an **independent reviewer**; without one, Experiment 2 is INCONCLUSIVE for the gate (open U-1 above is resolved).
  - Any PM-S AU code needs a recorded reason and a documented pilot attempt.

## 7. Summary: specific gaps before Postman is a fair baseline

- **Event:** P4, P7, P8, P9, P10 and P11 are unequal. P12, P13 and P15 must be labelled and disclosed. R2 and R3 need a pilot. M1–M6 are missing.
- **Order:** O1, O2 and O3 are missing.
- **Library:** no collection exists.
