# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8091 (no database access)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=28, FAIL=3, NOT_APPLICABLE=21}
- Duration: 1191 ms

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
    "url" : "/api/loans/7c569c1b-e364-443a-9c12-94336dd0997c/status",
    "body" : {
      "status" : "RETURNED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "7c569c1b-e364-443a-9c12-94336dd0997c",
      "issuedAt" : "2026-10-19T06:19:27.471934Z",
      "requestedAt" : "2026-10-19T06:19:27.246909Z",
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
    "url" : "/api/loans/7c569c1b-e364-443a-9c12-94336dd0997c",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "7c569c1b-e364-443a-9c12-94336dd0997c",
      "issuedAt" : "2026-10-19T06:19:27.471934Z",
      "requestedAt" : "2026-10-19T06:19:27.246909Z",
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
      "id" : "7c569c1b-e364-443a-9c12-94336dd0997c",
      "issuedAt" : "2026-10-19T06:19:27.471934Z",
      "requestedAt" : "2026-10-19T06:19:27.246909Z",
      "returnedAt" : null,
      "status" : "RETURNED"
    }, {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "79811762-e050-41b9-adea-3068512ec6aa",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:19:27.592482Z",
      "returnedAt" : null,
      "status" : "REQUESTED"
    }, {
      "memberId" : 42,
      "bookId" : 1003,
      "id" : "9939a618-406f-4b1a-b54e-d87d9c82426f",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:19:27.662298Z",
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

