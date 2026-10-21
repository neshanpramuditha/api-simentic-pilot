# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8091 (with database access)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=40, FAIL=1, NOT_APPLICABLE=11}
- Duration: 1321 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| LIB-INV-001 | 13 | 0 | 4 |
| LIB-INV-002 | 13 | 0 | 4 |
| LIB-ST-001 | 6 | 1 | 2 |
| LIB-DB-001 | 8 | 0 | 1 |

## Failures

### LIB-ST-001 — exchange #7 (updateLoanStatus)

forbidden transition REQUESTED -> ISSUED was accepted with HTTP 200 (allowed from REQUESTED: [APPROVED, REJECTED])

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/loans/c66d4842-db9b-45cb-9a0c-85d3f8df1cbb/status",
    "body" : {
      "status" : "ISSUED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "c66d4842-db9b-45cb-9a0c-85d3f8df1cbb",
      "issuedAt" : "2026-10-19T06:15:56.433540800Z",
      "requestedAt" : "2026-10-19T06:15:56.400948Z",
      "returnedAt" : null,
      "status" : "ISSUED"
    }
  },
  "state" : {
    "resourceId" : "c66d4842-db9b-45cb-9a0c-85d3f8df1cbb",
    "previous" : "REQUESTED",
    "previousSource" : "read-back before request",
    "readBack" : {
      "operationId" : "getLoan",
      "url" : "/api/loans/c66d4842-db9b-45cb-9a0c-85d3f8df1cbb",
      "status" : 200,
      "body" : {
        "memberId" : 42,
        "bookId" : 1002,
        "id" : "c66d4842-db9b-45cb-9a0c-85d3f8df1cbb",
        "issuedAt" : null,
        "requestedAt" : "2026-10-19T06:15:56.400948Z",
        "returnedAt" : null,
        "status" : "REQUESTED"
      }
    },
    "requested" : "ISSUED",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ "APPROVED", "REJECTED" ]
  }
}
```

