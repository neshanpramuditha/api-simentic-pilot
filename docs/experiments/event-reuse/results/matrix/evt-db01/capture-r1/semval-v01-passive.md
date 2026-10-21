# Semantic validation report

- Mode: check-har
- Source: HAR traffic.har.json (passive: no read-backs, no database)
- Rule set: event-api semantic rules v1 (engine 0.1 features only)
- Exchanges: 19, unmatched requests: 0
- Totals: {PASS=62, FAIL=2, NOT_APPLICABLE=32}
- Duration: 194 ms

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

checkedInAt is set if and only if state is CHECKED_IN — violated: {"$.checkedInAt":"2026-10-19T07:31:03.380582Z","$.state":"CANCELLED"}

```json
{
  "request" : {
    "method" : "GET",
    "url" : "/v1/registrations/REG-C5W2LVUB",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-C5W2LVUB",
      "attendeeRef" : "ATT-7",
      "eventCode" : "JAVA-CONF-26",
      "seats" : 2,
      "cancellationReason" : "Left early",
      "cancelledAt" : "2026-10-19T07:31:03.408290Z",
      "checkedInAt" : "2026-10-19T07:31:03.380582Z",
      "confirmedAt" : "2026-10-19T07:31:03.357293Z",
      "requestedAt" : "2026-10-19T07:31:03.204435Z",
      "state" : "CANCELLED"
    }
  },
  "values" : {
    "$.checkedInAt" : "2026-10-19T07:31:03.380582Z",
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
    "url" : "/v1/registrations/REG-YAPELAXT",
    "body" : null
  },
  "response" : {
    "status" : 200,
    "body" : {
      "registrationId" : "REG-YAPELAXT",
      "attendeeRef" : "ATT-7",
      "eventCode" : "API-SUMMIT",
      "seats" : 3,
      "cancellationReason" : null,
      "cancelledAt" : null,
      "checkedInAt" : "2026-10-19T07:31:03.542618Z",
      "confirmedAt" : null,
      "requestedAt" : "2026-10-19T07:31:03.518096Z",
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

