# Semantic validation report

- Mode: check-har
- Source: HAR traffic.har.json (passive: no read-backs, no database)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=29, FAIL=2, NOT_APPLICABLE=21}
- Duration: 50 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| LIB-INV-001 | 10 | 2 | 5 |
| LIB-INV-002 | 12 | 0 | 5 |
| LIB-ST-001 | 7 | 0 | 2 |
| LIB-DB-001 | 0 | 0 | 9 |

## Failures

### LIB-INV-001 — exchange #5 (getLoan)

returnedAt is set if and only if status is RETURNED — violated: {"$.returnedAt":"2026-10-19T06:18:41.597927Z","$.status":"ISSUED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/api/loans/4b5d7282-201c-4fea-a63f-e97ef429d815",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "4b5d7282-201c-4fea-a63f-e97ef429d815",
      "issuedAt" : "2026-10-19T06:18:41.607917Z",
      "requestedAt" : "2026-10-19T06:18:41.224472Z",
      "returnedAt" : "2026-10-19T06:18:41.597927Z",
      "status" : "ISSUED"
    }
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:18:41.597927Z",
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

returnedAt is set if and only if status is RETURNED — violated by item 0: {"$.returnedAt":"2026-10-19T06:18:41.597927Z","$.status":"ISSUED"}

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
      "id" : "4b5d7282-201c-4fea-a63f-e97ef429d815",
      "issuedAt" : "2026-10-19T06:18:41.607917Z",
      "requestedAt" : "2026-10-19T06:18:41.224472Z",
      "returnedAt" : "2026-10-19T06:18:41.597927Z",
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "ee4ed37d-0a84-4990-bb7f-b530c459ed0f",
      "issuedAt" : "2026-10-19T06:18:41.677929Z",
      "requestedAt" : "2026-10-19T06:18:41.665966Z",
      "returnedAt" : null,
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1003,
      "id" : "16c397fd-c2ac-4d36-acbc-b446a77f2f0f",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:18:41.701Z",
      "returnedAt" : null,
      "status" : "APPROVED"
    } ]
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:18:41.597927Z",
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

