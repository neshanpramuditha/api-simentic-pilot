# Go/no-go validation: audit and experiment protocols (2026-10-20)

This directory contains documentation only. It holds no new experimental results: nothing was run, and the templates are empty.

| File | Content |
|---|---|
| `prototype-readiness-audit.md` | Repository and engine audit, Git safety observations, existing evidence (measured vs not), readiness matrix |
| `experiment-1-rule-reuse-protocol.md` | Experiment 1 protocol: rule reuse and configuration effort against Postman. DRAFT, not frozen |
| `experiment-2-evidence-completeness-protocol.md` | Experiment 2 protocol: applicability-aware evidence rubric, answer key, tool configurations, decision rule. DRAFT, not frozen |
| `go-no-go-feasibility-assessment.md` | Feasibility, minimum work, validity threats, provisional recommendation (**CONTINUE INVESTIGATION**) |
| `experiment-data-template.csv` | Run- and trial-level record for both experiments (header only) |
| `evidence-matrix-template.csv` | Element-level raw evidence matrix for Experiment 2 (header only) |
| `baseline-hashes.sha256` | SHA-256 of 45 baseline artifacts at HEAD `4d9e7d8`. **Frozen; never edit.** Re-verified 45/45 on 2026-10-21 |
| `postman-baseline-checklist.md` | (2026-10-21) Line-level gaps between the Postman collections and the validator rules; reporting, fresh-state and metadata changes needed. Proposals only |
| `schemathesis-baseline-audit.md` | (2026-10-21) Schemathesis installation, scenario adapter, stateful support in the Event spec, database access, unverified requirements |
| `operational-definitions.md` | (2026-10-21) **Proposed** definitions: effort, engine vs configuration change, rule reuse, detection vs evidence code, detection, false positives, repeatability. Status and rationale of the 0.67 and 20 pp thresholds |
| `independent-api-selection-criteria.md` | (2026-10-21) **Proposed** tool-neutral selection criteria for the independent API, and the freeze-before-selection portability procedure |
| `proposed-freeze-manifest.md` | (2026-10-21) **Proposed** freeze identifiers (Git blob ids, authoritative; working-tree SHA-256, integrity). Not a freeze record |
| `freeze-record.md` | (2026-10-21) The **only authoritative freeze record** once completed (R18.1). Currently a template: **NOT FROZEN**, no entries |

## Status (2026-10-21)

- The baseline is committed in `33ed52a` ("Define baseline").
- The audit, protocols and assessment carry dated addenda or **proposed amendments (not adopted)**.
- The 0.67 and 20 pp thresholds are unchanged and remain proposals, not established standards.
- No experiment has been run since the baseline.
- **Revision 2 (2026-10-21, proposed, not frozen):**
  - Primary Experiment 2 metric is **scripted** evidence completeness; native completeness and authoring overhead are secondary.
  - Ordered, mutually exclusive outcome rules for Experiment 2, Experiment 1 and the project gate.
  - One remediation cycle.
  - Sensitivity reporting (10/20/30 pp; 0.5/0.67/0.8).
  - Corrected threshold arithmetic.
  - Earlier proposals are kept and marked superseded or withdrawn.
  - Details: `operational-definitions.md` §R0–§R13.
  - Open issues **O-1** and **O-2** need a decision before freezing. *(Superseded by revision 3.)*
- **Revision 3 (2026-10-21, proposed, not frozen):** O-1 to O-6 resolved (`operational-definitions.md` R14):
  - one-value rule limited to E4b, E6a and E7a;
  - **no reporting-only GO route**, so Experiment 2 alone cannot produce GO;
  - historical run set H read-only, and new run set N1 for every decision configuration;
  - Schemathesis feasibility pilot (6 h, proposed), with the exclusion rules;
  - the validator's AU elements stay visible;
  - the 90-minute cap covers evidence code only.
  - All thresholds remain judgement-based proposals.
- **Revision 3 final conditions (2026-10-21, proposed, NOT FROZEN):** `operational-definitions.md` R15–R16.
  - 6-hour Schemathesis pilot approved as a time box, with a stopping rule.
  - Equivalence review before any baseline is scored (independent reviewer preferred).
  - Detection effort recorded in `notes`.
  - Missing Schemathesis results are never a validator advantage.
  - Open items **U-1 to U-4**.
  - Proposed freeze manifest: `proposed-freeze-manifest.md` (**proposal only, not a freeze record**).
