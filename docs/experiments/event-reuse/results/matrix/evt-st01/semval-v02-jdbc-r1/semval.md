# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8092 (with database access)
- Rule set: event-api semantic rules v2 (engine 0.2: items, notAfter)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=91, FAIL=1, NOT_APPLICABLE=33}
- Duration: 1343 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 15 | 0 | 6 |
| EVT-INV-002 | 15 | 0 | 6 |
| EVT-INV-003 | 15 | 0 | 6 |
| EVT-INV-004 | 15 | 0 | 6 |
| EVT-INV-005 | 15 | 0 | 6 |
| EVT-ST-001 | 7 | 1 | 2 |
| EVT-DB-001 | 9 | 0 | 1 |

## Failures

### EVT-ST-001 — exchange #8 (transitionRegistration)

allowed transition WAITLISTED -> CONFIRMED was rejected with HTTP 422

```json
{
  "request" : {
    "method" : "POST",
    "url" : "/v1/registrations/REG-QE8ZBPPA/transitions",
    "body" : {
      "to" : "CONFIRMED"
    }
  },
  "response" : {
    "status" : 422,
    "body" : {
      "timestamp" : "2026-10-19T07:27:57.681Z",
      "status" : 422,
      "error" : "Unprocessable Content",
      "path" : "/v1/registrations/REG-QE8ZBPPA/transitions"
    }
  },
  "state" : {
    "resourceId" : "REG-QE8ZBPPA",
    "previous" : "WAITLISTED",
    "previousSource" : "read-back before request",
    "readBack" : {
      "operationId" : "fetchRegistration",
      "url" : "/v1/registrations/REG-QE8ZBPPA",
      "status" : 200,
      "body" : {
        "registrationId" : "REG-QE8ZBPPA",
        "attendeeRef" : "ATT-7",
        "eventCode" : "SPRING-DAY",
        "seats" : 1,
        "cancellationReason" : null,
        "cancelledAt" : null,
        "checkedInAt" : null,
        "confirmedAt" : null,
        "requestedAt" : "2026-10-19T07:27:57.625355Z",
        "state" : "WAITLISTED"
      }
    },
    "requested" : "CONFIRMED",
    "transitionAllowed" : true,
    "allowedFromPrevious" : [ "CONFIRMED", "CANCELLED" ]
  }
}
```

