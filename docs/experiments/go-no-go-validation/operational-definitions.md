# Operational definitions and decision-rule clarifications (2026-10-21)

> **Revision status (2026-10-21, revision 2).** Revision 2 is appended at the end (§R0–§R13). It records the decisions approved on 2026-10-21. Everything in it is still **PROPOSED** until the revised protocols are approved and frozen.
>
> | Earlier section | Status |
> |---|---|
> | §4 | Superseded in part by R1–R3 |
> | §8 | Superseded by R4–R5 |
> | §9 rows A2–A5 | Superseded by R8–R10 |
> | §1 arithmetic | Corrected (see §1 and R11) |
> | R3.4, R5 (scope of the one-value rule), R13 (open issues) | Revision 3 (2026-10-21): resolved or clarified in **R14** |
> | R14.4 (pilot), R14.6 (equivalence review), R8 V5 | Final conditions (2026-10-21): **R15**. Open items: **R16** |
> | R6 item 3, R8 step 1, R15.2, R16 U-1/U-2/U-4 | Freeze decisions (2026-10-21): **R17** |
> | R10 steps 2–3 (Δ̄_nat), R16 U-3, R17.5 F-1/F-3 | F-1/F-3 resolutions (2026-10-21): **R18** |
> | R18.1 wording, R17.2 "Consequence", R16 U-4, `freeze-record.md` §3 step 3 | Dated wording amendments W-1 to W-4 (2026-10-21): **R19** |
> | §1, the 20 pp row's "What it means" (native score against the best *non-scripted* baseline) | Superseded by R1, R4 and R8: scripted score against the best *scripted* baseline, plus the guard |
>
> The earlier text is kept as history; superseded statements are marked, not deleted.

**Status: PROPOSED.**

These definitions become binding only when they are adopted as dated amendments in the two protocols and frozen by hash (Experiment 1 §12; Experiment 2 §11). Until then they are proposals. *(R18.1: adoption and freeze are recorded only in `freeze-record.md`; this file is not edited at freeze.)*

**What does not change.** **No threshold is changed by this document.** The 0.67 time ratio (Experiment 1 §9) and the 20 percentage points (Experiment 2 §8) are unchanged. Where this document makes a rule more precise, the original wording is quoted and the change is listed in §9.

**Labels:**

| Label | Meaning |
|---|---|
| **[V]** | Verified fact |
| **[I]** | Inference |
| **[U]** | Untested |
| **[P]** | Proposal |

## 1. Status of the thresholds: proposals, not evidence

| Threshold | Where | What it means | Rationale | What it is not |
|---|---|---|---|---|
| **0.67** | Experiment 1 §9 | Per task, arm D's active adaptation time must be at most two-thirds of arm P's, a reduction of at least one third, both with and without repair time | **[P]** No data exists on timing variance for these tasks, so a threshold based on variance cannot be computed. A one-third saving is large enough that a practitioner would notice it, and large enough that order and learning effects are unlikely to produce it *in every counterbalanced task*. Requiring it per task stops one favourable task from carrying the result | Not an established standard; not a significance level; not supported by any existing measurement. The only existing timings (AI wall-clock) show the opposite direction **[V]** (audit §4) |
| **20 pp** | Experiment 2 §8 | On the state and database units, the validator's native criterion completeness exceeds the best non-scripted baseline by at least 20 percentage points on average, and is not lower on any unit | **[P]** Such units have 7–8 applicable criteria, so 20 pp is ~~about 1.5–2 whole criteria, for example prior state plus database evidence~~ **about 1.4 criteria (ST, 7 criteria) to 1.6 criteria (DB, 8 criteria); two whole criteria would be 25–29 pp (corrected 2026-10-21, revision 2, R11)**. ~~That is a class of information a developer would otherwise collect by hand.~~ A difference of one criterion or less is within what one ambiguous judgment by a single rater could cause | Not an established standard; no external benchmark; not derived from existing data. It is a pre-registered judgement |

Neither threshold has yet been applied to any data **[V]**.

## 2. Configuration effort (Experiment 1)

| Term | Definition |
|---|---|
| **Active adaptation time** (primary) | `END − START − Σ(RESUME − PAUSE)` in minutes, from the trial log written by the timer script. Repair time is reported separately and also added in |
| START | The UTC timestamp written when the implementer opens the requirement sheet for this arm, after the discovery phase |
| END | The later of two timestamps: (a) the implementer declares the arm done; (b) the experimenter confirms a clean acceptance run, in which every required check was executed and 0 failed |
| Included | Authoring, documentation lookup, tool runs, debugging, API restarts |
| Excluded | Logged pauses, discovery (reported separately), the experimenter's verification runs after END |
| Line counts (descriptive only) | `git diff --no-index --numstat <source> <final>` on the rules file and on the scenario file separately. For Postman: every line of every `exec` array is extracted, in collection order, into a plain-text file, and numstat is run on those files. Generator lines are counted the same way if a generator was used. Request definitions are counted as requests added, removed or edited |
| Not effort | Lines of code are never used as the effort measure on their own |

## 3. Engine change versus configuration change

These classes are mutually exclusive and are checked by hash against `baseline-hashes.sha256`, or against a later frozen engine manifest.

| Class | Definition | Counted as |
|---|---|---|
| **Engine change** | Any byte difference in `semantic-validator/src/main/**`, `semantic-validator/pom.xml`, or the built jar | **Engine change.** The trial is flagged "not configuration-only" |
| Engine test change | `semantic-validator/src/test/**` | Reported. It does not by itself flag the trial, but it always comes with an engine change |
| **Runtime configuration** | Command-line options, the JDBC URL, adding an unmodified third-party JDBC driver jar to the classpath | Reported separately. It is *not* an engine change |
| **Rule configuration** | The rules file and the scenario file for the target | Configuration (the arm D artifacts) |
| Helper | Any other file the arm needed: harness scripts, probe query list, start/stop scripts, a spec-format conversion | Reported as helper changes, with lines and minutes |
| Pre-registered generic extension | An engine change frozen by hash **before** the independent-API search starts (`independent-api-selection-criteria.md` §4) | Reported as the engine version ("v0.3, pre-registered"); not counted as per-API effort |

## 4. Detection code versus evidence code (Experiment 2 native and scripted scores)

> **Revision 2:** superseded in part. The deletion test is restated and extended in R2, and the scripting policy is in R3. The "Consequence" paragraph below (a native result supports a "by default" claim as the primary reading) is **withdrawn**: the primary metric is now scripted completeness (R1).

**The problem [V].** The existing Postman collection already puts data into assertion messages, for example `JSON.stringify({ state, checkedInAt })` (Event generator lines 40–42) and `"read-back GET … vs response …"` (line 66). Rubric §4.4 does not say whether such data counts as native (`P`) or scripted (`PS`). The answer decides PM-R's native score.

**Proposed rule [P].**
- **Detection code:** code needed to decide pass or fail. This includes:
  - the assertion expression;
  - the data it reads;
  - the test or check name;
  - the tool's default failure message for that assertion (for example Chai's `expected 'CONFIRMED' to deeply equal 'CANCELLED'`).

  Evidence that appears **only** through detection code or the tool's own output counts as **`P`**.
- **Evidence code:** code whose only effect is to put more data into the report. This includes:
  - custom message arguments;
  - `console.log`;
  - extra fields on the failure;
  - a probe query whose result is shown but not asserted.

  Evidence that appears only because of evidence code counts as **`PS`**.
- **The validator:** rule files contain no evidence code, so all of its report evidence is `P`. The evidence fields are fixed by the engine source.
- **Line counting:** count lines of evidence code and lines of detection code separately per artifact (the new CSV columns `evidence_code_lines` and `detection_code_lines`).

**Consequence, stated openly [I].**
- This rule makes the native score a measure of **evidence produced without authoring it**. A framework that emits evidence by design, like the validator, is favoured by construction.
- A favourable native result therefore supports only a "by default" claim (Experiment 2 §8.2). Any claim of "more complete evidence" in general also needs the scripted comparison against PM-S.

## 5. Rule reuse (Experiment 1, descriptive)

| Term | Definition |
|---|---|
| Reused rule | A target rule object with the same `kind` and the same condition structure as a source rule, where only field values changed: operationIds, paths, states, statuses, SQL identifiers |
| Adapted rule | Same `kind`, but the condition structure changed (operators added or removed) |
| New rule | No corresponding source rule |
| Reuse ratio | Reused rules / target rules, reported together with the line numstat |
| Postman counterpart | The same classification applied to `pm.test` blocks and to helper functions |

The reuse ratio is descriptive. It is not part of any decision rule.

## 6. Defect detection and false positives

