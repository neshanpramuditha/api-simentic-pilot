# docs

| File | Purpose |
|---|---|
| `openapi-baseline.json` | OpenAPI 3.1 document generated from the clean (defect-free) API. Reference contract for drift and validation experiments. |
| `defect-register.md` | Preliminary register of defects planned for injection, with detection hypotheses and measured results. |
| `experiments/evaluation/feasibility-conclusion.md` | **Feasibility conclusion** (go/narrow/pivot) from the comparative evaluation of Ajv, Postman, Schemathesis and the semantic-rule prototype (`../semantic-validator/`). |
| `experiments/evaluation/final-project-feasibility.md` | **Final feasibility decision (NARROW)** across the Order, Library and Event Registration APIs: portability, engine 0.2 changes, defect inventory, fair Postman comparison, diagnosis evidence, proposal framing. Third-API experiment in `experiments/event-reuse/` (`../event-api/`). |
| `experiments/evaluation/second-api-reuse-report.md` | **Second-API reuse report**: the unchanged semantic-validator engine applied to the Library Lending API (`../library-api/`) with a new rules file; experiment in `experiments/library-reuse/`. |
| `experiments/` | Reproducible experiment scripts and raw evidence: `start-api.ps1` / `stop-api.ps1`, `schema-only/`, `schemathesis/` (configuration and clean-API baseline), and one folder per injected defect (`sv-01/`, …) with design, results and tool output. |

## Regenerating the baseline

From the project root:

```
./mvnw test -Dtest=OpenApiBaselineExport -Dopenapi.export=true          # macOS / Linux / Git Bash
.\mvnw.cmd test "-Dtest=OpenApiBaselineExport" "-Dopenapi.export=true"  # Windows PowerShell
```

This runs `src/test/java/com/example/apipilot/OpenApiBaselineExport.java`, which:

1. Starts the application context against its own in-memory H2 database (no server, no port needed).
2. Fetches `/v3/api-docs` through MockMvc, with the server port set to 8080 so `servers[0].url` matches the running app.
3. Writes the document with sorted keys, two-space indentation and LF line endings, so re-exporting unchanged code produces an identical file.
4. Re-reads the file and asserts it is OpenAPI 3.x and contains exactly the four expected operations.

The export is excluded from the normal `mvnw test` run (its class name does not match Surefire's default includes, and it requires `-Dopenapi.export=true`), so ordinary builds never overwrite the baseline.

Regenerate the baseline **only from the clean API**, never after defects have been injected.
