# Schemathesis baseline: readiness audit (2026-10-21)

**Scope.** This document asks whether Schemathesis can be a fair baseline for Experiment 2 (ST-S, and optionally ST-N) on the Event API. It also covers Experiment 1 if Schemathesis is ever added there; it is not part of that protocol now.

**Nothing was installed or run.**

**Labels:**

| Label | Meaning |
|---|---|
| **[V]** | Verified by reading files or running a read-only command |
| **[I]** | Inference |
| **[U]** | Unverified |
| **[P]** | Proposed, not done |

**No engine change is proposed.** Everything below is on the Schemathesis side.

## 1. Current environment

| Item | Finding |
|---|---|
| Pinned version | `docs/experiments/schemathesis/requirements.txt`: `schemathesis==4.29.4`, `hypothesis==6.170.1`, `pytest==9.1.1`, `requests==2.34.2` and 22 other pins **[V]** |
| Recorded runtime | Python 3.13.6 in an isolated venv (`schemathesis/README.md`) **[V]** |
| Current machine | System Python **3.13.6** with pip 25.2 **[V]**. **Schemathesis is not installed** (it is not in `pip list`, and no `schemathesis` or `st` command is on PATH) **[V]**. No venv was found in the repository **[V]** |
| Network access to PyPI | **[U]**, not attempted |
| Existing runner | `run-schemathesis.ps1` is hard-wired to `docs/openapi-baseline.json` (the Order API) and `http://localhost:8080` **[V]**. It deletes `.schemathesis/` (a git-ignored crash cache) before each run **[V]**. It is **not usable for Event without a new script** |
| Existing evidence | Order API only: the clean baseline, SV-01, ST-01 (baseline, EXT-1, EXT-2 seeded), DB-02, and the evaluation matrix **[V]**. Built-in checks only; **no custom checks or hooks were ever used** **[V]** |
| Event / Library runs | none **[V]** |

## 2. Required preparation

Each step needs separate approval.

1. **[P]** Create a venv **outside the repository**, for example in a scratch directory: `python -m venv <dir>`, then `<dir>\Scripts\pip install -r docs\experiments\schemathesis\requirements.txt`.
2. **[P]** Record the output of `schemathesis --version` and `pip freeze` with the run evidence.
3. **[P]** Probe the 4.29.4 Python API in that venv. The names below are **from memory and unverified [U]** and must be confirmed against the installed package:
   - loading a schema from a file;
   - accessing an operation by path and method;
   - building a case with explicit path parameters and body;
   - calling it against a base URL;
   - validating the response with a chosen set of checks;
   - the custom-check registration decorator (`@schemathesis.check`, signature `(ctx, response, case)`);
   - the reproduction-command helper.
4. **[P]** Write a new runner for Event (a new file; the existing one is not edited).

## 3. Testing the same scenarios and defects

| Option | How | Comparable to SV and Postman? | Status |
|---|---|---|---|
| **ST-S** (primary) | A Python adapter sends the **same 19 steps** from `event-reuse/scenario.json` as Schemathesis cases, in order, against the same build with the same switch and a fresh database. It applies the custom checks (§5) to each response | **Yes**: the triggering requests are identical | **[U]** depends on the step-3 API probe. If the case API cannot do this, ST-S is recorded as "not comparable on identical triggering" and drops out of the decision rule (Experiment 2 §2) |
| **ST-N** (optional) | Native `schemathesis run` with `--phases stateful` (and others), custom checks loaded, database seeded or empty | **No**: generated requests differ, and the defects may not be exercised. The Order evidence shows this: ST-01 was not exercised without seeding (`st-01/results.md`) **[V]** | Record "exercised: yes/no" per defect from the recorded traffic. Score only if exercised |
| ST-0 | built-in checks only | Contrast only | On Order it raised no semantic failures **[V]**. On Event **[U]** |

**Why ST-N alone is not enough.** EVT-DB-01 and EVT-ST-01 need specific state sequences before the defect acts:
- CHECKED_IN, then a forbidden cancellation;
- WAITLISTED, then a confirmation.

Random stateful generation does not guarantee these sequences **[I]**.

## 4. Does the actual OpenAPI support stateful workflows?

`event-api/openapi-baseline.json` **[V]**:
- OpenAPI **3.1.0**;
- `servers` set to `http://localhost:8082`;
- four operations: `registerAttendee`, `fetchRegistration`, `listAttendeeRegistrations`, `transitionRegistration`;
- **no `links` on any response**;
- `registerAttendee` 201 documents a response **header**, the `Location` header in the contract design;
- the path parameter `{registrationId}` matches the `registrationId` response field.

Consequences:
- Declared links: **none**. Stateful testing would rely on Schemathesis's **link inference**.
  - On the Order API, Schemathesis 4.29.4 inferred 9 links with no `links` in the spec (`schemathesis/README.md`) **[V]**.
  - Whether it infers `registerAttendee → fetchRegistration` / `transitionRegistration` for Event (from the `Location` header or the parameter name) is **[U]**.
