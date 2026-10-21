# Semantic validation report

- Mode: check-har
- Source: HAR har-20261019T093245Z.json (passive: no read-backs, no database)
- Rule set: order-api semantic rules v1
- Exchanges: 553, unmatched requests: 28
- Totals: {PASS=175, FAIL=78, NOT_APPLICABLE=537}
- Duration: 186 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| ORD-INV-001 | 118 | 70 | 384 |
| ORD-ST-001 | 57 | 8 | 44 |
| ORD-DB-001 | 0 | 0 | 109 |

## Failures

### ORD-INV-001 — exchange #87 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/1/status",
    "body" : {
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-ST-001 — exchange #87 (updateStatus)

forbidden transition CANCELLED -> PAID was accepted with HTTP 200 (allowed from CANCELLED: [])

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/1/status",
    "body" : {
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "state" : {
    "resourceId" : "1",
    "previous" : "CANCELLED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "PAID",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-INV-001 — exchange #91 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #93 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #94 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #95 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #96 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #97 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #98 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #99 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #100 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #101 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #104 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #105 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #106 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #107 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #108 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #109 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #112 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #113 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #114 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #115 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #116 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #126 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #127 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #128 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #129 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #130 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #131 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #132 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #133 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #134 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #135 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #136 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #137 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #138 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #139 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #140 (getOrdersByUser)

completedAt is set if and only if status is COMPLETED — violated by item 0: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }, {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "CREATED"
    } ]
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #142 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #144 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #147 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #149 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #151 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #153 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #155 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #157 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #159 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #161 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #163 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #165 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #167 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #169 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #171 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #173 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #175 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #177 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #179 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #181 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #183 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #185 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #187 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #189 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #191 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-INV-001 — exchange #204 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
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

### ORD-INV-001 — exchange #214 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
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

### ORD-ST-001 — exchange #214 (updateStatus)

forbidden transition CANCELLED -> CANCELLED was accepted with HTTP 200 (allowed from CANCELLED: [])

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "state" : {
    "resourceId" : "1",
    "previous" : "CANCELLED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "CANCELLED",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-ST-001 — exchange #215 (updateStatus)

forbidden transition CANCELLED -> PAID was accepted with HTTP 200 (allowed from CANCELLED: [])

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/2/status",
    "body" : {
      " 򼠀" : null,
      "" : null,
      "\u0005󘂒öÍ󦾄\u0011" : null,
      "􉏔­" : [ null, "", true ],
      "c𭊏" : [ "øÍ", null, "򜵉ý𷺓.(񶨊6ØJ|<\"񌀵¶ª®" ],
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : null,
      "id" : 2,
      "status" : "PAID"
    }
  },
  "state" : {
    "resourceId" : "2",
    "previous" : "CANCELLED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "PAID",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-INV-001 — exchange #219 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/1/status",
    "body" : {
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-ST-001 — exchange #219 (updateStatus)

forbidden transition CANCELLED -> PAID was accepted with HTTP 200 (allowed from CANCELLED: [])

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/1/status",
    "body" : {
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "state" : {
    "resourceId" : "1",
    "previous" : "CANCELLED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "PAID",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-INV-001 — exchange #220 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
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

### ORD-INV-001 — exchange #222 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
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

### ORD-ST-001 — exchange #222 (updateStatus)

forbidden transition CANCELLED -> CANCELLED was accepted with HTTP 200 (allowed from CANCELLED: [])

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "state" : {
    "resourceId" : "1",
    "previous" : "CANCELLED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "CANCELLED",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-INV-001 — exchange #225 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
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

### ORD-ST-001 — exchange #225 (updateStatus)

forbidden transition CANCELLED -> CANCELLED was accepted with HTTP 200 (allowed from CANCELLED: [])

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "state" : {
    "resourceId" : "1",
    "previous" : "CANCELLED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "CANCELLED",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-INV-001 — exchange #227 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
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

### ORD-ST-001 — exchange #227 (updateStatus)

forbidden transition CANCELLED -> CANCELLED was accepted with HTTP 200 (allowed from CANCELLED: [])

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
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "state" : {
    "resourceId" : "1",
    "previous" : "CANCELLED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "CANCELLED",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-INV-001 — exchange #230 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:32:49.876663","$.status":"PAID"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/1/status",
    "body" : {
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:32:49.876663",
    "$.status" : "PAID"
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

### ORD-ST-001 — exchange #230 (updateStatus)

forbidden transition CANCELLED -> PAID was accepted with HTTP 200 (allowed from CANCELLED: [])

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/1/status",
    "body" : {
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:32:49.876663",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "state" : {
    "resourceId" : "1",
    "previous" : "CANCELLED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "PAID",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

