# Semantic validation report

- Mode: check-har
- Source: HAR traffic.har.json (passive: no read-backs, no database)
- Rule set: event-api semantic rules v2 (engine 0.2: items, notAfter)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=84, FAIL=4, NOT_APPLICABLE=37}
- Duration: 62 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 12 | 4 | 5 |
| EVT-INV-002 | 16 | 0 | 5 |
| EVT-INV-003 | 16 | 0 | 5 |
| EVT-INV-004 | 16 | 0 | 5 |
| EVT-INV-005 | 16 | 0 | 5 |
| EVT-ST-001 | 8 | 0 | 2 |
| EVT-DB-001 | 0 | 0 | 10 |

## Failures

### EVT-INV-001 — exchange #2 (transitionRegistration)

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":null,"$.state":"CHECKED_IN"}

```json
{
  "request" : {
    "method" : "POST",
    "url" : "/v1/registrations/REG-2BGJJT5F/transitions",
    "body" : {
      "to" : "CHECKED_IN"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-2BGJJT5F",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : null,
      "confirmedAt" : "2026-10-19T07:37:25.795650Z",
      "requestedAt" : "2026-10-19T07:37:25.580425Z",
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
    "url" : "/v1/registrations/REG-2BGJJT5F",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-2BGJJT5F",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : null,
      "confirmedAt" : "2026-10-19T07:37:25.795650Z",
      "requestedAt" : "2026-10-19T07:37:25.580425Z",
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
    "url" : "/v1/registrations/REG-2BGJJT5F",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-2BGJJT5F",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : null,
      "confirmedAt" : "2026-10-19T07:37:25.795650Z",
      "requestedAt" : "2026-10-19T07:37:25.580425Z",
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
        "registrationId" : "REG-2BGJJT5F",
        "attendeeRef" : "ATT-7",
        "eventCode" : "JAVA-CONF-26",
        "seats" : 2,
        "cancellationReason" : null,
        "cancelledAt" : null,
        "checkedInAt" : null,
        "confirmedAt" : "2026-10-19T07:37:25.795650Z",
        "requestedAt" : "2026-10-19T07:37:25.580425Z",
        "state" : "CHECKED_IN"
      }, {
        "registrationId" : "REG-TDSSXWCS",
        "attendeeRef" : "ATT-7",
        "eventCode" : "SPRING-DAY",
        "seats" : 1,
        "cancellationReason" : "Schedule conflict",
        "cancelledAt" : "2026-10-19T07:37:25.939700Z",
        "checkedInAt" : null,
        "confirmedAt" : "2026-10-19T07:37:25.929204Z",
        "requestedAt" : "2026-10-19T07:37:25.900692Z",
        "state" : "CANCELLED"
      }, {
        "registrationId" : "REG-MU3PJY9E",
        "attendeeRef" : "ATT-7",
        "eventCode" : "API-SUMMIT",
        "seats" : 3,
        "cancellationReason" : null,
        "cancelledAt" : null,
        "checkedInAt" : null,
        "confirmedAt" : null,
        "requestedAt" : "2026-10-19T07:37:25.960353Z",
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

