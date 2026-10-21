# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8080 (with database access)
- Rule set: order-api semantic rules v1
- Exchanges: 11, unmatched requests: 0
- Totals: {PASS=19, FAIL=4, NOT_APPLICABLE=1}
- Duration: 807 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| ORD-INV-001 | 8 | 3 | 1 |
| ORD-ST-001 | 5 | 1 | 0 |
| ORD-DB-001 | 6 | 0 | 0 |

## Failures

### ORD-INV-001 — exchange #5 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:28:40.336552","$.status":"CANCELLED"}

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
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:28:40.336552",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:28:40.336552",
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

### ORD-ST-001 — exchange #5 (updateStatus)

forbidden transition COMPLETED -> CANCELLED was accepted with HTTP 200 (allowed from COMPLETED: [])

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
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:28:40.336552",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "state" : {
    "resourceId" : "1",
    "previous" : "COMPLETED",
    "previousSource" : "read-back before request",
    "readBack" : {
      "operationId" : "getOrder",
      "url" : "/api/orders/1",
      "status" : 200,
      "body" : {
        "ownerUserId" : 7,
        "total" : 49.99,
        "completedAt" : "2026-10-19T09:28:40.336552",
        "id" : 1,
        "status" : "COMPLETED"
      }
    },
    "requested" : "CANCELLED",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-INV-001 — exchange #6 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:28:40.336552","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:28:40.336552",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:28:40.336552",
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

### ORD-INV-001 — exchange #10 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:28:40.336552","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:28:40.336552",
      "id" : 1,
      "status" : "CANCELLED"
    }, {
      "ownerUserId" : 7,
      "total" : 10.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:28:40.336552",
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

