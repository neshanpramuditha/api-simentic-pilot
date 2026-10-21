# Semantic validation report

- Mode: check-har
- Source: HAR traffic.har.json (passive: no read-backs, no database)
- Rule set: event-api semantic rules v1 (engine 0.1 features only)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=62, FAIL=2, NOT_APPLICABLE=32}
- Duration: 83 ms

| Rule | PASS | FAIL | NOT_APPLICABLE |
|---|---|---|---|
| EVT-INV-001 | 13 | 1 | 5 |
| EVT-INV-002 | 14 | 0 | 5 |
| EVT-INV-003 | 14 | 0 | 5 |
| EVT-INV-004 | 13 | 1 | 5 |
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

