# Library reuse experiment — evidence

Produced 2026-10-19 by `../run-library-matrix.ps1 -Repeats 2` (24 runs), copied by `../save-evidence.js` (UTF-8, local paths replaced by `<run-root>`, API logs and raw H2 files not copied; a scan for user name, drive paths and profile folders found nothing). Summaries regenerated from the copied files with `../summarize.js` and identical to those computed from the raw runs.

| Path | Content |
|---|---|
| `summary.md`, `summary.json` | Per-run verdicts, false negatives, database-rule outcomes, reproducibility |
| `matrix/<mode>/<config>-r<n>/run.json` | Runner metadata: mode, configuration, exit code, validator duration, `DEFECT LIB-…` startup lines found in the API log |
| `…/traffic.har.json` | Ground-truth traffic: recording proxy (active: 15 scenario requests + 9 validator read-backs) or capture-har.js (passive: 15 requests) |
| `…/semval.json`, `semval.md`, `semval.out.txt` | Validator report (all findings, FAIL evidence) and console output |
| `…/db-final.json` | Final `loans` table (DumpLoans.java) |
| `…/verify.json` | Ground-truth check (verify-run.js) |
| `engine-hashes-before.txt`, `engine-hashes-after.txt` | SHA-256 of every semantic-validator source file, its pom, jar and `order-api.rules.json`, before the work and after all runs; identical |

## Checks run outside the matrix (not stored as run directories)

**Verifier self-test** (copies of saved runs, edited, re-verified; the matrix runs were then all re-verified with the corrected verifier and the summary was byte-identical):

| Edit | Verifier result |
|---|---|
| All FAIL findings removed from a `lib-db01/semval-jdbc` report | FN 5 missed, TP 0 |
| A PASS turned into a FAIL with fabricated evidence in a `clean/semval-jdbc` report | FP 1, 4 evidence problems (request, response, state, read-back). The first version of `verify-run.js` crashed on this input (missing `state` object); fixed before the final verification |
| `returnedAt` of a FAIL's response evidence altered in a `lib-ri01/semval-jdbc` report | "response differs from traffic" |

**Broken database evidence** (clean API, validator `run` with an unusable `--jdbc`):

| Case | Result |
|---|---|
| (a) URL of a database that does not exist (`IFEXISTS=TRUE`) | Uncaught `JdbcSQLNonTransientConnectionException`, no report, exit code 1 |
| (b) URL of a different, empty database (H2 creates it; no `loans` table) | Uncaught `IllegalStateException: database query failed`, no report, exit code 1 |

Neither produced a false PASS or FAIL, but exit code 1 is also the "at least one FAIL" code (the `Main` Javadoc says errors exit with 2), so a caller cannot tell a crash from a detection by exit code alone.

**Two defect switches at once** (`st01` + `db01`): startup fails with `NoUniqueBeanDefinitionException: more than one 'primary' bean found`.
