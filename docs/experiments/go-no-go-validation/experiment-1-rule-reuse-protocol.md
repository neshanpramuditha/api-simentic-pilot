# Experiment 1: rule reuse and configuration effort (protocol)

**Status: DRAFT, NOT FROZEN.**

Before the first timed trial:
1. Finish §12 (pre-trial checklist).
2. Record the SHA-256 of this file in the freeze log (§12).
3. After that, change the protocol only through dated amendments appended at the end, each with its reason.

**Classification: exploratory, not confirmatory.**
- With one implementer and two or three tasks, no statistical test is meaningful.
- No significance will be claimed.
- §9 defines a decision rule, not a hypothesis test.

## 1. Research question and scope

**RQ1.** Can the declarative semantic-rule approach be adapted to a structurally different API with less effort than an equivalent Postman scripted suite, without changes to the engine's source code?

This experiment measures **adaptation effort and reuse**. It does not measure defect detection superiority. Detection is recorded only to confirm that the two implementations are equivalent.

## 2. Hypotheses

| ID | Statement | Notes |
|---|---|---|
| H1.1 | For each task, the declarative arm's active adaptation time is at most 0.67 × the Postman arm's | Primary decision quantity (§9) |
| H1.2 | The declarative arm needs **0 engine source changes** in each task | Checked by hash |
| H1.3 | Both arms implement all applicable checks (§4), pass the clean run, and detect the same injected defects at the cause step | Equivalence precondition. Without it, H1.1 is not interpreted |
| H1.4 (secondary) | On a held-out scenario the implementer never saw, the declarative checks keep detecting the defects at least as often as the Postman checks | Measures transfer, not effort |
| H1.0 | Postman adaptation effort is comparable to or lower than declarative effort | The result to report if H1.1 fails. **The only existing timing data (AI wall-clock, Event) points this way** (see audit §4) |

## 3. The two arms

### Arm D: declarative validator

- **Engine:** frozen at the hashes in `baseline-hashes.sha256`, or at a later generic engine version frozen *before* target selection (§5.3).
- **What the implementer writes:** a rules file plus a scenario file for the target (the validator needs a scenario to run active mode).
- **Counted:** rules, scenario and any run configuration. Any engine edit is logged, counted and **flags the trial as "engine change required"**.

### Arm P: Postman

The implementer writes a Postman collection (requests, pre-request and test scripts, collection-level scripts, variables or environment).

**Allowed (Postman must not be weakened):**
- `pm.sendRequest` follow-up requests;
- shared JavaScript functions in collection-level scripts;
- collection variables;
- a transition table written as JavaScript;
- the shared database probe (§3.1);
- the Postman app, or direct editing of collection JSON/YAML.

**Generators:** a code generator (like `build-postman-collection.js`) is allowed. The implementer chooses the authoring method **before the timer starts** and records it. Generator lines count as Postman script lines.

### 3.1 Shared fixtures

Shared fixtures are prepared before any trial. They are identical for both arms and are not timed.

- The target API, its start/stop scripts and a fresh H2 file database per run.
- **Database probe** (to be built): a localhost-only HTTP helper that executes one parameterised `SELECT` against the run's H2 database (`AUTO_SERVER=TRUE`), read-only, and returns the first row as JSON.
  - It gives Postman the same *capability* the validator has through JDBC.
  - **Limitation:** it is helper code that Postman needs and the validator does not. It is built once, shared, and reported as baseline overhead. It is not counted as trial effort.
  - **Recorded difference:** the validator snapshots the row *in-process before* each request. Postman must call the probe in a pre-request script to get the same "before" row. Writing that script is part of arm P's effort.
- The target's requirement sheet (§4) and discovery notes (§6).

## 4. Adaptation tasks and required checks

### 4.1 Required checks

Each check is defined in business terms and applies where the target supports it. Both arms must implement every applicable check.

| Check | Requirement | Rule type |
|---|---|---|
| C1 | Every allowed transition exercised by the scenario succeeds and returns the target state | state transition (permitted) |
| C2 | Every forbidden transition exercised by the scenario is rejected with the API's business-rejection status and is not accepted | state transition (forbidden) |
| C3 | After a rejected transition, the persisted row is unchanged (from the database) | rejected request must not persist |
| C4 | After a successful transition, the persisted state equals the response (from the database) | response–database consistency |
| C5 | At least one conditional-field invariant (field X set iff state ∈ S) on every successful single-resource **and** list response, including the items of an envelope | response invariant |
| C6 | A timestamp-order invariant, where the API has two or more lifecycle timestamps | cross-field invariant |

