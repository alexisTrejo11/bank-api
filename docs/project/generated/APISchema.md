# API Schema

**API type:** REST

## Accounts

### `POST` /api/v1/accounts

**Open a bank account**

Creates CHECKING, SAVINGS, or LOAN account for the authenticated user.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | — |
| **Tags** | accounts |

#### Request body

**Content-Type:** `application/json`

**Schema (summary):**

```json
{
  "type": "CHECKING | SAVINGS | LOAN (required)",
  "currency": "string ISO 4217 (required, e.g. USD)"
}
```

**Example:**

```json
{
  "type": "CHECKING",
  "currency": "USD"
}
```

#### Responses

- **200** — Account opened

```json
{
  "data": {
    "accountId": "660e8400-e29b-41d4-a716-446655440001",
    "type": "CHECKING",
    "currency": "USD",
    "status": "ACTIVE"
  }
}
```

---

### `GET` /api/v1/accounts/{accountId}/balance

**Get account balance**

Balance derived from ledger SUM(CREDIT) - SUM(DEBIT). User must own the account.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | — |
| **Tags** | accounts |

#### Parameters

| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| accountId | path | UUID | Yes | Account UUID |

#### Responses

- **200** — Balance retrieved

```json
{
  "data": {
    "accountId": "660e8400-e29b-41d4-a716-446655440001",
    "amount": "1250.00",
    "currency": "USD"
  }
}
```

---

### `GET` /api/v1/accounts/{accountId}/ledger

**Paginated ledger entries**

Returns DEBIT/CREDIT entries for the account (default page size 20).

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | — |
| **Tags** | accounts |

#### Parameters

| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| accountId | path | UUID | Yes | Account UUID |
| page | query | integer | No | Page number (0-based) |
| size | query | integer | No | Page size |

#### Responses

- **200** — Ledger page
---

## Audit

### `GET` /api/v1/audit/records

**Search audit records**

Append-only audit log query. Requires audit:read permission. Filters by eventType, actorId, entityType, entityId, date range.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | — |
| **Tags** | audit |

#### Parameters

| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| eventType | query | string | No |  |
| actorId | query | UUID | No |  |
| entityType | query | string | No |  |
| entityId | query | UUID | No |  |
| from | query | Instant (ISO-8601) | No |  |
| to | query | Instant (ISO-8601) | No |  |

#### Responses

- **200** — Paginated audit records
---

## Auth

### `POST` /api/v1/auth/register

**Register a new user**

Creates a customer account and returns JWT access + refresh tokens.

| | |
|---|---|
| **Auth required** | No |
| **Rate limit** | STRICT — token bucket per IP |
| **Tags** | auth |

#### Request body

**Content-Type:** `application/json`

**Schema (summary):**

```json
{
  "email": "string (required)",
  "password": "string (required)"
}
```

**Example:**

```json
{
  "email": "customer@example.com",
  "password": "SecurePass123!"
}
```

#### Responses

- **201** — Registration successful

```json
{
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "meta": {
    "timestamp": "2026-06-05T12:00:00Z"
  },
  "errors": []
}
```

---

### `POST` /api/v1/auth/login

**Authenticate and obtain JWT**

Email/password login. Returns access and refresh tokens (RS256 JWT with roles and permissions claims).

| | |
|---|---|
| **Auth required** | No |
| **Rate limit** | STRICT — token bucket per IP |
| **Tags** | auth |

#### Request body

**Content-Type:** `application/json`

**Schema (summary):**

```json
{
  "email": "string (required)",
  "password": "string (required)"
}
```

**Example:**

```json
{
  "email": "customer@example.com",
  "password": "SecurePass123!"
}
```

#### Responses

- **200** — Login successful

