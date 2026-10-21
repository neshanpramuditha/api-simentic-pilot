# Experiment 2: diagnostic evidence completeness (protocol)

**Status: DRAFT, NOT FROZEN.**

Freeze in this order:
1. The rubric (§4).
2. The answer key (§3.2).
3. The tool configurations (§2).
4. Record their SHA-256 in §11 **before any report is scored**. This includes re-scoring the existing Event artifacts.

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
| **SV** | Validator, engine 0.2 (hash-frozen), active mode with JDBC, `event-api.rules.v2.json`, default reporting. **The engine is not changed before scoring.** | `semval.json`, `semval.md` | Existing artifacts (`event-reuse/results/matrix/*/semval-v02-jdbc-r*`) |
| **PM-D** | Postman CLI 1.74.0, `event.postman_collection.json`, CLI text output as in the existing runs | `postman.txt` | Existing artifacts (`*/postman-r*`) |
| **PM-R** | Same collection, **reporting configured but no script change**: `--output <dir>` (YAML run summary and executions) plus `--verbose` | `postman.txt` and the `--output` files | To be run. A pilot must confirm what the files contain |
| **PM-S** | PM-R plus **scripted evidence**. Collection scripts add: the request and response context in the assertion message or a logged block; the before/after database row through the database probe; a run timestamp and collection version. Authoring time and lines are recorded as overhead. Capped at 90 active minutes | as PM-R | To be built |
| **ST-S** | Schemathesis 4.29.4 with **custom checks** (EVT-INV-001…005 equivalents, the transition table, database consistency through the probe). Driven by a **scenario adapter**: a Python script that sends the same 19 scenario requests through Schemathesis's case API and applies its checks, so the defects are triggered identically. Native output (failure report, reproduction command, NDJSON/JUnit) plus check messages | stdout, NDJSON, JUnit, adapter log | To be built. **The 4.29.4 Python API details must be verified in the pilot.** If the adapter is not possible, record ST-S as "not comparable on identical triggering" |
| ST-N (optional) | Schemathesis native stateful run with the same custom checks on a seeded database | as ST-S | Optional. Record "exercised: yes/no" per defect; score only if exercised |
| ST-0 | Schemathesis built-in checks only | — | Known from the Order API to raise no semantic failures (`docs/defect-register.md`). Not re-run on Event except for the schema contrast |

**Shared fixture: database probe.** A localhost read-only helper that runs one `SELECT` against the run's H2 database. PM-S and ST-S use it; SV uses JDBC directly. It is built once and reported as a collection requirement of the baselines.

**Why three Postman levels?**
- PM-D against PM-R separates a *reporting-configuration* effect from a tool effect (threat 10).
- PM-R against PM-S separates evidence that is available **by configuration** from evidence that has to be **authored**.
- Only a comparison against PM-R and ST-S supports any "better reporting" statement. The comparison against PM-S yields the **overhead** a baseline needs to reach parity.

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
- **One observed value satisfies at most one element.** If a value could satisfy two elements, it is assigned to the first in rubric order, and the other element needs separate evidence.

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
| `AU` | Absent, *unavailable*: no means under this protocol (for example, database evidence for a tool without probe or JDBC access) | no |
| `NA` | Not applicable (§4.3) | excluded |

Evidence that "could be retrieved with a custom script" but was not is **AM**, never P. Reports list AM and AU separately, so missing and unavailable evidence stay distinct.

## 5. Metrics

Every metric is computed per configuration, per category and per instance. The raw element codes are always published with the aggregates.

| Metric | Definition |
|---|---|
| **Criterion completeness (primary)** | For each applicable criterion E1–E8: (present elements / applicable elements). Then the mean over the applicable criteria of the unit. Each criterion carries equal weight, so the many E8 elements cannot dominate |
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

1. **Freeze** the rubric, the answer key and the configurations (§11). Recompute the hashes in `baseline-hashes.sha256`.
2. **Build** the database probe, the PM-S scripts (timed, as overhead) and the ST-S checks and adapter (timed, as overhead).
3. **Pilot.** Run the clean API and one defect for PM-R, PM-S and ST-S *only to confirm they run and produce no false positives on clean*. Pilot outputs are kept but **not scored**. If the pilot shows a configuration cannot run, record that and stop for that configuration. Do not redesign the protocol around it.
4. **Matrix.** Clean plus 5 defect modes × {PM-R, PM-S, ST-S} × 2 repeats, using the existing run conventions:
   - a new API process and a new H2 file database per run;
   - the switch-line check;
   - the recording proxy;
   - a database dump and the oracle (`verify-event-run.js`).

   SV and PM-D use the existing runs.
   - **Optionally, re-run SV and PM-D on the rebuilt jars to confirm** the results are identical to the stored ones. If they differ, the new runs replace the old ones, and the difference is reported.
5. **Extract.** A script, adapted from `diagnosis.js` to the new rubric, lists the failure items at the cause step for each unit. It proposes an evidence locator per element (file plus JSON path or line).
6. **Score manually.** The scorer assigns the element codes in `evidence-matrix-template.csv` and records each locator and a masked excerpt. The script's proposals are checked, never accepted unseen.
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
- **Without a second rater:** report the self-audit only, and state that no inter-rater reliability exists.

## 8. Decision rule and threshold

The rule is defined before scoring.

### 8.1 Favourable

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

- With 7–8 applicable criteria per state or database unit, 20 pp is a difference of about 1.5–2 whole criteria. An example is prior state plus database evidence: a class of information a developer would otherwise collect by hand.
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

| Date (UTC) | Item | SHA-256 | Recorded by |
|---|---|---|---|
| | experiment-2-evidence-completeness-protocol.md (rubric §4, answer key §3.2, configurations §2) | | |
| | PM-S collection | | |
| | ST-S checks and adapter | | |
| | database probe | | |

## Amendments

None yet.