How C1/C2 are implemented is the implementer's choice: a transition table or per-step expected statuses. The choice is recorded. Its consequence is measured by H1.4, not by C1/C2.

### 4.2 Tasks

The source is Event because it is the only API with *both* careful artifacts: rules v2 and a Postman collection with read-backs and invariants on envelope items.

| Task | Source | Target | Structural differences (beyond renaming) | Order of arms |
|---|---|---|---|---|
| T1 | Event | **Library** | String `REG-…` id vs UUID; `state` vs `status`; `POST …/transitions {"to"}` vs `PATCH …/status {"status"}`; 422 vs 409; envelope vs bare array; 5 vs 5 states, different branching; time order not required (C6 n/a) | **D then P** |
| T2 | Event | **Order** | As T1, plus a linear 6-state lifecycle and Long ids | **P then D** |
| T3 | Event | **Independent API X** (§5) | Unknown until selected | **D then P** |

**Acknowledged limitations:**
- T1 and T2 targets are self-authored and simpler than the source. Adapting from complex to simple is the easier direction.
- Library was designed to meet engine assumption A2.
- Engine 0.2 was extended *for* the Event API. T1 and T2 therefore test reuse of an engine already fitted to these APIs.
- **Only T3 can support any portability claim.**

## 5. API selection

### 5.1 Self-authored targets (T1, T2)

Use the existing APIs unchanged. Before each trial:
- remove `semantic-validator/rules/<target>*.json`, the target scenario and any target Postman collection from the implementer's workspace;
- keep them sealed. They serve only as a reference to check equivalence afterwards.

### 5.2 Independent target (T3)

Selection criteria are fixed before the search to avoid cherry-picking. A candidate must meet all of them:
1. It was not written in this project, nor by anyone who has seen the engine or its rules.
2. It runs locally without external services; Docker is acceptable.
3. It has an OpenAPI document, or one generated from code without hand-editing semantics.
4. It has a resource with an enforced lifecycle: at least 4 states and at least one forbidden transition that the API rejects.
5. It stores data in a relational database reachable over JDBC.
6. It is small enough to run and understand in at most 3 h.
7. It does not require authentication, or uses authentication that can be disabled by configuration (otherwise G2 applies).

**Search procedure.** Write the query, screen candidates in the order found, log each with the criterion that failed, and take the **first** candidate that meets all criteria.

**Fallback.** If none is found within 3 h, a peer writes an API from a short written functional specification:
- without seeing the engine, the rules or this repository;
- preferably on a different stack, for example Node/Express or Django with PostgreSQL.

**Defects.** Inject them as small, switchable patches covering C2/C3/C4/C5 equivalents. If possible, someone other than the implementer writes them.

**If neither option is available, do not run T3 and make no portability claim.**

### 5.3 Engine prerequisites for T3

Audit §6 lists probable blockers (G1, G2, G4, G5, G6). If you choose to address them:
- implement them as **generic** extensions *before* the T3 search starts;
- add unit tests;
- re-run the existing regression (Order, Library and Event findings must be identical);
- freeze the engine by hash and report it as "engine v0.3 (pre-registered generic changes)".

Any engine change after T3 selection is per-API effort and flags T3 as "engine change required".

## 6. Starting state, knowledge and documentation

| Item | Arm D | Arm P |
|---|---|---|
| Source artifact | `event-api.rules.v2.json` + `event-reuse/scenario.json` | `event.postman_collection.json` (or its generator, chosen before the trial) |
| Target documentation | the target's OpenAPI JSON, the database DDL (table and column names), the requirement sheet, the discovery notes | same |
| Tool documentation | the rule-format comments in the engine source (the Javadoc in `Condition`, `ResponseInvariantRule`, `StateTransitionRule` and `DatabaseConsistencyRule`); no other rules files | Postman's public documentation; no other collections |
| Not accessible during the trial | other APIs' rules or collections, `defects.md`, `*/defects/*.java`, the existing evaluation results | same |

**Discovery phase.** Discovery happens once per target, before either arm. It is timed and reported separately but excluded from arm times.
- The implementer reads the target's OpenAPI document and DDL, runs a few manual requests, and writes discovery notes: the operations, the identifiers, the state field, the rejection status and the table/columns.
- Both arms may use these notes.
- **Limitation:** this removes discovery from the comparison. It also removes the second arm's free ride on discovery.

**Requirement sheet.** For each target, the experimenter writes, before the trial:
- checks C1–C6 in business terms;
- a business-level scenario in prose, for example: "create A; approve; issue; return; attempt to issue A again (forbidden); read A; list member loans; an unknown id; an invalid status value".

