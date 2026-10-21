# Semantic validation report

- Mode: check-har
- Source: HAR har-20261019T093319Z.json (passive: no read-backs, no database)
- Rule set: order-api semantic rules v1
- Exchanges: 547, unmatched requests: 28
- Totals: {PASS=250, FAIL=22, NOT_APPLICABLE=502}
- Duration: 196 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| ORD-INV-001 | 180 | 16 | 370 |
| ORD-ST-001 | 70 | 6 | 28 |
| ORD-DB-001 | 0 | 0 | 104 |

## Failures

### ORD-INV-001 — exchange #57 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:21.327608","$.status":"PAID"}

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:21.327608",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:21.327608",
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

### ORD-INV-001 — exchange #59 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:21.327608","$.status":"PAID"}

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:21.327608",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:21.327608",
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

### ORD-INV-001 — exchange #84 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:21.327608","$.status":"CANCELLED"}

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:21.327608",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:21.327608",
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

### ORD-INV-001 — exchange #89 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:23.886434","$.status":"PAID"}

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:23.886434",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:23.886434",
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

### ORD-ST-001 — exchange #89 (updateStatus)

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:23.886434",
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

### ORD-INV-001 — exchange #153 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:23.886434","$.status":"PAID"}

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:23.886434",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:23.886434",
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

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:23.886434","$.status":"PAID"}

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:23.886434",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:23.886434",
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

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:23.886434","$.status":"PAID"}

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:23.886434",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:23.886434",
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

### ORD-INV-001 — exchange #168 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:23.886434","$.status":"PAID"}

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:23.886434",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:23.886434",
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

### ORD-INV-001 — exchange #199 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:23.886434","$.status":"CANCELLED"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/1/status",
    "body" : {
      "x60" : true,
      "" : {
        "Ⱥ" : 4.91110724710864E222
      },
      "W" : 10655876,
      "status" : "CANCELLED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:23.886434",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:23.886434",
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

### ORD-INV-001 — exchange #205 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:25.50879","$.status":"CANCELLED"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/31/status",
    "body" : {
      "FALSE" : { },
      "1􆩛\u0011w-w񓘙" : [ null, {
        "+󒐠Å³ÍY\u0001򴑈Ý" : true,
        "¸" : { },
        "´¼p򲱭¹" : [ ]
      }, [ ] ],
      "\u0013jQ" : [ ],
      "ÌýVLv򭟯\fª񤋟Õ" : {
        "e" : 1.0E-5,
        "󻷐" : 425,
        "򼺤B\u0011ôõZÝMk" : 340
      },
      "" : {
        "Üø􆏱C󔩡³·\u0007𦧱xÿ~\u0018iVm" : null
      },
      "status" : "CANCELLED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:33:25.50879",
      "id" : 31,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:25.50879",
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

### ORD-INV-001 — exchange #219 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:23.886434","$.status":"CANCELLED"}

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:23.886434",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:23.886434",
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

### ORD-ST-001 — exchange #219 (updateStatus)

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:23.886434",
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

### ORD-INV-001 — exchange #220 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:25.940859","$.status":"PAID"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/31/status",
    "body" : {
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:33:25.940859",
      "id" : 31,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:25.940859",
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

### ORD-ST-001 — exchange #220 (updateStatus)

forbidden transition CANCELLED -> PAID was accepted with HTTP 200 (allowed from CANCELLED: [])

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/31/status",
    "body" : {
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:33:25.940859",
      "id" : 31,
      "status" : "PAID"
    }
  },
  "state" : {
    "resourceId" : "31",
    "previous" : "CANCELLED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "PAID",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-INV-001 — exchange #226 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:26.18663","$.status":"PAID"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/32/status",
    "body" : {
      "" : { },
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : "2026-10-19T09:33:26.18663",
      "id" : 32,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:26.18663",
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

### ORD-ST-001 — exchange #226 (updateStatus)

forbidden transition CANCELLED -> PAID was accepted with HTTP 200 (allowed from CANCELLED: [])

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/32/status",
    "body" : {
      "" : { },
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 1.0,
      "completedAt" : "2026-10-19T09:33:26.18663",
      "id" : 32,
      "status" : "PAID"
    }
  },
  "state" : {
    "resourceId" : "32",
    "previous" : "CANCELLED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "PAID",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-INV-001 — exchange #229 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:23.886434","$.status":"PAID"}

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:23.886434",
      "id" : 1,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:23.886434",
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

### ORD-ST-001 — exchange #229 (updateStatus)

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:33:23.886434",
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

### ORD-INV-001 — exchange #230 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:25.940859","$.status":"PAID"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/31/status",
    "body" : {
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:33:25.940859",
      "id" : 31,
      "status" : "PAID"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:25.940859",
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

forbidden transition PAID -> PAID was accepted with HTTP 200 (allowed from PAID: [SHIPPED, CANCELLED])

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/31/status",
    "body" : {
      "status" : "PAID"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:33:25.940859",
      "id" : 31,
      "status" : "PAID"
    }
  },
  "state" : {
    "resourceId" : "31",
    "previous" : "PAID",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "PAID",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ "SHIPPED", "CANCELLED" ]
  }
}
```

### ORD-INV-001 — exchange #240 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:33:25.940859","$.status":"CANCELLED"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/31/status",
    "body" : {
      "status" : "CANCELLED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 7,
      "total" : 49.99,
      "completedAt" : "2026-10-19T09:33:25.940859",
      "id" : 31,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:33:25.940859",
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

