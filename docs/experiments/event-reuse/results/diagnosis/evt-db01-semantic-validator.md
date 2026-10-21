# evt-db01: semantic validator 0.2 report (failures only, ids masked)

Rule set: event-api semantic rules v2 (engine 0.2: items, notAfter). Totals: {"PASS":89,"FAIL":8,"NOT_APPLICABLE":28}.

## EVT-DB-001 — exchange #4 (transitionRegistration)

request was rejected with HTTP 422 but the row changed: before {"state":"CHECKED_IN","confirmed_at":"2026-10-19T07:30:37.165320Z","checked_in_at":"2026-10-19T07:30:37.240330Z","cancelled_at":null,"cancellation_reason":null}, after {"state":"CANCELLED","confirmed_at":"2026-10-19T07:30:37.165320Z","checked_in_at":"2026-10-19T07:30:37.240330Z","cancelled_at":"2026-10-19T07:30:37.339533Z","cancellation_reason":"Left early"}

```json
{
  "request": {
    "method": "POST",
    "url": "/v1/registrations/REG-xxxxxxxx/transitions",
    "body": {
      "to": "CANCELLED",
      "reason": "Left early"
    }
  },
  "response": {
    "status": 422,
    "body": {
      "timestamp": "2026-10-19T07:30:37.362Z",
      "status": 422,
      "error": "Unprocessable Content",
      "path": "/v1/registrations/REG-xxxxxxxx/transitions"
    }
  },
  "database": {
    "query": "SELECT state, confirmed_at, checked_in_at, cancelled_at, cancellation_reason FROM event_registration WHERE registration_id = ?",
    "params": [
      "REG-xxxxxxxx"
    ],
    "rowBefore": {
      "state": "CHECKED_IN",
      "confirmed_at": "2026-10-19T07:30:37.165320Z",
      "checked_in_at": "2026-10-19T07:30:37.240330Z",
      "cancelled_at": null,
      "cancellation_reason": null
    },
    "rowAfter": {
      "state": "CANCELLED",
      "confirmed_at": "2026-10-19T07:30:37.165320Z",
      "checked_in_at": "2026-10-19T07:30:37.240330Z",
      "cancelled_at": "2026-10-19T07:30:37.339533Z",
      "cancellation_reason": "Left early"
    }
  }
}
```

## EVT-INV-001 — exchange #5 (fetchRegistration)

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":"2026-10-19T07:30:37.240330Z","$.state":"CANCELLED"}

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
      "cancellationReason": "Left early",
      "cancelledAt": "2026-10-19T07:30:37.339533Z",
      "checkedInAt": "2026-10-19T07:30:37.240330Z",
      "confirmedAt": "2026-10-19T07:30:37.165320Z",
      "requestedAt": "2026-10-19T07:30:36.846993Z",
      "state": "CANCELLED"
    }
  },
  "values": {
    "$.checkedInAt": "2026-10-19T07:30:37.240330Z",
    "$.state": "CANCELLED"
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

## EVT-DB-001 — exchange #13 (transitionRegistration)

request was rejected with HTTP 422 but the row changed: before {"state":"WAITLISTED","confirmed_at":null,"checked_in_at":null,"cancelled_at":null,"cancellation_reason":null}, after {"state":"CHECKED_IN","confirmed_at":null,"checked_in_at":"2026-10-19T07:30:37.765522Z","cancelled_at":null,"cancellation_reason":null}

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
    "status": 422,
    "body": {
      "timestamp": "2026-10-19T07:30:37.772Z",
      "status": 422,
      "error": "Unprocessable Content",
      "path": "/v1/registrations/REG-xxxxxxxx/transitions"
    }
  },
  "database": {
    "query": "SELECT state, confirmed_at, checked_in_at, cancelled_at, cancellation_reason FROM event_registration WHERE registration_id = ?",
    "params": [
      "REG-xxxxxxxx"
    ],
    "rowBefore": {
      "state": "WAITLISTED",
      "confirmed_at": null,
      "checked_in_at": null,
      "cancelled_at": null,
      "cancellation_reason": null
    },
    "rowAfter": {
      "state": "CHECKED_IN",
      "confirmed_at": null,
      "checked_in_at": "2026-10-19T07:30:37.765522Z",
      "cancelled_at": null,
      "cancellation_reason": null
    }
  }
}
```

## EVT-INV-004 — exchange #14 (fetchRegistration)

A CHECKED_IN registration has been confirmed (confirmedAt set); confirmedAt is only set in CONFIRMED, CHECKED_IN or CANCELLED — violated: {"$.state":"CHECKED_IN","$.confirmedAt":null}

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
      "eventCode": "API-SUMMIT",
      "seats": 3,
      "cancellationReason": null,
      "cancelledAt": null,
      "checkedInAt": "2026-10-19T07:30:37.765522Z",
      "confirmedAt": null,
      "requestedAt": "2026-10-19T07:30:37.638379Z",
      "state": "CHECKED_IN"
    }
  },
  "values": {
    "$.state": "CHECKED_IN",
    "$.confirmedAt": null
  },
  "assertion": {
    "all": [
      {
        "implies": [
          {
            "path": "$.state",
            "equals": "CHECKED_IN"
          },
          {
            "path": "$.confirmedAt",
            "notNull": true
          }
        ]
      },
      {
        "implies": [
          {
            "path": "$.confirmedAt",
            "notNull": true
          },
          {
            "path": "$.state",
            "in": [
              "CONFIRMED",
              "CHECKED_IN",
              "CANCELLED"
            ]
          }
        ]
      }
    ]
  }
}
```

