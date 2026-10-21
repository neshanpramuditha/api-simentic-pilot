# Semantic validation report

- Mode: check-har
- Source: HAR har-20261019T091901Z.json (passive: no read-backs, no database)
- Rule set: order-api semantic rules v1
- Exchanges: 553, unmatched requests: 28
- Totals: {PASS=255, FAIL=2, NOT_APPLICABLE=533}
- Duration: 242 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| ORD-INV-001 | 178 | 0 | 394 |
| ORD-ST-001 | 77 | 2 | 30 |
| ORD-DB-001 | 0 | 0 | 109 |

## Failures

### ORD-ST-001 — exchange #88 (updateStatus)

forbidden transition CANCELLED -> PAID answered with HTTP 415, expected one of [409]

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
    "status" : 415,
    "body" : {
      "timestamp" : "2026-10-19T03:49:05.925Z",
      "status" : 415,
      "error" : "Unsupported Media Type",
      "path" : "/api/orders/1/status"
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

### ORD-ST-001 — exchange #89 (updateStatus)

forbidden transition CANCELLED -> PAID answered with HTTP 500, expected one of [409]

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
    "status" : 500,
    "body" : {
      "timestamp" : "2026-10-19T03:49:05.930Z",
      "status" : 500,
      "error" : "Internal Server Error",
      "path" : "/api/orders/1/status"
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

