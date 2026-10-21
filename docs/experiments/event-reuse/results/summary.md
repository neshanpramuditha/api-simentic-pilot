# Event API matrix — summary

Runs with verify.json: 48; tool results: 72; incomplete run directories: none.

| Mode | Tool | Rep | Switch | Traffic | Exercised | Cause steps | Affected steps | Tool failure steps | FP steps | Detected at cause | Key TP / FN (semval) | Exit | ms |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| clean | semval-v01-active | 1 | ok | ok | - | - | - | - | - | - | 0 / 0 | 0 | 2566 |
| clean | semval-v01-active | 2 | ok | ok | - | - | - | - | - | - | 0 / 0 | 0 | 2624 |
| clean | semval-v02-active | 1 | ok | ok | - | - | - | - | - | - | 0 / 0 | 0 | 2913 |
| clean | semval-v02-active | 2 | ok | ok | - | - | - | - | - | - | 0 / 0 | 0 | 2545 |
| clean | semval-v01-passive | 1 | ok | ok | - | - | - | - | - | - | 0 / 0 | 0 | 402 |
| clean | semval-v01-passive | 2 | ok | ok | - | - | - | - | - | - | 0 / 0 | 0 | 424 |
| clean | semval-v02-passive | 1 | ok | ok | - | - | - | - | - | - | 0 / 0 | 0 | 418 |
| clean | semval-v02-passive | 2 | ok | ok | - | - | - | - | - | - | 0 / 0 | 0 | 405 |
| clean | postman | 1 | ok | ok | - | - | - | - | - | - | - | 0 | 4941 |
| clean | postman | 2 | ok | ok | - | - | - | - | - | - | - | 0 | 5379 |
| clean | ajv | 1 | ok | ok | - | - | - | - | - | - | - | - | 261 |
| clean | ajv | 2 | ok | ok | - | - | - | - | - | - | - | - | 245 |
| evt-st01 | semval-v01-active | 1 | ok | ok | yes | 8 | 8 | 8 | - | yes | 1 / 0 | 1 | 2473 |
| evt-st01 | semval-v01-active | 2 | ok | ok | yes | 8 | 8 | 8 | - | yes | 1 / 0 | 1 | 2827 |
| evt-st01 | semval-v02-active | 1 | ok | ok | yes | 8 | 8 | 8 | - | yes | 1 / 0 | 1 | 2531 |
| evt-st01 | semval-v02-active | 2 | ok | ok | yes | 8 | 8 | 8 | - | yes | 1 / 0 | 1 | 2516 |
| evt-st01 | semval-v01-passive | 1 | ok | ok | yes | 8 | 8 | 8 | - | yes | 1 / 0 | 1 | 395 |
| evt-st01 | semval-v01-passive | 2 | ok | ok | yes | 8 | 8 | 8 | - | yes | 1 / 0 | 1 | 838 |
| evt-st01 | semval-v02-passive | 1 | ok | ok | yes | 8 | 8 | 8 | - | yes | 1 / 0 | 1 | 450 |
| evt-st01 | semval-v02-passive | 2 | ok | ok | yes | 8 | 8 | 8 | - | yes | 1 / 0 | 1 | 546 |
| evt-st01 | postman | 1 | ok | ok | yes | 8 | 8 | 8 | - | yes | - | 1 | 4944 |
| evt-st01 | postman | 2 | ok | ok | yes | 8 | 8 | 8 | - | yes | - | 1 | 9004 |
| evt-st01 | ajv | 1 | ok | ok | yes | 8 | 8 | - | - | no | - | - | 285 |
| evt-st01 | ajv | 2 | ok | ok | yes | 8 | 8 | - | - | no | - | - | 295 |
| evt-db01 | semval-v01-active | 1 | ok | ok | yes | 4,13 | 4,5,13,14,15 | 4,5,13,14 | - | yes | 4 / 2 | 1 | 6034 |
| evt-db01 | semval-v01-active | 2 | ok | ok | yes | 4,13 | 4,5,13,14,15 | 4,5,13,14 | - | yes | 4 / 2 | 1 | 5965 |
| evt-db01 | semval-v02-active | 1 | ok | ok | yes | 4,13 | 4,5,13,14,15 | 4,5,13,14,15 | - | yes | 6 / 0 | 1 | 5792 |
| evt-db01 | semval-v02-active | 2 | ok | ok | yes | 4,13 | 4,5,13,14,15 | 4,5,13,14,15 | - | yes | 6 / 0 | 1 | 4288 |
| evt-db01 | semval-v01-passive | 1 | ok | ok | yes | 4,13 | 4,5,13,14,15 | 5,14 | - | no | 2 / 4 | 1 | 1705 |
| evt-db01 | semval-v01-passive | 2 | ok | ok | yes | 4,13 | 4,5,13,14,15 | 5,14 | - | no | 2 / 4 | 1 | 555 |
| evt-db01 | semval-v02-passive | 1 | ok | ok | yes | 4,13 | 4,5,13,14,15 | 5,14,15 | - | no | 4 / 2 | 1 | 2824 |
| evt-db01 | semval-v02-passive | 2 | ok | ok | yes | 4,13 | 4,5,13,14,15 | 5,14,15 | - | no | 4 / 2 | 1 | 643 |
| evt-db01 | postman | 1 | ok | ok | yes | 4,13 | 4,5,13,14,15 | 4,5,13,14,15 | - | yes | - | 1 | 13001 |
| evt-db01 | postman | 2 | ok | ok | yes | 4,13 | 4,5,13,14,15 | 4,5,13,14,15 | - | yes | - | 1 | 7585 |
| evt-db01 | ajv | 1 | ok | ok | yes | 4,13 | 4,5,13,14,15 | - | - | no | - | - | 1345 |
| evt-db01 | ajv | 2 | ok | ok | yes | 4,13 | 4,5,13,14,15 | - | - | no | - | - | 437 |
| evt-db02 | semval-v01-active | 1 | ok | ok | yes | 9 | 9,10,15 | 9 | - | yes | 1 / 0 | 1 | 3862 |
| evt-db02 | semval-v01-active | 2 | ok | ok | yes | 9 | 9,10,15 | 9 | - | yes | 1 / 0 | 1 | 3262 |
| evt-db02 | semval-v02-active | 1 | ok | ok | yes | 9 | 9,10,15 | 9 | - | yes | 1 / 0 | 1 | 3553 |
| evt-db02 | semval-v02-active | 2 | ok | ok | yes | 9 | 9,10,15 | 9 | - | yes | 1 / 0 | 1 | 3681 |
| evt-db02 | semval-v01-passive | 1 | ok | ok | yes | 9 | 9,10,15 | - | - | no | 0 / 1 | 0 | 442 |
| evt-db02 | semval-v01-passive | 2 | ok | ok | yes | 9 | 9,10,15 | - | - | no | 0 / 1 | 0 | 734 |
| evt-db02 | semval-v02-passive | 1 | ok | ok | yes | 9 | 9,10,15 | - | - | no | 0 / 1 | 0 | 461 |
| evt-db02 | semval-v02-passive | 2 | ok | ok | yes | 9 | 9,10,15 | - | - | no | 0 / 1 | 0 | 526 |
| evt-db02 | postman | 1 | ok | ok | yes | 9 | 9,10,15 | 9,10,15 | - | yes | - | 1 | 6136 |
| evt-db02 | postman | 2 | ok | ok | yes | 9 | 9,10,15 | 9,10,15 | - | yes | - | 1 | 5920 |
| evt-db02 | ajv | 1 | ok | ok | yes | 9 | 9,10,15 | - | - | no | - | - | 260 |
| evt-db02 | ajv | 2 | ok | ok | yes | 9 | 9,10,15 | - | - | no | - | - | 324 |
| evt-ri01 | semval-v01-active | 1 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5 | - | yes | 3 / 1 | 1 | 3183 |
| evt-ri01 | semval-v01-active | 2 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5 | - | yes | 3 / 1 | 1 | 3456 |
| evt-ri01 | semval-v02-active | 1 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | 4 / 0 | 1 | 3539 |
| evt-ri01 | semval-v02-active | 2 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | 4 / 0 | 1 | 3235 |
| evt-ri01 | semval-v01-passive | 1 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5 | - | yes | 3 / 1 | 1 | 446 |
| evt-ri01 | semval-v01-passive | 2 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5 | - | yes | 3 / 1 | 1 | 444 |
| evt-ri01 | semval-v02-passive | 1 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | 4 / 0 | 1 | 445 |
| evt-ri01 | semval-v02-passive | 2 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | 4 / 0 | 1 | 488 |
| evt-ri01 | postman | 1 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | - | 1 | 6169 |
| evt-ri01 | postman | 2 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | - | 1 | 6104 |
| evt-ri01 | ajv | 1 | ok | ok | yes | 2 | 2,3,5,15 | - | - | no | - | - | 233 |
| evt-ri01 | ajv | 2 | ok | ok | yes | 2 | 2,3,5,15 | - | - | no | - | - | 283 |
| evt-cf01 | semval-v01-active | 1 | ok | ok | yes | 2 | 2,3,5,15 | - | - | no | 0 / 4 | 0 | 2966 |
| evt-cf01 | semval-v01-active | 2 | ok | ok | yes | 2 | 2,3,5,15 | - | - | no | 0 / 4 | 0 | 3359 |
| evt-cf01 | semval-v02-active | 1 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | 4 / 0 | 1 | 2610 |
| evt-cf01 | semval-v02-active | 2 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | 4 / 0 | 1 | 2734 |
| evt-cf01 | semval-v01-passive | 1 | ok | ok | yes | 2 | 2,3,5,15 | - | - | no | 0 / 4 | 0 | 536 |
| evt-cf01 | semval-v01-passive | 2 | ok | ok | yes | 2 | 2,3,5,15 | - | - | no | 0 / 4 | 0 | 430 |
| evt-cf01 | semval-v02-passive | 1 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | 4 / 0 | 1 | 1003 |
| evt-cf01 | semval-v02-passive | 2 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | 4 / 0 | 1 | 510 |
| evt-cf01 | postman | 1 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | - | 1 | 5396 |
| evt-cf01 | postman | 2 | ok | ok | yes | 2 | 2,3,5,15 | 2,3,5,15 | - | yes | - | 1 | 6059 |
| evt-cf01 | ajv | 1 | ok | ok | yes | 2 | 2,3,5,15 | - | - | no | - | - | 407 |
| evt-cf01 | ajv | 2 | ok | ok | yes | 2 | 2,3,5,15 | - | - | no | - | - | 257 |

