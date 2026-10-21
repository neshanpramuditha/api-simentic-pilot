# Go/no-go feasibility assessment (2026-10-20)

**Provisional recommendation: CONTINUE INVESTIGATION.** This is not a GO decision, and not a GO-CANDIDATE.

**What this is based on:**
- `prototype-readiness-audit.md`;
- the two protocols in this directory.

**What it is not based on:** no new experiment was run, and no result in this document is new data.

## 1. Summary

**Can the experiments be done?** Both can be run on the current prototype without redesigning the core engine.

**Experiment 1 (reuse effort) has three weaknesses:**
- Its tasks on the three existing APIs measure reuse of an engine already fitted to those APIs.
- With one implementer, it can only be exploratory.
- The only existing timings (AI wall-clock) show Postman *faster*.

A portability statement needs an independently written target API. None exists, and one would probably require generic engine extensions.

**Experiment 2 (evidence completeness)** can start almost at once on the existing validator artifacts. *(Revision 3 note: decision scoring now uses only the new run set N1; existing artifacts are the historical set H, R14.3.)* But the evidence advantage reported so far is partly explained by how the comparison was set up:
- Postman's text-only CLI output;
- no database access for Postman;
- inapplicable criteria counted as absences.

A fair rerun is likely to *narrow* the difference. The validator will probably score *lower* on traceability, because its reports have no timestamps, run ID or version hashes.

**What the experiments can establish** is a bounded claim: the validator provides prior-state and database evidence **without per-check authoring**. That claim is credible, but modest.

## 2. What can be tested immediately

These need no new implementation. Freezing the protocol comes first.

| Item | Source | Notes |
|---|---|---|
| Re-scoring the validator (SV) and default Postman (PM-D) on I1–I5 with the new rubric | Existing Event matrix artifacts | Deterministic, and the repeats are identical. Freeze the rubric before scoring. *(Revision 3: descriptive only, historical set H; not a decision input, R14.3)* |
| Scoring the I6 schema contrast for Postman and the Schemathesis baseline | `docs/experiments/sv-01/` | The validator has no schema rule, so it is recorded as "no report" |
| Descriptive diffs of the existing adaptations | Rules files, generator v1 against v2, the engine patch | Context only, not effort data |
| A Postman `--output` / `--verbose` pilot on the clean Event API | Existing collection | About 30 minutes. It settles whether Postman's default-configuration body gap is real |

## 3. What requires implementation first

All figures are **estimates**.

| Work item | For | Estimate | Status |
|---|---|---|---|
| W0: preserve the current state (commit to a branch or archive) | both | 0.5 h | Your decision. Highest priority, because the engine and evidence are untracked *(done by the owner in `33ed52a`; see audit addendum A1)* |
| W1: rebuild the API jars; check the hashes; freeze the protocols | both | 0.5–1 h | minor |
| W2: read-only database probe (localhost, SELECT-only) | both | 2–4 h | requires implementation |
| W3: PM-R pilot and the PM-S evidence scripts (time and lines recorded as overhead) | E2 | 3–5 h | requires implementation |
| W4: Schemathesis venv, custom checks, probe hook and scenario adapter | E2 | 7–11 h | requires implementation. The Python API for 4.29.4 is not yet verified |
| W5: Experiment 2 matrix runs (3 new configurations × 6 modes × 2 repeats) | E2 | about 2 h of machine time plus 1 h of supervision | minor |
| W6: rewrite the extractor; manual scoring; verification; self-audit | E2 | 5–8 h | requires implementation |
| W7: Experiment 1 preparation: sealed workspaces, requirement sheets, held-out scenarios, timer | E1 | 4–7 h | requires implementation |
| W8: Experiment 1 trials T1 and T2: 4 arms of at most 2 h, plus discovery and repairs | E1 | 6–10 h | — |
| W9: independent target selection, local setup, defect switches, oracle | E1 | 12–24 h | requires implementation, high risk |
| W9a: generic engine extensions frozen before target selection (G1, G2, G4, G5, G6), if needed | E1 | 6–12 h | requires implementation |
| W10: Experiment 1 trial T3 | E1 | 3–5 h | — |
| W11: analysis and write-up | both | 4–6 h | — |