```json
{
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

### `POST` /api/v1/auth/refresh

**Rotate refresh token**

Issues new access + refresh tokens; old refresh token invalidated in Redis.

| | |
|---|---|
| **Auth required** | No |
| **Rate limit** | STRICT — token bucket per IP |
| **Tags** | auth |

#### Request body

**Content-Type:** `application/json`

**Schema (summary):**

```json
{
  "refreshToken": "string (required)"
}
```

**Example:**

```json
{
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Responses

- **200** — Tokens rotated
---

### `POST` /api/v1/auth/logout

**Logout and blocklist JWT**

Requires Bearer access token. Adds jti to Redis blocklist for remaining TTL.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | STANDARD — per user |
| **Tags** | auth |

#### Responses

- **204** — Logout successful
---

### `GET` /api/v1/auth/me

**Get current user profile**

Returns userId and username for the authenticated principal.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | STANDARD — per user |
| **Tags** | auth |

#### Responses

- **200** — Profile retrieved

```json
{
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "customer@example.com"
  }
}
```

---

## Loans

### `POST` /api/v1/loans

**Originate a loan**

Creates a loan in PENDING_APPROVAL with amortization schedule generated at origination.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | SENSITIVE_OPERATIONS — per user |
| **Tags** | loans |

#### Request body

**Content-Type:** `application/json`

**Schema (summary):**

```json
{
  "checkingAccountId": "UUID (required)",
  "principal": "decimal (required)",
  "currency": "string (required)",
  "monthlyInterestRate": "decimal e.g. 0.0125 (required)",
  "termMonths": "integer (required)"
}
```

**Example:**

```json
{
  "checkingAccountId": "660e8400-e29b-41d4-a716-446655440001",
  "principal": "5000.00",
  "currency": "USD",
  "monthlyInterestRate": "0.01",
  "termMonths": 12
}
```

#### Responses

- **200** — Loan originated
---

### `POST` /api/v1/loans/{loanId}/approve

**Approve and disburse loan**

Transitions loan to ACTIVE, creates LOAN account, credits checking account via domain events.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | SENSITIVE_OPERATIONS — per user |
| **Tags** | loans |

#### Parameters

| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| loanId | path | UUID | Yes | Loan UUID |

#### Responses

- **200** — Loan approved and disbursed
---

### `GET` /api/v1/loans/{loanId}

**Get loan detail**

Returns loan aggregate including repayment schedule and status.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | — |
| **Tags** | loans |

#### Parameters

| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| loanId | path | UUID | Yes | Loan UUID |

#### Responses

- **200** — Loan detail
---

### `POST` /api/v1/loans/{loanId}/repayments/{repaymentId}/pay

**Pay a loan installment**

Debits customer checking account; marks repayment PAID; may transition loan to PAID_OFF.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | SENSITIVE_OPERATIONS — per user |
| **Tags** | loans |

#### Parameters

| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| loanId | path | UUID | Yes |  |
| repaymentId | path | UUID | Yes |  |

#### Responses

- **200** — Repayment paid
- **422** — Already paid or insufficient funds
---

## Notifications

### `GET` /api/v1/notifications/monitoring/records

**List notification delivery records**

Operational monitoring of sent/failed notifications. Filter by status and channel.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | — |
| **Tags** | notifications |

#### Parameters

| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| status | query | SENT | FAILED | No |  |
| channel | query | EMAIL | SMS | No |  |

#### Responses

- **200** — Notification records page
---

### `GET` /api/v1/notifications/monitoring/summary

**Notification delivery summary**

Aggregate counts by status and channel for ops dashboards.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | — |
| **Tags** | notifications |

#### Responses

- **200** — Summary counts
---

## Payments

### `POST` /api/v1/payments/transfers

**Initiate a transfer**

Moves funds between two accounts owned by the user. Requires Idempotency-Key header (UUID). Same-currency only; no FX in v1.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | STRICT — per user (controller-level) |
| **Tags** | payments |

#### Parameters

| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| Idempotency-Key | header | UUID | Yes | Client-generated idempotency key (24h Redis TTL in docker profile) |

#### Request body

**Content-Type:** `application/json`

**Schema (summary):**

```json
{
  "sourceAccountId": "UUID (required)",
  "targetAccountId": "UUID (required)",
  "amount": "decimal > 0 (required)",
  "currency": "string ISO 4217 (required)"
}
```

**Example:**

```json
{
  "sourceAccountId": "660e8400-e29b-41d4-a716-446655440001",
  "targetAccountId": "770e8400-e29b-41d4-a716-446655440002",
  "amount": "100.00",
  "currency": "USD"
}
```

#### Responses

- **200** — Transfer completed
- **422** — Domain failure (insufficient funds, currency mismatch, etc.)

```json
{
  "data": null,
  "errors": [
    {
      "code": "INSUFFICIENT_FUNDS",
      "message": "Source account balance too low"
    }
  ]
}
```

---

### `POST` /api/v1/payments/transfers/{transferId}/reverse

**Reverse a completed transfer**

Only COMPLETED transfers may be reversed. Requires Idempotency-Key header.

| | |
|---|---|
| **Auth required** | Yes |
| **Rate limit** | STRICT — per user |
| **Tags** | payments |

#### Parameters

| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| transferId | path | UUID | Yes | Transfer UUID |
| Idempotency-Key | header | UUID | Yes | Idempotency key for reversal |

#### Responses

- **200** — Transfer reversed
- **422** — Reversal not allowed
---

## Service

### `GET` /actuator/health

**Service health check**

Spring Boot Actuator liveness/readiness probe for load balancers, Docker HEALTHCHECK, and EC2 monitoring.

| | |
|---|---|
| **Auth required** | No |
| **Rate limit** | — |
| **Tags** | service |

#### Responses

- **200** — Service is up

```json
{
  "status": "UP"
}
```

---

### `GET` /actuator/prometheus

**Prometheus scrape endpoint**

Micrometer metrics in Prometheus text format. Scraped by Prometheus on EC2 (see docker/MONITORING.md).

| | |
|---|---|
| **Auth required** | No |
| **Rate limit** | — |
| **Tags** | service |

#### Responses

- **200** — Prometheus metrics text
---

### `GET` /swagger-ui.html

**Swagger UI**

Interactive OpenAPI documentation for all registered endpoints.

| | |
|---|---|
| **Auth required** | No |
| **Rate limit** | — |
| **Tags** | service |

#### Responses

- **200** — HTML Swagger UI
---

## Additional notes

# API Schema

> **Base URL (production placeholder):** `https://{{YOUR_DOMAIN_OR_EC2}}`

> **Auth header:** `Authorization: Bearer <access_token>` for protected routes.

> **Idempotency:** All `POST /api/v1/payments/*` mutations require `Idempotency-Key: <UUID>` header.

> **Response envelope:** Success returns `{ data, meta, errors: [] }`; logical failures use HTTP 422 with `errors[{ code, message }]`.

> **JWKS:** Public key available at `GET /.well-known/jwks.json` for token verification by gateways or BFFs.

> **Warning:** Audit and notification monitoring endpoints should be restricted to ADMIN/AUDITOR roles in production API gateways.