## EVT-INV-005 — exchange #14 (fetchRegistration)

Timestamps are in lifecycle order: requestedAt <= confirmedAt <= checkedInAt — violated: {"$.confirmedAt":null,"$.requestedAt":"2026-10-19T07:30:37.638379Z","$.checkedInAt":"2026-10-19T07:30:37.765522Z"}

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
      "eventCode": "API-SUMMIT",
      "seats": 3,
      "cancellationReason": null,
      "cancelledAt": null,
      "checkedInAt": "2026-10-19T07:30:37.765522Z",
      "confirmedAt": null,
      "requestedAt": "2026-10-19T07:30:37.638379Z",
      "state": "CHECKED_IN"
    }
  },
  "values": {
    "$.confirmedAt": null,
    "$.requestedAt": "2026-10-19T07:30:37.638379Z",
    "$.checkedInAt": "2026-10-19T07:30:37.765522Z"
  },
  "assertion": {
    "all": [
      {
        "implies": [
          {
            "path": "$.confirmedAt",
            "notNull": true
          },
          {
            "path": "$.requestedAt",
            "notAfter": "$.confirmedAt"
          }
        ]
      },
      {
        "implies": [
          {
            "path": "$.checkedInAt",
            "notNull": true
          },
          {
            "path": "$.confirmedAt",
            "notAfter": "$.checkedInAt"
          }
        ]
      }
    ]
  }
}
```

## EVT-INV-001 — exchange #15 (listAttendeeRegistrations)

checkedInAt is set if and only if state is CHECKED_IN — violated by item 0: {"$.checkedInAt":"2026-10-19T07:30:37.240330Z","$.state":"CANCELLED"}

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
          "cancellationReason": "Left early",
          "cancelledAt": "2026-10-19T07:30:37.339533Z",
          "checkedInAt": "2026-10-19T07:30:37.240330Z",
          "confirmedAt": "2026-10-19T07:30:37.165320Z",
          "requestedAt": "2026-10-19T07:30:36.846993Z",
          "state": "CANCELLED"
        },
        {
          "registrationId": "REG-xxxxxxxx",
          "attendeeRef": "ATT-7",
          "eventCode": "SPRING-DAY",
          "seats": 1,
          "cancellationReason": "Schedule conflict",
          "cancelledAt": "2026-10-19T07:30:37.593245Z",
          "checkedInAt": null,
          "confirmedAt": "2026-10-19T07:30:37.507333Z",
          "requestedAt": "2026-10-19T07:30:37.416067Z",
          "state": "CANCELLED"
        },
        {
          "registrationId": "REG-xxxxxxxx",
          "attendeeRef": "ATT-7",
          "eventCode": "API-SUMMIT",
          "seats": 3,
          "cancellationReason": null,
          "cancelledAt": null,
          "checkedInAt": "2026-10-19T07:30:37.765522Z",
          "confirmedAt": null,
          "requestedAt": "2026-10-19T07:30:37.638379Z",
          "state": "CHECKED_IN"
        }
      ]
    }
  },
  "values": {
    "$.checkedInAt": "2026-10-19T07:30:37.240330Z",
    "$.state": "CANCELLED"
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

## EVT-INV-004 — exchange #15 (listAttendeeRegistrations)

A CHECKED_IN registration has been confirmed (confirmedAt set); confirmedAt is only set in CONFIRMED, CHECKED_IN or CANCELLED — violated by item 2: {"$.state":"CHECKED_IN","$.confirmedAt":null}

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
          "cancellationReason": "Left early",
          "cancelledAt": "2026-10-19T07:30:37.339533Z",
          "checkedInAt": "2026-10-19T07:30:37.240330Z",
          "confirmedAt": "2026-10-19T07:30:37.165320Z",
          "requestedAt": "2026-10-19T07:30:36.846993Z",
          "state": "CANCELLED"
        },
        {
          "registrationId": "REG-xxxxxxxx",
          "attendeeRef": "ATT-7",
          "eventCode": "SPRING-DAY",
          "seats": 1,
          "cancellationReason": "Schedule conflict",
          "cancelledAt": "2026-10-19T07:30:37.593245Z",
          "checkedInAt": null,
          "confirmedAt": "2026-10-19T07:30:37.507333Z",
          "requestedAt": "2026-10-19T07:30:37.416067Z",
          "state": "CANCELLED"
        },
        {
          "registrationId": "REG-xxxxxxxx",
          "attendeeRef": "ATT-7",
          "eventCode": "API-SUMMIT",
          "seats": 3,
          "cancellationReason": null,
          "cancelledAt": null,
          "checkedInAt": "2026-10-19T07:30:37.765522Z",
          "confirmedAt": null,
          "requestedAt": "2026-10-19T07:30:37.638379Z",
          "state": "CHECKED_IN"
        }
      ]
    }
  },
  "values": {
    "$.state": "CHECKED_IN",
    "$.confirmedAt": null
  },
  "itemIndex": 2,
  "assertion": {
    "all": [
      {
        "implies": [
          {
            "path": "$.state",
            "equals": "CHECKED_IN"
          },
          {
            "path": "$.confirmedAt",
            "notNull": true
          }
        ]
      },
      {
        "implies": [
          {
            "path": "$.confirmedAt",
            "notNull": true
          },
          {
            "path": "$.state",
            "in": [
              "CONFIRMED",
              "CHECKED_IN",
              "CANCELLED"
            ]
          }
        ]
      }
    ]
  }
}
```

