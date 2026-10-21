# Semantic validation report

- Mode: check-har
- Source: HAR traffic.har.json (passive: no read-backs, no database)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=28, FAIL=3, NOT_APPLICABLE=21}
- Duration: 60 ms

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
    "url" : "/api/loans/3784fbe3-3850-4845-a7c2-0bd15aadbedd/status",
    "body" : {
      "status" : "RETURNED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "3784fbe3-3850-4845-a7c2-0bd15aadbedd",
      "issuedAt" : "2026-10-19T06:19:59.811899Z",
      "requestedAt" : "2026-10-19T06:19:59.508926Z",
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
    "url" : "/api/loans/3784fbe3-3850-4845-a7c2-0bd15aadbedd",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "3784fbe3-3850-4845-a7c2-0bd15aadbedd",
      "issuedAt" : "2026-10-19T06:19:59.811899Z",
      "requestedAt" : "2026-10-19T06:19:59.508926Z",
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
      "id" : "3784fbe3-3850-4845-a7c2-0bd15aadbedd",
      "issuedAt" : "2026-10-19T06:19:59.811899Z",
      "requestedAt" : "2026-10-19T06:19:59.508926Z",
      "returnedAt" : null,
      "status" : "RETURNED"
    }, {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "1a533f2a-ccc4-4d47-aa15-e8f06faff46a",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:19:59.892510Z",
      "returnedAt" : null,
      "status" : "REQUESTED"
    }, {
      "memberId" : 42,
      "bookId" : 1003,
      "id" : "c814a212-d43f-497d-9f0b-58e7948d5241",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:19:59.917916Z",
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

