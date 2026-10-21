# Semantic validation report

- Mode: check-har
- Source: HAR traffic.har.json (passive: no read-backs, no database)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=28, FAIL=3, NOT_APPLICABLE=21}
- Duration: 71 ms

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
    "url" : "/api/loans/e344d0fd-b04e-478e-91f2-45c4aaaafac9/status",
    "body" : {
      "status" : "RETURNED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "e344d0fd-b04e-478e-91f2-45c4aaaafac9",
      "issuedAt" : "2026-10-19T06:20:16.534976Z",
      "requestedAt" : "2026-10-19T06:20:16.248248Z",
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
    "url" : "/api/loans/e344d0fd-b04e-478e-91f2-45c4aaaafac9",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "e344d0fd-b04e-478e-91f2-45c4aaaafac9",
      "issuedAt" : "2026-10-19T06:20:16.534976Z",
      "requestedAt" : "2026-10-19T06:20:16.248248Z",
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
      "id" : "e344d0fd-b04e-478e-91f2-45c4aaaafac9",
      "issuedAt" : "2026-10-19T06:20:16.534976Z",
      "requestedAt" : "2026-10-19T06:20:16.248248Z",
      "returnedAt" : null,
      "status" : "RETURNED"
    }, {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "69684ee2-f0f5-411c-af4f-6d94e9f9bbf4",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:20:16.622315Z",
      "returnedAt" : null,
      "status" : "REQUESTED"
    }, {
      "memberId" : 42,
      "bookId" : 1003,
      "id" : "128ca4fc-c876-41b9-a46f-698eeead5dbd",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:20:16.656755Z",
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

