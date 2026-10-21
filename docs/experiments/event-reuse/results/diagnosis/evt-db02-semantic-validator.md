# evt-db02: semantic validator 0.2 report (failures only, ids masked)

Rule set: event-api semantic rules v2 (engine 0.2: items, notAfter). Totals: {"PASS":96,"FAIL":1,"NOT_APPLICABLE":28}.

## EVT-DB-001 — exchange #9 (transitionRegistration)

persisted state differs from the 200 response: [state: persisted=CONFIRMED response($.state)=CANCELLED, cancellation_reason: persisted=null response($.cancellationReason)=Schedule conflict]

```json
{
  "request": {
    "method": "POST",
    "url": "/v1/registrations/REG-xxxxxxxx/transitions",
    "body": {
      "to": "CANCELLED",
      "reason": "Schedule conflict"
    }
  },
  "response": {
    "status": 200,
    "body": {
      "registrationId": "REG-xxxxxxxx",
      "attendeeRef": "ATT-7",
      "eventCode": "SPRING-DAY",
      "seats": 1,
      "cancellationReason": "Schedule conflict",
      "cancelledAt": "2026-10-19T07:34:33.960553700Z",
      "checkedInAt": null,
      "confirmedAt": "2026-10-19T07:34:33.930538Z",
      "requestedAt": "2026-10-19T07:34:33.871347Z",
      "state": "CANCELLED"
    }
  },
  "database": {
    "query": "SELECT state, confirmed_at, checked_in_at, cancelled_at, cancellation_reason FROM event_registration WHERE registration_id = ?",
    "params": [
      "REG-xxxxxxxx"
    ],
    "rowBefore": {
      "state": "CONFIRMED",
      "confirmed_at": "2026-10-19T07:34:33.930538Z",
      "checked_in_at": null,
      "cancelled_at": null,
      "cancellation_reason": null
    },
    "rowAfter": {
      "state": "CONFIRMED",
      "confirmed_at": "2026-10-19T07:34:33.930538Z",
      "checked_in_at": null,
      "cancelled_at": null,
      "cancellation_reason": null
    }
  }
}
```

