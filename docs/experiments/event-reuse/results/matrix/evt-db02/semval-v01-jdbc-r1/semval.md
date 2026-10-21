# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8092 (with database access)
- Rule set: event-api semantic rules v1 (engine 0.1 features only)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=72, FAIL=1, NOT_APPLICABLE=23}
- Duration: 2507 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 14 | 0 | 5 |
| EVT-INV-002 | 14 | 0 | 5 |
| EVT-INV-003 | 14 | 0 | 5 |
| EVT-INV-004 | 14 | 0 | 5 |
| EVT-ST-001 | 8 | 0 | 2 |
| EVT-DB-001 | 8 | 1 | 1 |

## Failures

### EVT-DB-001 — exchange #9 (transitionRegistration)

persisted state differs from the 200 response: [state: persisted=CONFIRMED response($.state)=CANCELLED, cancellation_reason: persisted=null response($.cancellationReason)=Schedule conflict]

```json
{
  "request" : {
    "method" : "POST",
    "url" : "/v1/registrations/REG-BPN7JTEY/transitions",
    "body" : {
      "to" : "CANCELLED",
      "reason" : "Schedule conflict"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-BPN7JTEY",
      "attendeeRef" : "ATT-7",
      "eventCode" : "SPRING-DAY",
      "seats" : 1,
      "cancellationReason" : "Schedule conflict",
      "cancelledAt" : "2026-10-19T07:34:09.967963700Z",
      "checkedInAt" : null,
      "confirmedAt" : "2026-10-19T07:34:09.898148Z",
      "requestedAt" : "2026-10-19T07:34:09.772554Z",
      "state" : "CANCELLED"
    }
  },
  "database" : {
    "query" : "SELECT state, confirmed_at, checked_in_at, cancelled_at, cancellation_reason FROM event_registration WHERE registration_id = ?",
    "params" : [ "REG-BPN7JTEY" ],
    "rowBefore" : {
      "state" : "CONFIRMED",
      "confirmed_at" : "2026-10-19T07:34:09.898148Z",
      "checked_in_at" : null,
      "cancelled_at" : null,
      "cancellation_reason" : null
    },
    "rowAfter" : {
      "state" : "CONFIRMED",
      "confirmed_at" : "2026-10-19T07:34:09.898148Z",
      "checked_in_at" : null,
      "cancelled_at" : null,
      "cancellation_reason" : null
    }
  }
}
```

