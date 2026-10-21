# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8091 (no database access)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=29, FAIL=2, NOT_APPLICABLE=21}
- Duration: 1855 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| LIB-INV-001 | 10 | 2 | 5 |
| LIB-INV-002 | 12 | 0 | 5 |
| LIB-ST-001 | 7 | 0 | 2 |
| LIB-DB-001 | 0 | 0 | 9 |

## Failures

### LIB-INV-001 — exchange #5 (getLoan)

returnedAt is set if and only if status is RETURNED — violated: {"$.returnedAt":"2026-10-19T06:17:51.685694Z","$.status":"ISSUED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/api/loans/4a7886da-b686-416f-a86f-cf6c8b58b05b",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "4a7886da-b686-416f-a86f-cf6c8b58b05b",
      "issuedAt" : "2026-10-19T06:17:51.708442Z",
      "requestedAt" : "2026-10-19T06:17:51.269318Z",
      "returnedAt" : "2026-10-19T06:17:51.685694Z",
      "status" : "ISSUED"
    }
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:17:51.685694Z",
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

returnedAt is set if and only if status is RETURNED — violated by item 0: {"$.returnedAt":"2026-10-19T06:17:51.685694Z","$.status":"ISSUED"}

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
      "id" : "4a7886da-b686-416f-a86f-cf6c8b58b05b",
      "issuedAt" : "2026-10-19T06:17:51.708442Z",
      "requestedAt" : "2026-10-19T06:17:51.269318Z",
      "returnedAt" : "2026-10-19T06:17:51.685694Z",
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "69aab428-e1dc-4a8c-a5d6-f2ce1dc712c8",
      "issuedAt" : "2026-10-19T06:17:51.791297Z",
      "requestedAt" : "2026-10-19T06:17:51.762144Z",
      "returnedAt" : null,
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1003,
      "id" : "1a84dbb6-5e6d-4ffd-b92e-ab13f0e892f3",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:17:51.818684Z",
      "returnedAt" : null,
      "status" : "APPROVED"
    } ]
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:17:51.685694Z",
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

