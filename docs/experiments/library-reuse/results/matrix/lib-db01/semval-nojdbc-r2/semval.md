# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8091 (no database access)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=29, FAIL=2, NOT_APPLICABLE=21}
- Duration: 1912 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| LIB-INV-001 | 10 | 2 | 5 |
| LIB-INV-002 | 12 | 0 | 5 |
| LIB-ST-001 | 7 | 0 | 2 |
| LIB-DB-001 | 0 | 0 | 9 |

## Failures

### LIB-INV-001 — exchange #5 (getLoan)

returnedAt is set if and only if status is RETURNED — violated: {"$.returnedAt":"2026-10-19T06:18:08.727702Z","$.status":"ISSUED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/api/loans/600cb76a-7790-4618-b096-9a862a03dd76",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "600cb76a-7790-4618-b096-9a862a03dd76",
      "issuedAt" : "2026-10-19T06:18:08.757800Z",
      "requestedAt" : "2026-10-19T06:18:08.327836Z",
      "returnedAt" : "2026-10-19T06:18:08.727702Z",
      "status" : "ISSUED"
    }
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:18:08.727702Z",
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

returnedAt is set if and only if status is RETURNED — violated by item 0: {"$.returnedAt":"2026-10-19T06:18:08.727702Z","$.status":"ISSUED"}

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
      "id" : "600cb76a-7790-4618-b096-9a862a03dd76",
      "issuedAt" : "2026-10-19T06:18:08.757800Z",
      "requestedAt" : "2026-10-19T06:18:08.327836Z",
      "returnedAt" : "2026-10-19T06:18:08.727702Z",
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "650a892b-8eca-4e15-8e62-bb76957ae950",
      "issuedAt" : "2026-10-19T06:18:08.859955Z",
      "requestedAt" : "2026-10-19T06:18:08.827938Z",
      "returnedAt" : null,
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1003,
      "id" : "ac848965-415f-4c48-bff5-bd671a7de5a2",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:18:08.890015Z",
      "returnedAt" : null,
      "status" : "APPROVED"
    } ]
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:18:08.727702Z",
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

