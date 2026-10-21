# Prototype readiness audit (2026-10-20)

**Purpose.** This audit decides whether the existing prototype and evidence can support two experiments:
- **Experiment 1:** rule reuse and configuration effort.
- **Experiment 2:** completeness of diagnostic evidence.

The audit is the baseline. It records the state *before* any new experiment, engine change or timed trial.

**Method.**
- The audit consisted of reading files, `git status` / `git diff`, SHA-256 hashing, a tool-availability check and `postman collection run --help`.
- **Nothing was built, tested, started or run.** No API was started, no database was touched, no defect switch was enabled, no engine code was changed, and no existing artifact was modified.

**Status labels used below.**

| Label | Meaning |
|---|---|
| READY | The existing implementation and evidence are enough |
| READY WITH MINOR CHANGES | Feasible without touching the core architecture |
| REQUIRES IMPLEMENTATION | Missing functionality or instrumentation must be added |
| NOT FEASIBLE WITH CURRENT PROTOTYPE | Needs a substantial change or a different approach |

All effort figures are **estimates**, not measurements.

---

## 1. Git state and safety observations

| Item | Observation |
|---|---|
| Branch / HEAD | `master` at `4d9e7d889287c71eb3996c0be84a52715abe7903` ("experiment: add ST-01 defect switch…") |
| Modified tracked files | `docs/README.md` (+3), `docs/defect-register.md` (+3/−1), `docs/experiments/start-api.ps1` (+12/−3, adds `-DatasourceUrl`). Documentation and harness only; no product code |
| **Untracked** | `semantic-validator/` (the engine), `event-api/`, `library-api/`, `docs/experiments/{db-02,evaluation,event-reuse,library-reuse}/`, `docs/experiments/{save-schemathesis-evidence.ps1,seed-orders.js,snapshot-orders.js}`, `docs/experiments/st-01/{reachability-seeded.js,schemathesis/ext2-seeded-completed/}`, and the Order API DB-02 defect plus its test |
| Stashes | none |
| Engine version in the working copy | **Engine 0.2.** All 18 entries in `docs/experiments/event-reuse/results/engine-hashes-v0.2.txt` match the current files |
| Baseline hash manifest | Written by this audit to `baseline-hashes.sha256`: 45 files (engine sources, jar, rules, contracts, scenarios, Postman collections and generators, oracles, summaries, API jars) |
| API jars | `event-api/target/*.jar` and `library-api/target/*.jar` exist. Their modification time is 1980-02-01, the reproducible-build timestamp, so their build date cannot be read from the file system. A stale jar has already caused one aborted matrix (`timing.log` 07:24:58). **Rebuild and hash before any new run** |
| Tools present | JDK 21.0.12, Maven 3.9.16, Node 22.21.1, **Postman CLI 1.74.0** (the version used in the existing runs), Python 3.13 |
| Tools missing | **Schemathesis is not installed** in the system Python, and the virtual environment used for the recorded runs is not in the repository. It can be rebuilt from `docs/experiments/schemathesis/requirements.txt` (Schemathesis 4.29.4). The Ajv `node_modules` directory is external; scripts take it as a parameter |
| Credentials | The engine hardcodes the H2 user `sa` with an empty password (`Main.java`). These are local H2 defaults, not secrets. This audit did not scan for secrets beyond the scans already recorded in the evidence READMEs; the documents in this directory contain no credentials |

### Main safety risk

The engine, two of the three APIs and **all of the comparative evidence behind the NARROW decision are untracked**. As a result:
- one `git clean -fd`, or the loss of this working copy, destroys them;
- no commit identifies the evidence version, so reproducibility rests only on the hash files.

**Recommendation (your decision; not done here):** before any new experiment, commit the current state to a branch or make a dated archive. This audit did not commit, stash or clean anything.

## 2. Repository structure (relevant parts)