The sheet names neither operationIds nor JSON paths: mapping them is part of the work.

**Defect knowledge.** The implementer (the student) can read the documentation and knows that these defect categories exist. This cannot be undone and must be disclosed. During the trial:
- defects are never enabled;
- the implementer validates only against the clean API;
- detection is measured afterwards by the experimenter (§7.4).

## 7. Timing protocol

### 7.1 Start and end

- **Start:** the implementer opens the requirement sheet *for this arm* after discovery. A timer script writes `<UTC> START <task> <arm>`.
- **End:** both conditions hold, and the script writes `<UTC> END`:
  1. the implementer declares the arm done;
  2. a clean acceptance run passes: every required check is executed at least once on the clean API with 0 failures, which the experimenter confirms from the run output.

### 7.2 What counts

- **Counted:** authoring, reading documentation, running tests, debugging and API restarts. These are real work for both arms.
- **Not counted:** pauses. The implementer logs each pause with `PAUSE` and `RESUME` lines. Active time is END − START − pauses.
- **Cap:** 120 active minutes per arm. If the cap is reached, record "incomplete" with the checks implemented so far.

### 7.3 Repairs

After END, the experimenter runs the defect modes (§7.4). If an arm misses a defect that one of its required checks should detect:
- tell the implementer only which check failed to detect;
- time one repair session separately (`REPAIR-START` / `REPAIR-END`);
- report effort **both with and without** repair time.

### 7.4 Correctness and completeness checks

The experimenter performs these after END.

1. **Equivalence review:** map each required check to the artifact that implements it (template §10.2). A check counts as implemented only if a mapped assertion or rule exists **and** was executed on the clean run.
2. **Clean runs:** 2 repeats, fresh API and database each, so 0 failures are expected.
3. **Defect runs:** for each target defect, 2 repeats, fresh API and database, switch line checked. Use the existing matrix and oracle scripts.
   - Record detection at the cause step, downstream-only detection, false-positive steps (the "affected steps" definition from `verify-*.js`) and the exit code.
4. **Held-out scenario (H1.4):** a second scenario, written by the experimenter before the trials and unseen by the implementer, in which the same defects act at different steps. Run both arms' artifacts on it.
   - Arm P's request sequence must be replaced with the held-out sequence. Swapping in a new request sequence is mechanical and is not timed.
   - Record which checks still fire.
5. **Engine hash** after the trial, compared with the frozen manifest.

### 7.5 Repeated trials and order effects

- Arm order alternates across tasks (T1 D→P, T2 P→D, T3 D→P).
- Within a task, the second arm benefits from the first: understanding of the target and debugging insight. Alternation spreads this bias across both arms, but **one implementer cannot cancel it**.
- **Strongly recommended:** a second implementer (for example a classmate) does T1 and T2 in the opposite arm order. Each implementer is a separate observation; there is no pooling across people.
- Before T1, record each implementer's prior experience: years and frequency of Postman scripting and of JSON rule or DSL use (template §10.4).

## 8. Metrics (per task, arm and implementer)

| Metric | Operational definition |
|---|---|
| Active adaptation time | Sum of START→END intervals minus pauses, in minutes. Repair time reported separately |
| Discovery time | Per target, shared by both arms |
| Rule / config lines | `git diff --no-index --numstat` from the source artifact to the final artifact, split into rules file and scenario file |
| Postman script lines | Lines in `exec` script arrays (or generator lines), added/modified/deleted, via numstat on a normalised export (one script line per line) |
| Postman request definitions | Number of requests added, removed and edited |
| Engine source changes | Files and lines differing from the frozen manifest (expected 0) |
| Helper changes | Lines in any other file the arm needed (for example a probe query whitelist or a harness edit) |
| Configuration files changed | Count of files |
| Checks implemented | Number of required checks (C1–C6) mapped and executed / applicable |
| Clean pass | Checks passing on the clean runs / implemented |
| Defects detected | At the cause step / downstream only / missed |
| False positives | Failing steps outside the oracle's affected steps, clean and defect runs |
| Repeat consistency | Identical failure keys across the 2 repeats (yes/no) |
| Transfer (H1.4) | Defects still detected on the held-out scenario / applicable |
| Debug runs | Number of tool executions during the trial, from the tool logs or the shell history |

**Lines of code are never used as the sole measure of effort.** Time is the primary effort metric; lines and counts are descriptive.

## 9. Decision rule and threshold

The rule is defined before any trial.

### 9.1 Favourable

