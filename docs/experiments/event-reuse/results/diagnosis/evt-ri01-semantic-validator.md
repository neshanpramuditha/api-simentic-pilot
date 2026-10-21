# evt-ri01: semantic validator 0.2 report (failures only, ids masked)

Rule set: event-api semantic rules v2 (engine 0.2: items, notAfter). Totals: {"PASS":93,"FAIL":4,"NOT_APPLICABLE":28}.

## EVT-INV-001 — exchange #2 (transitionRegistration)

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":null,"$.state":"CHECKED_IN"}

```json
{
  "request": {
    "method": "POST",
    "url": "/v1/registrations/REG-xxxxxxxx/transitions",
    "body": {
      "to": "CHECKED_IN"
    }
  },
  "response": {
    "status": 200,
    "body": {
      "registrationId": "REG-xxxxxxxx",
      "attendeeRef": "ATT-7",
      "eventCode": "JAVA-CONF-26",
      "seats": 2,
      "cancellationReason": null,
      "cancelledAt": null,
      "checkedInAt": null,
      "confirmedAt": "2026-10-19T07:37:09.100697Z",
      "requestedAt": "2026-10-19T07:37:08.850439Z",
      "state": "CHECKED_IN"
    }
  },
  "values": {
    "$.checkedInAt": null,
    "$.state": "CHECKED_IN"
  },
  "assertion": {
    "iff": [
      {
        "path": "$.checkedInAt",
        "notNull": true
      },
      {
        "path": "$.state",
        "equals": "CHECKED_IN"
      }
    ]
  }
}
```

## EVT-INV-001 — exchange #3 (fetchRegistration)

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":null,"$.state":"CHECKED_IN"}

```json
{
  "request": {
    "method": "GET",
    "url": "/v1/registrations/REG-xxxxxxxx",
    "body": null
  },
  "response": {
    "status": 200,
    "body": {
      "registrationId": "REG-xxxxxxxx",
      "attendeeRef": "ATT-7",
      "eventCode": "JAVA-CONF-26",
      "seats": 2,
      "cancellationReason": null,
      "cancelledAt": null,
      "checkedInAt": null,
      "confirmedAt": "2026-10-19T07:37:09.100697Z",
      "requestedAt": "2026-10-19T07:37:08.850439Z",
      "state": "CHECKED_IN"
    }
  },
  "values": {
    "$.checkedInAt": null,
    "$.state": "CHECKED_IN"
  },
  "assertion": {
    "iff": [
      {
        "path": "$.checkedInAt",
        "notNull": true
      },
      {
        "path": "$.state",
        "equals": "CHECKED_IN"
      }
    ]
  }
}
```

## EVT-INV-001 — exchange #5 (fetchRegistration)

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":null,"$.state":"CHECKED_IN"}

```json
{
  "request": {
    "method": "GET",
    "url": "/v1/registrations/REG-xxxxxxxx",
    "body": null
  },
  "response": {
    "status": 200,
    "body": {
      "registrationId": "REG-xxxxxxxx",
      "attendeeRef": "ATT-7",
      "eventCode": "JAVA-CONF-26",
      "seats": 2,
      "cancellationReason": null,
      "cancelledAt": null,
      "checkedInAt": null,
      "confirmedAt": "2026-10-19T07:37:09.100697Z",
      "requestedAt": "2026-10-19T07:37:08.850439Z",
      "state": "CHECKED_IN"
    }
  },
  "values": {
    "$.checkedInAt": null,
    "$.state": "CHECKED_IN"
  },
  "assertion": {
    "iff": [
      {
        "path": "$.checkedInAt",
        "notNull": true
      },
      {
        "path": "$.state",
        "equals": "CHECKED_IN"
      }
    ]
  }
}
```

## EVT-INV-001 — exchange #15 (listAttendeeRegistrations)

checkedInAt is set if and only if state is CHECKED_IN — violated by item 0: {"$.checkedInAt":null,"$.state":"CHECKED_IN"}

```json
{
  "request": {
    "method": "GET",
    "url": "/v1/attendees/ATT-7/registrations",
    "body": null
  },
  "response": {
    "status": 200,
    "body": {
      "attendeeRef": "ATT-7",
      "count": 3,
      "items": [
        {
          "registrationId": "REG-xxxxxxxx",
          "attendeeRef": "ATT-7",
          "eventCode": "JAVA-CONF-26",
          "seats": 2,
          "cancellationReason": null,
          "cancelledAt": null,
          "checkedInAt": null,
          "confirmedAt": "2026-10-19T07:37:09.100697Z",
          "requestedAt": "2026-10-19T07:37:08.850439Z",
          "state": "CHECKED_IN"
        },
        {
          "registrationId": "REG-xxxxxxxx",
          "attendeeRef": "ATT-7",
          "eventCode": "SPRING-DAY",
          "seats": 1,
          "cancellationReason": "Schedule conflict",
          "cancelledAt": "2026-10-19T07:37:09.346389Z",
          "checkedInAt": null,
          "confirmedAt": "2026-10-19T07:37:09.317679Z",
          "requestedAt": "2026-10-19T07:37:09.258937Z",
          "state": "CANCELLED"
        },
        {
          "registrationId": "REG-xxxxxxxx",
          "attendeeRef": "ATT-7",
          "eventCode": "API-SUMMIT",
          "seats": 3,
          "cancellationReason": null,
          "cancelledAt": null,
          "checkedInAt": null,
          "confirmedAt": null,
          "requestedAt": "2026-10-19T07:37:09.376161Z",
          "state": "WAITLISTED"
        }
      ]
    }
  },
  "values": {
    "$.checkedInAt": null,
    "$.state": "CHECKED_IN"
  },
  "itemIndex": 0,
  "assertion": {
    "iff": [
      {
        "path": "$.checkedInAt",
        "notNull": true
      },
      {
        "path": "$.state",
        "equals": "CHECKED_IN"
      }
    ]
  }
}
```