| Term | Definition |
|---|---|
| Cause step / affected steps | As computed by the run's oracle (`verify-event-run.js` or `verify-run.js`), from the recorded traffic, the database dump and the transition table parsed from the OpenAPI text **[V]** |
| **Detected at the cause step** | At least one tool failure item (FAIL finding, failed assertion or check failure) whose step index is an oracle cause step, **in every repeat** |
| Detected downstream only | Failures only at affected steps that are not cause steps |
| Missed | No failure at any affected step |
| **False positive** | Any failure item at a step **outside** the oracle's affected steps, in any mode, including every failure in a clean run |
| Extra failure at an affected step | A failure at an affected step that the oracle does not attribute to this check. Reported, **not** counted as a false positive. This keeps the existing convention (`final-project-feasibility.md` §5), and it is lenient |
| Key | `(check id, step index, item index)`. Postman uses `(tag, step)` as in `summarize-event.js`. Ids and timestamps are masked |

## 7. Repeatability

**Repeatable** means that across at least 2 repeats, each with a new API process, a new database file and the same switch:
- the sets of failure keys are identical;
- the oracle verdicts are identical;
- the final database dumps are identical after masking ids and timestamps;
- for Experiment 2, the element codes are identical.

If any of these differs, the unit is reported as non-repeatable, with the difference. It is not dropped.

## 8. Evidence completeness: clarifications

> **Revision 2:** superseded by R4 (formulas, baseline definition, repeats) and R5 (one-value rule). In particular, the "best non-scripted baseline" below is replaced by the best *scripted* baseline (R4).

| Issue | Proposed resolution [P] |
|---|---|
| EVT-DB-01 appears twice (I2a, I2b) and would count twice in the 20 pp average | Average I2a and I2b first, then average across **defects**. Report the per-unit values too |
| "Best non-scripted baseline" | Per unit, the higher native score of PM-R and ST-S. If ST-S is excluded (`schemathesis-baseline-audit.md` §8), PM-R only, and this is disclosed |
| ST-S native | Only elements in Schemathesis's own output: case details, status, response excerpt, reproduction command, seed, version, timestamps. Check-message content is `PS` |
| `downstream_only` units | Scored, with E1b absent |

## 9. Ambiguities in the existing criteria, and proposed wording

> **Revision 2:** rows A2 and A5 are superseded by R9 and R8, and rows A3 and A4 by R10. Rows A1 and A6–A9 still stand as proposals.

| # | Existing wording | Ambiguity | Proposed resolution [P] |
|---|---|---|---|
| A1 | Experiment 1 §9.1(2): "or every difference is explained by a recorded difference in what was implemented" | "Explained" leaves room for interpretation | A difference is acceptable only if the check mapping (Experiment 1 §10.2) shows that one arm failed to implement or execute the relevant check. **The task is then not equivalent and is not interpreted for H1.1.** It is not counted as favourable |
| A2 | Experiment 1 §9.2: "every completed task" | Tasks that hit the cap | Arm D incomplete → the task is unfavourable. Arm P incomplete and arm D complete → arm P's time is recorded as the cap (120 min, a lower bound), and the task is favourable only if the other conditions hold and D ≤ 0.67 × 120 |
| A3 | Assessment §8 NO-GO: "PM-R or ST-S reaches comparable completeness" | "Comparable" is undefined | "Comparable" means the Experiment 2 favourable condition (§8.1 item 5) is **not** met against that baseline |
| A4 | Assessment §8 GO-CANDIDATE versus NO-GO | Some outcomes match neither (for example +25 pp on average but lower on one unit) | Every outcome that is neither GO-CANDIDATE nor NO-GO candidate falls into CONTINUE / NARROW FURTHER, so the gate always has an answer |
| A5 | Experiment 2 §8.1 item 5, "at least … on every one of those units" | Ties | "At least" includes equality |
| A6 | Experiment 2 rubric §4.4 | Native or scripted for custom messages | §4 above |
| A7 | Experiment 2 §6 step 4, "using the existing run conventions" | The existing runner cannot run PM-R, PM-S or ST-S, and needs a v0.1 jar that is not in the repository **[V]** | A new runner file, with separate approval |
| A8 | Experiment 1 §7.4(3), "Use the existing matrix and oracle scripts" | The Library oracle has no Postman support; the Event oracle assumes the fixed 19-step sequence and identifies read-backs by position **[V]** | Extend the oracles in new files, or parameterise copies, and self-test them before any trial (separate approval) |
| A9 | Experiment 1 §8, "Debug runs: … from tool logs or shell history" | Shell history is not reliable | Count tool executions logged by the trial timer wrapper only |

---

# Revision 2 (2026-10-21): approved decisions, recorded as PROPOSED until freeze

On 2026-10-21 the repository owner approved the direction of the decisions below. **They stay proposals until the revised protocols are approved and frozen by hash** (Experiment 1 §12; Experiment 2 §11). Every threshold and outcome rule here is **proposed**: none is an externally validated standard, and none has been applied to any data.

## R0. What kind of content each part of this document is

| Label | Meaning | Where it appears |
|---|---|---|
| **[M]** | Existing measured evidence (unchanged) | §R0.1 only |
| **[P]** | Proposed rule, definition or threshold | §R1–§R11, §R14–§R19 |
| **[H]** | Hypothetical example; **not data** | §R12 only |
| Future results | — | Only in the CSV templates and new result directories, never in this document |

### R0.1 Existing measured evidence relevant here [M]

None of it has been scored under the rubric in this document.

| Evidence | Source |
|---|---|
| Report-content comparison R1–R8, validator 0.2 active against **default** Postman (PM-D), 5 Event defects: R5 response body 5/5 against 0/5; R6 prior state 3/5 against 1/5; R7 stored state 2/5 (database row) against 2/5 partial (HTTP read-back). Its known scoring weaknesses are listed in audit §5 | `event-reuse/results/diagnosis/README.md`, `scores.json` |
| Detection matrix, Event API: 48 runs, 72 tool results | `event-reuse/results/summary.md` |
| AI-assistant wall-clock timings (not human effort) | `event-reuse/results/timing.log` |

The 0.67 and 20 pp thresholds have **never** been applied to any data.

## R1. Metrics for Experiment 2 [P]

| Role | Metric | Use |
|---|---|---|
| **Primary** | Scripted criterion completeness `S_scr` (R4) | The only input to the Experiment 2 outcome (R8), together with the guard |
| Secondary | Native criterion completeness `S_nat` (R4) | Descriptive. It is also the only input to the native-margin condition of the effort-only gate route (R10). It cannot change the Experiment 2 outcome |
| Secondary | Authoring overhead | Descriptive only. **No effort threshold applies in Experiment 2.** Configuration effort is decided in Experiment 1 |

Authoring overhead is recorded per baseline configuration:
- active authoring minutes;
- `evidence_code_lines`;
- `detection_code_lines`;
- configuration or helper lines.

## R2. Evidence classification: the deletion test [P]

**The test.** For each element coded present, ask: *would this element still appear in the tool's own output if all evidence code were deleted?*
- Evidence code means: custom message arguments, `console.log` and other logging, added fields, extra follow-up requests, and probe calls whose results are displayed but not asserted.
- If the element would still appear, it is **P**. If not, it is **PS**.
- The detection code (the assertion expression, its name, the data it reads) is never deleted for this test.
- The answer is decided by inspecting the code. The code location that produces the evidence goes into `producing_code_locator` (`engine` for validator fields, `framework` for tool-native output).

| Source | Classification under the deletion test |
|---|---|
| Validator report fields | P. They are fixed by the engine, and the validator has no scripting extension point, so for the validator `S_scr = S_nat` |
| Default framework assertion text (Chai, Schemathesis check failure) | P |
| Custom assertion message (including the messages in the existing Event collection) | PS if the element appears only in the message; P if it also appears in default output |
| `console.log` or other logged blocks | PS. Whether Postman's CLI or `--output` captures console output is **unverified** |
| A follow-up request (`pm.sendRequest`) whose result is **asserted** | The request itself is detection code. Its request line is P if the tool prints it automatically. Values shown only through a message or log are PS |
| A follow-up request whose result is only **displayed** | Evidence code, so PS |
| Database-probe results in a report | Always PS for Postman and Schemathesis: removing the evidence code removes them |
| Schemathesis framework output (case, status, response excerpt, reproduction command, seed, version, timestamps) | P, **if** it appears for custom-check failures (**unverified**) |
| Harness-supplied metadata (`--env-var`) echoed by a script | PS. For the validator, E8 elements it cannot emit are **AU** |
| Harness HAR, `run.json`, API logs, database dumps | Not counted for any tool |

**E7 rule (approved).**
- An HTTP read-back may satisfy **E7a only**, and only when the report labels it as **API-observed state** (for example "read-back GET"), as opposed to stored state.
- A read-back never satisfies **E7b**, which requires database-storage provenance (a query, or a table plus key, executed against the database).

