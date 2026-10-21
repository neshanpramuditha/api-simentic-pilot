# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8092 (with database access)
- Rule set: event-api semantic rules v2 (engine 0.2: items, notAfter)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=96, FAIL=1, NOT_APPLICABLE=28}
- Duration: 2360 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 16 | 0 | 5 |
| EVT-INV-002 | 16 | 0 | 5 |
| EVT-INV-003 | 16 | 0 | 5 |
| EVT-INV-004 | 16 | 0 | 5 |
| EVT-INV-005 | 16 | 0 | 5 |
| EVT-ST-001 | 8 | 0 | 2 |
| EVT-DB-001 | 8 | 1 | 1 |

## Failures

### EVT-DB-001 — exchange #9 (transitionRegistration)

persisted state differs from the 200 response: [state: persisted=CONFIRMED response($.state)=CANCELLED, cancellation_reason: persisted=null response($.cancellationReason)=Schedule conflict]

```json
{
  "request" : {
    "method" : "POST",
    "url" : "/v1/registrations/REG-MN8VBTZ6/transitions",
    "body" : {
      "to" : "CANCELLED",
      "reason" : "Schedule conflict"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-MN8VBTZ6",
      "attendeeRef" : "ATT-7",
      "eventCode" : "SPRING-DAY",
      "seats" : 1,
      "cancellationReason" : "Schedule conflict",
      "cancelledAt" : "2026-10-19T07:34:33.960553700Z",
      "checkedInAt" : null,
      "confirmedAt" : "2026-10-19T07:34:33.930538Z",
      "requestedAt" : "2026-10-19T07:34:33.871347Z",
      "state" : "CANCELLED"
    }
  },
  "database" : {
    "query" : "SELECT state, confirmed_at, checked_in_at, cancelled_at, cancellation_reason FROM event_registration WHERE registration_id = ?",
    "params" : [ "REG-MN8VBTZ6" ],
    "rowBefore" : {
      "state" : "CONFIRMED",
      "confirmed_at" : "2026-10-19T07:34:33.930538Z",
      "checked_in_at" : null,
      "cancelled_at" : null,
      "cancellation_reason" : null
    },
    "rowAfter" : {
      "state" : "CONFIRMED",
      "confirmed_at" : "2026-10-19T07:34:33.930538Z",
      "checked_in_at" : null,
      "cancelled_at" : null,
      "cancellation_reason" : null
    }
  }
}
```

