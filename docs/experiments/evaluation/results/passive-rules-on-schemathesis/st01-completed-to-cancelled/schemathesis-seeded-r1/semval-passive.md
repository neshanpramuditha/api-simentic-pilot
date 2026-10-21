# Semantic validation report

- Mode: check-har
- Source: HAR har-20261019T092734Z.json (passive: no read-backs, no database)
- Rule set: order-api semantic rules v1
- Exchanges: 547, unmatched requests: 28
- Totals: {PASS=293, FAIL=7, NOT_APPLICABLE=530}
- Duration: 181 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| ORD-INV-001 | 194 | 6 | 366 |
| ORD-ST-001 | 99 | 1 | 32 |
| ORD-DB-001 | 0 | 0 | 132 |

## Failures

### ORD-INV-001 — exchange #84 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:27:32.563201","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:27:32.563201",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:27:32.563201",
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

### ORD-ST-001 — exchange #84 (updateStatus)

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
      "ownerUserId" : 1000,
      "total" : 10.0,
      "completedAt" : "2026-10-19T09:27:32.563201",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "state" : {
    "resourceId" : "1",
    "previous" : "COMPLETED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "CANCELLED",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-INV-001 — exchange #153 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:27:32.563201","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:27:32.563201",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:27:32.563201",
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

### ORD-INV-001 — exchange #163 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:27:32.563201","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:27:32.563201",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:27:32.563201",
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

### ORD-INV-001 — exchange #165 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:27:32.563201","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:27:32.563201",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:27:32.563201",
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

### ORD-INV-001 — exchange #168 (getOrder)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:27:32.563201","$.status":"CANCELLED"}

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
      "completedAt" : "2026-10-19T09:27:32.563201",
      "id" : 1,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:27:32.563201",
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

### ORD-INV-001 — exchange #204 (updateStatus)

completedAt is set if and only if status is COMPLETED — violated: {"$.completedAt":"2026-10-19T09:27:33.326561","$.status":"CANCELLED"}

```json
{
  "request" : {
    "method" : "PATCH",
    "url" : "/api/orders/28/status",
    "body" : {
      "" : { },
      "³·\u0007𦧱xÿ~\u0018iVm" : true,
      "o󵽙ú􆥠XM\u0007􉲪򋉞9È\u0007Ô},ÏÖwêH򫋫ÕÚ󟵝" : [ false, "\u000FvÎÏ", false ],
      "󢾀öö" : {
        "ù¹4򒮵¨ãVI" : -312,
        "𙼏򑘡\b×h򁬧i𵞒6񆽠񻟲" : -23
      },
      "\u001D󎆢" : [ false, true, -6.618144104106349E16 ],
      "status" : "CANCELLED"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "ownerUserId" : 1027,
      "total" : 37.0,
      "completedAt" : "2026-10-19T09:27:33.326561",
      "id" : 28,
      "status" : "CANCELLED"
    }
  },
  "values" : {
    "$.completedAt" : "2026-10-19T09:27:33.326561",
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