**E5b clarification (approved).**
- A concrete expected status code (for example "status is 200") satisfies E5b.
- This **does not** establish the business reason for that expectation (for example, that WAITLISTED → CONFIRMED is allowed). No rubric element scores the reason.
- Scorers record in `notes` whether a reason or an allowed set was stated. This is descriptive only and is a known limitation of the rubric.

## R3. Scripting policy for PM-S and ST-S [P]

The same policy applies to both.

1. **Generic.** Scripts and checks apply uniformly to every request or check of a kind. They must not reference defect IDs, cause steps or answer-key values.
2. **Derived.** Evidence comes only from data captured during the run. No hardcoded values.
3. **Frozen before any defect run.** Development uses the clean API plus at most the one pilot defect. Then the scripts are hashed, and they are not changed during the matrix.
4. **Cap: 90 active minutes of authoring, the same for Postman and Schemathesis.** *(Revision 3: clarified by R14.6. The cap applies to **evidence-authoring code only**.)*
   - Recorded **separately**: active minutes, `evidence_code_lines`, `detection_code_lines`, and configuration or helper lines.
   - Anything not finished at the cap is left unfinished. The resulting AM codes stand.
5. **The rubric is the evidence specification** and is given to the script author.
6. **Same access:** the read-only probe, with the fixed query list identical to the rule SQL.
7. **Who authored what** and when, and what they knew, are recorded (Postman checklist P15).

## R4. Scoring formulas [P]

Element codes: P, PS, AM, AU, NA. NA elements are removed from denominators. AM and AU score 0 and are counted separately.

```
nat(e) = 1 if P else 0
scr(e) = 1 if P or PS else 0

c_k(u)   = Σ_{e ∈ A_k(u)} x(e) / |A_k(u)|               x ∈ {nat, scr}; A_k(u) = applicable elements of criterion k
S(u)     = (1/K_u) Σ_{k applicable} c_k(u)               K_u = 6 (SCH, INV, XF), 7 (ST), 8 (DBR, DBL)
D(d,cfg) = mean of S(u) over the units of defect d       EVT-DB-01: D = (S(I2a) + S(I2b)) / 2, counted ONCE

Primary:   B(d)      = max( D_scr(d, PM-S), D_scr(d, ST-S) )       (PM-S alone if ST-S is excluded, and disclosed)
Secondary: B_nat(d)  = max over {PM-D, PM-R, PM-S, ST-S} of D_nat(d, cfg)

Δ_d  = D_scr(d, SV) − B(d)          Δ̄ = mean over T of Δ_d          Δ_min = min over T of Δ_d
T    = {EVT-ST-01, EVT-DB-01, EVT-DB-02}         (equal weight)
G    = {EVT-RI-01, EVT-CF-01}                    (guard set)
Δ_g  = D_scr(g, SV) − B(g)
Δ̄_nat = mean over T of [ D_nat(d, SV) − B_nat(d) ]
```

**Repeats.** Repeat 1 and repeat 2 are both coded. A defect in T ∪ G with any element code differing between the repeats is **non-repeatable**; that fails precondition V3 (R8).

**Categories and APIs.**
- Results are reported per category and per configuration.
- ORD-SV-01 (SCH) is a contrast case and is never in T or G.
- Secondary instances from Order or Library are reported **per API** and never pooled into T or G.

## R5. One-value rule and provenance [P]

> **Revision 3 (2026-10-21):** the scope of the one-value rule in the first paragraph below is **superseded by R14.1**: it is restricted to E4b, E6a and E7a, and E1b and E5 become statement elements. The provenance paragraph (E6b/E7b) still stands.

**Rule (approved).** The one-value rule applies to **observed data values**: state values, timestamps, field values, row column values and bodies. A single observed data value may support **at most one** element. It does **not** apply to **labels or provenance**: query text, parameter lists, table and key names, `previousSource`, field names or headings.

**How one provenance statement can support both E6b and E7b without double counting.** As read from `DatabaseConsistencyRule.java` **[V, code reading]**, a validator database FAIL contains `database: {query, params, rowBefore, rowAfter}`. Under this rule:
- **E6a** ← `rowBefore.state` (a data value);
- **E7a** ← `rowAfter.state` (a *different* data value, from a separate read);
- **E6b** and **E7b** ← the one provenance statement (`query` + `params`). This is allowed because it is a label, and because it explicitly covers **both** rows: both sit under the same `database` object for that query.

A provenance statement that covers only one of the values (for example a query shown only for the after-row) supports only the matching element.

The same treatment applies to a PM-S or ST-S probe record that shows the query and key together with the before and after rows.

**Unresolved conflict found while applying this rule.** It is not decided here; see R13, item O-1. *(Resolved in revision 3: R14.1.)*

## R6. Resolving disputed element codes [P]

1. **Disputed element:** an element whose code differs between scoring passes (first pass against self-audit or second rater), or that the scorer flags as uncertain at scoring time. The `disputed` column holds the alternative plausible code; it is empty when not disputed.
2. **If an independent second rater is available** (preferred):
   - both raters score the shared units independently;
   - disagreements are discussed with the rubric and the answer key, and a consensus code is recorded;
   - an element with no consensus stays disputed.
   - Percentage agreement per element is reported. κ is reported only for units both raters scored.
3. **Without a second rater:** the self-audit (at least 48 h later, a random 30% of units, seed recorded) is mandatory. Every disagreement becomes disputed. *(R17.1: without an independent reviewer, Experiment 2 is INCONCLUSIVE for the project gate. The self-audit and bounding analysis are still performed and reported, as descriptive results.)*
4. **Bounding analysis (robustness, V6).** Compute the outcome twice:
   - *SV-low*: every disputed SV element takes its less favourable plausible code, and every disputed baseline element its more favourable one;
   - *SV-high*: the reverse.

   If the Experiment 2 categories (R8) differ between the two computations, the outcome is **INCONCLUSIVE**.
5. **Disclosure:** report the number of disputed elements and how each was resolved. If there was no second rater, state that no inter-rater reliability exists.

## R7. Sensitivity analysis [P]

These alternative values are **proposed for reporting only, not validated standards**:
- **10 / 20 / 30 pp** for the evidence margin;
- **0.5 / 0.67 / 0.8** for the time ratio.

**Decisions use only 20 pp and 0.67.** For each alternative, the category that *would* result is reported.
- For an evidence margin θ ∈ {0.10, 0.30}: every ±0.20 in R8 steps 2–5 is replaced by ±θ, and the guard bound −0.20 is replaced by −θ.
- For a time ratio ρ ∈ {0.5, 0.8}: 0.67 in R9 step 3 is replaced by ρ.
- Nothing else changes. If the categories differ across the alternatives, the result is flagged **threshold-sensitive**. The flag never changes the decided category, but it must be stated wherever the result is cited.

## R8. Experiment 2 outcome: ordered, mutually exclusive [P]

**Preconditions:**

| ID | Precondition |
|---|---|
| V1 | Same build, scenario, switch and fresh database for every configuration |
| V2 | Rubric, answer key, policy and configurations frozen before scoring |
| V3 | Every defect in T ∪ G is repeatable (R4) |
| V4 | The double-counting rules (R4 for EVT-DB-01, R5) were applied, and the self-audit confirmed it |
| V5 | PM-S was built under R3, and ST-S is either included or its exclusion is documented (`schemathesis-baseline-audit.md` §8). *Revision 3:* each included baseline must pass the detection equivalence review (R14.6), and an ST-S exclusion must follow R14.4. *Final conditions:* the review is completed before any baseline is scored, with the reviewer-independence requirement (R15.2). Missing or unusable baseline results are handled by R15.4 |
| V1 note | *Revision 3:* V1 is assessed on run set N1 only (R14.3) |
| V7 | *Freeze decisions (R17.1):* an independent reviewer completed (a) the detection-check equivalence review of every included baseline and (b) the review of disputed evidence scoring. If no independent reviewer is available, V7 fails, so the outcome is INCONCLUSIVE: an evaluation limitation, not evidence for or against the validator |

**Category function `C(scores)`.** Steps are evaluated in order; the first match decides.

| Step | Condition | Category |
|---|---|---|
| 1 | Any of V1–V5 fails *(R17.1: also V7)* | **INCONCLUSIVE** |
| 2 | Δ̄ ≥ 0.20 **and** Δ_min ≥ 0 **and** guard passes | **FAVOURABLE** |
| 3 | Δ̄ ≥ 0.20, but not step 2 (some Δ_d < 0 or the guard fails) | **INCONCLUSIVE** (mixed) |
| 4 | Δ̄ ≤ −0.20 | **UNFAVOURABLE** |
| 5 | −0.20 < Δ̄ < 0.20 | **COMPARABLE** |

**Guard calculation.** The guard passes if and only if `min over g ∈ G of Δ_g ≥ −0.20`, using scripted scores against the same B(g) definition. In words: the validator is not more than 20 pp below the best scripted baseline on either EVT-RI-01 or EVT-CF-01.

