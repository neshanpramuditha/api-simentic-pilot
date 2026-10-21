# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8091 (with database access)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=34, FAIL=5, NOT_APPLICABLE=13}
- Duration: 1739 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| LIB-INV-001 | 10 | 2 | 5 |
| LIB-INV-002 | 12 | 0 | 5 |
| LIB-ST-001 | 7 | 0 | 2 |
| LIB-DB-001 | 5 | 3 | 1 |

## Failures

### LIB-DB-001 — exchange #4 (updateLoanStatus)

request was rejected with HTTP 409 but the row changed: before {"status":"RETURNED","issued_at":"2026-10-19T06:17:18.978406Z","returned_at":"2026-10-19T06:17:19.008062Z"}, after {"status":"ISSUED","issued_at":"2026-10-19T06:17:19.048093Z","returned_at":"2026-10-19T06:17:19.008062Z"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/loans/c1dd7ca2-2e29-4770-9a98-94f5effb7903/status",
    "body" : {
      "status" : "ISSUED"
    }
  },
  "response" : {
    "status" : 409,
    "body" : {
      "timestamp" : "2026-10-19T06:17:19.057Z",
      "status" : 409,
      "error" : "Conflict",
      "path" : "/api/loans/c1dd7ca2-2e29-4770-9a98-94f5effb7903/status"
    }
  },
  "database" : {
    "query" : "SELECT status, issued_at, returned_at FROM loans WHERE id = ?",
    "params" : [ "c1dd7ca2-2e29-4770-9a98-94f5effb7903" ],
    "rowBefore" : {
      "status" : "RETURNED",
      "issued_at" : "2026-10-19T06:17:18.978406Z",
      "returned_at" : "2026-10-19T06:17:19.008062Z"
    },
    "rowAfter" : {
      "status" : "ISSUED",
      "issued_at" : "2026-10-19T06:17:19.048093Z",
      "returned_at" : "2026-10-19T06:17:19.008062Z"
    }
  }
}
```

### LIB-INV-001 — exchange #5 (getLoan)

returnedAt is set if and only if status is RETURNED — violated: {"$.returnedAt":"2026-10-19T06:17:19.008062Z","$.status":"ISSUED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/api/loans/c1dd7ca2-2e29-4770-9a98-94f5effb7903",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "c1dd7ca2-2e29-4770-9a98-94f5effb7903",
      "issuedAt" : "2026-10-19T06:17:19.048093Z",
      "requestedAt" : "2026-10-19T06:17:18.594105Z",
      "returnedAt" : "2026-10-19T06:17:19.008062Z",
      "status" : "ISSUED"
    }
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:17:19.008062Z",
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

### LIB-DB-001 — exchange #7 (updateLoanStatus)

request was rejected with HTTP 409 but the row changed: before {"status":"REQUESTED","issued_at":null,"returned_at":null}, after {"status":"ISSUED","issued_at":"2026-10-19T06:17:19.139594Z","returned_at":null}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/loans/a4f4db7d-837b-47bc-9e1c-af901d330807/status",
    "body" : {
      "status" : "ISSUED"
    }
  },
  "response" : {
    "status" : 409,
    "body" : {
      "timestamp" : "2026-10-19T06:17:19.143Z",
      "status" : 409,
      "error" : "Conflict",
      "path" : "/api/loans/a4f4db7d-837b-47bc-9e1c-af901d330807/status"
    }
  },
  "database" : {
    "query" : "SELECT status, issued_at, returned_at FROM loans WHERE id = ?",
    "params" : [ "a4f4db7d-837b-47bc-9e1c-af901d330807" ],
    "rowBefore" : {
      "status" : "REQUESTED",
      "issued_at" : null,
      "returned_at" : null
    },
    "rowAfter" : {
      "status" : "ISSUED",
      "issued_at" : "2026-10-19T06:17:19.139594Z",
      "returned_at" : null
    }
  }
}
```

### LIB-DB-001 — exchange #11 (updateLoanStatus)

request was rejected with HTTP 409 but the row changed: before {"status":"REJECTED","issued_at":null,"returned_at":null}, after {"status":"APPROVED","issued_at":null,"returned_at":null}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/loans/33ffa6b4-c1ed-41d9-a780-5b6599645e53/status",
    "body" : {
      "status" : "APPROVED"
    }
  },
  "response" : {
    "status" : 409,
    "body" : {
      "timestamp" : "2026-10-19T06:17:19.225Z",
      "status" : 409,
      "error" : "Conflict",
      "path" : "/api/loans/33ffa6b4-c1ed-41d9-a780-5b6599645e53/status"
    }
  },
  "database" : {
    "query" : "SELECT status, issued_at, returned_at FROM loans WHERE id = ?",
    "params" : [ "33ffa6b4-c1ed-41d9-a780-5b6599645e53" ],
    "rowBefore" : {
      "status" : "REJECTED",
      "issued_at" : null,
      "returned_at" : null
    },
    "rowAfter" : {
      "status" : "APPROVED",
      "issued_at" : null,
      "returned_at" : null
    }
  }
}
```

### LIB-INV-001 — exchange #12 (listMemberLoans)

returnedAt is set if and only if status is RETURNED — violated by item 0: {"$.returnedAt":"2026-10-19T06:17:19.008062Z","$.status":"ISSUED"}

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
      "id" : "c1dd7ca2-2e29-4770-9a98-94f5effb7903",
      "issuedAt" : "2026-10-19T06:17:19.048093Z",
      "requestedAt" : "2026-10-19T06:17:18.594105Z",
      "returnedAt" : "2026-10-19T06:17:19.008062Z",
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "a4f4db7d-837b-47bc-9e1c-af901d330807",
      "issuedAt" : "2026-10-19T06:17:19.139594Z",
      "requestedAt" : "2026-10-19T06:17:19.100435Z",
      "returnedAt" : null,
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1003,
      "id" : "33ffa6b4-c1ed-41d9-a780-5b6599645e53",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:17:19.163990Z",
      "returnedAt" : null,
      "status" : "APPROVED"
    } ]
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:17:19.008062Z",
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