- The transition body `{"to": enum, "reason"}` gives the generator 5 target states. Reaching a given precondition is probabilistic **[I]**.
- **Adding `links` to the spec is not proposed.** It would change the frozen contract (hash `event-contract-frozen.sha256`). Any such change would have to be a separately labelled configuration.

## 5. Custom checks

These are proposed, not written.

| Check | Mirrors | Data source |
|---|---|---|
| `inv_001`…`inv_005` | EVT-INV-001…005, including envelope items | the response |
| `transition_table` | EVT-ST-001 | previous state from the probe or from a GET issued by the adapter **before** the case; table from the rules |
| `db_consistency` | EVT-DB-001 | the probe, before and after, **with the same SQL** as the rule |

**Detection versus evidence code.** The rule is in `operational-definitions.md` §4 *(superseded in part; the current classification is the R2 deletion test)*:
- the check's assertion and its check name are *detection code* (counted as native);
- extra data put into the failure message is *evidence code* (`PS`).

Schemathesis's own output (case details, status, the response excerpt it prints, the reproduction command, seed, version, NDJSON timestamps) is native `P`, **if** it appears for custom-check failures **[U]**.

**Revision 2 (2026-10-21, proposed).**
- ST-S follows the same scripting policy as PM-S (`operational-definitions.md` R3), including the **90-active-minute authoring cap**, with active time and code size recorded separately.
- Elements are classified with the deletion test (R2): check-message content is PS, and framework output is P (if present; unverified).
- ST-S counts in the **primary** (scripted) decision with P + PS (R4, R8). This replaces the earlier "only its native P elements" reading in §8.

**Revision 3 (2026-10-21, proposed; R14.4, R14.6).**
- A **bounded feasibility pilot** (6 active hours, a proposed judgement) settles §7 items 1–4 and builds a minimal adapter with one check on the clean API plus one defect. It runs before the matrix and is not scored.
- The 90-minute cap covers evidence-authoring code only. Setup, installation, adapter and detection-check effort are recorded separately and uncapped.
- If the pilot fails or no fair baseline is possible:
  - record the blocker(s), every attempt and the time spent;
  - B(d) = PM-S only, and claims are limited to Postman;
  - Schemathesis is reported as "not evaluated";
  - **the exclusion is not evidence that the validator is better than Schemathesis.**

**Final conditions (2026-10-21, proposed; R15.1, R15.2, R15.4).**
- The 6-active-hour time box is **approved** as a judgement-based bound. When it is reached without the pass criterion being met, the pilot **stops** and is not extended.
- If the pilot passes, the ST-S detection checks need the equivalence review before any ST-S unit is scored (independent reviewer preferred; open U-1).
- Missing or unusable ST-S results are never imputed, never scored as 0 in the baseline maximum, and never counted as a validator advantage.

**Freeze decisions (2026-10-21, proposed; R17.1, R17.3, R17.4).**
- The 6-hour limit is kept. Its limitations are documented in R17.3: a judgement; may exclude a baseline that would be feasible with more time; self-recorded time; covers clean plus one defect only.
- The ST-S equivalence review and the review of disputed scoring need an **independent reviewer**. Without one, Experiment 2 is INCONCLUSIVE for the gate.
- Any ST-S AU code needs a recorded reason and a documented pilot attempt.

## 6. Fair database inspection

- Schemathesis has no database access of its own **[I]**.
- The fair route is the **same read-only probe** used by Postman: a fixed query list identical to the rule SQL, called over localhost HTTP from the check or the adapter (`postman-baseline-checklist.md` §6).
- The probe is not built **[P]**.
- Probe calls are not routed through the recording proxy, so the oracle must not expect them in `traffic.har.json` **[I]**.

## 7. Unverified requirements

1. PyPI reachability, and that 4.29.4 installs cleanly on Python 3.13.6 here **[U]**.
2. The 4.29.4 case API can send ordered, explicit cases with chosen path parameters and bodies **[U]**.
3. Custom checks can be applied to manually built cases outside `schemathesis run` **[U]**.
4. Custom-check failures include the reproduction command and response data in the stdout, NDJSON or JUnit reports **[U]**.
5. Link inference on the Event spec **[U]**.
6. The probe works alongside the API under `AUTO_SERVER` without affecting it **[U]**.
7. Results repeat identically with `--seed` (ST-N) or deterministically (ST-S) **[U]**. The Order baseline repeated identically **[V]**, but that is a different API and configuration.

## 8. Decision consequences

- **If items 2–4 fail:** ST-S drops out. Experiment 2 then rests on Postman (PM-R / PM-S) alone, and that is stated as a limitation, not hidden. *(Revision 3: the decision baseline is PM-S. Schemathesis is reported as "not evaluated", R14.4/R15.1.)*
- **If items 2–4 hold:** ST-S counts as a non-scripted baseline for the decision rule, but only its native `P` elements. *(Superseded 2026-10-21, revision 2: ST-S counts with P + PS in the primary scripted decision; its native score is secondary. See §5 note.)*