**Final outcome:**
- If any element is disputed: compute `C` under SV-low and SV-high (R6). If the two differ, the outcome is **INCONCLUSIVE**; otherwise it is the common category.
- If nothing is disputed: the outcome is `C`.

**Exclusivity.** Steps 2–5 partition every value of (Δ̄, Δ_min, guard), so exactly one category results.

## R9. Experiment 1 outcome: ordered, mutually exclusive [P]

**Required tasks:** R = {T1, T2, T3}.

For each task t and implementer:
- `r⁻_t` = arm D minutes ÷ arm P minutes, without repair time;
- `r⁺_t` = the same ratio with repair time added;
- `r_t = max(r⁻_t, r⁺_t)`.

If arm P hits the 120-minute cap, 120 is used as a lower bound, so `r_t` is an upper bound.

| Step | Condition | Category |
|---|---|---|
| 1 | Over all implementers: arm D incomplete in any task; or any engine change in arm D (any task) or after T3 selection; or any equivalent task with r_t ≥ 1 | **UNFAVOURABLE** |
| 2 | Any task in R not run; or equivalence (H1.3) fails in any task; or, with two or more implementers, the categories from steps 3–4 computed separately per implementer differ | **INCONCLUSIVE** |
| 3 | Every task in R has r_t ≤ 0.67 | **FAVOURABLE** |
| 4 | Otherwise (every r_t < 1, at least one > 0.67) | **COMPARABLE** |

**Scope-limited result.** The same procedure applied to {T1, T2} only is reported as "Experiment 1 (T1/T2 only)". It can support **CONTINUE** at most, never a GO-CANDIDATE route.

## R10. Project gate: ordered, mutually exclusive [P]

**Principle: GO-CANDIDATE requires positive evidence on every listed condition. No outcome becomes GO, or GO-CANDIDATE, merely because no NO-GO condition was met.** GO-CANDIDATE is not a final GO.

`cycle_used` records whether the single remediation cycle has already been spent.

| Step | Condition | Gate |
|---|---|---|
| 1 | Experiment 2 = FAVOURABLE **and** Experiment 1 = FAVOURABLE (all of R, including T3) | **GO-CANDIDATE (reporting contribution)** |
| 2 | Experiment 2 = COMPARABLE **and** Δ̄_nat ≥ 0.20 *(R18.2: Δ̄_nat^low ≥ 0.20)* **and** Experiment 1 = FAVOURABLE (all of R, including the independent API T3) | **GO-CANDIDATE (effort contribution only).** The claim is limited to: *comparable evidence completeness with lower authoring effort* |
| 3 | Experiment 2 = UNFAVOURABLE; **or** Experiment 2 = COMPARABLE and Δ̄_nat < 0.20 *(R18.2: Δ̄_nat^high < 0.20; if the bounds disagree, neither step 2 nor this clause matches)*; **or** Experiment 1 = UNFAVOURABLE and Experiment 2 ≠ FAVOURABLE | **NO-GO CANDIDATE** |
| 4 | Not steps 1–3, and `cycle_used` = yes | **NO-GO CANDIDATE (not GO; unresolved after the remediation cycle)** |
| 5 | Not steps 1–3, and `cycle_used` = no | **CONTINUE**: one remediation cycle permitted |

**One remediation cycle** (approved), defined:
- There is exactly **one** cycle for the whole project gate.
- **Permitted:** fixing a specific, documented cause. Examples:
  - a failed precondition V1–V5 *(R17.1: or V7, for example by obtaining an independent reviewer)*;
  - a non-repeatable defect;
  - a required task not yet run (such as T3);
  - a baseline configuration that could not run;
  - an equivalence failure traced to an implementation omission.
- **Not permitted:** changing thresholds, the rubric, the answer key, decision rules, scripts frozen under R3, or the engine (except a pre-registered generic extension frozen before T3 selection).
- Every amendment is dated **before** any re-run. Only the affected units or tasks are re-run. All earlier results are kept and reported.

## R11. Thresholds: exact calculation and limitations [P]

**20 pp (Experiment 2).**
- *Calculation:* R4 and R8 step 2. The favourable condition needs Δ̄ ≥ 0.20 over T, Δ_min ≥ 0, and the guard.
- *Corrected arithmetic:*

| Category | 1 criterion | 1 element (2-element criterion) | 1 E3 element | 1 E8 element | 20 pp equals |
|---|---|---|---|---|---|
| ST (7 criteria) | 14.3 pp | 7.1 pp | 4.8 pp | 2.4 pp | ≈ 1.4 criteria |
| DBR / DBL (8 criteria) | 12.5 pp | 6.25 pp | 4.2 pp | 2.1 pp | ≈ 1.6 criteria |

- *Limitations:*
  - It is a judgement. There is no external benchmark, and no evidence that 20 pp matters to diagnosis.
  - T has 3 defects from one self-authored API.
  - Scores are discrete, so the threshold falls between one and two criteria and is sensitive to single judgments.
  - Equal criterion weighting is an assumption.
  - With a single rater, the self-audit is weak protection.
  - The per-defect best baseline is deliberately conservative toward the claim.

**0.67 (Experiment 1).**
- *Calculation:* R9. Per task, `r_t = max(r⁻_t, r⁺_t) ≤ 0.67` on active minutes, which is at least a one-third reduction.
- *Limitations:*
  - one or two implementers, and no estimate of variance;
  - learning and order effects, which counterbalancing only spreads across arms;
  - discovery time is excluded;
  - short tasks make the ratio sensitive to single interruptions;
  - the ratio is asymmetric (0.67 compared with its inverse, 1.5);
  - the cap makes some ratios bounds rather than values;
  - no external validation.

**Sensitivity** values to report: R7.

## R12. Worked examples: HYPOTHETICAL, NOT DATA [H]

All numbers below are invented to show how the formulas behave. They are not observations, predictions or results. Unless an example says otherwise, preconditions V1–V5 (and, after R17.1, V7) are assumed to hold and no element is disputed.

**H-A. One EVT-DB-02 unit (DBL, 8 criteria).**

| Criterion | SV codes | SV score | PM-S codes | PM-S nat | PM-S scr |
|---|---|---|---|---|---|
| E1 | P, P | 1 | P, P | 1 | 1 |
| E2 | P, P | 1 | P, P | 1 | 1 |
| E3 | P, P, P | 1 | P, P, P | 1 | 1 |
| E4 | P, P | 1 | P, PS | 0.5 | 1 |
| E5 | P, P | 1 | PS, PS | 0 | 1 |
| E6 | P, P | 1 | PS, PS | 0 | 1 |
| E7 | P, P | 1 | PS, PS | 0 | 1 |
| E8 | AU, AU, P, P, AU, P | 0.5 | P, P, PS, PS, PS, P | 0.5 | 1 |
| **S(u)** | | **7.5/8 = 0.9375** | | **4/8 = 0.50** | **8/8 = 1.00** |

Primary (scripted) Δ = −0.0625. Native Δ = +0.4375. The same codes give opposite readings, which is why the metric was fixed before scoring.

**H-B. Aggregation over T (scripted).**

| Defect | SV | B(d) | Δ_d |
|---|---|---|---|
| EVT-ST-01 | 0.857 | 0.786 | +0.071 |
| EVT-DB-01 | (0.9375 + 0.9375)/2 = 0.9375 | max(PM-S (0.875 + 0.750)/2 = 0.8125, ST-S 0.80) = 0.8125 | +0.125 |
| EVT-DB-02 | 0.9375 | 1.000 | −0.0625 |

Δ̄ = +0.044 and Δ_min = −0.0625. R8: steps 1–4 do not match, so step 5 applies: **COMPARABLE**.

At the R7 alternatives: 10 pp → COMPARABLE; 30 pp → COMPARABLE. Not threshold-sensitive.

**H-C. Guard.**
- Δ_RI = −0.05 and Δ_CF = −0.25, so min = −0.25 < −0.20 and the guard **fails**.
- With Δ̄ = +0.24 and Δ_min ≥ 0, R8 step 2 fails and step 3 applies: **INCONCLUSIVE (mixed)**.

**H-D. Disputed elements (R6).**
- Two elements are disputed.
- SV-low gives Δ̄ = 0.18, so COMPARABLE.
- SV-high gives Δ̄ = 0.22 with Δ_min ≥ 0 and the guard passing, so FAVOURABLE.
- The categories differ, so the outcome is **INCONCLUSIVE**.

**H-E. Time ratio (R9).**

| Task | D | P | Repair D | r⁻ | r⁺ | r_t |
|---|---|---|---|---|---|---|
| T1 | 48 | 80 | 15 | 0.60 | 0.79 | 0.79 |
| T2 | 55 | cap (≥ 120) | 0 | ≤ 0.46 | ≤ 0.46 | ≤ 0.46 |
| T3 | 40 | 70 | 0 | 0.57 | 0.57 | 0.57 |