**Totals (estimates):**

| Package | Estimate |
|---|---|
| Experiment 2 alone (W0–W6) | about 20–32 h |
| Experiment 1 without T3 (W7, W8) | about 10–17 h |
| T3 (W9, W9a, W10) | about 20–40 h more |
| Write-up (W11) | 4–6 h |
| **Credible full package** | **about 55–95 h**, roughly 2–3 weeks of focused work |

## 4. Is the current prototype sufficient?

| Question | Answer |
|---|---|
| Can the validator produce the reports for Experiment 2 without changes? | **Yes** |
| Can the validator be adapted to Library and Order (T1, T2) without engine changes? | **Yes, very probably.** Engine 0.2 already runs all three APIs, and the regression check was identical |
| Can it be adapted to an independent API without engine changes? | **Unknown, probably not.** Static request bodies, no headers or authentication, H2-only credentials and the read-back path-parameter assumption are probable blockers (audit §6) |
| Is a redesign of the core needed for either experiment? | **No.** The gaps are bounded extensions or are on the baseline side |
| Are the baseline tools ready? | Postman: yes, with configuration work. Schemathesis: not installed, and its custom checks and adapter do not exist |

## 5. Minimum work for a credible evaluation

The **minimum credible package** is the smallest set whose results could change the decision.

1. **W0, W1, W2:** preserve the state, freeze the protocols, build the database probe.
2. **All of Experiment 2** (W3–W6), with PM-R and ST-S as the comparison baselines *(Revision 2/3: the decision baselines are PM-S and ST-S)*. Without them, Experiment 2 only repeats the existing, configuration-confounded comparison.
3. **Experiment 1, T1 and T2** (W7, W8), labelled exploratory. If possible, add a second implementer in the opposite arm order.
4. **T3** (W9, W10, and W9a if needed). Without T3, write "reuse across three self-authored Spring/H2 APIs" and nothing broader.

If time allows only part of this:
- **Drop T3 before dropping PM-R or ST-S.** *(Revision 3 note: under the current gate, dropping T3 rules out every GO-CANDIDATE route, because R10 requires Experiment 1 FAVOURABLE on T3. The decision baselines are now PM-S and ST-S.)*
- A portability claim without T3 is not allowed anyway. A reporting claim without fair baselines would be misleading.

## 6. Cross-experiment validity threats

| # | Threat | Practical mitigation | Cost |
|---|---|---|---|
| 1 | All APIs are by the same developer (an AI assistant in this project) | T3, an independent target chosen by fixed criteria, with a search log, or peer-written from a functional specification; otherwise limit the claims | 12–24 h; may fail to find a suitable API |
| 2 | The developer knows the implementation and the injected defects | Experiment 1: defects never enabled during trials, discovery notes for both arms, defect files off-limits, a second implementer. Experiment 2: the answer key is fixed before scoring | Low. The knowledge cannot be removed; disclose it |
| 3 | The validator may have been designed around these APIs (A2 "by design"; engine 0.2 written for Event) | Treat T1/T2 as reuse of a fitted engine; make generic extensions *before* target selection and freeze them; count any later change | Included in W9a |
| 4 | Postman and Schemathesis can be extended with custom code | Three Postman levels (default, reporter-configured, scripted) and Schemathesis custom checks; native and scripted scores reported separately; overhead measured | W3, W4: about 10–16 h |
| 5 | Database access is easier for one tool | Shared read-only database probe for both baselines; AM/AU codes keep missing and unavailable evidence apart | W2: 2–4 h |
| 6 | Too few defects, or too little variety | 7 instances in 6 categories, all reported per instance; secondary instances (ORD-ST-01, ORD-DB-02, LIB-DB-01) added if time allows; no rate is generalised | 3–5 h per extra careful collection |
| 7 | Times reflect familiarity, not reuse | Discovery timed separately; arm order alternated; implementer background recorded; per-task decision rule; result labelled exploratory | Low cost; the threat remains |
| 8 | Biased scoring by the tool's author | Frozen rubric with operational definitions; every element verified against ground truth; delayed self-audit; a second rater on at least 8 units; no inter-rater reliability claimed without one | 2–3 h of a second person's time |
| 9 | A configuration-only claim hides helper or engine changes | Engine hash checked before and after every trial; every non-artifact diff reported; scenario lines counted inside arm D | Under 1 h |
| 10 | Evidence differences come from logging settings | PM-D against PM-R isolates this; only PM-R and ST-S count in the decision rule *(Revision 2/3: PM-S and ST-S)*; report size is recorded as a verbosity control | Included in W3 |

