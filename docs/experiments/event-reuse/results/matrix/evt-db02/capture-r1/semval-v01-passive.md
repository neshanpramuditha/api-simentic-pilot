# Semantic validation report

- Mode: check-har
- Source: HAR traffic.har.json (passive: no read-backs, no database)
- Rule set: event-api semantic rules v1 (engine 0.1 features only)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=64, FAIL=0, NOT_APPLICABLE=32}
- Duration: 41 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 14 | 0 | 5 |
| EVT-INV-002 | 14 | 0 | 5 |
| EVT-INV-003 | 14 | 0 | 5 |
| EVT-INV-004 | 14 | 0 | 5 |
| EVT-ST-001 | 8 | 0 | 2 |
| EVT-DB-001 | 0 | 0 | 10 |

## Failures

None.
