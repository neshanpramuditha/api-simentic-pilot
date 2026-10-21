# Semantic validation report

- Mode: run
- Source: scenario scenario.json against http://localhost:8092 (with database access)
- Rule set: event-api semantic rules v1 (engine 0.1 features only)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=70, FAIL=3, NOT_APPLICABLE=23}
- Duration: 2263 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 11 | 3 | 5 |
| EVT-INV-002 | 14 | 0 | 5 |
| EVT-INV-003 | 14 | 0 | 5 |
| EVT-INV-004 | 14 | 0 | 5 |
| EVT-ST-001 | 8 | 0 | 2 |
| EVT-DB-001 | 9 | 0 | 1 |

## Failures

### EVT-INV-001 — exchange #2 (transitionRegistration)

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":null,"$.state":"CHECKED_IN"}

```json
{
  "request" : {
    "method" : "POST",
    "url" : "/v1/registrations/REG-BHS2D3SM/transitions",
    "body" : {
      "to" : "CHECKED_IN"
    }
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-BHS2D3SM",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : null,
      "confirmedAt" : "2026-10-19T07:38:03.780541Z",
      "requestedAt" : "2026-10-19T07:38:03.521717Z",
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
    "url" : "/v1/registrations/REG-BHS2D3SM",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-BHS2D3SM",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : null,
      "confirmedAt" : "2026-10-19T07:38:03.780541Z",
      "requestedAt" : "2026-10-19T07:38:03.521717Z",
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
    "url" : "/v1/registrations/REG-BHS2D3SM",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-BHS2D3SM",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : null,
      "confirmedAt" : "2026-10-19T07:38:03.780541Z",
      "requestedAt" : "2026-10-19T07:38:03.521717Z",
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

