# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8092 (with database access)
- Rule set: event-api semantic rules v2 (engine 0.2: items, notAfter)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=93, FAIL=4, NOT_APPLICABLE=28}
- Duration: 1393 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 16 | 0 | 5 |
| EVT-INV-002 | 16 | 0 | 5 |
| EVT-INV-003 | 16 | 0 | 5 |
| EVT-INV-004 | 16 | 0 | 5 |
| EVT-INV-005 | 12 | 4 | 5 |
| EVT-ST-001 | 8 | 0 | 2 |
| EVT-DB-001 | 9 | 0 | 1 |

## Failures

### EVT-INV-005 — exchange #2 (transitionRegistration)

Timestamps are in lifecycle order: requestedAt <= confirmedAt <= checkedInAt — violated: {"$.confirmedAt":"2026-10-19T07:39:30.460437Z","$.requestedAt":"2026-10-19T07:39:30.294790Z","$.checkedInAt":"2026-10-19T07:39:30.294790Z"}

```json
{
  "request" : {
    "method" : "POST",
    "url" : "/v1/registrations/REG-5ZQ6XBXB/transitions",
    "body" : {
      "to" : "CHECKED_IN"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-5ZQ6XBXB",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : "2026-10-19T07:39:30.294790Z",
      "confirmedAt" : "2026-10-19T07:39:30.460437Z",
      "requestedAt" : "2026-10-19T07:39:30.294790Z",
      "state" : "CHECKED_IN"
    }
  },
  "values" : {
    "$.confirmedAt" : "2026-10-19T07:39:30.460437Z",
    "$.requestedAt" : "2026-10-19T07:39:30.294790Z",
    "$.checkedInAt" : "2026-10-19T07:39:30.294790Z"
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

### EVT-INV-005 — exchange #3 (fetchRegistration)

Timestamps are in lifecycle order: requestedAt <= confirmedAt <= checkedInAt — violated: {"$.confirmedAt":"2026-10-19T07:39:30.460437Z","$.requestedAt":"2026-10-19T07:39:30.294790Z","$.checkedInAt":"2026-10-19T07:39:30.294790Z"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/registrations/REG-5ZQ6XBXB",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-5ZQ6XBXB",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : "2026-10-19T07:39:30.294790Z",
      "confirmedAt" : "2026-10-19T07:39:30.460437Z",
      "requestedAt" : "2026-10-19T07:39:30.294790Z",
      "state" : "CHECKED_IN"
    }
  },
  "values" : {
    "$.confirmedAt" : "2026-10-19T07:39:30.460437Z",
    "$.requestedAt" : "2026-10-19T07:39:30.294790Z",
    "$.checkedInAt" : "2026-10-19T07:39:30.294790Z"
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

### EVT-INV-005 — exchange #5 (fetchRegistration)

Timestamps are in lifecycle order: requestedAt <= confirmedAt <= checkedInAt — violated: {"$.confirmedAt":"2026-10-19T07:39:30.460437Z","$.requestedAt":"2026-10-19T07:39:30.294790Z","$.checkedInAt":"2026-10-19T07:39:30.294790Z"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/registrations/REG-5ZQ6XBXB",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-5ZQ6XBXB",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : "2026-10-19T07:39:30.294790Z",
      "confirmedAt" : "2026-10-19T07:39:30.460437Z",
      "requestedAt" : "2026-10-19T07:39:30.294790Z",
      "state" : "CHECKED_IN"
    }
  },
  "values" : {
    "$.confirmedAt" : "2026-10-19T07:39:30.460437Z",
    "$.requestedAt" : "2026-10-19T07:39:30.294790Z",
    "$.checkedInAt" : "2026-10-19T07:39:30.294790Z"
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

### EVT-INV-005 — exchange #15 (listAttendeeRegistrations)

Timestamps are in lifecycle order: requestedAt <= confirmedAt <= checkedInAt — violated by item 0: {"$.confirmedAt":"2026-10-19T07:39:30.460437Z","$.requestedAt":"2026-10-19T07:39:30.294790Z","$.checkedInAt":"2026-10-19T07:39:30.294790Z"}

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
        "registrationId" : "REG-5ZQ6XBXB",
        "attendeeRef" : "ATT-7",
        "eventCode" : "JAVA-CONF-26",
        "seats" : 2,
        "cancellationReason" : null,
        "cancelledAt" : null,
        "checkedInAt" : "2026-10-19T07:39:30.294790Z",
        "confirmedAt" : "2026-10-19T07:39:30.460437Z",
        "requestedAt" : "2026-10-19T07:39:30.294790Z",
        "state" : "CHECKED_IN"
      }, {
        "registrationId" : "REG-R7Q4EBPV",
        "attendeeRef" : "ATT-7",
        "eventCode" : "SPRING-DAY",
        "seats" : 1,
        "cancellationReason" : "Schedule conflict",
        "cancelledAt" : "2026-10-19T07:39:30.746909Z",
        "checkedInAt" : null,
        "confirmedAt" : "2026-10-19T07:39:30.705790Z",
        "requestedAt" : "2026-10-19T07:39:30.624939Z",
        "state" : "CANCELLED"
      }, {
        "registrationId" : "REG-3DTMNYGX",
        "attendeeRef" : "ATT-7",
        "eventCode" : "API-SUMMIT",
        "seats" : 3,
        "cancellationReason" : null,
        "cancelledAt" : null,
        "checkedInAt" : null,
        "confirmedAt" : null,
        "requestedAt" : "2026-10-19T07:39:30.791630Z",
        "state" : "WAITLISTED"
      } ]
    }
  },
  "values" : {
    "$.confirmedAt" : "2026-10-19T07:39:30.460437Z",
    "$.requestedAt" : "2026-10-19T07:39:30.294790Z",
    "$.checkedInAt" : "2026-10-19T07:39:30.294790Z"
  },
  "itemIndex" : 0,
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

