# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8092 (with database access)
- Rule set: event-api semantic rules v1 (engine 0.1 features only)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=68, FAIL=1, NOT_APPLICABLE=27}
- Duration: 1529 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 13 | 0 | 6 |
| EVT-INV-002 | 13 | 0 | 6 |
| EVT-INV-003 | 13 | 0 | 6 |
| EVT-INV-004 | 13 | 0 | 6 |
| EVT-ST-001 | 7 | 1 | 2 |
| EVT-DB-001 | 9 | 0 | 1 |

## Failures

### EVT-ST-001 — exchange #8 (transitionRegistration)

allowed transition WAITLISTED -> CONFIRMED was rejected with HTTP 422

```json
{
  "request" : {
    "method" : "POST",
    "url" : "/v1/registrations/REG-UD4HC62A/transitions",
    "body" : {
      "to" : "CONFIRMED"
    }
  },
  "response" : {
    "status" : 422,
    "body" : {
      "timestamp" : "2026-10-19T07:28:48.723Z",
      "status" : 422,
      "error" : "Unprocessable Content",
      "path" : "/v1/registrations/REG-UD4HC62A/transitions"
    }
  },
  "state" : {
    "resourceId" : "REG-UD4HC62A",
    "previous" : "WAITLISTED",
    "previousSource" : "read-back before request",
    "readBack" : {
      "operationId" : "fetchRegistration",
      "url" : "/v1/registrations/REG-UD4HC62A",
      "status" : 200,
      "body" : {
        "registrationId" : "REG-UD4HC62A",
        "attendeeRef" : "ATT-7",
        "eventCode" : "SPRING-DAY",
        "seats" : 1,
        "cancellationReason" : null,
        "cancelledAt" : null,
        "checkedInAt" : null,
        "confirmedAt" : null,
        "requestedAt" : "2026-10-19T07:28:48.664391Z",
        "state" : "WAITLISTED"
      }
    },
    "requested" : "CONFIRMED",
    "transitionAllowed" : true,
    "allowedFromPrevious" : [ "CONFIRMED", "CANCELLED" ]
  }
}
```