## Reproducibility

| Mode/tool | Repeats | Failure keys identical | Oracle identical | Final DB identical |
|---|---|---|---|---|
| clean/semval-v01-active | 2 | true | true | true |
| clean/semval-v02-active | 2 | true | true | true |
| clean/semval-v01-passive | 2 | true | true | true |
| clean/semval-v02-passive | 2 | true | true | true |
| clean/postman | 2 | true | true | true |
| clean/ajv | 2 | true | true | true |
| evt-st01/semval-v01-active | 2 | true | true | true |
| evt-st01/semval-v02-active | 2 | true | true | true |
| evt-st01/semval-v01-passive | 2 | true | true | true |
| evt-st01/semval-v02-passive | 2 | true | true | true |
| evt-st01/postman | 2 | true | true | true |
| evt-st01/ajv | 2 | true | true | true |
| evt-db01/semval-v01-active | 2 | true | true | true |
| evt-db01/semval-v02-active | 2 | true | true | true |
| evt-db01/semval-v01-passive | 2 | true | true | true |
| evt-db01/semval-v02-passive | 2 | true | true | true |
| evt-db01/postman | 2 | true | true | true |
| evt-db01/ajv | 2 | true | true | true |
| evt-db02/semval-v01-active | 2 | true | true | true |
| evt-db02/semval-v02-active | 2 | true | true | true |
| evt-db02/semval-v01-passive | 2 | true | true | true |
| evt-db02/semval-v02-passive | 2 | true | true | true |
| evt-db02/postman | 2 | true | true | true |
| evt-db02/ajv | 2 | true | true | true |
| evt-ri01/semval-v01-active | 2 | true | true | true |
| evt-ri01/semval-v02-active | 2 | true | true | true |
| evt-ri01/semval-v01-passive | 2 | true | true | true |
| evt-ri01/semval-v02-passive | 2 | true | true | true |
| evt-ri01/postman | 2 | true | true | true |
| evt-ri01/ajv | 2 | true | true | true |
| evt-cf01/semval-v01-active | 2 | true | true | true |
| evt-cf01/semval-v02-active | 2 | true | true | true |
| evt-cf01/semval-v01-passive | 2 | true | true | true |
| evt-cf01/semval-v02-passive | 2 | true | true | true |
| evt-cf01/postman | 2 | true | true | true |
| evt-cf01/ajv | 2 | true | true | true |

