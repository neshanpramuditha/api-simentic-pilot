# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8091 (with database access)
- Rule set: library-api semantic rules v1
- Exchanges: 15, unmatched requests: 0
- Totals: {PASS=34, FAIL=5, NOT_APPLICABLE=13}
- Duration: 1879 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| LIB-INV-001 | 10 | 2 | 5 |
| LIB-INV-002 | 12 | 0 | 5 |
| LIB-ST-001 | 7 | 0 | 2 |
| LIB-DB-001 | 5 | 3 | 1 |

## Failures

### LIB-DB-001 — exchange #4 (updateLoanStatus)

request was rejected with HTTP 409 but the row changed: before {"status":"RETURNED","issued_at":"2026-10-19T06:17:34.757916Z","returned_at":"2026-10-19T06:17:34.791044Z"}, after {"status":"ISSUED","issued_at":"2026-10-19T06:17:34.827881Z","returned_at":"2026-10-19T06:17:34.791044Z"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/loans/2c51f6af-db1f-4b41-8951-6b43bda4696a/status",
    "body" : {
      "status" : "ISSUED"
    }
  },
  "response" : {
    "status" : 409,
    "body" : {
      "timestamp" : "2026-10-19T06:17:34.838Z",
      "status" : 409,
      "error" : "Conflict",
      "path" : "/api/loans/2c51f6af-db1f-4b41-8951-6b43bda4696a/status"
    }
  },
  "database" : {
    "query" : "SELECT status, issued_at, returned_at FROM loans WHERE id = ?",
    "params" : [ "2c51f6af-db1f-4b41-8951-6b43bda4696a" ],
    "rowBefore" : {
      "status" : "RETURNED",
      "issued_at" : "2026-10-19T06:17:34.757916Z",
      "returned_at" : "2026-10-19T06:17:34.791044Z"
    },
    "rowAfter" : {
      "status" : "ISSUED",
      "issued_at" : "2026-10-19T06:17:34.827881Z",
      "returned_at" : "2026-10-19T06:17:34.791044Z"
    }
  }
}
```

### LIB-INV-001 — exchange #5 (getLoan)

returnedAt is set if and only if status is RETURNED — violated: {"$.returnedAt":"2026-10-19T06:17:34.791044Z","$.status":"ISSUED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/api/loans/2c51f6af-db1f-4b41-8951-6b43bda4696a",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "memberId" : 42,
      "bookId" : 1001,
      "id" : "2c51f6af-db1f-4b41-8951-6b43bda4696a",
      "issuedAt" : "2026-10-19T06:17:34.827881Z",
      "requestedAt" : "2026-10-19T06:17:34.394053Z",
      "returnedAt" : "2026-10-19T06:17:34.791044Z",
      "status" : "ISSUED"
    }
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:17:34.791044Z",
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

request was rejected with HTTP 409 but the row changed: before {"status":"REQUESTED","issued_at":null,"returned_at":null}, after {"status":"ISSUED","issued_at":"2026-10-19T06:17:34.905170Z","returned_at":null}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/loans/f063e4fa-0eaf-4efe-ba4b-f517959a0fae/status",
    "body" : {
      "status" : "ISSUED"
    }
  },
  "response" : {
    "status" : 409,
    "body" : {
      "timestamp" : "2026-10-19T06:17:34.912Z",
      "status" : 409,
      "error" : "Conflict",
      "path" : "/api/loans/f063e4fa-0eaf-4efe-ba4b-f517959a0fae/status"
    }
  },
  "database" : {
    "query" : "SELECT status, issued_at, returned_at FROM loans WHERE id = ?",
    "params" : [ "f063e4fa-0eaf-4efe-ba4b-f517959a0fae" ],
    "rowBefore" : {
      "status" : "REQUESTED",
      "issued_at" : null,
      "returned_at" : null
    },
    "rowAfter" : {
      "status" : "ISSUED",
      "issued_at" : "2026-10-19T06:17:34.905170Z",
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
    "url" : "/api/loans/1b48755c-6f11-46a9-9be8-4feb34ea3319/status",
    "body" : {
      "status" : "APPROVED"
    }
  },
  "response" : {
    "status" : 409,
    "body" : {
      "timestamp" : "2026-10-19T06:17:34.991Z",
      "status" : 409,
      "error" : "Conflict",
      "path" : "/api/loans/1b48755c-6f11-46a9-9be8-4feb34ea3319/status"
    }
  },
  "database" : {
    "query" : "SELECT status, issued_at, returned_at FROM loans WHERE id = ?",
    "params" : [ "1b48755c-6f11-46a9-9be8-4feb34ea3319" ],
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

returnedAt is set if and only if status is RETURNED — violated by item 0: {"$.returnedAt":"2026-10-19T06:17:34.791044Z","$.status":"ISSUED"}

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
      "id" : "2c51f6af-db1f-4b41-8951-6b43bda4696a",
      "issuedAt" : "2026-10-19T06:17:34.827881Z",
      "requestedAt" : "2026-10-19T06:17:34.394053Z",
      "returnedAt" : "2026-10-19T06:17:34.791044Z",
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1002,
      "id" : "f063e4fa-0eaf-4efe-ba4b-f517959a0fae",
      "issuedAt" : "2026-10-19T06:17:34.905170Z",
      "requestedAt" : "2026-10-19T06:17:34.877854Z",
      "returnedAt" : null,
      "status" : "ISSUED"
    }, {
      "memberId" : 42,
      "bookId" : 1003,
      "id" : "1b48755c-6f11-46a9-9be8-4feb34ea3319",
      "issuedAt" : null,
      "requestedAt" : "2026-10-19T06:17:34.935896Z",
      "returnedAt" : null,
      "status" : "APPROVED"
    } ]
  },
  "values" : {
    "$.returnedAt" : "2026-10-19T06:17:34.791044Z",
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

