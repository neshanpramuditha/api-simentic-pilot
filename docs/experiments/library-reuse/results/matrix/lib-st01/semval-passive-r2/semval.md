# Semantic validation report

- Mode: check-har
- Source: HAR traffic.har.json (passive: no read-backs, no database)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=32, FAIL=1, NOT_APPLICABLE=19}
- Duration: 42 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| LIB-INV-001 | 13 | 0 | 4 |
| LIB-INV-002 | 13 | 0 | 4 |
| LIB-ST-001 | 6 | 1 | 2 |
| LIB-DB-001 | 0 | 0 | 9 |

## Failures

### LIB-ST-001 — exchange #7 (updateLoanStatus)

forbidden transition REQUESTED -> ISSUED was accepted with HTTP 200 (allowed from REQUESTED: [APPROVED, REJECTED])

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/loans/306a3f28-c2ca-4361-a120-ecfc963aeb4a/status",
    "body" : {
      "status" : "ISSUED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "306a3f28-c2ca-4361-a120-ecfc963aeb4a",
      "issuedAt" : "2026-10-19T06:17:01.390875400Z",
      "requestedAt" : "2026-10-19T06:17:01.380713Z",
      "returnedAt" : null,
      "status" : "ISSUED"
    }
  },
  "state" : {
    "resourceId" : "306a3f28-c2ca-4361-a120-ecfc963aeb4a",
    "previous" : "REQUESTED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "ISSUED",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ "APPROVED", "REJECTED" ]
  }
}
```

