# Independent target API: selection criteria and portability procedure (2026-10-21)

**Status: PROPOSED.** This document extends Experiment 1 §5.2 and becomes binding when it is adopted as an amendment and frozen.

**No API has been selected, searched for or modified [V].**

## 1. Principle

The criteria must be **neutral across the three tools**. They describe what *any* of the three tools (validator, Postman, Schemathesis) needs in order to be tested on an API. They must **not** filter for what this engine already supports. In particular, none of these may be a criterion:
- a JSON-format spec;
- operationIds present;
- H2;
- path-parameter naming;
- no authentication.

Filtering on any of them would select an API that fits the engine. Engine incompatibilities found after selection are **results**, not reasons to reject the API.

## 2. Inclusion criteria

A candidate must meet all of these.

| # | Criterion | How it is checked |
|---|---|---|
| S1 | **Not created for this validator**: not written in this project, and its authors have not seen the engine, the rules or this repository | Public commit history from before this project, or a written statement from the peer author |
| S2 | **Usable OpenAPI description.** Published by the project, or generated from its code by the project's own tooling, with no hand-edits to semantics. YAML or JSON | The file exists and parses. The format is recorded |
| S3 | **Lifecycle.** At least one resource with at least 4 states and at least one forbidden transition that the API rejects with a 4xx | Verified by manual requests during screening |
| S4 | **Relational persistence** reachable read-only over JDBC (any vendor) | Database settings in the project's configuration |
| S5 | **Local and isolated.** Runs on this machine (Docker allowed) without external services or shared databases | A trial start-up |
| S6 | **Controllable test data.** The database can be reset to a known state for every run (fresh container or file, a schema-create setting, or a seed script) without changing the API's behaviour | Two consecutive resets give identical dumps |
| S7 | **Defects can be injected.** Small switchable patches (an environment variable or property) are possible for at least the C2, C3, C4 and C5 equivalents, and are off by default | Inspection of the code structure |
| S8 | **Size.** It can be run and understood in at most 3 h | Time-boxed screening |
| S9 | **Licence** allows local modification for research | The licence file |
| S10 | **Authentication**, if present, can be satisfied by test credentials or configuration that every tool can use equally | Documentation |

**Preferred, not required:** a stack other than Spring Boot / H2.

## 3. Search procedure

The procedure is fixed before searching.

1. Write the search query or queries and the sources before the search, and freeze them in the search log.
2. Screen candidates **in the order found**. For each one, log the name, URL, commit, and the first criterion that failed.
3. Select the **first** candidate that meets S1–S10.
   - The experimenter does not look at the engine's fit while screening.
   - The fit (gaps G1–G12 in the audit) is assessed **after** selection and recorded as a result.
4. **Time box:** 3 h of screening.
5. **Fallback:** a peer writes an API from a short written functional specification: entities, lifecycle, persistence, and the S6 reset requirement.
   - The peer does not see the engine, the rules or this repository.
   - A different stack is preferred.
   - The specification is written and hashed before the peer starts.
6. **If neither works, T3 is not run** and no portability claim is made.

## 4. Testing portability without engine changes after selection

1. **Freeze the engine before the search.** Record a manifest of the engine sources, `pom.xml` and the jar. If any generic extensions are wanted (from audit §6: body substitution, headers and authentication, configurable credentials, read-back parameter mapping, `$response`/`$request` database parameters), they must be:
   - written, unit-tested and regression-checked (identical findings on Order, Library and Event);
   - frozen **before** step 3.1, and reported as "engine v0.3, pre-registered".
2. **After selection, only these may change** (classes as in `operational-definitions.md` §3):
   - rule configuration (rules file, scenario);
   - runtime configuration (options, JDBC URL, an unmodified driver jar on the classpath);
   - helpers (spec conversion, start/stop scripts, the probe query list).
3. **Hash check** of the engine before and after every T3 trial.
4. **If the engine must change after selection:**
   - make the change and record it as per-API engine effort (minutes, files, lines);
   - flag T3 as "engine change required";
   - under the decision rules (assessment §8), this is a NO-GO signal for the configuration-only claim. *(Revision 2/3: under `operational-definitions.md` R9 step 1, Experiment 1 becomes UNFAVOURABLE, so R10 cannot reach GO-CANDIDATE.)*
5. **Defects in the target** are written, if possible, by someone other than the implementer, before the trials, and checked against the clean behaviour with the target's own tests.
6. **Oracle.** A ground-truth check for the target is written and self-tested **before** the trials. It uses recorded traffic, a database dump, and the lifecycle taken from the target's documentation, not from the rules.

## 5. What this procedure cannot fix

- **One API is one data point.** A favourable T3 supports "configuration-only reuse was achieved for this one external API". It does not support general portability.
- **The implementer still knows the defect categories.**
- **A spec-format conversion** (YAML → JSON) is a helper. If it alters semantics, the selection rule S2 is violated.