**Not added**, as beyond what an individual undergraduate capstone needs: a multi-participant human study, statistical testing, or a multi-database engine.

## 7. Can the experiments establish a measurable contribution?

### Experiment 2

It **can** establish a bounded, measurable reporting-content result for the tested categories.

**Likely outcome (a prediction, not a result):**
- The validator keeps an advantage on prior-state and database evidence for ST, DBR and DBL against PM-R. The advantage shrinks or disappears against PM-S.
- It is equal on request and response evidence once Postman's `--output` is used, *if* the pilot shows that the output includes bodies.
- It is **behind** Schemathesis on traceability (E8), since Schemathesis output carries a seed, version, timestamps and a reproduction command.

**If that happens,** the defensible contribution is "complete state and database evidence by default, at zero per-check authoring cost", **quantified by the PM-S overhead**. That is real, but it is an effort claim, so its strength depends on Experiment 1.

### Experiment 1

It **cannot** be confirmatory within a capstone:
- with one or two implementers and two or three tasks, it yields exploratory per-task observations;
- a confirmatory result would need several independent implementers and independent targets.

Without T3, its result is about reuse *within* a family of APIs written for the engine.

### Together

The two experiments can support a **narrow, honest contribution statement**. They cannot support "superior" or "generally portable".

## 8. Decision rules for the next gate

> **Revision 2 (2026-10-21):** a proposed replacement for this section is in "Addendum 2" at the end of this file (`operational-definitions.md` R10). The text below is kept as history.

These are fixed now, before any results.

| Next-gate outcome | Condition |
|---|---|
| **GO-CANDIDATE** (still not GO) | Experiment 2 is favourable against **PM-R and ST-S** under its decision rule, **and** Experiment 1 is favourable in every completed task **including T3**, with 0 engine changes after selection, **and** the PM-S parity overhead is reported |
| **CONTINUE / NARROW FURTHER** | Experiment 2 is favourable only against PM-D, or only against PM-R with PM-S parity at low overhead; or Experiment 1 is favourable on T1/T2 but T3 is not run or is inconclusive |
| **NO-GO CANDIDATE** for the reuse and evidence contribution | Any one of: PM-R or ST-S reaches comparable completeness on the state and database units; Experiment 1 shows Postman comparable or faster in any task; T3 needs per-API engine changes beyond the pre-registered generic ones |

A NO-GO candidate on these two contributions does not by itself invalidate the project. It would mean the proposal has to rest on something other than reuse effort or evidence completeness, for example passive-mode checking of recorded traffic, which neither experiment evaluates.

## 9. Risks and methodological limitations