- All r_t < 1 and T1 > 0.67, so the outcome is **COMPARABLE** at 0.67.
- Sensitivity: at 0.8 the result would be FAVOURABLE, so it is flagged **threshold-sensitive**.
- **Gate (R10):** suppose Experiment 2 = COMPARABLE with Δ̄_nat = 0.25.
  - Step 2 needs Experiment 1 = FAVOURABLE, so it does not match.
  - Step 3 does not match either: Experiment 2 is COMPARABLE but Δ̄_nat ≥ 0.20, and Experiment 1 is not UNFAVOURABLE.
  - So steps 4 or 5 apply: **CONTINUE** if the remediation cycle is unused, otherwise **NO-GO CANDIDATE**.

## R13. Open issues that must be decided before freezing [P]

| ID | Issue | Why it is open | Recommended resolution (not adopted) |
|---|---|---|---|
| **O-1** (**RESOLVED rev 3 → R14.1**) | The one-value rule (R5) conflicts with elements that inherently quote a data value that also supports another element | Found while applying R5 to the answer key (protocol §3.2). E5a "actual" for DBR (stored CANCELLED) is the value that supports E7a. E5b "expected" for DBR (unchanged CHECKED_IN) is the E6a value. For DBL, E5b (response CANCELLED) is part of E4b. For INV and XF, E5a (`checkedInAt` null) is part of E4b. And E1b's request line also supports E3a/E3b. **Applied literally at the value level, these elements become unsatisfiable**; applied per occurrence, they reward restating values | Restrict the one-value rule to the *state-data* elements {E4b, E6a, E7a}: one occurrence supports at most one of them. Treat E1b (localisation) and E5a/E5b (an explicit statement juxtaposing actual and expected) as *statement* elements: satisfied by a locator or an explicit violation statement, which may quote values that also appear elsewhere. Values found **only** in a body or row never satisfy E5 |
| O-2 (**RESOLVED rev 3 → R14.2**: not adopted) | Reporting-only route | Under R10, Experiment 2 FAVOURABLE with Experiment 1 UNFAVOURABLE or COMPARABLE gives CONTINUE, and after the cycle NO-GO CANDIDATE. That is the case even when the reporting advantage is real | Decide whether a "GO-CANDIDATE (reporting contribution, no reuse or effort claim)" route should exist. It is **not** added here |
| O-3 (**RESOLVED rev 3 → R14.3**) | Reuse versus re-run of the stored SV and PM-D artifacts | A rebuilt jar may differ; the hash identity of a rebuild is untested | Re-run on rebuilt jars, and use the stored artifacts only if the results are identical |
| O-4 (**RESOLVED rev 3 → R14.4**) | ST-S exclusion | If ST-S is excluded, B(d) rests on PM-S alone | Accept, with disclosure (V5); no further change proposed |
| O-5 (**RESOLVED rev 3 → R14.5**) | Validator AU on E8 | It cannot be fixed without an engine change, which is not allowed before scoring | Score as AU. Any later engine version is a separate, labelled configuration |
| O-6 (**RESOLVED rev 3 → R14.6**) | What the 90-minute cap (R3.4) covers | Building ST-S needs detection checks plus a scenario adapter, estimated at 7–11 h (assessment §3, W4). PM-S needs detection changes P7–P11 (transition table, probe queries before and after). If the cap covers detection code and infrastructure, ST-S is infeasible by construction and PM-S may be truncated. If it covers only evidence code, the cap is equal across tools, but detection-side effort goes uncapped | Cap **evidence code only**, with equal 90 minutes for PM-S and ST-S. Record detection-code and infrastructure effort separately and uncapped (descriptive), and build both before the evidence-scripting clock starts. **Needs your decision** |

---

# Revision 3 (2026-10-21): resolutions of O-1 to O-6, recorded as PROPOSED until freeze

On 2026-10-21 the repository owner decided O-1 to O-6 as recorded below. The rules here remain **proposed** until the revised protocols are approved and frozen by hash. **No threshold changes.** The 20 pp margin, the −20 pp guard bound and the 0.67 ratio, together with the sensitivity values (10/30 pp; 0.5/0.8), all stay **judgement-based proposals, not externally validated standards** (R7, R11).

## R14.1 O-1: scope of the one-value rule (adopted)

**Element types:**

| Type | Elements | Rule |
|---|---|---|
| **State-data elements** | E4b (response body), E6a (prior state value), E7a (persisted state after the request) | **One-value rule:** a single occurrence of an observed data value in the tool's output may support **at most one** of E4b, E6a and E7a |
| **Statement elements** | E1b (localisation), E5a (actual value statement), E5b (expected value statement) | Assessed **separately** from the state-data elements. They may quote values that also appear elsewhere |
| **Other elements** | E1a, E2a, E2b, E3a–E3c, E6b, E7b, E8a–E8f | Not subject to the one-value rule. E6b and E7b keep the provenance rule in R5 |

**Definitions of the statement elements:**
- **E1b** is satisfied by a locator in the failure item: step index, request name, exchange index, or a request line unique to the cause step. It may share content with E3a/E3b.
- **E5a** is satisfied by an **explicit statement** in a failure item that names the actual value(s) violating the property. A value that appears **only** inside a body (E4b) or a row or read-back (E6a/E7a), with no such statement, does **not** satisfy E5a.
- **E5b** is satisfied by an explicit, **concrete** statement of the expected value or allowed set. A concrete expected status code counts (R2), but it does not establish the business reason. Boolean-only output (`expected false to equal true`) does not count.

**Rationale.**
- The value-level rule made E5 and E1b impossible to satisfy in several categories (R13, O-1).
- Under this resolution, a statement that juxtaposes actual and expected is treated as diagnostic content in its own right, not as a repeat of the body.
- The verbosity protection stays where it matters: one body or row occurrence cannot earn more than one of E4b, E6a and E7a.
- **Residual effect, disclosed:** E5 credits a restatement of values. This is the same for every tool, and every tool can satisfy it (by default or with permitted scripts).

**Satisfiability check.** This is a design-level argument [I/P], not tested on any tool. Every applicable element has at least one admissible evidence form that does not conflict with the one-value rule:

| Category | E4b | E6a | E7a | E5a / E5b | Distinct occurrences? |
|---|---|---|---|---|---|
| SCH, INV, XF | response body | NA | NA | statement naming the violating field value and the expected type, value or relation | Only one state-data element applies |
| ST | response body (422 or 200) | read-back body before the request, an earlier observed response, or a database row before the request | NA | statement "allowed X → Y rejected with 422" or "expected 200" | E4b and E6a come from different exchanges |
| DBR | response body (422) | database row before the request, or a labelled read-back before it | database row after the request, or a labelled read-back after it | statement "rejected but row changed: before X, after Y" / "row unchanged expected" | three separate reads |
| DBL | response body (200) | database row or read-back before the request | database row or read-back after the request | statement "persisted X ≠ response Y" | three separate reads |

All other elements (E1a, E2, E3, E6b, E7b, E8) are not limited by the rule. Whether a *particular* tool can produce them is a property of that tool (AM/AU), not a feature of the rubric that makes them impossible.

## R14.2 O-2: no reporting-only GO route (decision: not adopted)

- R10 is unchanged, and **has no reporting-only GO-CANDIDATE route.**
- **Experiment 2 alone cannot produce GO or GO-CANDIDATE.** Both GO-CANDIDATE steps in R10 (steps 1 and 2) require Experiment 1 = FAVOURABLE on T1, T2 and T3. A FAVOURABLE Experiment 2 combined with any Experiment 1 result other than FAVOURABLE matches neither step 1 nor step 2. It also cannot match step 3, whose clauses all need Experiment 2 to be UNFAVOURABLE, COMPARABLE or not FAVOURABLE. So it reaches step 5 (CONTINUE) before the remediation cycle and step 4 (NO-GO CANDIDATE, not GO) after it.
- **Future option, not part of the current scope:** a separate reporting-only project claim may be considered **only** if all of these happen:
  1. the project scope and the gate criteria are explicitly revised and approved;
  2. the revision is dated and frozen **before** any further evaluation;
  3. the revised claim is described as a different contribution.

  **The current contribution is not redefined** by this note. Results obtained under the current gate may not later be relabelled under a revised gate.

## R14.3 O-3: historical versus new evidence (adopted)

| Run set | Content | Rules |
|---|---|---|
| **H (historical)** | Everything already stored: `event-reuse/results/`, `library-reuse/results/`, `evaluation/results/`, and the per-defect directories | **Read-only; preserved unchanged.** It is used only (a) as historical context and (b) to compare reproducibility against N. Never used in the Experiment 2 decision |
| **N1 (new)** | A clean, reproducible run of **every** configuration in the decision matrix: SV and PM-D **re-run** together with PM-R, PM-S and ST-S, on the **same rebuilt jars**, in one run set | Written to a **new** directory, proposed as `docs/experiments/go-no-go-validation/results/run-set-N1/`. Runners and extractors refuse to write if it already exists. Only N1 enters R4/R8 |