| Path | Content |
|---|---|
| `src/`, `docs/openapi-baseline.json` | Order API (Spring Boot 4.1.1 / H2, port 8080). Defects SV-01, ST-01 and DB-02 behind `pilot.defects.*` switches |
| `library-api/` | Library Lending API (port 8081). Defects LIB-ST-01, LIB-DB-01 and LIB-RI-01 |
| `event-api/` | Event Registration API (port 8082). Defects EVT-ST-01, EVT-DB-01, EVT-DB-02, EVT-RI-01 and EVT-CF-01. Contract frozen before any rules were written |
| `semantic-validator/` | Engine (about 1,000 lines of Java, 5 unit tests) and `rules/` with Order v1, Library v1, Event v1 and Event v2 |
| `docs/experiments/evaluation/` | Order comparative matrix (Ajv, Postman, validator, Schemathesis baseline and seeded) and the feasibility reports |
| `docs/experiments/library-reuse/` | Library reuse experiment: 24 runs, validator only |
| `docs/experiments/event-reuse/` | Event reuse and comparison experiment: 48 runs, 72 tool results, diagnosis analysis |
| `docs/experiments/{sv-01,st-01,db-02,schemathesis,schema-only}/` | Per-defect experiments on the Order API and the Schemathesis clean baseline |

## 3. How the prototype works

These answers come from reading the source.

### Q1. How do rules bind to OpenAPI operations?

- Each rule lists `operations` by operationId.
- `RuleSet` calls `OpenApiIndex.require` on every listed operationId at load time, so an unknown operationId aborts the run.
- `OpenApiIndex` maps operationId → method + path template, and matches concrete requests back to operations with a regex built from the template. The first match wins.

Limits:
- The OpenAPI document must be **JSON**; YAML is not read.
- `servers` and base paths are ignored.
- Ambiguous templates (for example `/x/{id}` versus `/x/search`) are not resolved.

### Q2. How are semantic rules evaluated?

There are three rule kinds:

| Kind | What it checks |
|---|---|
| `response-invariant` | A condition over each successful response body. Arrays are checked item by item; the optional per-operation `items` path reaches into an envelope. A subject in which none of the referenced fields is present becomes NOT_APPLICABLE |
| `state-transition` | Uses a transition table. The previous state comes from a read-back (active mode) or from the last state observed in earlier responses (passive mode). Statuses listed in `rejectedStatuses` are business rejections; other error statuses are NOT_APPLICABLE |
| `db-consistency` | A row snapshot before and after the request. After a 2xx, `columnsMatchResponse` is compared as text. After a 4xx, `rowUnchanged` must hold |

Condition language: `equals`, `notNull`, `isNull`, `in`, `notAfter`, `iff`, `implies`, `all`, `any`, `not`.

Path syntax: only `$` and `$.a.b`. There are no array indices and no wildcards.

### Q3. How does active validation execute requests?

`Engine.runScenario` executes a JSON scenario. Each step has an operationId, `pathParams` with `${var}` substitution, a **static** body, and `save` (a response path into a variable).

What the scenario format cannot do:
- **no headers, authentication or query parameters**;
- **no variable substitution inside request bodies**;
- **no per-step expectations.** The `expect` blocks in the scenario files are ignored by the engine. Only the Postman generators and the oracles use them.

Before each request, the rules capture state:
- the transition rule GETs `readOperation` with `Map.of(idParam, id)`;
- the database rule snapshots the row.

### Q4. How is database state inspected?

- JDBC through `DriverManager`, with the credentials hardcoded to `sa`/`""`.
- The query is the rule's own SQL. The engine reads the first row only.
- Parameters can come **only from `$path.*`** and are bound with `setString`.
- Values are compared with the response as text.
- The database must be the same H2 file database as the API's, opened through `AUTO_SERVER=TRUE`.

### Q5. How do reports represent the evidence?

The engine writes `semval.json` (all findings) and `semval.md` (failures only). Only FAIL findings carry evidence:

| Field | Content |
|---|---|
| `request` | method, URL, body |
| `response` | status, body |
| invariant evidence | `values` (each referenced path), `assertion`, `itemIndex` |
| transition evidence | `state`: `resourceId`, `previous`, `previousSource`, the full `readBack` (`status` and `body`), `requested`, `transitionAllowed`, `allowedFromPrevious` |
| database evidence | `database`: `query`, `params`, `rowBefore`, `rowAfter` |

At report level: mode, a source string (scenario file and base URL), rule-set name, duration, exchange count, totals and per-rule totals.

**Not represented:**
- timestamps (neither for the run nor for a finding);
- a run identifier;
- a rules-file hash or version beyond the name;
- the API build or defect mode;
- the engine build (the `tool` string is hardcoded to `"semantic-validator 0.2.0"`);
- request and response headers;
- the scenario step name;
- the full exchange list (the report holds only a count);
- any link from a downstream failure to the request that caused it.

The Markdown has an encoding defect (`â€”` instead of an em dash). It is cosmetic.

### Q6. How are defect injection and clean baselines controlled?

- Each defect is a Spring `@Service @Primary @ConditionalOnProperty` subclass. It is off by default and logs exactly one `DEFECT … ENABLED` warning at startup.
- The matrix scripts start a **new API process with a new H2 file database for every run**. They abort if the number of `DEFECT` lines is not exactly the expected one.
- Library refuses to start with two switches on.
- Order starts through `mvnw spring-boot:run`; Library and Event start from their packaged jars.

### Q7. Can the same defect be evaluated consistently by the validator and Postman?

| API | Answer |
|---|---|
| Event | **Yes.** Same frozen contract, same scenario, same build and switches, and a recording proxy between each tool and the API |
| Order | Yes, but the Order Postman collection is much weaker: 29 assertions (status, returned status, list statuses, schema), no invariants, no read-back |
| Library | **No Postman collection exists** |

**Schemathesis** ran only on the Order API, with built-in checks only (no custom checks, no hooks). It generates its own requests, so "the same defect" there means the same build and switch, not the same triggering requests.

### Q8. Do existing reports contain enough raw data?

**Experiment 2:**
- **Yes for the validator and default Postman on the Event API.** Each run directory has `semval.json`, `postman.txt`, `traffic.har.json` (ground-truth traffic recorded by the harness proxy, *not* by the tools), `db-final.json`, `verify.json` and `run.json`.
- **Gap:** the Postman runs captured only the CLI text output. Postman CLI 1.74.0 lists `--output <folder>` (a YAML run report with an executions file) and `--verbose` in its help, and neither was used. What these outputs contain was **not inspected** and must be checked in a pilot.
- There is no Schemathesis run on the Event or Library API.

**Experiment 1:**
- **No human timing exists.**
- `timing.log` holds the AI assistant's wall-clock times.
- Diffs can be measured: rules files, Postman generator v1 against the current version, and the engine patch.

### Q9. Can existing results be reused?

- **Reusable:** the Event-matrix validator and default-Postman artifacts can be re-scored for Experiment 2. Both repeats were identical for all 36 mode × tool groups.
- **Must be generated:**
  - every configured-Postman or Schemathesis result;
  - every Order or Library comparison with a careful Postman collection;
  - **all Experiment 1 timing data.**

### Q10. What scripts and artifacts already exist?

**Already present:**
- matrix runners: `run-event-matrix.ps1`, `run-library-matrix.ps1`, `run-matrix.ps1`;
- the recording proxy `record-proxy.js`;
- independent oracles `verify-event-run.js` and `verify-run.js`, which take the transition table from the OpenAPI text and have self-tests;
- database dumpers `Dump*.java`;
- evidence redaction and saving scripts;
- the rubric automation `diagnosis.js` (weaknesses in §5).

**Missing:**
- a timing harness for human trials;
- a database-access helper for Postman or Schemathesis;
- Schemathesis custom checks or a scenario adapter;
- held-out scenarios;
- sealed starting workspaces.

## 4. Existing evidence relevant to Experiment 1: measured vs estimated