## Semantic-validator false negatives and extra failures (repeat 1)

- **evt-db01 / semval-v01-active**: EVT-INV-001@15#0 (list item not checked (one finding for the whole envelope)); EVT-INV-004@15#2 (list item not checked (one finding for the whole envelope))
- **evt-db01 / semval-v02-active**: no FN; extra FAIL keys: EVT-INV-005@14 (affected step), EVT-INV-005@15#2 (affected step)
- **evt-db01 / semval-v01-passive**: EVT-INV-001@15#0 (list item not checked (one finding for the whole envelope)); EVT-INV-004@15#2 (list item not checked (one finding for the whole envelope)); EVT-DB-001@4 (not evaluable: no database access (passive mode or no JDBC URL configured)); EVT-DB-001@13 (not evaluable: no database access (passive mode or no JDBC URL configured))
- **evt-db01 / semval-v02-passive**: EVT-DB-001@4 (not evaluable: no database access (passive mode or no JDBC URL configured)); EVT-DB-001@13 (not evaluable: no database access (passive mode or no JDBC URL configured)); extra FAIL keys: EVT-INV-005@14 (affected step), EVT-INV-005@15#2 (affected step)
- **evt-db02 / semval-v01-passive**: EVT-DB-001@9 (not evaluable: no database access (passive mode or no JDBC URL configured))
- **evt-db02 / semval-v02-passive**: EVT-DB-001@9 (not evaluable: no database access (passive mode or no JDBC URL configured))
- **evt-ri01 / semval-v01-active**: EVT-INV-001@15#0 (list item not checked (one finding for the whole envelope))
- **evt-ri01 / semval-v01-passive**: EVT-INV-001@15#0 (list item not checked (one finding for the whole envelope))
- **evt-cf01 / semval-v01-active**: EVT-INV-005@2 (rule absent from this rule set); EVT-INV-005@3 (rule absent from this rule set); EVT-INV-005@5 (rule absent from this rule set); EVT-INV-005@15#0 (rule absent from this rule set)
- **evt-cf01 / semval-v01-passive**: EVT-INV-005@2 (rule absent from this rule set); EVT-INV-005@3 (rule absent from this rule set); EVT-INV-005@5 (rule absent from this rule set); EVT-INV-005@15#0 (rule absent from this rule set)

