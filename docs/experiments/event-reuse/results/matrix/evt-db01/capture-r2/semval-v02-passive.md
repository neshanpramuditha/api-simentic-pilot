# Semantic validation report

- Mode: check-har
- Source: HAR traffic.har.json (passive: no read-backs, no database)
- Rule set: event-api semantic rules v2 (engine 0.2: items, notAfter)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=82, FAIL=6, NOT_APPLICABLE=37}
- Duration: 93 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 14 | 2 | 5 |
| EVT-INV-002 | 16 | 0 | 5 |
| EVT-INV-003 | 16 | 0 | 5 |
| EVT-INV-004 | 14 | 2 | 5 |
| EVT-INV-005 | 14 | 2 | 5 |
| EVT-ST-001 | 8 | 0 | 2 |
| EVT-DB-001 | 0 | 0 | 10 |

## Failures

### EVT-INV-001 — exchange #5 (fetchRegistration)

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":"2026-10-19T07:33:16.026170Z","$.state":"CANCELLED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/registrations/REG-R5KNM9RU",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-R5KNM9RU",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : "Left early",
      "cancelledAt" : "2026-10-19T07:33:16.071365Z",
      "checkedInAt" : "2026-10-19T07:33:16.026170Z",
      "confirmedAt" : "2026-10-19T07:33:15.992052Z",
      "requestedAt" : "2026-10-19T07:33:15.729316Z",
      "state" : "CANCELLED"
    }
  },
  "values" : {
    "$.checkedInAt" : "2026-10-19T07:33:16.026170Z",
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

### EVT-INV-004 — exchange #14 (fetchRegistration)

A CHECKED_IN registration has been confirmed (confirmedAt set); confirmedAt is only set in CONFIRMED, CHECKED_IN or CANCELLED — violated: {"$.state":"CHECKED_IN","$.confirmedAt":null}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/registrations/REG-WPW4PMNX",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-WPW4PMNX",
      "attendeeRef" : "ATT-7",
      "eventCode" : "API-SUMMIT",
      "seats" : 3,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : "2026-10-19T07:33:16.231820Z",
      "confirmedAt" : null,
      "requestedAt" : "2026-10-19T07:33:16.205595Z",
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

### EVT-INV-005 — exchange #14 (fetchRegistration)

Timestamps are in lifecycle order: requestedAt <= confirmedAt <= checkedInAt — violated: {"$.confirmedAt":null,"$.requestedAt":"2026-10-19T07:33:16.205595Z","$.checkedInAt":"2026-10-19T07:33:16.231820Z"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/registrations/REG-WPW4PMNX",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-WPW4PMNX",
      "attendeeRef" : "ATT-7",
      "eventCode" : "API-SUMMIT",
      "seats" : 3,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : "2026-10-19T07:33:16.231820Z",
      "confirmedAt" : null,
      "requestedAt" : "2026-10-19T07:33:16.205595Z",
      "state" : "CHECKED_IN"
    }
  },
  "values" : {
    "$.confirmedAt" : null,
    "$.requestedAt" : "2026-10-19T07:33:16.205595Z",
    "$.checkedInAt" : "2026-10-19T07:33:16.231820Z"
  },
  "assertion" : {
    "all" : [ {
      "implies" : [ {
        "path" : "$.confirmedAt",
        "notNull" : true
      }, {
        "path" : "$.requestedAt",
        "notAfter" : "$.confirmedAt"
      } ]
    }, {
      "implies" : [ {
        "path" : "$.checkedInAt",
        "notNull" : true
      }, {
        "path" : "$.confirmedAt",
        "notAfter" : "$.checkedInAt"
      } ]
    } ]
  }
}
```

### EVT-INV-001 — exchange #15 (listAttendeeRegistrations)

checkedInAt is set if and only if state is CHECKED_IN — violated by item 0: {"$.checkedInAt":"2026-10-19T07:33:16.026170Z","$.state":"CANCELLED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/attendees/ATT-7/registrations",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "attendeeRef" : "ATT-7",
      "count" : 3,
      "items" : [ {
        "registrationId" : "REG-R5KNM9RU",
        "attendeeRef" : "ATT-7",
        "eventCode" : "JAVA-CONF-26",
        "seats" : 2,
        "cancellationReason" : "Left early",
        "cancelledAt" : "2026-10-19T07:33:16.071365Z",
        "checkedInAt" : "2026-10-19T07:33:16.026170Z",
        "confirmedAt" : "2026-10-19T07:33:15.992052Z",
        "requestedAt" : "2026-10-19T07:33:15.729316Z",
        "state" : "CANCELLED"
      }, {
        "registrationId" : "REG-XU2HUBQ4",
        "attendeeRef" : "ATT-7",
        "eventCode" : "SPRING-DAY",
        "seats" : 1,
        "cancellationReason" : "Schedule conflict",
        "cancelledAt" : "2026-10-19T07:33:16.180370Z",
        "checkedInAt" : null,
        "confirmedAt" : "2026-10-19T07:33:16.165590Z",
        "requestedAt" : "2026-10-19T07:33:16.134287Z",
        "state" : "CANCELLED"
      }, {
        "registrationId" : "REG-WPW4PMNX",
        "attendeeRef" : "ATT-7",
        "eventCode" : "API-SUMMIT",
        "seats" : 3,
        "cancellationReason" : null,
        "cancelledAt" : null,
        "checkedInAt" : "2026-10-19T07:33:16.231820Z",
        "confirmedAt" : null,
        "requestedAt" : "2026-10-19T07:33:16.205595Z",
        "state" : "CHECKED_IN"
      } ]
    }
  },
  "values" : {
    "$.checkedInAt" : "2026-10-19T07:33:16.026170Z",
    "$.state" : "CANCELLED"
  },
  "itemIndex" : 0,
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

### EVT-INV-004 — exchange #15 (listAttendeeRegistrations)

A CHECKED_IN registration has been confirmed (confirmedAt set); confirmedAt is only set in CONFIRMED, CHECKED_IN or CANCELLED — violated by item 2: {"$.state":"CHECKED_IN","$.confirmedAt":null}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/attendees/ATT-7/registrations",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "attendeeRef" : "ATT-7",
      "count" : 3,
      "items" : [ {
        "registrationId" : "REG-R5KNM9RU",
        "attendeeRef" : "ATT-7",
        "eventCode" : "JAVA-CONF-26",
        "seats" : 2,
        "cancellationReason" : "Left early",
        "cancelledAt" : "2026-10-19T07:33:16.071365Z",
        "checkedInAt" : "2026-10-19T07:33:16.026170Z",
        "confirmedAt" : "2026-10-19T07:33:15.992052Z",
        "requestedAt" : "2026-10-19T07:33:15.729316Z",
        "state" : "CANCELLED"
      }, {
        "registrationId" : "REG-XU2HUBQ4",
        "attendeeRef" : "ATT-7",
        "eventCode" : "SPRING-DAY",
        "seats" : 1,
        "cancellationReason" : "Schedule conflict",
        "cancelledAt" : "2026-10-19T07:33:16.180370Z",
        "checkedInAt" : null,
        "confirmedAt" : "2026-10-19T07:33:16.165590Z",
        "requestedAt" : "2026-10-19T07:33:16.134287Z",
        "state" : "CANCELLED"
      }, {
        "registrationId" : "REG-WPW4PMNX",
        "attendeeRef" : "ATT-7",
        "eventCode" : "API-SUMMIT",
        "seats" : 3,
        "cancellationReason" : null,
        "cancelledAt" : null,
        "checkedInAt" : "2026-10-19T07:33:16.231820Z",
        "confirmedAt" : null,
        "requestedAt" : "2026-10-19T07:33:16.205595Z",
        "state" : "CHECKED_IN"
      } ]
    }
  },
  "values" : {
    "$.state" : "CHECKED_IN",
    "$.confirmedAt" : null
  },
  "itemIndex" : 2,
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

### EVT-INV-005 — exchange #15 (listAttendeeRegistrations)

Timestamps are in lifecycle order: requestedAt <= confirmedAt <= checkedInAt — violated by item 2: {"$.confirmedAt":null,"$.requestedAt":"2026-10-19T07:33:16.205595Z","$.checkedInAt":"2026-10-19T07:33:16.231820Z"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/attendees/ATT-7/registrations",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "attendeeRef" : "ATT-7",
      "count" : 3,
      "items" : [ {
        "registrationId" : "REG-R5KNM9RU",
        "attendeeRef" : "ATT-7",
        "eventCode" : "JAVA-CONF-26",
        "seats" : 2,
        "cancellationReason" : "Left early",
        "cancelledAt" : "2026-10-19T07:33:16.071365Z",
        "checkedInAt" : "2026-10-19T07:33:16.026170Z",
        "confirmedAt" : "2026-10-19T07:33:15.992052Z",
        "requestedAt" : "2026-10-19T07:33:15.729316Z",
        "state" : "CANCELLED"
      }, {
        "registrationId" : "REG-XU2HUBQ4",
        "attendeeRef" : "ATT-7",
        "eventCode" : "SPRING-DAY",
        "seats" : 1,
        "cancellationReason" : "Schedule conflict",
        "cancelledAt" : "2026-10-19T07:33:16.180370Z",
        "checkedInAt" : null,
        "confirmedAt" : "2026-10-19T07:33:16.165590Z",
        "requestedAt" : "2026-10-19T07:33:16.134287Z",
        "state" : "CANCELLED"
      }, {
        "registrationId" : "REG-WPW4PMNX",
        "attendeeRef" : "ATT-7",
        "eventCode" : "API-SUMMIT",
        "seats" : 3,
        "cancellationReason" : null,
        "cancelledAt" : null,
        "checkedInAt" : "2026-10-19T07:33:16.231820Z",
        "confirmedAt" : null,
        "requestedAt" : "2026-10-19T07:33:16.205595Z",
        "state" : "CHECKED_IN"
      } ]
    }
  },
  "values" : {
    "$.confirmedAt" : null,
    "$.requestedAt" : "2026-10-19T07:33:16.205595Z",
    "$.checkedInAt" : "2026-10-19T07:33:16.231820Z"
  },
  "itemIndex" : 2,
  "assertion" : {
    "all" : [ {
      "implies" : [ {
        "path" : "$.confirmedAt",
        "notNull" : true
      }, {
        "path" : "$.requestedAt",
        "notAfter" : "$.confirmedAt"
      } ]
    }, {
      "implies" : [ {
        "path" : "$.checkedInAt",
        "notNull" : true
      }, {
        "path" : "$.confirmedAt",
        "notAfter" : "$.checkedInAt"
      } ]
    } ]
  }
}
```