- **Untracked work.** The engine, the APIs and the evidence can be lost, and the evidence version cannot be identified by a commit (W0). *(Resolved by commit `33ed52a`; build outputs under `target/` remain untracked, see audit addendum A1.)*
- **Schemathesis adapter feasibility.** The Python case API of 4.29.4 has not been checked. If driving the scenario through Schemathesis is not possible, ST-S drops out and the Experiment 2 comparison rests on Postman alone.
- **Contents of Postman `--output`.** Not inspected. If it holds bodies and timestamps, part of the existing advantage disappears.
- **Validator traceability gap (E8).** It must be scored as it is. Improving it after scoring creates a separate, labelled configuration.
- **Small defect set** from one API, plus one contrast case. Results are per instance.
- **No human diagnosis evidence**, and none is planned here. No claim of diagnosis time or usability is possible.
- **Same-person roles.** The implementer, experimenter and scorer may all be the same person. Each mitigation is partial.
- **No independent target may be found.** In that case Experiment 1 can only describe reuse among self-authored APIs.

## 10. What must not be claimed, whatever the results

- Superior defect detection.
- General portability, or configuration-only reuse for arbitrary APIs, without T3; even with T3, the claim covers only that API.
- Reduced diagnosis time, productivity or usability.
- Statistical significance, or inter-rater reliability without a second rater.
- That fewer lines of configuration mean less effort.
- That existing AI wall-clock timings measure human effort.

---

## Addendum: updated blockers and sequencing (2026-10-21)

The assessment above is unchanged. This addendum adds what the 2026-10-21 re-verification found.

**Labels:**

| Label | Meaning |
|---|---|
| **[V]** | Verified |
| **[I]** | Inference |
| **[U]** | Untested |
| **[P]** | Proposal |

### Readiness verdict

**Not ready to run the fair-baseline experiments [V].**
- The baseline is preserved: committed in `33ed52a`, with 45/45 hashes matching.
- But the database probe, the Schemathesis environment, the new runners and oracles, and the adopted operational definitions do not exist yet.

### Blockers, most important first

1. **No read-only database probe** **[V]**. Both baselines lack database evidence equivalent to the validator's.
2. **Schemathesis is not installed**, has never been run on Event, has no custom checks or adapter, and its 4.29.4 case API is unverified **[V]/[U]** (`schemathesis-baseline-audit.md`).
3. **The runners and oracles do not support the protocols** (audit addendum N1–N3) **[V]**.
4. **The decision rules are ambiguous** (`operational-definitions.md` §9) until amendments E1-A1…A7 and E2-A1…A8 are adopted **[V]**. *(Later revisions add E1-A8…A10 and E2-A9…A26; see Addenda 2–4.)*
5. **The Postman collection is not equivalent** (`postman-baseline-checklist.md` §7) **[V]**.
6. **No run metadata**: versions, hashes and timestamps are missing from `run.json` **[V]**.
7. **The content of Postman `--output` and `--verbose` is unverified** **[U]**.
8. **Experiment 1 infrastructure and the independent API are missing** **[V]**.
9. **Whether a jar rebuild reproduces the recorded hashes is unverified** **[U]**.

### Proposed sequencing [P]

Each step needs separate approval.
1. Adopt or revise the proposed amendments, then freeze the protocols.
2. Rebuild the jars and compare their hashes.
3. Build the probe.
4. Install Schemathesis in a venv outside the repository and probe its API.
5. Write the new runners and oracles, and self-test them.
6. Run the pilots for PM-R, PM-S and ST-S.
7. Run the Experiment 2 matrix and score it.
8. **Gate:** decide on Experiment 1 and the independent API only after the Experiment 2 result.

**Why Experiment 2 first [I].** It is the cheapest experiment that can produce a NO-GO signal: if PM-R or ST-S reaches comparable completeness on the state and database units, the evidence-completeness differentiation fails. *(Revision 2/3 note: the decision baselines are now PM-S and ST-S under the scripted metric. Experiment 2 can contribute to a NO-GO, but **cannot by itself produce GO**.)*

### Recommendation

**CONTINUE to the Experiment 2 fair-baseline preparation only.** This is still a provisional recommendation, not GO and not GO-CANDIDATE.