A task is favourable only if **all** of these hold:
1. Equivalence: both arms implement all applicable checks and pass the clean runs (H1.3).
2. Both arms detect the same target defects at the cause step, or every difference is explained by a recorded difference in what was implemented.
3. Arm D's active time ≤ 0.67 × arm P's active time, *both* with and without repair time.
4. Arm D needed 0 engine source changes, and any helper change is reported.

### 9.2 Overall outcome

| Outcome | Condition |
|---|---|
| Supports a reduced-effort claim for the tested APIs | Every completed task is favourable and the direction is the same for every implementer. **T3 is required for any statement beyond the self-authored APIs** |
| Comparable | Arm D is faster but by less than 33% in at least one task, or the direction differs between tasks or implementers. Report as "no meaningful difference" |
| Against | Arm P is faster or equal in any task, or T3 needed engine changes after selection |

### 9.3 Why a ratio of 0.67 (at least a one-third reduction)

- No data exists to estimate how much timing varies within or between participants on these tasks, so a threshold based on variance cannot be justified.
- The threshold is therefore chosen on practical grounds, and is deliberately strict:
  - a one-third saving is a difference a practitioner would notice;
  - it is large enough that learning and order effects alone are unlikely to produce it *in every counterbalanced task*.
- Requiring it per task rather than on average prevents one favourable task from carrying the result.
- This is a decision rule for an exploratory study, **not** a test of significance.

## 10. Recording templates

Per-run data goes into `experiment-data-template.csv` with `experiment = E1`. The tables below are for the written record.

### 10.1 Trial log (one per task × arm × implementer)

| Field | Value |
|---|---|
| Task / arm / implementer | |
| Authoring method declared before start (app / JSON / generator) | |
| Engine hash before / after | |
| START (UTC) | |
| PAUSE / RESUME intervals | |
| END (UTC) and clean acceptance run id | |
| Active minutes | |
| REPAIR-START / REPAIR-END, reason | |
| Notes (blockers, documentation consulted, errors) | |

### 10.2 Check-equivalence mapping

| Check | Arm D: rule id + scenario steps | Arm P: test name(s) + request(s) | Executed on clean (D/P) | Implementation style (table / per-step) | Difference noted |
|---|---|---|---|---|---|
| C1 | | | | | |
| C2 | | | | | |
| C3 | | | | | |
| C4 | | | | | |
| C5 | | | | | |
| C6 | | | | | |

### 10.3 Diff summary

| Artifact | Added | Modified | Deleted | Source → target files |
|---|---|---|---|---|
| Rules file (D) | | | | |
| Scenario (D) | | | | |
| Postman scripts (P) | | | | |
| Postman requests (P) | | | | |
| Engine source | | | | |
| Helpers | | | | |

### 10.4 Implementer background (completed before T1)

| Item | Answer |
|---|---|
| Postman scripting experience (none / some / regular) | |
| JSON or DSL rule configuration experience | |
| Has read or written the engine source? | |
| Has read the target APIs' source or `defects.md`? | |

## 11. Threats specific to Experiment 1

| Threat | Mitigation in this protocol | Residual |
|---|---|---|
| One implementer, who knows the APIs and defects | Discovery notes for both arms, defects never enabled during trials, a second implementer recommended | High: disclose |
| Learning and order effects | Alternating arm order, per-task decision rule | Not removable with n = 1 |
| Self-authored, engine-aware targets | T3 independent target; T1/T2 limits stated | No portability claim without T3 |
| Postman weakened by an unequal source or no database access | Careful Event source collection, database probe, free choice of authoring method | Probe overhead reported |
| Hidden engine or helper changes | Hash check before and after; every non-artifact diff reported | — |
| Experimenter bias in the equivalence review | Mapping table published; a second person reviews it if available | Medium |
| Scenario authoring dominates the time | Scenario lines and time reported inside arm D; arm P also builds its request sequence | Both arms do comparable sequencing work |

## 12. Pre-trial checklist and freeze log

- [ ] Current state committed to a branch or archived (your decision; see audit §1)
- [ ] API jars rebuilt; hashes recorded
- [ ] Database probe built, tested on the clean API, hash recorded
- [ ] Sealed workspaces for T1 and T2 prepared (target rules, scenarios and collections removed); hashes recorded
- [ ] Requirement sheets and held-out scenarios written for T1 and T2 and hashed **before** any trial
- [ ] (T3) Engine v0.3 generic changes, if any, frozen *before* the target search
- [ ] (T3) Target selected per §5.2, with the search log saved
- [ ] Timer script tested
- [ ] SHA-256 of this protocol recorded below

| Date (UTC) | File | SHA-256 | Recorded by |
|---|---|---|---|
| | experiment-1-rule-reuse-protocol.md | | |

## Amendments

None yet.
