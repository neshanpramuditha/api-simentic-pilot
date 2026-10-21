# Experiment 2: diagnostic evidence completeness (protocol)

**Status: DRAFT, NOT FROZEN.**

> *Proposed revision E2-A31 (F-1):* this file is **not edited at freeze**. The authoritative freeze and adoption status is recorded only in `freeze-record.md`. Until that record shows the freeze procedure completed, this protocol is DRAFT, NOT FROZEN. After that, the status line above and the "NOT ADOPTED" labels below are historical.

Freeze in this order:
1. The rubric (§4).
2. The answer key (§3.2).
3. The tool configurations (§2).
4. Record their SHA-256 in §11 **before any report is scored**. This includes re-scoring the existing Event artifacts. *(Proposed revision E2-A18: decision scoring uses only the new run set N1. The existing artifacts are the historical set H and are compared descriptively only.)* *(Proposed revision E2-A30: the Git blob id is authoritative. Recording this file's own hash inside it is self-referential; the freeze-record mechanics are open, R17.5 F-1.)* *(F-1 resolved by E2-A31: the record goes in `freeze-record.md`.)*

**Disclosure.** The author of this protocol has already read the existing validator and Postman reports for the Event defects (audit §5). The rubric was written with that knowledge. A second rater (§7) is the main mitigation.

## 1. Research question and scope

**RQ2.** For selected semantic, state and database violations, does the semantic validator produce more complete, traceable evidence than Postman and Schemathesis, under a fair and reproducible configuration?

This experiment measures **what the failure reports contain**:
- not human diagnosis time;
- not diagnosis accuracy;
- not productivity.

A favourable result supports a reporting-content claim **only for the tested defect categories, tools and configurations**.

## 2. Tool configurations

All configurations are frozen before scoring. Each one is a separate column in the evidence matrix.

| Config ID | Tool and setup | Evidence artifacts scored | Status |
|---|---|---|---|
| **SV** | Validator, engine 0.2 (hash-frozen), active mode with JDBC, `event-api.rules.v2.json`, default reporting. **The engine is not changed before scoring.** | `semval.json`, `semval.md` | Existing artifacts (`event-reuse/results/matrix/*/semval-v02-jdbc-r*`) *(E2-A18: re-run in N1; existing = historical set H)* |
| **PM-D** | Postman CLI 1.74.0, `event.postman_collection.json`, CLI text output as in the existing runs | `postman.txt` | Existing artifacts (`*/postman-r*`) *(E2-A18: re-run in N1; existing = historical set H)* |
| **PM-R** | Same collection, **reporting configured but no script change**: `--output <dir>` (YAML run summary and executions) plus `--verbose` | `postman.txt` and the `--output` files | To be run. A pilot must confirm what the files contain |
| **PM-S** | PM-R plus **scripted evidence**. Collection scripts add: the request and response context in the assertion message or a logged block; the before/after database row through the database probe; a run timestamp and collection version. Authoring time and lines are recorded as overhead. Capped at 90 active minutes *(proposed revisions: the cap covers evidence code only, see E2-A20; PM-S is a primary decision baseline, see E2-A9)* | as PM-R | To be built |
| **ST-S** | Schemathesis 4.29.4 with **custom checks** (EVT-INV-001…005 equivalents, the transition table, database consistency through the probe). Driven by a **scenario adapter**: a Python script that sends the same 19 scenario requests through Schemathesis's case API and applies its checks, so the defects are triggered identically. Native output (failure report, reproduction command, NDJSON/JUnit) plus check messages | stdout, NDJSON, JUnit, adapter log | To be built. **The 4.29.4 Python API details must be verified in the pilot.** If the adapter is not possible, record ST-S as "not comparable on identical triggering" *(E2-A19, E2-A23: a bounded 6-hour pilot, then stop; "not evaluated"; never a validator advantage)* |
| ST-N (optional) | Schemathesis native stateful run with the same custom checks on a seeded database | as ST-S | Optional. Record "exercised: yes/no" per defect; score only if exercised |
| ST-0 | Schemathesis built-in checks only | — | Known from the Order API to raise no semantic failures (`docs/defect-register.md`). Not re-run on Event except for the schema contrast |

**Shared fixture: database probe.** A localhost read-only helper that runs one `SELECT` against the run's H2 database. PM-S and ST-S use it; SV uses JDBC directly. It is built once and reported as a collection requirement of the baselines.

**Why three Postman levels?**
- PM-D against PM-R separates a *reporting-configuration* effect from a tool effect (threat 10).
- PM-R against PM-S separates evidence that is available **by configuration** from evidence that has to be **authored**.
- Only a comparison against PM-R and ST-S supports any "better reporting" statement. The comparison against PM-S yields the **overhead** a baseline needs to reach parity. *(Proposed revision E2-A9: the decision baselines are PM-S and ST-S under the scripted metric; PM-D and PM-R are descriptive.)*

## 3. Defect instances

### 3.1 Selection

| Instance | Defect | API | Category | Cause step(s) in the scenario | Primary / contrast |
|---|---|---|---|---|---|
| I1 | EVT-ST-01: allowed WAITLISTED→CONFIRMED rejected (422) | Event | ST, state transition | 8 | primary |
| I2a | EVT-DB-01: forbidden change rejected (422) but stored (R1→CANCELLED) | Event | DBR, rejected request persisted | 4 | primary |
| I2b | EVT-DB-01, as I2a (R3→CHECKED_IN) | Event | DBR | 13 | primary |
| I3 | EVT-DB-02: 200 CANCELLED returned, nothing stored (lost write) | Event | DBL, response–database inconsistency | 9 | primary |
| I4 | EVT-RI-01: CHECKED_IN with `checkedInAt` null | Event | INV, conditional field invariant | 2 | primary |
| I5 | EVT-CF-01: `checkedInAt` earlier than `confirmedAt` | Event | XF, timestamp-order invariant | 2 | primary |
| I6 | ORD-SV-01: `total` serialised as a string | Order | SCH, schema mismatch | the create request(s) in the SV-01 run | **contrast** |

**Rationale.**
- The primary set is the Event API, the only API where every configuration can run the same scenario against the same build.
- The scenario is `event-reuse/scenario.json`: 19 steps, a fresh API and a fresh H2 file database per run, and exactly one defect switch, checked through the startup `DEFECT` line.
- I6 shows the scope boundary. SV has no schema rules, so it produces no report for I6; that is recorded as "no report", not scored as zero completeness. I6 uses the existing `docs/experiments/sv-01/` artifacts: Postman with a 1-request collection, and the Schemathesis baseline.
- **Not included, with reasons:** a forbidden transition accepted (ORD-ST-01, LIB-ST-01), and a second API per category (ORD-DB-02, LIB-DB-01).
  - Both need a careful Postman collection for Order or Library (3–5 h each), and Schemathesis checks for them.
  - Add them only if time allows. They are listed as secondary instances in `experiment-data-template.csv` (`instance_set = secondary`).

**Limitations.** There are 7 instances in 6 categories, from one API plus one contrast case, all self-authored. Results are descriptive per instance. No rates generalise.

### 3.2 Answer key

The answer key defines what "expected vs actual" and "prior/persisted state" mean for each instance. It follows `event-reuse/defects.md` and the diagnosis answer key.

| Instance | Faulty request | Violated property | Expected | Actual | Prior state | Persisted after |
|---|---|---|---|---|---|---|
| I1 | `POST /v1/registrations/{R2}/transitions {"to":"CONFIRMED"}` | WAITLISTED→CONFIRMED is allowed | 200, state CONFIRMED | 422 | WAITLISTED | (n/a) |
| I2a | `POST …/{R1}/transitions {"to":"CANCELLED","reason":"Left early"}` | rejected request leaves the row unchanged | row unchanged (CHECKED_IN) | row state CANCELLED | CHECKED_IN | CANCELLED |
| I2b | `POST …/{R3}/transitions {"to":"CHECKED_IN"}` | as I2a | row unchanged (WAITLISTED) | row state CHECKED_IN | WAITLISTED | CHECKED_IN |
| I3 | `POST …/{R2}/transitions {"to":"CANCELLED","reason":"Schedule conflict"}` | persisted state equals the 200 response | stored CANCELLED with the reason | stored CONFIRMED, reason null | CONFIRMED | CONFIRMED |
| I4 | `POST …/{R1}/transitions {"to":"CHECKED_IN"}` | `checkedInAt` set iff CHECKED_IN | `checkedInAt` non-null | null | (n/a) | (n/a) |
| I5 | as I4 | `requestedAt ≤ confirmedAt ≤ checkedInAt` | `confirmedAt ≤ checkedInAt` | `checkedInAt` < `confirmedAt` | (n/a) | (n/a) |
| I6 | `POST /api/orders` | response matches the `Order` schema | `total` is a number | `total` is a string | (n/a) | (n/a) |

## 4. Evidence rubric

### 4.1 Scoring unit and sources

**Scoring unit:** one *(instance, configuration, repeat)*. The evidence considered is the **union of every failure item the tool reports for the cause step**:
- a failure item is a finding, a failed assertion or a check failure;
- items at other steps are ignored, except that if the tool reports **no** failure at the cause step, its earliest failure item is scored and the unit is flagged `downstream_only`, which makes E1b absent.

**Counting rules.**
- **Each element is counted once per unit**, however many items contain it.
- **One observed value satisfies at most one element.** If a value could satisfy two elements, it is assigned to the first in rubric order, and the other element needs separate evidence. *(Proposed revision E2-A17: this applies only to E4b, E6a and E7a; E1b and E5 are statement elements.)*

**What counts as present.** An element is *present* only if all four conditions hold:
1. It is in the **tool's own output artifacts** for that run. The harness proxy HAR, API logs, database dumps and the tool's memory do not count.
2. It is attributable to the failure: it sits in the same failure item, or is explicitly linked to it by step, request name or exchange index.
3. It is **correct** against the ground truth: the run's `traffic.har.json` and `db-final.json` / oracle. Masked identifiers are acceptable.
4. It can be read without executing code. JSON and YAML are acceptable.

### 4.2 Elements

| Criterion | Element | Operational definition (present if …) |
|---|---|---|
| **E1 Failure identification** | E1a | The run signals failure in a machine-checkable way: a non-zero exit code or a failure count above 0 in the artifacts |
| | E1b | The failure is localised to the cause request, by step index, request name, exchange index, or a method + URL unique to that step |
| **E2 Rule or assertion identification** | E2a | A stable identifier of the violated check: rule ID, test name or check name |
| | E2b | A statement of the property itself, in natural language or as a formal expression. A test name counts only if it states the property |
| **E3 Request evidence** | E3a | HTTP method |
| | E3b | Concrete path with resolved path parameters |
| | E3c | Request body (*applicable only if the request has a body*) |
| **E4 Response evidence** | E4a | HTTP status code |
| | E4b | The full response body, not only the fields the check read |
| **E5 Expected vs actual** | E5a | The concrete actual value(s) that violate the property, as defined in the answer key |
| | E5b | The concrete expected value or allowed set. **`expected false to equal true` or similar boolean output does not count** |
| **E6 Prior-state evidence** | E6a | The resource's state before the request, with its value |
| | E6b | Where the prior state came from (read-back, earlier response, database) |
| **E7 Database evidence** | E7a | The persisted state after the request |
| | E7b | Storage provenance: the value demonstrably comes from the database (query, or table + key), not from an API read |
| **E8 Traceability and reproducibility** | E8a | A timestamp of the run or the failure |
| | E8b | A run identifier unique to the run |
| | E8c | The tool name and version |
| | E8d | The exact check-suite version: a hash, or an explicit version label that changes with the suite. A plain name does not count |
| | E8e | The target's identity: API build or version, or the defect or configuration mode |
| | E8f | Enough to re-issue the failing request: method, absolute URL (or base URL + path), body and any non-default headers, or a ready-made command |

### 4.3 Applicability by category

✓ means applicable; — means not applicable (N/A) and excluded from the denominator.

| Element | SCH | INV | XF | ST | DBR | DBL |
|---|---|---|---|---|---|---|
| E1a, E1b, E2a, E2b, E3a, E3b, E4a, E4b, E5a, E5b, E8a–E8f | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| E3c | if body | if body | if body | if body | if body | if body |
| E6a, E6b | — | — | — | ✓ | ✓ | ✓ |
| E7a, E7b | — | — | — | — | ✓ | ✓ |

Rationale for the exclusions:
- Prior state is not needed to see that a single response violates a response-level property (SCH, INV, XF).
- Database evidence is not needed when the defect is visible in the response (SCH, INV, XF, ST).

**State transitions and database evidence.** For ST, E7 is excluded because the violation is fully visible in the response. A tool that adds database evidence there gets no credit for it, because that would reward verbosity.

### 4.4 Status codes for each element

Each element in a unit gets exactly one code:

| Code | Meaning | Counts as present? |
|---|---|---|
| `P` | Present, produced by the tool or its configuration with no evidence-specific scripting | yes, in both native and scripted scores |
| `PS` | Present only because evidence-specific script or check code was written (PM-S, ST-S check messages) | only in the scripted score |
| `AM` | Absent, *missing*: the tool could produce it under this protocol's allowed configuration, but this configuration did not | no |
| `AU` | Absent, *unavailable*: no means under this protocol (for example, database evidence for a tool without probe or JDBC access). *(E2-A28: per element and per unit, with a recorded reason; without one it is recoded AM)* | no |
| `NA` | Not applicable (§4.3) | excluded |

Evidence that "could be retrieved with a custom script" but was not is **AM**, never P. Reports list AM and AU separately, so missing and unavailable evidence stay distinct.

## 5. Metrics

Every metric is computed per configuration, per category and per instance. The raw element codes are always published with the aggregates.

| Metric | Definition |
|---|---|
| **Criterion completeness (primary)** | For each applicable criterion E1–E8: (present elements / applicable elements). Then the mean over the applicable criteria of the unit. Each criterion carries equal weight, so the many E8 elements cannot dominate. *(E2-A9: the primary score counts P + PS, i.e. scripted; formulas in R4)* |
| Element completeness | Present / applicable elements, pooled over the unit |
| Native vs scripted | Both metrics with only `P` counted (native) and with `P`+`PS` counted (scripted) |
| Traceable request and response | % of units with E3a, E3b, E3c (if applicable), E4a and E4b all present |
| Prior-state evidence | % of ST, DBR and DBL units with E6a present |
| Database evidence | % of DBR and DBL units with E7a and E7b present |
| Reproducible failure context | % of units with E8c, E8d, E8e and E8f all present |
| Completeness by category | Criterion completeness averaged per category and configuration |
| Reporting overhead | For each baseline level: extra script lines, extra helper lines, extra configuration flags, and the authoring time needed to go from PM-D to PM-R and to PM-S (and for ST-S) |
| Collection requirements | What each configuration needs: JDBC, database probe, scenario adapter, reporter flags |
| Report size | Bytes of the scored artifacts per unit. Recorded **only as a control**, to show whether completeness tracks verbosity |
| Repeat identity | Element codes identical across the 2 repeats (yes/no) |

## 6. Procedure

1. **Freeze** the rubric, the answer key and the configurations (§11). Recompute the hashes in `baseline-hashes.sha256`. *(Meaning: verify them against the file. The file is never modified.)*
2. **Build** the database probe, the PM-S scripts (timed, as overhead) and the ST-S checks and adapter (timed, as overhead). *(E2-A20 / R14.6 / R15.3: detection, setup and adapter effort uncapped, recorded in `notes`; only evidence code is capped at 90 minutes.)*
3. **Pilot.** Run the clean API and one defect for PM-R, PM-S and ST-S *only to confirm they run and produce no false positives on clean*. Pilot outputs are kept but **not scored**. If the pilot shows a configuration cannot run, record that and stop for that configuration. Do not redesign the protocol around it. *(E2-A23: the ST-S feasibility pilot is time-boxed at 6 active hours. E2-A24: the detection-check equivalence review is completed **before any baseline is scored**.)*
4. **Matrix.** Clean plus 5 defect modes × {PM-R, PM-S, ST-S} × 2 repeats, using the existing run conventions:
   - a new API process and a new H2 file database per run;
   - the switch-line check;
   - the recording proxy;
   - a database dump and the oracle (`verify-event-run.js`).

   SV and PM-D use the existing runs. *(Proposed revision E2-A18: SV and PM-D are re-run in N1.)*
   - **Optionally, re-run SV and PM-D on the rebuilt jars to confirm** the results are identical to the stored ones. If they differ, the new runs replace the old ones, and the difference is reported. *(Proposed revision E2-A18: the re-run is **required** in run set N1. Stored results are never replaced; they stay as the historical set H.)*
5. **Extract.** A script, adapted from `diagnosis.js` to the new rubric, lists the failure items at the cause step for each unit. It proposes an evidence locator per element (file plus JSON path or line).
6. **Score manually.** The scorer assigns the element codes in `evidence-matrix-template.csv` and records each locator and a masked excerpt. The script's proposals are checked, never accepted unseen. *(E2-A24: baseline units are scored only after the equivalence review is recorded.)*
7. **Verify.** For each `P` or `PS`, compare the value with the HAR or database dump. If it is wrong, change the code to `AM` and add a note.
8. **Self-audit** at least 48 h later: re-score a random 30% of units (the seed is recorded) without looking at the first scores. Report the agreement between the two passes.
9. **Second rater** (§7).
10. **Compute** the metrics (§5). Apply the decision rule (§8).

## 7. Rater bias and blinding

- Blinding to the tool is **not achievable**: report formats identify the tool immediately. This is disclosed. Tool names may be masked in file names, but that does not blind the rater.
- **Second rater, if available** (a classmate or supervisor):
  - they score at least 8 units, covering every configuration and at least one instance from each of ST, DBR, DBL and INV;
  - they use only this rubric and the answer key;
  - they are not told the hypothesis.
- Report the **percentage agreement per element**, and list each disagreement with how it was resolved.
- Cohen's κ is reported only if two raters scored the same units, and it is labelled as based on a small sample.
- **Without a second rater:** report the self-audit only, and state that no inter-rater reliability exists. *(Proposed revision E2-A27: without an independent reviewer, Experiment 2 is INCONCLUSIVE for the project gate. This is an evaluation limitation, not evidence of validator failure or superiority.)*

## 8. Decision rule and threshold

The rule is defined before scoring.

### 8.1 Favourable

> *Proposed revisions E2-A9, E2-A10, E2-A15 and E2-A22 replace §8.1 and §8.2 with the scripted primary metric and the ordered outcome function (`operational-definitions.md` R8). This text is kept as history until freeze.*

The result is favourable for a reporting-content claim only if **all** of these hold:
1. The instances are comparable: the same build, scenario, switch and fresh database for every configuration. ST-S uses the adapter, or it is excluded with a reason.
2. The rubric, answer key and configurations were frozen before scoring (§11).
3. The raw evidence is reproducible: element codes are identical across repeats, or every difference is explained.
4. There is no double counting (§4.1 rules held, as checked in the self-audit).
5. On the **ST, DBR and DBL units**, SV's native criterion completeness exceeds the **best non-scripted baseline** (the higher of PM-R and ST-S, where ST-S counts *only `P`*) by **at least 20 percentage points** on average, **and** SV is at least the baseline on every one of those units.

### 8.2 Interpretation

| Result | Reading |
|---|---|
| Item 5 holds against PM-D but not against PM-R or ST-S | The existing difference was an **output-configuration effect**. This is evidence against the differentiation |
| Item 5 holds, and PM-S reaches parity | The claim narrows to "the validator provides this evidence **without authoring** it". Its strength is the measured PM-S overhead (lines and minutes). That overhead is an effort claim, which is Experiment 1's question, not a reporting claim |
| SV completeness ≤ baseline on INV, XF or E8 | Report it as it is. A traceability gap (E8) in SV is a finding, not a scoring error. Do not fix the engine before the baseline is scored. A later engine change can be scored as a separate, labelled configuration |
| All configurations comparable | Record this as **evidence against** the proposed differentiation |

### 8.3 Why 20 percentage points

- With 7–8 applicable criteria per state or database unit, 20 pp is a difference of ~~about 1.5–2 whole criteria. An example is prior state plus database evidence: a class of information a developer would otherwise collect by hand.~~ **about 1.4 criteria (ST) to 1.6 criteria (DB); two whole criteria would be 25–29 pp (corrected 2026-10-21, revision 2; see `operational-definitions.md` R11).**
- Differences of one criterion or less are within what one ambiguous element judgment by a single rater can produce.
- No external benchmark for this threshold exists. It is a pre-registered judgement, and it is stated as one.

## 9. Reproducibility requirements

For every scored unit, record:
- the tool version and configuration ID;
- the engine, rules, collection, checks and adapter hashes;
- the API jar hash and defect switch;
- the `DEFECT` line check result;
- the run directory;
- the start time (UTC);
- the repeat number;
- the oracle verdict.

Store all raw artifacts. Redact them in the same way as the existing evidence: local paths become `<run-root>`, ids are masked only in the derived files, and there are no credentials.

Record the commands exactly. Store no Postman credentials, and run with `--no-report-events`, as in the existing runs.

## 10. Templates

- **Element-level raw matrix:** `evidence-matrix-template.csv`, one row per unit × element.
- **Run-level record:** `experiment-data-template.csv`, with `experiment = E2`.
- **Summary table** (filled only from the raw matrix):

| Configuration | Category | Units | Criterion completeness, native (mean) | Criterion completeness, scripted (mean) | E6 present | E7 present | E8 reproducible context | AM count | AU count | Overhead (lines / min) |
|---|---|---|---|---|---|---|---|---|---|---|
| SV | ST | | | | | | | | | 0 / 0 |
| … | | | | | | | | | | |

## 11. Freeze log

> *E2-A31: the freeze of this file is recorded in `freeze-record.md`, not here; the first row below stays empty. The supporting artifacts in the other rows are frozen separately when built, before any run, and recorded in their own freeze-record entries.*

| Date (UTC) | Item | SHA-256 | Recorded by |
|---|---|---|---|
| | experiment-2-evidence-completeness-protocol.md (rubric §4, answer key §3.2, configurations §2) | | |
| | PM-S collection | | |
| | ST-S checks and adapter | | |
| | database probe | | |

## Amendments

### Proposed amendments (2026-10-21): NOT ADOPTED

These are proposals. The protocol text above still applies until each one is adopted (dated here) and the freeze log (§11) is filled. *(E2-A31: adoption and freeze are recorded in `freeze-record.md`, not here.)* **No threshold changes:** the margin stays 20 percentage points, with SV at least equal to the baseline on every unit.

| ID | Section affected | Proposed change | Reason |
|---|---|---|---|
| E2-A1 (**SUPERSEDED 2026-10-21 rev 2 by E2-A11**: classification kept and refined) | §4.4 | Use the detection-code / evidence-code rule (`operational-definitions.md` §4): data that appears only through custom messages, logs or extra fields is `PS`; the assertion, its name and the tool's default message are `P` | The existing collection already puts evidence into custom messages (Event generator lines 40–42, 66); without a rule, PM-R's native score is undefined |
| E2-A2 (**WITHDRAWN 2026-10-21 rev 2**: the native metric is no longer primary; see E2-A9) | §8.2 | Add: because of E2-A1, a favourable *native* result supports only "evidence without authoring". Any wider "more complete evidence" claim also needs the scripted comparison against PM-S | Says openly that the native metric favours the validator by construction |
| E2-A3 (**SUPERSEDED IN PART 2026-10-21 rev 2 by E2-A10**: the defect averaging is kept; "best non-scripted baseline" is replaced by the best *scripted* baseline) | §8.1(5) | The average is taken over **defects**: I2a and I2b are averaged first. "At least" includes ties. "Best non-scripted baseline" is per unit, the higher native score of PM-R and ST-S, or PM-R alone if ST-S is excluded | EVT-DB-01 would otherwise count twice; definitions were missing (A3–A5) |
| E2-A4 | §6 step 4 | Replace "using the existing run conventions" with: a **new** runner file that keeps those conventions (new API process, new H2 file database, the `DEFECT` line check, the recording proxy, the database dump, the oracle) and records the run metadata in `postman-baseline-checklist.md` §5 (M1–M6). The existing runner is not edited | `run-event-matrix.ps1` cannot run PM-R, PM-S or ST-S, and needs a v0.1 jar that is not in the repository (audit addendum N1) |
| E2-A5 | §6 step 4 | The oracle must be told that database-probe calls are not in `traffic.har.json`, and that PM-S may add pre-request GETs. Use a new or parameterised oracle file, self-tested first | `verify-event-run.js` classifies read-backs by position (N3) |
| E2-A6 (**REFINED 2026-10-21 rev 2 by E2-A12**: the scripting policy now applies to PM-S and ST-S alike) | §2 PM-S | The PM-S changes are those in `postman-baseline-checklist.md` items P4 and P7–P11, together with the evidence scripting. Record `detection_code_lines` and `evidence_code_lines` | Ties "scripted" to a concrete list of changes |
| E2-A7 | §2 ST-S | Prerequisites and exclusion conditions as in `schemathesis-baseline-audit.md` §7–§8 | Makes ST-S's dependency on untested Schemathesis features explicit |
| E2-A8 | §6 steps 5, 10 | Extraction and scoring scripts must write to a **new** output directory and refuse to write if it already exists | Existing save and diagnosis scripts overwrite their destination (N6) |

### Proposed amendments, revision 2 (2026-10-21): NOT ADOPTED

On 2026-10-21 the repository owner approved the direction of these amendments. They remain **proposed** until this protocol is frozen (§11). Full definitions are in `operational-definitions.md` R1–R13. **Thresholds are unchanged and remain proposed** (20 pp; the guard at −20 pp).

| ID | Section affected | Proposed change |
|---|---|---|
| E2-A9 | §2 ("Why three Postman levels?": the statement that only PM-R and ST-S support a "better reporting" statement), §5, §8 | **Primary metric: scripted criterion completeness**, so PM-S and ST-S (P + PS) are the decision baselines; PM-D and PM-R remain descriptive. Secondary metrics: native completeness, and authoring overhead (minutes and lines, descriptive). **No effort threshold in Experiment 2** (R1) |
| E2-A10 | §5, §8.1 | Scoring formulas, the defect-level averaging (EVT-DB-01 counted once), the decision set T and guard set G, the best scripted baseline B(d), repeat handling, and no pooling across categories or APIs (R4) |
| E2-A11 | §4.1, §4.4 | Deletion-test classification for custom messages, logs, follow-up requests, probe results, framework output and harness metadata (R2). A read-back may satisfy E7a only, labelled as API-observed state; never E7b. E5b clarification: a concrete expected status code counts, but does not establish the business reason (R2) |
| E2-A12 | §2 (PM-S, ST-S) | Scripting policy: generic, derived, frozen before defect runs, the rubric given as the evidence specification, the same probe, **a 90-active-minute cap for both Postman and Schemathesis**, active time and code size recorded separately (R3) |
| E2-A13 | §4.1 | The one-value rule applies to observed data values, not to labels or provenance. One provenance statement covering both rows may support E6b and E7b (R5). **Open issue O-1** (R13) must be decided before freezing. *(Revision 3: O-1 resolved, and the rule scope is replaced by E2-A17.)* |
| E2-A14 | §7 | Disputed-code resolution: a second rater preferred, otherwise the mandatory self-audit; bounding analysis (SV-low / SV-high); disclosure (R6) |
| E2-A15 | §8.1, §8.2 | Replace with the ordered, mutually exclusive outcome function: preconditions V1–V5, then FAVOURABLE / INCONCLUSIVE (mixed) / UNFAVOURABLE / COMPARABLE, the guard calculation, and V6 robustness (R8) |
| E2-A16 | §8.3 | Arithmetic corrected in place (struck-through original kept). Sensitivity reporting at 10 / 30 pp, proposed and not validated; it never changes the decided category (R7, R11) |

### Proposed amendments, revision 3 (2026-10-21): NOT ADOPTED

These record the owner's decisions on O-1 to O-6. They remain **proposed** until this protocol is frozen (§11). Full text is in `operational-definitions.md` R14. **Thresholds are unchanged and remain judgement-based proposals**: 20 pp, the −20 pp guard bound, sensitivity at 10/30 pp, and the Schemathesis pilot time box of 6 h.

| ID | Section affected | Proposed change |
|---|---|---|
| E2-A17 | §4.1 (the sentence "One observed value satisfies at most one element") | The one-value rule applies only to the state-data elements **E4b, E6a and E7a**. **E1b, E5a and E5b are statement elements**, assessed separately. A satisfiability check shows every applicable element remains satisfiable (R14.1). Replaces the scope stated in E2-A13 |
| E2-A18 | §6 step 4 ("Optionally, re-run SV and PM-D …") | **Required, not optional.** All decision configurations, including SV and PM-D, come from one new run set **N1** on the same rebuilt jars, with full run metadata. The historical set **H** is read-only and only compared descriptively (R14.3) |
| E2-A19 | §2 (ST-S row) | A bounded Schemathesis **feasibility pilot** (6 active hours, proposed) runs before the matrix and is not scored. On failure: a documented exclusion (blocker, attempts, consequences). Claims are then limited to Postman, and the exclusion is **not** evidence that the validator is superior (R14.4) |
| E2-A20 | §2 (PM-S row, "Capped at 90 active minutes"; ST-S) | The 90-minute cap covers **evidence-authoring code only**, the same for PM-S and ST-S. Detection checks, setup, environment and adapter effort are recorded separately and uncapped, and must pass an equivalence review **before** the evidence clock starts. A baseline that fails the review gives V5 failure (INCONCLUSIVE), never a weaker scored baseline (R14.6) |
| E2-A21 | §4.4 (AU) | The validator's E8 elements stay applicable and visible. AU is assigned only per §4.4/R2, with a reason recorded per element. No report fields are added and the engine is not modified (R14.5) |
| E2-A22 | §8 | Confirmed: this experiment **alone cannot produce GO or GO-CANDIDATE**. No reporting-only GO route exists under the current scope; one could only be introduced through an explicit, dated, frozen scope and gate revision before further evaluation (R14.2) |

### Proposed amendments, revision 3 final conditions (2026-10-21): NOT ADOPTED, NOT FROZEN

| ID | Section affected | Proposed change |
|---|---|---|
| E2-A23 | §2 (ST-S), §6 step 3 | **The 6-active-hour pilot time box is approved as a judgement-based bound.** If the pass criterion is not met within it, the pilot stops; the blocker and attempts are recorded; Schemathesis is reported as "not evaluated"; this is not evidence of validator superiority (R15.1) |
| E2-A24 | §6 steps 3 and 6, §7 | **Detection-check equivalence review before any baseline is scored.** An independent second reviewer is preferred. A self-review is recorded as such and is not treated as independent verification. The outcome stays INCONCLUSIVE until the requirement is satisfied (R15.2). **Open U-1:** whether a self-review can ever satisfy it. Interim reading: no |
| E2-A25 | §2, §8 | Missing, unusable or excluded ST-S (or PM-S) results are never imputed or scored as 0 in any baseline maximum. Either the baseline is excluded through documented exclusion, or V5 fails. No validator advantage can be inferred (R15.4) |
| E2-A24 note | — | **Freeze decisions (2026-10-21):** open item U-1 in E2-A24 is resolved by E2-A27 |
| E2-A26 | §5 (reporting overhead), §10 | Detection, setup and adapter effort are recorded in `notes` as `DETECTION:`, `SETUP:` and `ADAPTER:` entries with active minutes and a description; no new CSV column. `overhead_minutes` = evidence-authoring minutes only (R15.3) |

### Proposed amendments, freeze decisions (2026-10-21): NOT ADOPTED, NOT FROZEN

| ID | Section affected | Proposed change |
|---|---|---|
| E2-A27 | §6 steps 8–9, §7, §8 | **Independent review is required** for (a) detection-check equivalence and (b) disputed evidence scoring, as new precondition **V7**. Without an independent reviewer, Experiment 2 is **INCONCLUSIVE for the project gate**; scores are reported descriptively. This is an evaluation limitation, not evidence of validator failure or superiority (R17.1) |
| E2-A28 | §4.4 | **AU** is per element and per unit, under the operational definition, with a recorded reason in `notes` (`AU: <element> — <reason> — <reference>`). An AU without a reason is recoded AM. Baselines also need a documented pilot attempt (R17.4) |
| E2-A29 | §2 (ST-S), §6 step 3 | The 6-hour pilot limit is kept. Its limitations are documented: a judgement; may exclude a feasible baseline, which narrows the claims but gives no validator advantage; self-recorded time; clean plus one defect only (R17.3) |
| E2-A30 | §11 (freeze log) | Freeze identifiers: the Git blob id is authoritative for version-controlled content; working-tree SHA-256 is an integrity check of the bytes used. Fresh-checkout behaviour is untested. **The freeze-record mechanics (R17.5, F-1) are undecided** *(resolved by E2-A31)* |

### Proposed amendments, F-1/F-3 resolutions (2026-10-21): NOT ADOPTED, NOT FROZEN

| ID | Section affected | Proposed change |
|---|---|---|
| E2-A31 | Header, §11 | **Separate freeze record (F-1).** The freeze, the blob ids, the working-tree SHA-256 values and the adoption status of E2-A1 to E2-A32 are recorded only in `freeze-record.md`. This file is not edited at freeze, and never contains its own hash. Its status line and amendment labels become historical once `freeze-record.md` shows the procedure completed (R18.1) |
| E2-A32 | §5 (native score, secondary), §8 (gate interface) | **Native-margin bound (F-3).** The native margin used by the effort-only gate route is computed under both plausible-code bounds. The route needs **Δ̄_nat^low ≥ 0.20** (the least favourable bound), together with Experiment 2 = COMPARABLE, all preconditions including V7, and Experiment 1 = FAVOURABLE on T1–T3. If the bounds disagree, the native-margin result is INCONCLUSIVE and the route is not taken. **The Experiment 2 outcome (R8) is unchanged, and no reporting-only GO route is created** (R18.2) |
