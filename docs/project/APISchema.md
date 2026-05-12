# API schema (REST)

Human-readable reference derived from [`source/APISchema.md`](source/APISchema.md). **Type:** REST. All business routes live under **`/api/v1`**. Most responses use the shared envelope **`ApiResponse<T>`** unless noted.

## Conventions

### Authentication

- **JWT Bearer** for protected routes: header `Authorization: Bearer <access_token>`.  
- **Anonymous** routes: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, OpenAPI static paths (`/swagger-ui/**`, `/v3/api-docs/**`), and `/.well-known/jwks.json` (see `SecurityConfig`).  
- **`POST /api/v1/auth/logout`:** expects Bearer access token; returns **204 No Content** with **no JSON body** on success.

### Authorities (examples)

Routes are protected with Spring Security **authorities** such as `accounts:read`, `accounts:write`, `payments:write`, `loans:read`, `loans:write`, `audit:read`, `notifications:read`. See [`SecurityConfig`](../../bank-config/src/main/java/io/github/alexisTrejo11/bank/security/SecurityConfig.java) for the authoritative mapping.

### Rate limiting (when `bank.rate-limiting.enabled=true`)

- **Global** Redis token bucket (default **64** burst, **1** token/s refill) applies broadly.  
- **nginx** (Compose) adds **30 r/s** zone on `/api/` (see `infra/nginx/nginx.conf`).  
- **Annotated profiles:** **STRICT** (auth), **STANDARD** (e.g. `/me`), **SENSITIVE_OPERATIONS** (loan writes), **STRICT** per user on `TransferController`. Defaults: STRICT **12 @ 0.2/s**, STANDARD **48 @ 0.8/s**, SENSITIVE **6 @ 0.1/s** unless overridden in YAML.

### Error shape

Domain failures on payments/loans often return **422 Unprocessable Content** with:

```json
{ "success": false, "errorCode": "SOME_CODE", "errorMessage": "Human-readable message" }
```

Exact field names match your `ApiResponse` implementation in **`bank-shared`**.

---

## Endpoint index

| Method | Path | Authenticated | Summary |
|--------|------|---------------|---------|
| POST | `/api/v1/auth/register` | No | Register; returns tokens (**201**) |
| POST | `/api/v1/auth/login` | No | Login with email/password |
| POST | `/api/v1/auth/refresh` | No | Rotate tokens with refresh token |
| POST | `/api/v1/auth/logout` | Yes (Bearer) | Logout — **204** empty body |
| GET | `/api/v1/auth/me` | Yes | Current user id + email |
| POST | `/api/v1/accounts` | Yes | Open account (`accounts:write`) |
| GET | `/api/v1/accounts/{accountId}/balance` | Yes | Derived balance (`accounts:read`) |
| GET | `/api/v1/accounts/{accountId}/ledger` | Yes | Paginated ledger (`accounts:read`) |
| POST | `/api/v1/payments/transfers` | Yes | Transfer (`payments:write`) + **`Idempotency-Key`** header |
| POST | `/api/v1/payments/transfers/{transferId}/reverse` | Yes | Reverse transfer + **`Idempotency-Key`** |
| POST | `/api/v1/loans` | Yes | Originate loan (`loans:write`) |
| POST | `/api/v1/loans/{loanId}/approve` | Yes | Approve loan (`loans:write`) |
| GET | `/api/v1/loans/{loanId}` | Yes | Loan detail (`loans:read`) |
| POST | `/api/v1/loans/{loanId}/repayments/{repaymentId}/pay` | Yes | Pay installment (`loans:write`) |
| GET | `/api/v1/audit/records` | Yes | Search audit (`audit:read`) |
| GET | `/api/v1/notifications/monitoring/records` | Yes | Notification records (`notifications:read`) |
| GET | `/api/v1/notifications/monitoring/summary` | Yes | Notification summary (`notifications:read`) |
| GET | `/actuator/health` | No* | Liveness/readiness — **restrict on AWS** |

\*Permitted without auth in app config; **must** be protected by network controls in production.

---

## Authentication

### `POST /api/v1/auth/register`

- **Body:** `{ "email": "string", "password": "string" }` — password length **8–128** (see `RegisterRequest`).  
- **Responses:** **201** `ApiResponse<TokenResponse>`; **400** validation; **429** rate limit.  
- **`TokenResponse` fields:** `accessToken`, `refreshToken`, `tokenType` (e.g. `Bearer`), `expiresInSeconds`.

### `POST /api/v1/auth/login`

- **Body:** `{ "email": "string", "password": "string" }` (`LoginRequest`).  
- **Responses:** **200** token bundle; **401** invalid credentials.