**Why SV and PM-D must be re-run.** Precondition V1 requires the same build for every configuration. Historical artifacts come from jars whose rebuild identity is untested (audit addendum A4). Mixing sets would break V1.

**Required metadata per run in N1** (`run.json`):
- run-set ID, configuration ID, repeat;
- start and end times (UTC);
- `git rev-parse HEAD` and `git status --porcelain` summary;
- tool name and version (`postman --version`, `schemathesis --version`, validator jar hash);
- SHA-256 of the engine jar, the API jar, the rules file, the scenario, the collection or checks and adapter, and the probe and its query list;
- defect switch and the `DEFECT` line check result;
- the fresh-database path;
- the oracle verdict.

**Run-set manifest:** the list of runs plus the hashes above, written once at the start and once at the end of the run set.

**H against N comparison** (descriptive): for SV and PM-D, report whether the N1 failure keys and oracle verdicts match H, after masking. Any difference is reported. H is never altered.

**Nothing has been run** [V].

## R14.4 O-4: Schemathesis feasibility pilot (adopted)

- Schemathesis stays a **planned** baseline (ST-S).
- **Bounded feasibility pilot**, before the main matrix:
  - **Time box: 6 active hours.** This is a proposed judgement. It is set *below* the W4 estimate for the full build (7–11 h), because the pilot only tests feasibility and does not produce the complete ST-S configuration.
  - The time box covers environment setup, verifying `schemathesis-baseline-audit.md` §7 items 1–4, and a minimal adapter with one detection check, on the clean API plus one defect.
  - The pilot outputs are kept and **never scored**.
- **Pass criterion:** items 1–4 are verified, and the minimal adapter reproduces the scenario requests in order, which the oracle traffic check confirms.
- **If the pilot fails**, or no fair baseline can be built:
  - record the specific blocker(s), every attempt (commands, versions, outcome) and the time spent;
  - B(d) = PM-S only (R4), and this is disclosed;
  - every claim is limited to "compared with Postman"; Schemathesis is reported as **"not evaluated"**;
  - **the exclusion is not evidence that the validator is better than Schemathesis**, and must not be presented as such.
- A documented exclusion satisfies V5 (R8). An undocumented exclusion does not.

## R14.5 O-5: validator traceability (adopted)

- E8a–E8f stay **applicable and visible** for the validator in every category (§4.3 of the protocol); they are counted in the denominators.
- **AU** is assigned only under the definition in protocol §4.4 and R2: the element cannot be produced by any means allowed for that tool under this protocol. The scorer records the reason per element.
- For the validator, allowed means are limited to the rules file, the scenario and runtime options. No script hook exists [V, code reading of `Report.java`].
- Elements the validator does emit (for example the tool string and the rule-set name) are coded on their own merits. E8d needs an exact version label or hash (protocol §4.2).
- **No report fields are added, and the engine is not modified at this stage.** A later engine version would be scored as a separate, labelled configuration and never replaces N1 results.

## R14.6 O-6: what the cap covers (adopted)

**The 90-active-minute cap (R3.4) applies to evidence-authoring code only, equally for PM-S and ST-S.**

| Effort class | Capped? | Recorded as |
|---|---|---|
| Evidence-authoring code: custom messages, logs, displayed-only follow-ups or probe calls, added fields | **Yes, 90 active minutes per baseline** | `overhead_minutes`, `evidence_code_lines` |
| Detection-check implementation (PM-S: checklist P4, P7–P11; ST-S: checks in `schemathesis-baseline-audit.md` §5) | No | `detection_code_lines`, with active minutes in `notes` |
| Setup and environment preparation (venv, installation, probe wiring) | No | `notes`, with minutes |
| Adapter development (ST-S scenario adapter) | No | `helper_lines_changed`, with minutes in `notes` |

**Order.** Detection checks and infrastructure are completed and pass an **equivalence review** against the validator rules (checks C1–C6 mapped, executed on clean, 0 false positives) **before** the evidence-authoring clock starts.

**No unfairly weaker baseline:**
- if a baseline's detection configuration cannot pass the equivalence review, that baseline fails **V5** and the Experiment 2 outcome is **INCONCLUSIVE**; a weakened baseline is **not** scored instead;
- for ST-S only, R14.4 applies first (pilot failure means a documented exclusion).

## R14.7 Consistency confirmations after revision 3

These were checked by reading the rules, not by running anything.

1. **The Experiment 2 categories are mutually exclusive.** R8 is evaluated in order with first-match semantics. Step 1 handles failed preconditions. Steps 2–5 partition (Δ̄, Δ_min, guard): step 2 and step 3 need Δ̄ ≥ 0.20 and are complementary on (Δ_min ≥ 0 ∧ guard); step 4 needs Δ̄ ≤ −0.20; step 5 is −0.20 < Δ̄ < 0.20. The V6 bounding step maps any disagreement to INCONCLUSIVE.
2. **The project gate is mutually exclusive.** R10 is ordered with first match. Steps 1 and 2 are disjoint, because Experiment 2 is FAVOURABLE in one and COMPARABLE in the other. Steps 4 and 5 are complementary on `cycle_used`.
3. **Experiment 2 alone cannot produce GO** (R14.2).
4. **Inputs to V1 and V5 are now defined:** V1 uses run set N1 only (R14.3). V5 covers the PM-S equivalence review (R14.6) and the ST-S pilot or documented exclusion (R14.4).
5. **Superseded material is kept and marked:** §1 (20 pp row), §4, §8, §9 A2–A5, R3.4 (clarified), R5 scope, R13 rows.

---

# Revision 3, final conditions (2026-10-21): PROPOSED, NOT FROZEN

On 2026-10-21 the repository owner approved Revision 3 subject to the conditions below. **The protocols are not frozen**: freezing needs separate approval of the proposed freeze manifest.

## R15.1 Schemathesis pilot time box (approved)

- The **6-active-hour** time box (R14.4) is approved as a **judgement-based** bound. It is not a validated standard.
- **Stopping rule:** if the R14.4 pass criterion is not met when 6 active hours are reached, the pilot **stops** and is not extended.
  - Record the blocker(s) and every attempt: commands, versions, outcome and minutes.
  - Schemathesis is reported as **"not evaluated"**.
- This outcome is **not** evidence that the validator is better than Schemathesis (R15.4).

## R15.2 Detection-check equivalence review before scoring (approved)

**Scope.** This applies to the Experiment 2 baselines that enter the decision (PM-S, and ST-S if the pilot passes): their detection checks must be equivalent to the validator rules. The review uses checks C1–C6 (Experiment 1 §4.1), mapped to EVT rules v2, plus `postman-baseline-checklist.md` items P4 and P7–P11, or `schemathesis-baseline-audit.md` §5.

**Timing.** The review is completed and recorded **before any baseline unit is scored**.

**Pass criterion.** All of these hold:
- every applicable check is mapped;
- every mapped check is executed on the clean run;
- there are 0 false positives on clean.

**Reviewer:**
- **Preferred:** an independent second reviewer (not the author of the baseline scripts or checks, and not the scorer) applies the pass criterion and signs the record.
- **If no independent reviewer is available:**
  - the limitation is documented;
  - the author's self-review is recorded **as a self-review**;
  - it is **not** treated as equivalent to independent verification.

**Effect on the outcome.** Until the equivalence requirement is satisfied, the baseline fails V5, so **the Experiment 2 outcome stays INCONCLUSIVE** (R8 step 1).

**Record per baseline:**
- reviewer and role;
- independence status (`independent` or `self-review`);
- date (UTC);
- the hashes of the artifacts reviewed;
- the mapping table;
- the clean-run ID;
- the result.

> *Freeze decisions: the two paragraphs below are **superseded by R17.1**. An independent review is required, and without one the outcome is INCONCLUSIVE for the gate. Kept as history.*

**Open (U-1, §R16).** Whether a documented self-review can *ever* satisfy the requirement is not decided. The approved condition says self-review alone is "not equivalent to independent verification", and that the outcome stays INCONCLUSIVE "until the equivalence requirement is satisfied". **Interim reading, pending your confirmation:** the requirement is satisfied **only** by an independent review. A self-review leaves V5 failed.

**Consequence of the interim reading, stated openly.** If no independent reviewer exists, Experiment 2 cannot leave INCONCLUSIVE. The project gate then reaches at most CONTINUE (R10 step 5), and NO-GO CANDIDATE after the remediation cycle (step 4).

## R15.3 Recording detection-check effort (approved)

- Detection-check effort goes in the `notes` field of `experiment-data-template.csv`, using the form `DETECTION: <active minutes> min — <concise description of the work>`.
- **No CSV column is added at this stage.**
- Setup, environment and adapter effort use the same form, with the prefixes `SETUP:` and `ADAPTER:`.

