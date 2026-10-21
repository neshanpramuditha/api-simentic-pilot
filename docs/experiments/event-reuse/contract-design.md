# Event Registration API — contract design (frozen before any validator rules were written)

Written 2026-10-19 before the API was implemented and before `event-api.rules.json` existed. The design follows common REST conventions chosen for this domain, not the Order or Library APIs. **Honesty note:** the designer (an AI assistant working in this repository) already knew the engine's assumptions from the two earlier experiments, so the design is *independent in intent* but not blind; it is not a third-party API.

## Conventions (and how they differ from the earlier APIs)

| Aspect | Order / Library APIs | Event Registration API |
|---|---|---|
| Base path | `/api/...` | `/v1/...` |
| Identifier | `id` (Long / UUID), path `{id}` | `registrationId`, server-generated string `REG-XXXXXXXX`, path `{registrationId}` |
| Owner key | `ownerUserId` / `memberId` (integer) | `attendeeRef` (string `ATT-<digits>`), plus `eventCode` (string) |
| State field | `status` | `state` |
| State change | `PATCH /…/{id}/status`, body `{"status": X}` | **Action endpoint** `POST /v1/registrations/{registrationId}/transitions`, body `{"to": X, "reason": "…"}` |
| Forbidden transition | `409 Conflict` | `422 Unprocessable Content` |
| Collection response | bare JSON array | **Envelope** `{"attendeeRef": …, "count": n, "items": [ … ]}` |
| Create response | `201` | `201` + `Location` header |
| Lifecycle | linear (Order) / single branch (Library) | 5 states, waitlist branch, cancellation from three states, cancellation needs a reason |
| operationIds | `createOrder`, `getOrder`, `updateStatus`, … / `createLoan`, `getLoan`, `updateLoanStatus`, … | `registerAttendee`, `fetchRegistration`, `listAttendeeRegistrations`, `transitionRegistration` |

## Operations

| operationId | Request | Success | Errors |
|---|---|---|---|
| `registerAttendee` | `POST /v1/registrations` `{"attendeeRef": "ATT-7", "eventCode": "JAVA-CONF-26", "seats": 2}` | `201`, `Location: /v1/registrations/{registrationId}`, Registration in `REQUESTED` | `400`: missing/invalid `attendeeRef` (`^ATT-[0-9]{1,8}$`), `eventCode` (`^[A-Z0-9-]{3,20}$`), `seats` (1..4) |
| `fetchRegistration` | `GET /v1/registrations/{registrationId}` | `200` Registration | `404` |
| `listAttendeeRegistrations` | `GET /v1/attendees/{attendeeRef}/registrations` | `200` envelope, items oldest request first (empty `items` if none) | — |
| `transitionRegistration` | `POST /v1/registrations/{registrationId}/transitions` `{"to": "CONFIRMED"}` or `{"to": "CANCELLED", "reason": "…"}` | `200` updated Registration | `400` (unknown target, missing body, `CANCELLED` without non-blank `reason`, reason > 200 chars), `404`, `422` transition not allowed |

## Registration resource

| Field | Type | Rule |
|---|---|---|
| `registrationId` | string | `REG-` + 8 upper-case letters/digits, server-generated |
| `attendeeRef`, `eventCode`, `seats` | string, string, integer | as created |
| `state` | `REQUESTED`, `WAITLISTED`, `CONFIRMED`, `CHECKED_IN`, `CANCELLED` | |
| `requestedAt` | date-time | always set |
| `confirmedAt` | date-time or null | set on the move to `CONFIRMED`; kept afterwards (also after a cancellation of a confirmed registration) |
| `checkedInAt` | date-time or null | set on the move to `CHECKED_IN` |
| `cancelledAt` | date-time or null | set on the move to `CANCELLED` |
| `cancellationReason` | string or null | the `reason` of the cancellation; null otherwise |

## Lifecycle (clean behaviour)

| From | Allowed targets |
|---|---|
| `REQUESTED` | `CONFIRMED`, `WAITLISTED`, `CANCELLED` |
| `WAITLISTED` | `CONFIRMED`, `CANCELLED` |
| `CONFIRMED` | `CHECKED_IN`, `CANCELLED` |
| `CHECKED_IN` | final |
| `CANCELLED` | final |

Every other pair, including a transition to the current state, is `422` and changes nothing.

## Semantic properties of the clean API (what a correct implementation guarantees)

1. `checkedInAt` is set iff `state = CHECKED_IN`.
2. `cancelledAt` is set iff `state = CANCELLED`; `cancellationReason` is set iff `state = CANCELLED`.
3. `state = CHECKED_IN` implies `confirmedAt` is set; `confirmedAt` set implies `state ∈ {CONFIRMED, CHECKED_IN, CANCELLED}`.
4. Time order: `requestedAt ≤ confirmedAt ≤ checkedInAt` when set.
5. Transitions follow the table; forbidden ones are `422` with nothing stored; the stored registration equals the returned one after a `200`.

Database (H2, separate from the other APIs): table `event_registration`, primary key `registration_id`.
