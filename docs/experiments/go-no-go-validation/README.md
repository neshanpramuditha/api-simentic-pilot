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
| `baseline-hashes.sha256` | SHA-256 of 45 baseline artifacts at HEAD `4d9e7d8`, many of which are untracked |

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
| `overhead_script_lines` / `overhead_minutes` | Extra baseline work needed to reach this configuration, starting from PM-D |

### `evidence-matrix-template.csv`

| Column | Meaning |
|---|---|
| `unit_id` | instance × configuration × repeat |
| `element_id` | E1a … E8f (Experiment 2 protocol §4.2) |
| `applicable` | `y` or `n`, per the category table (§4.3) |
| `status_code` | `P`, `PS`, `AM`, `AU` or `NA` (§4.4) |
| `evidence_locator` | File plus JSON path or line number in the tool's own artifact |
| `scoring_pass` | `first`, `self-audit` or `rater2` |
| `audit_sample` | `y` if the unit was in the random re-scoring sample (record the seed in the notes) |
