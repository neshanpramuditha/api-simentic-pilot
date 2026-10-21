# Semantic validation report

- Mode: check-har
- Source: HAR traffic.har.json (passive: no read-backs, no database)
- Rule set: event-api semantic rules v1 (engine 0.1 features only)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=59, FAIL=1, NOT_APPLICABLE=36}
- Duration: 38 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 13 | 0 | 6 |
| EVT-INV-002 | 13 | 0 | 6 |
| EVT-INV-003 | 13 | 0 | 6 |
| EVT-INV-004 | 13 | 0 | 6 |
| EVT-ST-001 | 7 | 1 | 2 |
| EVT-DB-001 | 0 | 0 | 10 |

## Failures

### EVT-ST-001 — exchange #8 (transitionRegistration)

allowed transition WAITLISTED -> CONFIRMED was rejected with HTTP 422

```json
{
  "request" : {
    "method" : "POST",
    "url" : "/v1/registrations/REG-KP974HWM/transitions",
    "body" : {
      "to" : "CONFIRMED"
    }
  },
  "response" : {
    "status" : 422,
    "body" : {
      "timestamp" : "2026-10-19T07:28:12.837Z",
      "status" : 422,
      "error" : "Unprocessable Content",
      "path" : "/v1/registrations/REG-KP974HWM/transitions"
    }
  },
  "state" : {
    "resourceId" : "REG-KP974HWM",
    "previous" : "WAITLISTED",
    "previousSource" : "last state observed in earlier responses",
    "requested" : "CONFIRMED",
    "transitionAllowed" : true,
    "allowedFromPrevious" : [ "CONFIRMED", "CANCELLED" ]
  }
}
```