### `POST /api/v1/auth/refresh`

- **Body:** `{ "refreshToken": "string" }` (`RefreshRequest`).  
- **Responses:** **200** new tokens; **401** invalid refresh.

### `POST /api/v1/auth/logout`

- **Headers:** `Authorization: Bearer <access>`.  
- **Responses:** **204** no body; **401** missing/invalid header.

### `GET /api/v1/auth/me`

- **Responses:** **200** `ApiResponse<MeResponse>` with `userId`, `email`.

---

## Accounts

### `POST /api/v1/accounts`

- **Authority:** `accounts:write`.  
- **Body:** `{ "type": "CHECKING|SAVINGS|INTERNAL|LOAN", "currency": "USD" }` (ISO 4217 alpha **3** letters).  
- **Responses:** **200** `ApiResponse<OpenAccountResponse>` (`accountId`, `currency`, `type`); **403** without authority.

### `GET /api/v1/accounts/{accountId}/balance`

- **Authority:** `accounts:read`.  
- **Path:** `accountId` UUID.  
- **Responses:** **200** `ApiResponse<BalanceResponse>`; **404** not found / not owned.

### `GET /api/v1/accounts/{accountId}/ledger`

- **Authority:** `accounts:read`.  
- **Query:** standard Spring Data **`page`**, **`size`** (default **20**), optional **`sort`**.  
- **Responses:** **200** `ApiResponse<LedgerPageResponse>`.

---

## Payments

### `POST /api/v1/payments/transfers`

- **Authority:** `payments:write`.  
- **Headers:** **`Idempotency-Key: <UUID>`** (required).  
- **Body:** `{ "sourceAccountId", "targetAccountId", "amount", "currency" }` (`TransferFundsRequest`).  
- **Responses:** **200** success; **422** domain failure (`ApiResponse` failure).

### `POST /api/v1/payments/transfers/{transferId}/reverse`

- **Authority:** `payments:write`.  
- **Headers:** **`Idempotency-Key`**.  
- **Responses:** **200** / **422** analogous to transfer.

---

## Loans

### `POST /api/v1/loans`

- **Authority:** `loans:write`.  
- **Body:** `checkingAccountId`, `principal`, `currency`, `monthlyInterestRate`, `termMonths` (`OriginateLoanRequest`).  
- **Responses:** **200** `ApiResponse<LoanDetailResponse>`.

### `POST /api/v1/loans/{loanId}/approve`

- **Authority:** `loans:write`.  
- **Responses:** **200** `ApiResponse<LoanDetailResponse>`.

### `GET /api/v1/loans/{loanId}`

- **Authority:** `loans:read`.  
- **Responses:** **200** `ApiResponse<LoanDetailResponse>`.

### `POST /api/v1/loans/{loanId}/repayments/{repaymentId}/pay`

- **Authority:** `loans:write`.  
- **Responses:** **200** `ApiResponse<PayRepaymentResponse>`; **422** on business rule failure.

---

## Audit

### `GET /api/v1/audit/records`

- **Authority:** `audit:read`.  
- **Query (optional):** `eventType`, `actorId`, `entityType`, `entityId`, `from`, `to` (instants), plus **`page`**, **`size`**.  
- **Responses:** **200** `ApiResponse<AuditRecordsPageResponse>`.

---

## Notifications (monitoring)

### `GET /api/v1/notifications/monitoring/records`

- **Authority:** `notifications:read`.  
- **Query:** optional `status`, `channel`; pagination `page`, `size`, optional `sort` (default **`createdAt,DESC`**).  
- **Responses:** **200** `ApiResponse<NotificationRecordsPageResponse>`.

### `GET /api/v1/notifications/monitoring/summary`

- **Authority:** `notifications:read`.  
- **Responses:** **200** `ApiResponse<NotificationSummaryResponse>`.

---

## Operations

### `GET /actuator/health`

- **Auth:** Unauthenticated in app configuration (dedicated filter chain).  
- **Responses:** **200** `UP`; **503** `DOWN` when dependencies fail.  
- **Production:** Do **not** expose publicly without VPC/security group controls; ALB target groups may call this path from private networks only.

---

## OpenAPI and Swagger

- **Swagger UI:** `/swagger-ui/` (and related springdoc paths).  
- **OpenAPI JSON:** `/v3/api-docs` (springdoc).  
- These URLs are **`permitAll`** — protect on AWS if the catalog should not be public.

## Related documentation

- [Project features](ProjectFeatures.md) — feature-level description  
- [InfrastructureModel.md](InfrastructureModel.md) — nginx and TLS story  
- [Project links](ProjectLinks.md) — placeholder public Swagger URL  