---

## Addendum 2: revised decision rules (2026-10-21), PROPOSED

Approved in direction on 2026-10-21; **proposed until the revised protocols are frozen.** No experiment has been run, and no result exists for any row below.

### Metrics

- **Primary, Experiment 2:** scripted evidence completeness.
- **Secondary:** native completeness, and authoring overhead (descriptive, no threshold).
- **Effort is decided in Experiment 1.**

Details: `operational-definitions.md` R1–R4.

### Project gate (R10)

Rows are evaluated in order, and the first match decides.

| Step | Condition | Gate |
|---|---|---|
| 1 | Experiment 2 FAVOURABLE **and** Experiment 1 FAVOURABLE on T1, T2 **and T3** | GO-CANDIDATE (reporting contribution) |
| 2 | Experiment 2 COMPARABLE **and** native Δ̄ ≥ 0.20 *(Addendum 6 / R18.2: under the least favourable bound, Δ̄_nat^low ≥ 0.20)* **and** Experiment 1 FAVOURABLE on T1, T2 **and T3** | GO-CANDIDATE (effort only). Claim limited to *comparable evidence with lower authoring effort* |
| 3 | Experiment 2 UNFAVOURABLE; **or** Experiment 2 COMPARABLE with native Δ̄ < 0.20 *(R18.2: Δ̄_nat^high < 0.20; if the bounds disagree, neither step 2 nor this clause matches)*; **or** Experiment 1 UNFAVOURABLE while Experiment 2 is not FAVOURABLE | NO-GO CANDIDATE |
| 4 | Otherwise, after the single remediation cycle has been used | NO-GO CANDIDATE (not GO) |
| 5 | Otherwise, before that cycle | CONTINUE (one remediation cycle) |

**GO-CANDIDATE requires positive evidence on every condition.** An ambiguous or inconclusive outcome can never be labelled GO. GO-CANDIDATE is not a final GO.

The Experiment 2 outcome categories (R8) and the Experiment 1 categories (R9) are ordered and mutually exclusive.

### Thresholds

The thresholds (20 pp, the −20 pp guard, 0.67) and the sensitivity values (10/30 pp, 0.5/0.8) are **proposed judgement-based thresholds, not validated standards** (R7, R11).

### Open before freezing

R13 items O-1 to O-5. **O-1** (the scope of the one-value rule) and **O-2** (a reporting-only route) need your decision. *(Superseded by Addendum 3: O-1 to O-6 resolved.)*

---

## Addendum 3: resolutions of O-1 to O-6 (2026-10-21), PROPOSED

Decided by the repository owner on 2026-10-21, and **proposed until the revised protocols are frozen.** Full text: `operational-definitions.md` R14. No experiment has been run.

| Issue | Resolution |
|---|---|
| O-1 | The one-value rule is limited to E4b, E6a and E7a. E1b and E5 are statement elements. Every applicable element remains satisfiable |
| O-2 | **No reporting-only GO route.** Experiment 2 alone cannot produce GO or GO-CANDIDATE. A reporting-only claim would need an explicit, dated, frozen scope and gate revision **before** any further evaluation; the current contribution is not redefined |
| O-3 | The historical set H is preserved unchanged. A new run set N1 (SV, PM-D, PM-R, PM-S, ST-S on the same rebuilt jars, with full metadata) is the only input to the decision |
| O-4 | Schemathesis is kept, with a bounded feasibility pilot (6 active hours, proposed). If it fails: a documented exclusion, claims limited to Postman, and the exclusion is **not** evidence of superiority |
| O-5 | The validator's traceability elements stay visible. AU is assigned per definition. No engine or report change |
| O-6 | The 90-minute cap covers evidence-authoring code only, equally for both baselines. Detection, setup and adapter effort are recorded separately. A baseline that fails the equivalence review makes the outcome INCONCLUSIVE, never a weaker scored baseline |

