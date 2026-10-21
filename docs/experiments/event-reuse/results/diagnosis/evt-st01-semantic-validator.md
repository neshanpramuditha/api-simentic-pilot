# evt-st01: semantic validator 0.2 report (failures only, ids masked)

Rule set: event-api semantic rules v2 (engine 0.2: items, notAfter). Totals: {"PASS":91,"FAIL":1,"NOT_APPLICABLE":33}.

## EVT-ST-001 — exchange #8 (transitionRegistration)

allowed transition WAITLISTED -> CONFIRMED was rejected with HTTP 422

```json
{
  "request": {
    "method": "POST",
    "url": "/v1/registrations/REG-xxxxxxxx/transitions",
    "body": {
      "to": "CONFIRMED"
    }
  },
  "response": {
    "status": 422,
    "body": {
      "timestamp": "2026-10-19T07:27:57.681Z",
      "status": 422,
      "error": "Unprocessable Content",
      "path": "/v1/registrations/REG-xxxxxxxx/transitions"
    }
  },
  "state": {
    "resourceId": "REG-xxxxxxxx",
    "previous": "WAITLISTED",
    "previousSource": "read-back before request",
    "readBack": {
      "operationId": "fetchRegistration",
      "url": "/v1/registrations/REG-xxxxxxxx",
      "status": 200,
      "body": {
        "registrationId": "REG-xxxxxxxx",
        "attendeeRef": "ATT-7",
        "eventCode": "SPRING-DAY",
        "seats": 1,
        "cancellationReason": null,
        "cancelledAt": null,
        "checkedInAt": null,
        "confirmedAt": null,
        "requestedAt": "2026-10-19T07:27:57.625355Z",
        "state": "WAITLISTED"
      }
    },
    "requested": "CONFIRMED",
    "transitionAllowed": true,
    "allowedFromPrevious": [
      "CONFIRMED",
      "CANCELLED"
    ]
  }
}
```

