# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8080 (with database access)
- Rule set: order-api semantic rules v1
- Exchanges: 11, unmatched requests: 0
- Totals: {PASS=22, FAIL=0, NOT_APPLICABLE=2}
- Duration: 797 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| ORD-INV-001 | 10 | 0 | 2 |
| ORD-ST-001 | 6 | 0 | 0 |
| ORD-DB-001 | 6 | 0 | 0 |

## Failures

None.
