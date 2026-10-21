# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8091 (no database access)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=28, FAIL=3, NOT_APPLICABLE=21}
- Duration: 1100 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| LIB-INV-001 | 9 | 3 | 5 |
| LIB-INV-002 | 12 | 0 | 5 |
| LIB-ST-001 | 7 | 0 | 2 |
| LIB-DB-001 | 0 | 0 | 9 |

## Failures

### LIB-INV-001 — exchange #3 (updateLoanStatus)

returnedAt is set if and only if status is RETURNED — violated: {"$.returnedAt":null,"$.status":"RETURNED"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/loans/f42928f9-a398-48bc-9956-577b5edfbb0c/status",
    "body" : {
      "status" : "RETURNED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "f42928f9-a398-48bc-9956-577b5edfbb0c",
      "issuedAt" : "2026-10-19T06:19:43.919674Z",
      "requestedAt" : "2026-10-19T06:19:43.699889Z",
      "returnedAt" : null,
      "status" : "RETURNED"
    }
  },
  "values" : {
    "$.returnedAt" : null,
    "$.status" : "RETURNED"
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

### LIB-INV-001 — exchange #5 (getLoan)

returnedAt is set if and only if status is RETURNED — violated: {"$.returnedAt":null,"$.status":"RETURNED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/api/loans/f42928f9-a398-48bc-9956-577b5edfbb0c",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "f42928f9-a398-48bc-9956-577b5edfbb0c",
      "issuedAt" : "2026-10-19T06:19:43.919674Z",
      "requestedAt" : "2026-10-19T06:19:43.699889Z",
      "returnedAt" : null,
      "status" : "RETURNED"
    }
  },
  "values" : {
    "$.returnedAt" : null,
    "$.status" : "RETURNED"
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

returnedAt is set if and only if status is RETURNED — violated by item 0: {"$.returnedAt":null,"$.status":"RETURNED"}

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
      "id" : "f42928f9-a398-48bc-9956-577b5edfbb0c",
      "issuedAt" : "2026-10-19T06:19:43.919674Z",
      "requestedAt" : "2026-10-19T06:19:43.699889Z",
      "returnedAt" : null,
      "status" : "RETURNED"
    }, {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "66082029-ef68-45c8-8677-dca4b487f231",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:19:44.018048Z",
      "returnedAt" : null,
      "status" : "REQUESTED"
    }, {
      "memberId" : 42,
      "bookId" : 1003,
      "id" : "a90b632d-6778-43d2-bf76-b93710d7cb24",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:19:44.073213Z",
      "returnedAt" : null,
      "status" : "REJECTED"
    } ]
  },
  "values" : {
    "$.returnedAt" : null,
    "$.status" : "RETURNED"
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

