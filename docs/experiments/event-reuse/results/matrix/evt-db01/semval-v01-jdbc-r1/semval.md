# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8092 (with database access)
- Rule set: event-api semantic rules v1 (engine 0.1 features only)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=69, FAIL=4, NOT_APPLICABLE=23}
- Duration: 3693 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 13 | 1 | 5 |
| EVT-INV-002 | 14 | 0 | 5 |
| EVT-INV-003 | 14 | 0 | 5 |
| EVT-INV-004 | 13 | 1 | 5 |
| EVT-ST-001 | 8 | 0 | 2 |
| EVT-DB-001 | 7 | 2 | 1 |

## Failures

### EVT-DB-001 — exchange #4 (transitionRegistration)

request was rejected with HTTP 422 but the row changed: before {"state":"CHECKED_IN","confirmed_at":"2026-10-19T07:30:11.954273Z","checked_in_at":"2026-10-19T07:30:12.132278Z","cancelled_at":null,"cancellation_reason":null}, after {"state":"CANCELLED","confirmed_at":"2026-10-19T07:30:11.954273Z","checked_in_at":"2026-10-19T07:30:12.132278Z","cancelled_at":"2026-10-19T07:30:12.235163Z","cancellation_reason":"Left early"}

```json
{
  "request" : {
    "method" : "POST",
    "url" : "/v1/registrations/REG-BZSLEWBZ/transitions",
    "body" : {
      "to" : "CANCELLED",
      "reason" : "Left early"
    }
  },
  "response" : {
    "status" : 422,
    "body" : {
      "timestamp" : "2026-10-19T07:30:12.263Z",
      "status" : 422,
      "error" : "Unprocessable Content",
      "path" : "/v1/registrations/REG-BZSLEWBZ/transitions"
    }
  },
  "database" : {
    "query" : "SELECT state, confirmed_at, checked_in_at, cancelled_at, cancellation_reason FROM event_registration WHERE registration_id = ?",
    "params" : [ "REG-BZSLEWBZ" ],
    "rowBefore" : {
      "state" : "CHECKED_IN",
      "confirmed_at" : "2026-10-19T07:30:11.954273Z",
      "checked_in_at" : "2026-10-19T07:30:12.132278Z",
      "cancelled_at" : null,
      "cancellation_reason" : null
    },
    "rowAfter" : {
      "state" : "CANCELLED",
      "confirmed_at" : "2026-10-19T07:30:11.954273Z",
      "checked_in_at" : "2026-10-19T07:30:12.132278Z",
      "cancelled_at" : "2026-10-19T07:30:12.235163Z",
      "cancellation_reason" : "Left early"
    }
  }
}
```

### EVT-INV-001 — exchange #5 (fetchRegistration)

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":"2026-10-19T07:30:12.132278Z","$.state":"CANCELLED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/registrations/REG-BZSLEWBZ",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-BZSLEWBZ",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : "Left early",
      "cancelledAt" : "2026-10-19T07:30:12.235163Z",
      "checkedInAt" : "2026-10-19T07:30:12.132278Z",
      "confirmedAt" : "2026-10-19T07:30:11.954273Z",
      "requestedAt" : "2026-10-19T07:30:11.552536Z",
      "state" : "CANCELLED"
    }
  },
  "values" : {
    "$.checkedInAt" : "2026-10-19T07:30:12.132278Z",
    "$.state" : "CANCELLED"
  },
  "assertion" : {
    "iff" : [ {
      "path" : "$.checkedInAt",
      "notNull" : true
    }, {
      "path" : "$.state",
      "equals" : "CHECKED_IN"
    } ]
  }
}
```

### EVT-DB-001 — exchange #13 (transitionRegistration)

request was rejected with HTTP 422 but the row changed: before {"state":"WAITLISTED","confirmed_at":null,"checked_in_at":null,"cancelled_at":null,"cancellation_reason":null}, after {"state":"CHECKED_IN","confirmed_at":null,"checked_in_at":"2026-10-19T07:30:12.758389Z","cancelled_at":null,"cancellation_reason":null}

```json
{
  "request" : {
    "method" : "POST",
    "url" : "/v1/registrations/REG-NNQSWRZU/transitions",
    "body" : {
      "to" : "CHECKED_IN"
    }
  },
  "response" : {
    "status" : 422,
    "body" : {
      "timestamp" : "2026-10-19T07:30:12.766Z",
      "status" : 422,
      "error" : "Unprocessable Content",
      "path" : "/v1/registrations/REG-NNQSWRZU/transitions"
    }
  },
  "database" : {
    "query" : "SELECT state, confirmed_at, checked_in_at, cancelled_at, cancellation_reason FROM event_registration WHERE registration_id = ?",
    "params" : [ "REG-NNQSWRZU" ],
    "rowBefore" : {
      "state" : "WAITLISTED",
      "confirmed_at" : null,
      "checked_in_at" : null,
      "cancelled_at" : null,
      "cancellation_reason" : null
    },
    "rowAfter" : {
      "state" : "CHECKED_IN",
      "confirmed_at" : null,
      "checked_in_at" : "2026-10-19T07:30:12.758389Z",
      "cancelled_at" : null,
      "cancellation_reason" : null
    }
  }
}
```

### EVT-INV-004 — exchange #14 (fetchRegistration)

A CHECKED_IN registration has been confirmed (confirmedAt set); confirmedAt is only set in CONFIRMED, CHECKED_IN or CANCELLED — violated: {"$.state":"CHECKED_IN","$.confirmedAt":null}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/registrations/REG-NNQSWRZU",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-NNQSWRZU",
      "attendeeRef" : "ATT-7",
      "eventCode" : "API-SUMMIT",
      "seats" : 3,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : "2026-10-19T07:30:12.758389Z",
      "confirmedAt" : null,
      "requestedAt" : "2026-10-19T07:30:12.617672Z",
      "state" : "CHECKED_IN"
    }
  },
  "values" : {
    "$.state" : "CHECKED_IN",
    "$.confirmedAt" : null
  },
  "assertion" : {
    "all" : [ {
      "implies" : [ {
        "path" : "$.state",
        "equals" : "CHECKED_IN"
      }, {
        "path" : "$.confirmedAt",
        "notNull" : true
      } ]
    }, {
      "implies" : [ {
        "path" : "$.confirmedAt",
        "notNull" : true
      }, {
        "path" : "$.state",
        "in" : [ "CONFIRMED", "CHECKED_IN", "CANCELLED" ]
      } ]
    } ]
  }
}
```