| Quantity | Order | Library | Event | Status of the number |
|---|---|---|---|---|
| Initial rule writing time | not found in repository | **not measured** (report §2: "Human adaptation time was not measured") | rules v1: 9 min 15 s, including writing the scenario and fixing one script bug | Event only: **AI wall-clock**, not human |
| Rules size / diff | 44 lines | 55 lines; numstat against Order: +31/−20 | v1: 62 lines, +43/−36 against Library; v2: 77 lines, +16/−1 | Measured (diff) |
| Engine source changes | — | 0 (hashes identical before and after) | **+50/−3 in 3 main files, plus 30 test lines** (engine 0.2) in 3 min 28 s AI wall-clock | Measured |
| Rules v2 time | — | — | `rules-v2-start` and `rules-v2-done` carry the **same second** (07:18:37) | **No usable measurement.** "Under 1 min" in the final report is an upper bound, not a measurement |
| Postman effort | Order generator (29 assertions) | **no Postman collection** | Generator adapted from Order (+74/−26), 2 min 28 s including trials. Revision 1: +7/−3. Revision 2: 1 harness line | AI wall-clock; the effort went into a **generator script**, not a hand-built collection |
| Helper changes | — | scenario (15 steps), start/stop scripts, oracle | scenario (19 steps), start/stop scripts, oracle, dumper | Counted as files, not timed separately |
| API discovery included? | — | — | No. The same agent wrote the API 4 minutes earlier (06:57→07:00) | Discovery cost was effectively zero |
| Defects known in advance? | yes | yes | Rules v1 were written *before* the defects were implemented (07:01 < 07:12), by the same author who designed them. **Postman was built last (07:19), after the defects and after the validator's v0.1 misses were known** | Knowledge differs between the arms |
| API designed with the engine in mind? | origin of the engine | **yes**: assumption A2 "met by design" (report §3) | "Independent in intent but not blind" (contract-design.md) | Acknowledged in the existing reports |

**Conclusions from the existing Experiment 1 evidence:**
- **No existing number measures human adaptation effort.**
- All the times are AI-assistant wall-clock times of a few minutes, with zero discovery cost and full prior knowledge.
- Taken at face value, the only side-by-side times (Event) show **Postman faster**: 2 min 28 s against 9 min 15 s plus 3 min 28 s for the engine change. The tasks were not comparable: the Postman effort reused a generator and the existing scenario. Even so, nothing in the repository already favours the declarative approach on effort.

## 5. Existing evidence relevant to Experiment 2: audit of the diagnosis rubric

`event-reuse/results/diagnosis/` holds the matched reports (validator 0.2 active against default Postman, repeat 1, ids masked) and the rubric R1–R8, scored automatically by `diagnosis.js`.

The rubric scoring has these weaknesses:

1. **Inapplicable criteria count as absences.** The headline tallies use 5 as the denominator for every criterion:
   - R6, prior state: 3 of 5;
   - R7, stored state: 2 of 5.

   But prior and stored state are not required evidence for RI-01 and CF-01, which are response-invariant defects. Postman is penalised the same way.
2. **Lenient text matching:**
   - Postman R4 ("states what was expected") is credited whenever the text contains the substring `expected`. That includes Chai's `expected false to deeply equal true`, which says nothing about the expected state.
   - Validator R4 is credited by the presence of `rowBefore` even for EVT-DB-02. There the expected value is the *response* state, not the row before the request.
3. **Postman R5 (response body) is hard-coded `false`.** It was never checked. The CLI ran without `--output` and without `--verbose`, so the result reflects the **output configuration**, not something Postman cannot do.
4. **Different scoring units for the two tools.**
   - Validator: only the first FAIL at the cause step.
   - Postman: all sections at the cause step.
5. **No traceability or reproducibility criterion.** The validator would likely score poorly on one: its reports have no timestamps, run ID, rules hash or API build.
6. **Asymmetric database access.** Postman had no database access in the protocol; the validator had direct JDBC. The R7 difference is therefore partly a difference in *capability granted*, not only in reporting.
7. **Harness traffic must not be credited to Postman.** The HAR files for Postman runs were recorded by the harness proxy.