## EVT-INV-005 — exchange #15 (listAttendeeRegistrations)

Timestamps are in lifecycle order: requestedAt <= confirmedAt <= checkedInAt — violated by item 2: {"$.confirmedAt":null,"$.requestedAt":"2026-10-19T07:30:37.638379Z","$.checkedInAt":"2026-10-19T07:30:37.765522Z"}

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
          "cancellationReason": "Left early",
          "cancelledAt": "2026-10-19T07:30:37.339533Z",
          "checkedInAt": "2026-10-19T07:30:37.240330Z",
          "confirmedAt": "2026-10-19T07:30:37.165320Z",
          "requestedAt": "2026-10-19T07:30:36.846993Z",
          "state": "CANCELLED"
        },
        {
          "registrationId": "REG-xxxxxxxx",
          "attendeeRef": "ATT-7",
          "eventCode": "SPRING-DAY",
          "seats": 1,
          "cancellationReason": "Schedule conflict",
          "cancelledAt": "2026-10-19T07:30:37.593245Z",
          "checkedInAt": null,
          "confirmedAt": "2026-10-19T07:30:37.507333Z",
          "requestedAt": "2026-10-19T07:30:37.416067Z",
          "state": "CANCELLED"
        },
        {
          "registrationId": "REG-xxxxxxxx",
          "attendeeRef": "ATT-7",
          "eventCode": "API-SUMMIT",
          "seats": 3,
          "cancellationReason": null,
          "cancelledAt": null,
          "checkedInAt": "2026-10-19T07:30:37.765522Z",
          "confirmedAt": null,
          "requestedAt": "2026-10-19T07:30:37.638379Z",
          "state": "CHECKED_IN"
        }
      ]
    }
  },
  "values": {
    "$.confirmedAt": null,
    "$.requestedAt": "2026-10-19T07:30:37.638379Z",
    "$.checkedInAt": "2026-10-19T07:30:37.765522Z"
  },
  "itemIndex": 2,
  "assertion": {
    "all": [
      {
        "implies": [
          {
            "path": "$.confirmedAt",
            "notNull": true
          },
          {
            "path": "$.requestedAt",
            "notAfter": "$.confirmedAt"
          }
        ]
      },
      {
        "implies": [
          {
            "path": "$.checkedInAt",
            "notNull": true
          },
          {
            "path": "$.confirmedAt",
            "notAfter": "$.checkedInAt"
          }
        ]
      }
    ]
  }
}
```

