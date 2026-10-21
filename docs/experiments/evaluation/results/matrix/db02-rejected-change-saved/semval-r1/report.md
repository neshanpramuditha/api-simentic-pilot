# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8080 (with database access)
- Rule set: order-api semantic rules v1
- Exchanges: 11, unmatched requests: 0
- Totals: {PASS=18, FAIL=4, NOT_APPLICABLE=2}
- Duration: 691 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| ORD-INV-001 | 8 | 2 | 2 |
| ORD-ST-001 | 6 | 0 | 0 |
| ORD-DB-001 | 4 | 2 | 0 |

## Failures

### ORD-DB-001 — exchange #5 (updateStatus)

request was rejected with HTTP 409 but the row changed: before {"status":"COMPLETED","completed_at":"2026-10-19 09:30:34.467203"}, after {"status":"CANCELLED","completed_at":"2026-10-19 09:30:34.467203"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/1/status",
    "body" : {
      "status" : "CANCELLED"
    }
  },
  "response" : {
    "status" : 409,
    "body" : {
      "timestamp" : "2026-10-19T04:00:34.496Z",
      "status" : 409,
      "error" : "Conflict",
      "path" : "/api/orders/1/status"
    }
  },
  "database" : {
    "query" : "SELECT status, completed_at FROM orders WHERE id = ?",
    "params" : [ "1" ],
    "rowBefore" : {
      "status" : "COMPLETED",
      "completed_at" : "2026-10-19 09:30:34.467203"
    },
    "rowAfter" : {
      "status" : "CANCELLED",
      "completed_at" : "2026-10-19 09:30:34.467203"
    }
  }
}
```

### ORD-INV-001 — exchange #6 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:30:34.467203","$.status":"CANCELLED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/api/orders/1",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:30:34.467203",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:30:34.467203",
    "$.status" : "CANCELLED"
  },
  "assertion" : {
    "iff" : [ {
      "path" : "$.completedAt",
      "notNull" : true
    }, {
      "path" : "$.status",
      "equals" : "COMPLETED"
    } ]
  }
}
```

### ORD-DB-001 — exchange #8 (updateStatus)

request was rejected with HTTP 409 but the row changed: before {"status":"CREATED","completed_at":null}, after {"status":"DELIVERED","completed_at":null}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/2/status",
    "body" : {
      "status" : "DELIVERED"
    }
  },
  "response" : {
    "status" : 409,
    "body" : {
      "timestamp" : "2026-10-19T04:00:34.533Z",
      "status" : 409,
      "error" : "Conflict",
      "path" : "/api/orders/2/status"
    }
  },
  "database" : {
    "query" : "SELECT status, completed_at FROM orders WHERE id = ?",
    "params" : [ "2" ],
    "rowBefore" : {
      "status" : "CREATED",
      "completed_at" : null
    },
    "rowAfter" : {
      "status" : "DELIVERED",
      "completed_at" : null
    }
  }
}
```

### ORD-INV-001 — exchange #10 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:30:34.467203","$.status":"CANCELLED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/api/users/7/orders",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : [ {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:30:34.467203",
      "id" : 1,
      "status" : "CANCELLED"
    }, {
      "ownerUserId" : 7,
      "total" : 10.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "DELIVERED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:30:34.467203",
    "$.status" : "CANCELLED"
  },
  "itemIndex" : 0,
  "assertion" : {
    "iff" : [ {
      "path" : "$.completedAt",
      "notNull" : true
    }, {
      "path" : "$.status",
      "equals" : "COMPLETED"
    } ]
  }
}
```

