# Semantic validation report

- Mode: check-har
- Source: HAR har-20261019T091930Z.json (passive: no read-backs, no database)
- Rule set: order-api semantic rules v1
- Exchanges: 536, unmatched requests: 28
- Totals: {PASS=291, FAIL=2, NOT_APPLICABLE=512}
- Duration: 663 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| ORD-INV-001 | 196 | 0 | 359 |
| ORD-ST-001 | 95 | 2 | 28 |
| ORD-DB-001 | 0 | 0 | 125 |

## Failures

### ORD-ST-001 — exchange #90 (updateStatus)

forbidden transition COMPLETED -> PAID answered with HTTP 415, expected one of [409]

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
      "timestamp" : "2026-10-19T03:49:34.589Z",
      "status" : 415,
      "error" : "Unsupported Media Type",
      "path" : "/api/orders/1/status"
    }
  },
  "state" : {
    "resourceId" : "1",
    "previous" : "COMPLETED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "PAID",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

### ORD-ST-001 — exchange #91 (updateStatus)

forbidden transition COMPLETED -> PAID answered with HTTP 500, expected one of [409]

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
      "timestamp" : "2026-10-19T03:49:34.594Z",
      "status" : 500,
      "error" : "Internal Server Error",
      "path" : "/api/orders/1/status"
    }
  },
  "state" : {
    "resourceId" : "1",
    "previous" : "COMPLETED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "PAID",
    "transitionAllowed" : false,
    "allowedFromPrevious" : [ ]
  }
}
```