**Gate unchanged** (Addendum 2 / R10). It is mutually exclusive, and GO-CANDIDATE always needs Experiment 1 FAVOURABLE on T1, T2 and T3.

---

## Addendum 4: final conditions (2026-10-21), PROPOSED, NOT FROZEN

Full text: `operational-definitions.md` R15–R16.

| Condition | Effect |
|---|---|
| Schemathesis pilot | 6 active hours approved as a judgement-based box. If no fair baseline is established within it: stop, record the blocker and attempts, report "not evaluated". **Not** evidence of validator superiority |
| Equivalence review | Required before any baseline is scored; an independent reviewer is preferred. A self-review is documented as a limitation and is not treated as independent verification. Experiment 2 stays INCONCLUSIVE until the requirement is satisfied |
| Detection effort | Recorded in `notes` (active minutes plus a description); no new CSV column |
| Missing or unusable Schemathesis results | Never imputed and never a validator advantage (R15.4) |

**Confirmations (by reading the rules):**
- Experiment 2 alone cannot produce GO or GO-CANDIDATE.
- No rule produces a final GO.
- The outcome categories and gate steps are mutually exclusive.

**Open items U-1 to U-4** (R16) need decisions before freezing. **Consequence of U-1** under the interim reading: with no independent reviewer, the project gate cannot pass CONTINUE. *(Superseded by Addendum 5.)*

---

## Addendum 5: freeze decisions (2026-10-21), PROPOSED, NOT FROZEN

Full text: `operational-definitions.md` R17.

**Independent review (U-1, U-2):**
- **Experiment 2:** independent review of the detection-check equivalence **and** of disputed evidence scoring is required (precondition V7).
- Without an independent reviewer, Experiment 2 is **INCONCLUSIVE for the gate**. That is an evaluation limitation, not evidence of validator failure or superiority.
- **Consequence:** the gate then reaches at most CONTINUE, and NO-GO CANDIDATE (not GO) after the remediation cycle.
- **Experiment 1:** the self-review fallback is kept, labelled as weaker evidence, with the limitation recorded.

**Hashes (U-4):**
- The Git blob id is authoritative for version-controlled content; the working-tree SHA-256 is an integrity check of the current bytes.
- Fresh-checkout line endings are **untested**.
- The 45/45 baseline verification applies to this working copy only.

**Also recorded:**
- the limitations of the 6-hour Schemathesis pilot (R17.3);
- the AU reason requirement (R17.4).

**Remaining blockers to freezing (R17.5):**
- F-1: freeze-record mechanics;
- F-2: the files to be frozen are uncommitted;
- F-3: U-3 (the bound used for the native margin);
- F-4: manifest approval.

*(Superseded by Addendum 6: F-1 and F-3 resolved.)*

---

## Addendum 6: F-1 and F-3 resolutions (2026-10-21), PROPOSED, NOT FROZEN

Full text: `operational-definitions.md` R18.

**F-1, separate freeze record:**
- The freeze, the authoritative blob ids, the working-tree SHA-256 values, amendment adoption, the freeze date and the manifest version are recorded **only in `freeze-record.md`**. That file currently reads NOT FROZEN.
- Protocol files are never edited at freeze and never contain their own hash.
- The protocols stay DRAFT, NOT FROZEN until that record shows the procedure completed.

**F-3, native-margin bound:**
- The effort-only GO route needs **Δ̄_nat^low ≥ 0.20**, the least favourable plausible bound, plus every existing condition: Experiment 2 COMPARABLE, V1–V7, and Experiment 1 FAVOURABLE on T1–T3.
- If the bounds disagree, the native margin is INCONCLUSIVE: no GO-CANDIDATE, and the gate proceeds to step 4 or 5.
- **No reporting-only GO route is created.**

**Remaining blockers to freezing:**
- **F-2:** commit the documentation (your action).
- **F-4:** explicit approval of the manifest revision.
