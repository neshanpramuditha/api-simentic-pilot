# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8091 (no database access)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=32, FAIL=1, NOT_APPLICABLE=19}
- Duration: 1544 ms

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
    "url" : "/api/loans/27a7bce8-d9fc-4848-a408-ca9e1895d728/status",
    "body" : {
      "status" : "ISSUED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "27a7bce8-d9fc-4848-a408-ca9e1895d728",
      "issuedAt" : "2026-10-19T06:16:12.850061500Z",
      "requestedAt" : "2026-10-19T06:16:12.798026Z",
      "returnedAt" : null,
      "status" : "ISSUED"
    }
  },
  "state" : {
    "resourceId" : "27a7bce8-d9fc-4848-a408-ca9e1895d728",
    "previous" : "REQUESTED",
    "previousSource" : "read-back before request",
    "readBack" : {
      "operationId" : "getLoan",
      "url" : "/api/loans/27a7bce8-d9fc-4848-a408-ca9e1895d728",
      "status" : 200,
      "body" : {
        "memberId" : 42,
        "bookId" : 1002,
        "id" : "27a7bce8-d9fc-4848-a408-ca9e1895d728",
        "issuedAt" : null,
        "requestedAt" : "2026-10-19T06:16:12.798026Z",
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

