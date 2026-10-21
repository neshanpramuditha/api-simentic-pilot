# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8092 (with database access)
- Rule set: event-api semantic rules v2 (engine 0.2: items, notAfter)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=93, FAIL=4, NOT_APPLICABLE=28}
- Duration: 2276 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 12 | 4 | 5 |
| EVT-INV-002 | 16 | 0 | 5 |
| EVT-INV-003 | 16 | 0 | 5 |
| EVT-INV-004 | 16 | 0 | 5 |
| EVT-INV-005 | 16 | 0 | 5 |
| EVT-ST-001 | 8 | 0 | 2 |
| EVT-DB-001 | 9 | 0 | 1 |

## Failures

### EVT-INV-001 — exchange #2 (transitionRegistration)

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":null,"$.state":"CHECKED_IN"}

```json
{
  "request" : {
    "method" : "POST",
    "url" : "/v1/registrations/REG-UQNHXZ6K/transitions",
    "body" : {
      "to" : "CHECKED_IN"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-UQNHXZ6K",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : null,
      "confirmedAt" : "2026-10-19T07:37:09.100697Z",
      "requestedAt" : "2026-10-19T07:37:08.850439Z",
      "state" : "CHECKED_IN"
    }
  },
  "values" : {
    "$.checkedInAt" : null,
    "$.state" : "CHECKED_IN"
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

### EVT-INV-001 — exchange #3 (fetchRegistration)

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":null,"$.state":"CHECKED_IN"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/registrations/REG-UQNHXZ6K",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-UQNHXZ6K",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : null,
      "confirmedAt" : "2026-10-19T07:37:09.100697Z",
      "requestedAt" : "2026-10-19T07:37:08.850439Z",
      "state" : "CHECKED_IN"
    }
  },
  "values" : {
    "$.checkedInAt" : null,
    "$.state" : "CHECKED_IN"
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

### EVT-INV-001 — exchange #5 (fetchRegistration)

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":null,"$.state":"CHECKED_IN"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/registrations/REG-UQNHXZ6K",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-UQNHXZ6K",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : null,
      "confirmedAt" : "2026-10-19T07:37:09.100697Z",
      "requestedAt" : "2026-10-19T07:37:08.850439Z",
      "state" : "CHECKED_IN"
    }
  },
  "values" : {
    "$.checkedInAt" : null,
    "$.state" : "CHECKED_IN"
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

### EVT-INV-001 — exchange #15 (listAttendeeRegistrations)

checkedInAt is set if and only if state is CHECKED_IN — violated by item 0: {"$.checkedInAt":null,"$.state":"CHECKED_IN"}

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
        "registrationId" : "REG-UQNHXZ6K",
        "attendeeRef" : "ATT-7",
        "eventCode" : "JAVA-CONF-26",
        "seats" : 2,
        "cancellationReason" : null,
        "cancelledAt" : null,
        "checkedInAt" : null,
        "confirmedAt" : "2026-10-19T07:37:09.100697Z",
        "requestedAt" : "2026-10-19T07:37:08.850439Z",
        "state" : "CHECKED_IN"
      }, {
        "registrationId" : "REG-PZURVVGD",
        "attendeeRef" : "ATT-7",
        "eventCode" : "SPRING-DAY",
        "seats" : 1,
        "cancellationReason" : "Schedule conflict",
        "cancelledAt" : "2026-10-19T07:37:09.346389Z",
        "checkedInAt" : null,
        "confirmedAt" : "2026-10-19T07:37:09.317679Z",
        "requestedAt" : "2026-10-19T07:37:09.258937Z",
        "state" : "CANCELLED"
      }, {
        "registrationId" : "REG-DYVTTVWM",
        "attendeeRef" : "ATT-7",
        "eventCode" : "API-SUMMIT",
        "seats" : 3,
        "cancellationReason" : null,
        "cancelledAt" : null,
        "checkedInAt" : null,
        "confirmedAt" : null,
        "requestedAt" : "2026-10-19T07:37:09.376161Z",
        "state" : "WAITLISTED"
      } ]
    }
  },
  "values" : {
    "$.checkedInAt" : null,
    "$.state" : "CHECKED_IN"
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