The existing table (R5 5/0, R6 3/1, R7 2/2 partial) is a **real observation of those configurations**. It is not evidence of a general difference in reporting quality.

## 6. Engine assumptions that limit adaptation to a new or independent API

A1–A7 are from `second-api-reuse-report.md` §3; G-items are new in this audit.

| # | Limitation | Likely effect on an independently written API |
|---|---|---|
| G1 | Request bodies are static: no `${var}` substitution in bodies | Fails as soon as a body must reference a created id (for example a line item pointing at its order) |
| G2 / A6 | No headers, no authentication, no query parameters in scenarios | Any authenticated API, or any list filtered by query parameters, cannot be driven |
| G3 | The OpenAPI document must be JSON | Can be handled by an external YAML→JSON conversion; not an engine change |
| G4 / A1 | JDBC credentials hardcoded; only the H2 driver is on the classpath | A PostgreSQL or MySQL target needs an engine change, or at least a classpath change |
| G5 / A2 | The read-back reuses the write operation's path-parameter name | Fails if the GET uses `{loanId}` while the PATCH uses `{id}` |
| G6 / A5 | Database parameters only from `$path.*` | Cannot key a check on a created id that appears only in a body, or on a composite key |
| G7 | Paths without indices or wildcards; one `items` path per operation | Nested collections and arrays inside items cannot be addressed |
| G8 / A3 | Observed state keyed by id alone, across all operations | Ids that collide across resource types corrupt passive mode |
| G9 / A7 | Database errors exit with code 1, the same as a rule FAIL | A CI pipeline cannot tell a failed check from a broken setup |
| G10 | No passive state-continuity rule | EVT-DB-02 (a lost write) is invisible in passive mode |
| G11 | No reproducibility metadata in reports | Weakens Experiment 2, criterion E8 |
| G12 | `servers` and base path ignored; first-match operation lookup | Problems with APIs mounted under a prefix |

None of these is a redesign. Each is a bounded extension. But **any of them made during or after selecting a target API is an engine change**, and it must be reported as adaptation effort for that API.

## 7. Readiness matrix

### 7a. By dimension

| Dimension | Experiment 1: rule reuse and effort | Experiment 2: evidence completeness |
|---|---|---|
| Existing code | **READY** for the three self-authored APIs (engine 0.2 handles all three). **Unknown, probably REQUIRES IMPLEMENTATION** for an independent API (G1, G2, G4, G5, G6) | **READY** for the validator: no engine change is needed to *produce* reports. Its weak traceability (G11) is measured, not fixed |
| Existing test fixtures | **READY WITH MINOR CHANGES**: APIs, switches, start/stop scripts, oracles and matrix runners exist. Missing: sealed starting workspaces, requirement sheets, held-out scenarios | **READY** for Event (all 5 defects, oracle, answer key). **READY WITH MINOR CHANGES** for Order SV-01 as the contrast case |
| Existing evidence | **Not usable as measurement.** AI wall-clock only (§4). Diffs are reusable as descriptive context | **Partly READY**: Event validator and default-Postman artifacts can be re-scored. Configured Postman and Schemathesis: none |
| Required adapters | A read-only **database probe** so that Postman has database access equivalent to the validator's | The same **database probe**, plus a **Schemathesis scenario adapter** and **custom checks** |
| Required instrumentation | Trial timer and log, a diff/LOC script against the sealed workspaces, an engine-hash check before and after each trial | Evidence extractor and scorer (rewrite of `diagnosis.js` to the new rubric); a Postman `--output` pilot |
| Required API changes | None for the self-authored APIs. Defect switches in an independent target API | None |
| Required new tests | Oracle and acceptance check for an independent target; held-out scenario per target | Oracle self-test re-run; a smoke test of the Schemathesis adapter on clean and one defect |
| Estimated effort (estimate) | Preparation 6–11 h (including the database probe); T1/T2 trials 6–10 h; independent target 12–24 h, plus generic engine extensions 6–12 h if needed, plus trials 3–5 h | 20–32 h in total (§7b; assessment §3) |
| Main risks | n = 1 implementer; learning and order effects; the implementer knows the APIs and defects; the independent target forces engine changes | Configured baselines may close the gap; scoring by the tool's own author; Schemathesis Python API details not verified for 4.29.4 |