**Consistency with the README data dictionary.**
- Per R14.6, `overhead_minutes` holds **evidence-authoring minutes only** (capped at 90).
- `overhead_script_lines` keeps its README meaning: all script lines added relative to PM-D, both detection and evidence.
- The README's earlier description of `overhead_minutes` ("extra baseline work … starting from PM-D") is superseded for `overhead_minutes` only.

## R15.4 Missing or unusable Schemathesis results (proposed, follows from R14.4 and R15.1)

- **Missing, unusable or excluded ST-S results are never imputed, and never scored as 0, in B(d), B(g) or B_nat(d).** The baseline maximum is taken only over baselines with valid results.
- If ST-S is included but has a missing or unusable unit for any defect in T ∪ G (for example a failed run, broken output, or a non-repeatable result), there are only two possibilities:
  - **(a)** ST-S is excluded for the whole matrix by the R14.4 documentation procedure; or
  - **(b)** if the cause is not documented, V5 fails and the outcome is INCONCLUSIVE.
- In every case, Schemathesis is reported as "not evaluated" or partially evaluated. **No validator advantage can be inferred from an absent or unusable Schemathesis result.**
- The same rule applies to any missing PM-S result: it is a V5 failure, never an imputed score.

## R15.5 Final consistency confirmations

These were checked by reading the rules; nothing was executed.

1. **Experiment 2 alone cannot produce GO or GO-CANDIDATE.** Both GO-CANDIDATE steps in R10 require Experiment 1 = FAVOURABLE on T1, T2 and T3 (R14.2). **No rule in these documents produces a final GO.** GO-CANDIDATE is the strongest outcome, and it needs every listed positive condition.
2. **The Experiment 2 categories are mutually exclusive** (R8, R14.7 item 1). They are also total: every input reaches exactly one category.
3. **The Experiment 1 categories are mutually exclusive** (R9). After steps 1 and 2 fail, every task is equivalent, run and has r_t < 1, so steps 3 and 4 are complementary.
4. **The project gate is mutually exclusive and cannot reach GO-CANDIDATE without its positive conditions** (R10). Experiment 2 or Experiment 1 being INCONCLUSIVE can lead only to step 3, 4 or 5.
5. **A missing or unusable Schemathesis result cannot count as a validator advantage** (R15.4, R14.4).

## R16. Remaining open items after the final conditions (not resolved here)

| ID | Item | Interim handling |
|---|---|---|
| U-1 (**RESOLVED → R17.1**) | Can a documented self-review satisfy the equivalence requirement (R15.2)? | Interim: no. The outcome stays INCONCLUSIVE without an independent reviewer. **Needs explicit confirmation** |
| U-2 (**RESOLVED → R17.1**) | Does the independent-review condition also apply to the Experiment 1 equivalence review (Experiment 1 §7.4 item 1, arm D against arm P)? | Not extended. Experiment 1 keeps "a second person reviews it if available" (Experiment 1 §11). **Needs a decision** |
| U-3 (**RESOLVED → R18.2**) | Which disputed-code bound is used for Δ̄_nat in gate step 2? R6 bounding is defined only for the R8 category | Not defined. A conservative choice would be the SV-low bound. **Needs a decision** |
| U-4 (**RESOLVED → R17.2**) | Hash representation for the freeze. Files are LF in the working tree, and Git reports that they will be converted to CRLF on checkout *(W-4, R19, proposed: superseded as a description of the current state; see R19)* | The proposed manifest records both the working-tree SHA-256 and the Git blob id. **Needs a decision on which one is authoritative** |

---

# Freeze decisions (2026-10-21): PROPOSED, NOT FROZEN

On 2026-10-21 the repository owner decided U-1, U-2 and U-4 as recorded below. **These remain proposed until the freeze is explicitly approved.** No threshold changes.

## R17.1 Independent review (U-1, U-2)

### Experiment 2: required for a valid gate outcome

- An **independent reviewer** is someone who:
  - did not author the baseline scripts or checks under review;
  - did not do the first scoring pass;
  - is not the developer of the validator or of its rules (the project author).

  One independent person may fill both roles below.
- **Precondition V7** (added to R8 step 1). Both of these must be completed by an independent reviewer and recorded:
  - **(a)** the detection-check equivalence review of every included baseline (R15.2);
  - **(b)** the review of **disputed evidence scoring**. The reviewer independently scores at least the R6 sample (≥ 8 units covering every configuration and the ST, DBR, DBL and INV categories), and every disputed element is resolved by consensus. Elements without consensus stay disputed and go into the V6 bounding analysis.
- **If no independent reviewer is available:**
  - V7 fails, so **Experiment 2 is INCONCLUSIVE for the project gate**;
  - the self-audit, the bounding analysis and all scores are still computed and reported, as **descriptive results only**;
  - this is documented as an **evaluation limitation**;
  - it is **not** evidence of validator failure, nor of validator superiority, and must not be presented as either.
- This supersedes the interim reading in R15.2 (U-1) and the "without a second rater" path in R6 item 3, for the purpose of the gate.
- **Gate consequence:** without an independent reviewer, R10 can reach at most CONTINUE (step 5), and NO-GO CANDIDATE (not GO) after the remediation cycle (step 4).

### Experiment 1: fallback kept, labelled as weaker evidence

- The equivalence review (Experiment 1 §7.4 item 1) uses an independent reviewer **where feasible**.
- If that is not feasible, the existing fallback (review by the experimenter) is **kept**, under these conditions:
  - the review record states `self-review`;
  - the result is labelled **weaker evidence** wherever it is reported;
  - the limitation is recorded in the trial log (Experiment 1 §10.1 "Notes") and in the write-up.
- The Experiment 1 outcome rules (R9) are unchanged. A self-reviewed Experiment 1 can still be FAVOURABLE, but every statement of it must carry the weaker-evidence label.

## R17.2 Hash representation (U-4)

| Identifier | Meaning |
|---|---|
| **Git blob id** (`git hash-object`) | **Authoritative identifier of the version-controlled content of each frozen file** |
| **Working-tree SHA-256** | Integrity check of the **actual bytes** used in the current execution environment |

- Both are kept in the freeze manifest.
- **Fresh-checkout line-ending behaviour has not been tested.** `core.autocrlf=true` is set, and no `.gitattributes` rule covers these files. A fresh checkout may write different bytes (CRLF), giving a different working-tree SHA-256 with the same blob id. This is an inference, not a test result.
- **The 45/45 baseline verification applies only to the present working copy.** `baseline-hashes.sha256` records working-tree SHA-256 values only. It is **not** modified, and no blob ids are added to it.
- **Not changed** to resolve this: `.gitattributes`, the Git configuration, and the frozen baseline.
- **Consequence:** a blob id identifies version-controlled content only once that content is **committed**. Most of the documents to be frozen are uncommitted working-tree changes (see the freeze blockers in R17.5). *(W-3, R19, proposed: superseded. The documentation is committed in `560d775` and merged to `master` by `4f19c98`.)*

## R17.3 Six-hour Schemathesis pilot: limitations (kept as approved)

The 6-active-hour time box (R14.4, R15.1) is kept. Its limitations are stated openly:
- It is a **judgement**, not derived from data. It is set below the full-build estimate (7–11 h, itself an estimate).
- A baseline that would be feasible with more time can be **excluded by the time box**. That would narrow the evaluation to Postman only. It would **not** create a validator advantage (R15.4).
- Active time is **self-recorded** by the person running the pilot. Interruptions and network or installation delays (PyPI access is unverified) count against the box.
- The pilot uses only the clean API plus one defect, so passing it does not prove the full matrix will run.
- If the person running the pilot also built the validator rules, knowledge bias can affect how fast they work. It is disclosed, not corrected.

## R17.4 AU classification: element-level rule with a recorded reason

- **AU** ("absent, unavailable") may be assigned only under protocol §4.4 and R2: the element cannot be produced by **any means allowed for that tool** under this protocol.
- It is assigned **per element and per unit**, never as a blanket per-tool decision.
- **Every AU needs a recorded reason** in the `notes` field of `evidence-matrix-template.csv`, in the form `AU: <element> — <why no allowed means exists> — <reference>`:
  - for the validator, the reference is to the code or report format, for example `Report.java` writing no timestamp field;
  - for a baseline, the reference is to the documented pilot attempt, or to tool documentation showing the absence.
- **An AU without a recorded reason is invalid and is recoded AM.** Both score 0, but they are reported separately.
- For a baseline, AU additionally needs a **documented attempt** during the pilot. Evidence that permitted scripting could produce is never AU.

## R17.5 Remaining blockers to freezing

