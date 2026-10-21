# Semantic validation report

- Mode: check-har
- Source: HAR traffic.har.json (passive: no read-backs, no database)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=29, FAIL=2, NOT_APPLICABLE=21}
- Duration: 58 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| LIB-INV-001 | 10 | 2 | 5 |
| LIB-INV-002 | 12 | 0 | 5 |
| LIB-ST-001 | 7 | 0 | 2 |
| LIB-DB-001 | 0 | 0 | 9 |

## Failures

### LIB-INV-001 — exchange #5 (getLoan)

returnedAt is set if and only if status is RETURNED — violated: {"$.returnedAt":"2026-10-19T06:18:25.472624Z","$.status":"ISSUED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/api/loans/06925656-7f4b-4e88-86b5-25b669510d74",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "06925656-7f4b-4e88-86b5-25b669510d74",
      "issuedAt" : "2026-10-19T06:18:25.488017Z",
      "requestedAt" : "2026-10-19T06:18:25.069338Z",
      "returnedAt" : "2026-10-19T06:18:25.472624Z",
      "status" : "ISSUED"
    }
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:18:25.472624Z",
    "$.status" : "ISSUED"
  },
  "assertion" : {
    "iff" : [ {
      "path" : "$.returnedAt",
      "notNull" : true
    }, {
      "path" : "$.status",
      "equals" : "RETURNED"
    } ]
  }
}
```

### LIB-INV-001 — exchange #12 (listMemberLoans)

returnedAt is set if and only if status is RETURNED — violated by item 0: {"$.returnedAt":"2026-10-19T06:18:25.472624Z","$.status":"ISSUED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/api/members/42/loans",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : [ {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "06925656-7f4b-4e88-86b5-25b669510d74",
      "issuedAt" : "2026-10-19T06:18:25.488017Z",
      "requestedAt" : "2026-10-19T06:18:25.069338Z",
      "returnedAt" : "2026-10-19T06:18:25.472624Z",
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "13833bab-24d1-40a4-b561-40ea0a75fd7d",
      "issuedAt" : "2026-10-19T06:18:25.547954Z",
      "requestedAt" : "2026-10-19T06:18:25.531819Z",
      "returnedAt" : null,
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1003,
      "id" : "20b7ff6c-20e2-476e-8a6e-61cae42af055",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:18:25.568153Z",
      "returnedAt" : null,
      "status" : "APPROVED"
    } ]
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:18:25.472624Z",
    "$.status" : "ISSUED"
  },
  "itemIndex" : 0,
  "assertion" : {
    "iff" : [ {
      "path" : "$.returnedAt",
      "notNull" : true
    }, {
      "path" : "$.status",
      "equals" : "RETURNED"
    } ]
  }
}
```