## Postman failures by tag (repeat 1)

- clean: 0/136 assertions failed; {}; keys -; invariant failures match oracle: true
- evt-st01: 2/130 assertions failed; {"STEP":2}; keys STEP@8, STEP@8; invariant failures match oracle: true
- evt-db01: 11/136 assertions failed; {"READBACK":2,"INV-001":2,"STEP":3,"INV-004":2,"INV-005":2}; keys READBACK@4, EVT-INV-001@5, STEP@5, READBACK@13, EVT-INV-004@14, EVT-INV-005@14, STEP@14, EVT-INV-001@15#0, EVT-INV-004@15#2, EVT-INV-005@15#2, STEP@15; invariant failures match oracle: false
- evt-db02: 3/136 assertions failed; {"READBACK":1,"STEP":2}; keys READBACK@9, STEP@10, STEP@15; invariant failures match oracle: true
- evt-ri01: 4/136 assertions failed; {"INV-001":4}; keys EVT-INV-001@2, EVT-INV-001@3, EVT-INV-001@5, EVT-INV-001@15#0; invariant failures match oracle: true
- evt-cf01: 4/136 assertions failed; {"INV-005":4}; keys EVT-INV-005@2, EVT-INV-005@3, EVT-INV-005@5, EVT-INV-005@15#0; invariant failures match oracle: true
