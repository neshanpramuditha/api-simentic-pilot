# Event API experiment — evidence

All files are UTF-8 with local paths replaced by `<run-root>`. API logs and raw H2 files were not copied. A scan for the user name, drive paths and profile folders found nothing. Summaries and diagnosis files regenerated from these copies are identical to those computed from the raw runs (ignoring line endings).

| Path | Content |
|---|---|
| `summary.md`, `summary.json` | 48 runs / 72 tool results: per-run verdicts, reproducibility, false negatives, Postman failures by tag. No incomplete run directories |
| `matrix/<mode>/<type>-r<n>/` | `run.json` (metadata, startup `DEFECT` lines), `traffic.har.json` (ground-truth traffic incl. read-backs), `db-final.json`, `verify.json`, and the tool outputs: `semval.json/.md/.out.txt` (active), `semval-v0x-passive.*`, `ajv.json` (capture runs), `postman.txt` (Postman runs) |
| `diagnosis/` | Matched failure reports (validator 0.2 active vs Postman, repeat 1, ids masked), the diagnosis task with answer key, rubric scores (`README.md`, `scores.json`). Report-content analysis only; no participants |
| `first-attempt/event-v01/clean-r1/` | The first configuration-only run of the original engine 0.1 on the clean API (the vacuous PASS on the envelope list is visible at exchange 15) |
| `regression-v02/order/<mode>/semval.json`, `regression-v02/library/<mode>/<config>-r1/semval.json` | Engine 0.2 on the Order API (4 modes) and the Library API (12 runs): findings identical to the saved 0.1 evidence |
| `engine-hashes-v0.1.txt`, `engine-hashes-v0.2.txt`, `engine-v0.2.patch` | Engine before and after the fix; the patch is exact (the 0.1 sources were reconstructed and matched the recorded hashes byte for byte) |
| `event-contract-frozen.sha256` | Frozen contract hash, re-checked at the end |
| `timing.log` | Wall-clock timestamps of each phase (UTC) |
| `build-postman-collection.v1.js` | First version of the Postman builder, before revision 1 |

Not stored: the aborted first matrix attempt (5 clean runs made with a stale jar). It was stopped and discarded, and none of its results are used.