| ID | Blocker | Status |
|---|---|---|
| F-1 (**RESOLVED → R18.1**) | **Freeze mechanics are self-referential.** The protocols say to record each file's SHA-256 in its own freeze log (Experiment 1 §12, Experiment 2 §11), and to mark amendments as adopted inside the file. Either edit changes the file's hash and blob id, which invalidates the manifest | **Needs a decision.** Recommended, not adopted: record the freeze, including the list of adopted and withdrawn amendments, in a **separate freeze-record file** that references the blob ids, so the protocol files are left byte-identical |
| F-2 | **The files to be frozen are uncommitted.** Blob ids are authoritative only for version-controlled content (R17.2) | **Needs your action**: commit before or at freeze (not done here; no commits are made by this session) |
| F-3 (**RESOLVED → R18.2**) | **U-3**: which disputed-code bound applies to Δ̄_nat in gate step 2 | Open (R16) |
| F-4 | Explicit approval of the revised proposed manifest | Pending |
| F-5 | The supporting artifacts named in the freeze logs (PM-S collection, ST-S checks and adapter, database probe) do not exist yet | Not a blocker for freezing the protocols. These are frozen separately when built, before any run |
| F-6 | Independent reviewer availability | Not a blocker for freezing. Without one, Experiment 2 is INCONCLUSIVE for the gate (R17.1) |

*(R18: F-1 and F-3 are resolved in R18.1 and R18.2.)*

---

# F-1 and F-3 resolutions (2026-10-21): PROPOSED, NOT FROZEN

On 2026-10-21 the repository owner decided F-1 and F-3 as recorded below. **These remain proposed until the freeze procedure is completed.**

## R18.1 F-1: separate freeze record

**Where freeze status lives.**
- The freeze is recorded **only** in `freeze-record.md`, in this directory.
- That file holds, for each frozen file: the authoritative **Git blob id**, the **working-tree SHA-256** integrity value, and the verification result.
- It also holds: the approval and adoption status of every amendment, the freeze date (UTC), the version and blob id of the approved freeze manifest, the commit that contains the frozen content, and the approver.

**No file records its own final hash.**
- A frozen file never contains its own hash, and is not edited at freeze.
- `freeze-record.md` does not list itself.
- `proposed-freeze-manifest.md` does not list itself, and does not list `freeze-record.md`, which changes when the freeze is completed.

**The protocol files are not edited at freeze**, so they stay byte-identical to the approved manifest. *(W-1, R19, proposed: read "content-identical to the approved manifest by authoritative Git blob id". Working-tree bytes may differ between environments, for example by line endings.)* In particular:
- their status lines keep reading **"DRAFT, NOT FROZEN"**;
- their in-file freeze logs (Experiment 1 §12, Experiment 2 §11) stay empty;
- their amendment tables keep reading **"NOT ADOPTED"**.

**After the freeze procedure is completed**, the **authoritative** status of every listed file and amendment is whatever `freeze-record.md` states. The in-file "DRAFT, NOT FROZEN" and "NOT ADOPTED" labels are then historical. The protocols say this explicitly (amendments E1-A13 and E2-A31), so it cannot be misread.

**Until the procedure is completed:**
- `freeze-record.md` reads **NOT FROZEN** and contains no freeze entries;
- the protocols are **DRAFT, NOT FROZEN** in every sense.

**Freeze procedure.** Each step needs your explicit approval at the time.
1. Approve the proposed manifest version.
2. Commit the documentation.
3. Verify that `git ls-tree` blob ids equal the manifest's.
4. Fill in `freeze-record.md`.
5. Commit `freeze-record.md`.

## R18.2 F-3: native-margin bound for the effort-only GO route

**Definitions.** Δ̄_nat is computed twice, using the R6 plausible-code bounds applied to **native** element codes:

| Bound | How it is computed |
|---|---|
| Δ̄_nat^low (least favourable to the validator) | Every disputed SV element takes its less favourable plausible native code, and every disputed baseline element its more favourable one (all baseline configurations in B_nat) |
| Δ̄_nat^high | The reverse |

- With no disputed elements, Δ̄_nat^low = Δ̄_nat^high.
- By construction, Δ̄_nat^low ≤ Δ̄_nat^high.

**Gate conditions, replacing the Δ̄_nat parts of R10 steps 2 and 3:**

| R10 step | Condition on the native margin | Other conditions (unchanged) |
|---|---|---|
| 2: GO-CANDIDATE (effort only) | **Δ̄_nat^low ≥ 0.20**, i.e. it passes under the least favourable plausible bound | Experiment 2 = COMPARABLE (already robust under V6 bounding); Experiment 1 = FAVOURABLE on T1, T2 **and** T3; all R8 preconditions including V7 |
| 3: NO-GO CANDIDATE, second clause | **Δ̄_nat^high < 0.20**, i.e. it fails under every plausible bound | Experiment 2 = COMPARABLE |
| Bounds disagree (Δ̄_nat^low < 0.20 ≤ Δ̄_nat^high) | The native-margin result is **INCONCLUSIVE** | Neither step 2 nor step 3's second clause matches. The gate continues to step 3's remaining clauses, then to step 4 (NO-GO CANDIDATE, not GO, after the cycle) or step 5 (CONTINUE) |

**Properties** (checked by reading):
- Steps 2 and 3's second clause are disjoint, because low ≤ high.
- The bound-disagreement case can never reach GO-CANDIDATE.
- **This rule creates no reporting-only GO route.** Step 2 still requires Experiment 1 = FAVOURABLE including T3, and the claim stays limited to "comparable evidence completeness with lower authoring effort".
- **It does not change the Experiment 2 outcome (R8).** The native margin is used only at the gate.

**Sensitivity** (descriptive, extending R7 to this margin; disclosed as an addition):
- Δ̄_nat^low and Δ̄_nat^high are also reported at θ ∈ {0.10, 0.30}.
- This never changes the gate. Like every other threshold here, 0.20 is a judgement-based proposal, not a validated standard.

**Supersedes:** the Δ̄_nat parts of R10 steps 2 and 3 (pointers added there), and R16 U-3.

---

# R19. Dated wording amendments (2026-10-21): PROPOSED, NOT ADOPTED, NOT FROZEN

The repository owner requested these four wording corrections on 2026-10-21. **They change wording and recorded facts only.** No threshold, outcome category, precondition or gate step changes. The original text is kept, with pointers added. Adoption status is recorded in `freeze-record.md` §6 (proposed disposition: adopt; the "Adopted at freeze?" column is still empty).

| ID | Location | Original wording (kept) | Amended wording (proposed) |
|---|---|---|---|
| **W-1** | R18.1, "The protocol files are not edited at freeze" | "…so they stay byte-identical to the approved manifest." | "…so they stay **content-identical to the approved manifest, as identified by the authoritative Git blob id**. Their working-tree bytes need not be identical in every environment: line-ending conversion (for example `core.autocrlf=true`) can change the bytes, and so the working-tree SHA-256, without changing the blob id." |
| **W-2** | `freeze-record.md` §3 step 3 | "Verify (§4) that, for every listed file: the `git ls-tree HEAD` blob id equals the manifest's blob id; the working-tree SHA-256 equals the manifest's value." | **3a:** a blob-id mismatch **stops the freeze**; it cannot be resolved under the same manifest revision. **3b:** a working-tree SHA-256 mismatch is **investigated with executed commands recorded in §4**. It counts as resolved, and so as a passed check for step 5, **only if** the blob id passed 3a, `git diff --quiet HEAD -- <file>` exits with 0, **and** the SHA-256 of the current bytes with CRLF replaced by LF equals the SHA-256 of `git cat-file blob <blob id>`. Any other cause, or an unexplained mismatch, is a **failed** check: stop, and nothing is frozen. *(Full text in `freeze-record.md` §3.)* |
| **W-3** | R17.2, "Consequence" bullet | "Most of the documents to be frozen are uncommitted working-tree changes (see the freeze blockers in R17.5)." | "The documentation to be frozen **is committed** in `560d775` ('docs: go/no-go validation protocols and proposed freeze manifest (not frozen)') and **merged** to `master` by `4f19c98` ('Merge pull request #1 …'). Any later edit to a listed file must be committed again before its blob id can be authoritative for a freeze." |
| **W-4** | R16, row U-4 | "Files are LF in the working tree, and Git reports that they will be converted to CRLF on checkout." | "**Current working copy (observed by executed commands, 2026-10-21):** after the merge checkout under `core.autocrlf=true`, 11 of the 12 manifest-listed files are CRLF on disk; `baseline-hashes.sha256`, which the merge did not rewrite, is LF. Their LF-normalised SHA-256 values equal the earlier LF values, and the blob ids are unchanged. **Fresh-checkout behaviour in any other environment remains untested.** The 45/45 baseline verification applies only to the present working copy." |

**Scope check** (by reading):
- W-2 refines only §3 step 3. It does **not** change §3 step 5 ("record the failure and stop"). Instead, 3b defines which outcomes count as passed and which as failed for step 5.
- No other rule, precondition or gate (R8, R9, R10, R14–R18) is affected.
