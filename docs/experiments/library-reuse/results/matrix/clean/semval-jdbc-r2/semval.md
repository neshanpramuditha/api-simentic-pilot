# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8091 (with database access)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=39, FAIL=0, NOT_APPLICABLE=13}
- Duration: 1079 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| LIB-INV-001 | 12 | 0 | 5 |
| LIB-INV-002 | 12 | 0 | 5 |
| LIB-ST-001 | 7 | 0 | 2 |
| LIB-DB-001 | 8 | 0 | 1 |

## Failures

None.
