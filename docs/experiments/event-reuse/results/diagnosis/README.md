# Diagnosis comparison — report-content analysis

**This is an analysis of what each failure report contains, not a measurement of human diagnosis time or accuracy. No participants took part.**

Task (repeatable, for a future human study): given only one tool's failure report for a run, identify (a) the faulty operation and step, (b) the violated rule, (c) the supporting evidence. The answer key is below; the reports are in this directory (`<mode>-semantic-validator.md`, `<mode>-postman.txt`), both from repeat 1 of the same mode, ids masked.

## Answer key

| Mode | Faulty operation / step | Violated rule | Evidence |
|---|---|---|---|
| evt-st01 | transitionRegistration (POST /v1/registrations/{id}/transitions), step 8 R2 WAITLISTED -> CONFIRMED | allowed transition rejected (lifecycle / EVT-ST-001) | HTTP 422 for an allowed WAITLISTED -> CONFIRMED; previous state WAITLISTED |
| evt-db01 | transitionRegistration, steps 4 (R1 -> CANCELLED) and 13 (R3 -> CHECKED_IN) | rejected change stored (database consistency / EVT-DB-001) | HTTP 422 while the stored state changed (before/after state) |
| evt-db02 | transitionRegistration, step 9 R2 -> CANCELLED | successful response not stored (database consistency / EVT-DB-001) | HTTP 200 CANCELLED while the stored state is still CONFIRMED |
| evt-ri01 | transitionRegistration, step 2 R1 -> CHECKED_IN | checkedInAt set iff CHECKED_IN (EVT-INV-001) | state CHECKED_IN with checkedInAt null |
| evt-cf01 | transitionRegistration, step 2 R1 -> CHECKED_IN | time order requestedAt <= confirmedAt <= checkedInAt (EVT-INV-005) | checkedInAt earlier than confirmedAt |

## Rubric

- **R1** Names the faulty request (method and URL of the step where the defect acts)
- **R2** Names the violated rule or expectation
- **R3** Shows the observed values that violate it
- **R4** States what was expected or allowed
- **R5** Includes the response body
- **R6** Includes the state before the request
- **R7** Includes the stored state after the request (database row; HTTP read-back counted as "partial")
- **R8** First failure reported is at the cause step (not only downstream)

## Scores (SV = semantic validator 0.2 active with JDBC; PM = Postman collection)

| Mode | Cause steps | SV fail steps | PM fail steps | R1 SV/PM | R2 SV/PM | R3 SV/PM | R4 SV/PM | R5 SV/PM | R6 SV/PM | R7 SV/PM | R8 SV/PM |
|---|---|---|---|---|---|---|---|---|---|---|---|
| evt-st01 | 8 | 8 | 8 | yes/yes | yes/yes | yes/yes | yes/yes | yes/no | yes/no | no/no | yes/yes |
| evt-db01 | 4,13 | 4,5,13,14,15 | 4,5,13,14,15 | yes/yes | yes/yes | yes/yes | yes/yes | yes/no | yes/yes | yes/partial | yes/yes |
| evt-db02 | 9 | 9 | 9,10,15 | yes/yes | yes/yes | yes/yes | yes/yes | yes/no | yes/no | yes/partial | yes/yes |
| evt-ri01 | 2 | 2,3,5,15 | 2,3,5,15 | yes/yes | yes/yes | yes/yes | yes/yes | yes/no | no/no | no/no | yes/yes |
| evt-cf01 | 2 | 2,3,5,15 | 2,3,5,15 | yes/yes | yes/yes | yes/yes | yes/yes | yes/no | no/no | no/no | yes/yes |

Scored automatically by diagnosis.js from the report files: R1 needs method + URL in the report; R3/R4 look for observed and expected values in the failure text; R5-R7 for the response body, the prior state and the stored state. Postman CLI output never contains response bodies (R5) and has no database access (R7 at most a read-back over HTTP).