### 7b. By requirement

| Requirement | Status | What exists / what is missing | Effort (estimate) |
|---|---|---|---|
| E1: source and target APIs with real structural differences | READY | Event against Library or Order: id format, state field, action endpoint vs PATCH, 422 vs 409, envelope vs array, time order | 0 |
| E1: independently authored target API | **REQUIRES IMPLEMENTATION** | None exists; all three APIs were written by the same agent with knowledge of the engine | 12–24 h (selection, local setup, defect switches, oracle, scenario) |
| E1: sealed starting workspaces (target artifacts removed) | READY WITH MINOR CHANGES | Copy the repository, remove the target rules, scenarios and collections, record hashes | 1–2 h |
| E1: careful Postman *source* collection | READY for Event as the source | Event collection (41 `pm.test` statements, read-backs, invariants on envelope items). Order's (29 assertions) is not equivalent; Library has none | 0 if Event is the source; 3–5 h for each other source |
| E1: equivalent database access for Postman | REQUIRES IMPLEMENTATION | Postman only has HTTP read-back. Needs a read-only SELECT-only probe on localhost | 2–4 h (shared with E2) |
| E1: timing and diff instrumentation | READY WITH MINOR CHANGES | `git diff --no-index --numstat` is already in use; a timer script must be added | 1–2 h |
| E1: held-out scenario per target | REQUIRES IMPLEMENTATION | One scenario per API exists, and the implementer can see it | 1–2 h per API |
| E1: configuration-only adaptation on the self-authored APIs | READY | Engine 0.2 covers all three (regression-checked) | 0 |
| E1: configuration-only adaptation on an independent API | **Unknown / likely REQUIRES IMPLEMENTATION** | G1, G2, G4, G5 and G6 are probable blockers | 6–12 h if done as generic changes frozen before the target is chosen |
| E2: validator reports | READY | `semval.json` and `.md` in the Event matrix | 0 |
| E2: default Postman reports | READY | `postman.txt` in the Event matrix | 0 |
| E2: configured Postman reports (`--output`, scripted evidence) | REQUIRES IMPLEMENTATION | Not run. Contents of `--output` not verified | 3–5 h, including a pilot |
| E2: Schemathesis with custom checks on the same defects | **REQUIRES IMPLEMENTATION** (native stateful mode alone: **NOT FEASIBLE** for identical triggering) | Not installed; no custom checks; never run on Event. Native stateful generation cannot guarantee the scenario's preconditions (for example, a registration that is already CHECKED_IN) | venv 0.5 h; checks and probe hook 4–6 h; scenario adapter 3–5 h |
| E2: applicability-aware rubric and answer key | READY (written in this directory) | `experiment-2-evidence-completeness-protocol.md` | 0 |
| E2: scoring and self-audit | REQUIRES IMPLEMENTATION | `diagnosis.js` has to be rewritten to the new rubric; manual verification against the HAR and database dump | 5–8 h |
| E2: second independent rater | Depends on a person | None identified | 2–3 h of their time |

## 8. Can both experiments run without redesigning the core engine?

- **Experiment 2: yes.** The validator side needs no change. Everything missing is on the baseline side (adapters, configurations) or in scoring.
- **Experiment 1 on the three self-authored APIs: yes.** Engine 0.2 already supports them.
- **Experiment 1 on an independent API: probably not without engine *extensions* (not a redesign).**
  - Body substitution, headers and authentication, configurable JDBC and drivers, and the read-back parameter mapping are likely to be needed.
  - To keep "configuration-only" testable, make these changes as **generic extensions before the target is selected**, freeze them by hash, and report them.
  - Anything changed after the target is seen counts as per-API adaptation effort.
