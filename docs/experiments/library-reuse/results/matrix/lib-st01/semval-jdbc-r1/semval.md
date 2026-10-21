# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8091 (with database access)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=40, FAIL=1, NOT_APPLICABLE=11}
- Duration: 1397 ms

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
    "url" : "/api/loans/54e412bb-8520-4ed5-8fd9-87fea22aa24a/status",
    "body" : {
      "status" : "ISSUED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "54e412bb-8520-4ed5-8fd9-87fea22aa24a",
      "issuedAt" : "2026-10-19T06:15:36.711100700Z",
      "requestedAt" : "2026-10-19T06:15:36.676020Z",
      "returnedAt" : null,
      "status" : "ISSUED"
    }
  },
  "state" : {
    "resourceId" : "54e412bb-8520-4ed5-8fd9-87fea22aa24a",
    "previous" : "REQUESTED",
    "previousSource" : "read-back before request",
    "readBack" : {
      "operationId" : "getLoan",
      "url" : "/api/loans/54e412bb-8520-4ed5-8fd9-87fea22aa24a",
      "status" : 200,
      "body" : {
        "memberId" : 42,
        "bookId" : 1002,
        "id" : "54e412bb-8520-4ed5-8fd9-87fea22aa24a",
        "issuedAt" : null,
        "requestedAt" : "2026-10-19T06:15:36.676020Z",
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