- **Freeze decisions (2026-10-21, proposed, NOT FROZEN):** `operational-definitions.md` R17.
  - Independent review is required for Experiment 2 (V7); without it, Experiment 2 is INCONCLUSIVE for the gate.
  - Experiment 1 self-review is allowed but labelled weaker evidence.
  - The Git blob id is authoritative, and SHA-256 is a working-copy integrity check; fresh checkout is untested.
  - The limitations of the 6-hour pilot are documented.
  - AU requires a recorded reason.
  - Remaining blockers: **F-1 to F-4** (R17.5). *(F-1 and F-3 resolved below.)*
- **F-1/F-3 resolutions (2026-10-21, proposed, NOT FROZEN):** `operational-definitions.md` R18.
  - The freeze is recorded only in `freeze-record.md`; protocol files are not edited at freeze.
  - The effort-only GO route needs the least favourable native-margin bound ≥ 0.20; if the bounds disagree, the result is INCONCLUSIVE.
  - No reporting-only route.
  - Remaining blockers: **F-2** (commit) and **F-4** (manifest approval).

## Data dictionary (non-obvious columns)

### `experiment-data-template.csv`

| Column | Meaning |
|---|---|
| `experiment` | `E1` or `E2` |
| `instance_set` | `primary`, `contrast` or `secondary` (Experiment 2) |
| `arm_or_config_id` | Experiment 1: `D` or `P`. Experiment 2: `SV`, `PM-D`, `PM-R`, `PM-S`, `ST-S` or `ST-N` |
| `authoring_method` | Arm P only: `app`, `json` or `generator`, declared before the timer starts |
| `artifact_sha256` | Hash of the rules + scenario file, the collection, or the checks + adapter that were run |
| `run_type` | `clean`, `defect`, `heldout` or `pilot`. Pilot rows are never scored |
| `completed` | `yes`, `no` or `cap` (hit the 120-minute cap) |
| `active_minutes` | trial END − START − pauses. Repair time is in `repair_minutes` |
| `detected_at_cause` / `detected_downstream_only` | `yes`, `no` or `na`, per the oracle's cause and affected steps |
| `false_positive_steps` | Failing steps outside the oracle's affected steps; `-` if none |
| `criterion_completeness_*` | Experiment 2 primary metric (protocol §5), filled only from `evidence-matrix-template.csv` |
| `count_AM` / `count_AU` | Absent elements that were missing but configurable / unavailable under the protocol |
| `overhead_script_lines` / `overhead_minutes` | Extra baseline work needed to reach this configuration, starting from PM-D. *(Revision 3, R14.6/R15.3: `overhead_minutes` = evidence-authoring minutes only, capped at 90. `overhead_script_lines` keeps this meaning. Detection, setup and adapter minutes go in `notes`.)* |
| `notes` | Free text. (Revision 3) also holds `DETECTION:`, `SETUP:` and `ADAPTER:` effort entries: active minutes plus a concise description |
| `detection_code_lines` / `evidence_code_lines` | (added 2026-10-21) Lines of detection code and of evidence-only code in the artifact, as defined in `operational-definitions.md` §4 |
| `postman_cli_version` / `schemathesis_version` | (added 2026-10-21) Exact versions used in the run, as printed by the tool; empty if the tool was not used |

### `evidence-matrix-template.csv`

| Column | Meaning |
|---|---|
| `unit_id` | instance × configuration × repeat |
| `element_id` | E1a … E8f (Experiment 2 protocol §4.2) |
| `applicable` | `y` or `n`, per the category table (§4.3) |
| `status_code` | `P`, `PS`, `AM`, `AU` or `NA` (§4.4) |
| `evidence_locator` | File plus JSON path or line number in the tool's own artifact |
| `producing_code_locator` | (added 2026-10-21, revision 2) The code that produces the evidence, for the deletion test: `engine`, `framework`, or file + line of the script or check |
| `disputed` | (added 2026-10-21, revision 2) Empty if not disputed; otherwise the alternative plausible code, used in the SV-low / SV-high bounding analysis (`operational-definitions.md` R6) |
| `scoring_pass` | `first`, `self-audit` or `rater2` |
| `audit_sample` | `y` if the unit was in the random re-scoring sample (record the seed in the notes) |
