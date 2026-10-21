# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8091 (with database access)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=36, FAIL=3, NOT_APPLICABLE=13}
- Duration: 1119 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| LIB-INV-001 | 9 | 3 | 5 |
| LIB-INV-002 | 12 | 0 | 5 |
| LIB-ST-001 | 7 | 0 | 2 |
| LIB-DB-001 | 8 | 0 | 1 |

## Failures

### LIB-INV-001 — exchange #3 (updateLoanStatus)

returnedAt is set if and only if status is RETURNED — violated: {"$.returnedAt":null,"$.status":"RETURNED"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/loans/542d6f23-d208-4b70-98ee-36277dc5545b/status",
    "body" : {
      "status" : "RETURNED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "542d6f23-d208-4b70-98ee-36277dc5545b",
      "issuedAt" : "2026-10-19T06:19:13.398043Z",
      "requestedAt" : "2026-10-19T06:19:13.191970Z",
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
    "url" : "/api/loans/542d6f23-d208-4b70-98ee-36277dc5545b",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "542d6f23-d208-4b70-98ee-36277dc5545b",
      "issuedAt" : "2026-10-19T06:19:13.398043Z",
      "requestedAt" : "2026-10-19T06:19:13.191970Z",
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
      "id" : "542d6f23-d208-4b70-98ee-36277dc5545b",
      "issuedAt" : "2026-10-19T06:19:13.398043Z",
      "requestedAt" : "2026-10-19T06:19:13.191970Z",
      "returnedAt" : null,
      "status" : "RETURNED"
    }, {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "6ee428d2-8191-46d4-910e-75049b41a4c7",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:19:13.496678Z",
      "returnedAt" : null,
      "status" : "REQUESTED"
    }, {
      "memberId" : 42,
      "bookId" : 1003,
      "id" : "d1fce79e-7a68-4052-b1dc-377aa81c74da",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:19:13.557161Z",
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

