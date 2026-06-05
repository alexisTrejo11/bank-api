---
type: "REST"

httpEndpoints:
  - id: "health-check"
    method: "GET"
    urlPath: "/actuator/health"
    summary: "Service health check"
    description: "Spring Boot Actuator liveness/readiness probe for load balancers, Docker HEALTHCHECK, and EC2 monitoring."
    tags: ["service"]
    authenticated: false
    rateLimit: "—"
    responses:
      - status: 200
        description: "Service is up"
        example:
          status: "UP"

  - id: "prometheus-metrics"
    method: "GET"
    urlPath: "/actuator/prometheus"
    summary: "Prometheus scrape endpoint"
    description: "Micrometer metrics in Prometheus text format. Scraped by Prometheus on EC2 (see docker/MONITORING.md)."
    tags: ["service"]
    authenticated: false
    rateLimit: "—"
    responses:
      - status: 200
        description: "Prometheus metrics text"

  - id: "openapi-swagger"
    method: "GET"
    urlPath: "/swagger-ui.html"
    summary: "Swagger UI"
    description: "Interactive OpenAPI documentation for all registered endpoints."
    tags: ["service"]
    authenticated: false
    rateLimit: "—"
    responses:
      - status: 200
        description: "HTML Swagger UI"

  - id: "auth-register"
    method: "POST"
    urlPath: "/api/v1/auth/register"
    summary: "Register a new user"
    description: "Creates a customer account and returns JWT access + refresh tokens."
    tags: ["auth"]
    authenticated: false
    rateLimit: "STRICT — token bucket per IP"
    requestBody:
      contentType: "application/json"
      schema:
        email: "string (required)"
        password: "string (required)"
      example:
        email: "customer@example.com"
        password: "SecurePass123!"
    responses:
      - status: 201
        description: "Registration successful"
        example:
          data:
            accessToken: "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
            refreshToken: "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
          meta:
            timestamp: "2026-06-05T12:00:00Z"
          errors: []

  - id: "auth-login"
    method: "POST"
    urlPath: "/api/v1/auth/login"
    summary: "Authenticate and obtain JWT"
    description: "Email/password login. Returns access and refresh tokens (RS256 JWT with roles and permissions claims)."
    tags: ["auth"]
    authenticated: false
    rateLimit: "STRICT — token bucket per IP"
    requestBody:
      contentType: "application/json"
      schema:
        email: "string (required)"
        password: "string (required)"
      example:
        email: "customer@example.com"
        password: "SecurePass123!"
    responses:
      - status: 200
        description: "Login successful"
        example:
          data:
            accessToken: "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
            refreshToken: "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

  - id: "auth-refresh"
    method: "POST"
    urlPath: "/api/v1/auth/refresh"
    summary: "Rotate refresh token"
    description: "Issues new access + refresh tokens; old refresh token invalidated in Redis."
    tags: ["auth"]
    authenticated: false
    rateLimit: "STRICT — token bucket per IP"
    requestBody:
      contentType: "application/json"
      schema:
        refreshToken: "string (required)"
      example:
        refreshToken: "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
    responses:
      - status: 200
        description: "Tokens rotated"

  - id: "auth-logout"
    method: "POST"
    urlPath: "/api/v1/auth/logout"
    summary: "Logout and blocklist JWT"
    description: "Requires Bearer access token. Adds jti to Redis blocklist for remaining TTL."
    tags: ["auth"]
    authenticated: true
    rateLimit: "STANDARD — per user"
    responses:
      - status: 204
        description: "Logout successful"

  - id: "auth-me"
    method: "GET"
    urlPath: "/api/v1/auth/me"
    summary: "Get current user profile"
    description: "Returns userId and username for the authenticated principal."
    tags: ["auth"]
    authenticated: true
    rateLimit: "STANDARD — per user"
    responses:
      - status: 200
        description: "Profile retrieved"
        example:
          data:
            userId: "550e8400-e29b-41d4-a716-446655440000"
            username: "customer@example.com"

  - id: "accounts-open"
    method: "POST"
    urlPath: "/api/v1/accounts"
    summary: "Open a bank account"
    description: "Creates CHECKING, SAVINGS, or LOAN account for the authenticated user."
    tags: ["accounts"]
    authenticated: true
    rateLimit: "—"
    requestBody:
      contentType: "application/json"
      schema:
        type: "CHECKING | SAVINGS | LOAN (required)"
        currency: "string ISO 4217 (required, e.g. USD)"
      example:
        type: "CHECKING"
        currency: "USD"
    responses:
      - status: 200
        description: "Account opened"
        example:
          data:
            accountId: "660e8400-e29b-41d4-a716-446655440001"
            type: "CHECKING"
            currency: "USD"
            status: "ACTIVE"

  - id: "accounts-balance"
    method: "GET"
    urlPath: "/api/v1/accounts/{accountId}/balance"
    summary: "Get account balance"
    description: "Balance derived from ledger SUM(CREDIT) - SUM(DEBIT). User must own the account."
    tags: ["accounts"]
    authenticated: true
    rateLimit: "—"
    parameters:
      - name: "accountId"
        in: "path"
        type: "UUID"
        required: true
        description: "Account UUID"
    responses:
      - status: 200
        description: "Balance retrieved"
        example:
          data:
            accountId: "660e8400-e29b-41d4-a716-446655440001"
            amount: "1250.00"
            currency: "USD"

  - id: "accounts-ledger"
    method: "GET"
    urlPath: "/api/v1/accounts/{accountId}/ledger"
    summary: "Paginated ledger entries"
    description: "Returns DEBIT/CREDIT entries for the account (default page size 20)."
    tags: ["accounts"]
    authenticated: true
    rateLimit: "—"
    parameters:
      - name: "accountId"
        in: "path"
        type: "UUID"
        required: true
        description: "Account UUID"
      - name: "page"
        in: "query"
        type: "integer"
        required: false
        description: "Page number (0-based)"
      - name: "size"
        in: "query"
        type: "integer"
        required: false
        description: "Page size"
    responses:
      - status: 200
        description: "Ledger page"

  - id: "payments-transfer"
    method: "POST"
    urlPath: "/api/v1/payments/transfers"
    summary: "Initiate a transfer"
    description: "Moves funds between two accounts owned by the user. Requires Idempotency-Key header (UUID). Same-currency only; no FX in v1."
    tags: ["payments"]
    authenticated: true
    rateLimit: "STRICT — per user (controller-level)"
    parameters:
      - name: "Idempotency-Key"
        in: "header"
        type: "UUID"
        required: true
        description: "Client-generated idempotency key (24h Redis TTL in docker profile)"
    requestBody:
      contentType: "application/json"
      schema:
        sourceAccountId: "UUID (required)"
        targetAccountId: "UUID (required)"
        amount: "decimal > 0 (required)"
        currency: "string ISO 4217 (required)"
      example:
        sourceAccountId: "660e8400-e29b-41d4-a716-446655440001"
        targetAccountId: "770e8400-e29b-41d4-a716-446655440002"
        amount: "100.00"
        currency: "USD"
    responses:
      - status: 200
        description: "Transfer completed"
      - status: 422
        description: "Domain failure (insufficient funds, currency mismatch, etc.)"
        example:
          data: null
          errors:
            - code: "INSUFFICIENT_FUNDS"
              message: "Source account balance too low"

  - id: "payments-reverse"
    method: "POST"
    urlPath: "/api/v1/payments/transfers/{transferId}/reverse"
    summary: "Reverse a completed transfer"
    description: "Only COMPLETED transfers may be reversed. Requires Idempotency-Key header."
    tags: ["payments"]
    authenticated: true
    rateLimit: "STRICT — per user"
    parameters:
      - name: "transferId"
        in: "path"
        type: "UUID"
        required: true
        description: "Transfer UUID"
      - name: "Idempotency-Key"
        in: "header"
        type: "UUID"
        required: true
        description: "Idempotency key for reversal"
    responses:
      - status: 200
        description: "Transfer reversed"
      - status: 422
        description: "Reversal not allowed"

  - id: "loans-originate"
    method: "POST"
    urlPath: "/api/v1/loans"
    summary: "Originate a loan"
    description: "Creates a loan in PENDING_APPROVAL with amortization schedule generated at origination."
    tags: ["loans"]
    authenticated: true
    rateLimit: "SENSITIVE_OPERATIONS — per user"
    requestBody:
      contentType: "application/json"
      schema:
        checkingAccountId: "UUID (required)"
        principal: "decimal (required)"
        currency: "string (required)"
        monthlyInterestRate: "decimal e.g. 0.0125 (required)"
        termMonths: "integer (required)"
      example:
        checkingAccountId: "660e8400-e29b-41d4-a716-446655440001"
        principal: "5000.00"
        currency: "USD"
        monthlyInterestRate: "0.01"
        termMonths: 12
    responses:
      - status: 200
        description: "Loan originated"

  - id: "loans-approve"
    method: "POST"
    urlPath: "/api/v1/loans/{loanId}/approve"
    summary: "Approve and disburse loan"
    description: "Transitions loan to ACTIVE, creates LOAN account, credits checking account via domain events."
    tags: ["loans"]
    authenticated: true
    rateLimit: "SENSITIVE_OPERATIONS — per user"
    parameters:
      - name: "loanId"
        in: "path"
        type: "UUID"
        required: true
        description: "Loan UUID"
    responses:
      - status: 200
        description: "Loan approved and disbursed"

  - id: "loans-get"
    method: "GET"
    urlPath: "/api/v1/loans/{loanId}"
    summary: "Get loan detail"
    description: "Returns loan aggregate including repayment schedule and status."
    tags: ["loans"]
    authenticated: true
    rateLimit: "—"
    parameters:
      - name: "loanId"
        in: "path"
        type: "UUID"
        required: true
        description: "Loan UUID"
    responses:
      - status: 200
        description: "Loan detail"

  - id: "loans-pay-repayment"
    method: "POST"
    urlPath: "/api/v1/loans/{loanId}/repayments/{repaymentId}/pay"
    summary: "Pay a loan installment"
    description: "Debits customer checking account; marks repayment PAID; may transition loan to PAID_OFF."
    tags: ["loans"]
    authenticated: true
    rateLimit: "SENSITIVE_OPERATIONS — per user"
    parameters:
      - name: "loanId"
        in: "path"
        type: "UUID"
        required: true
      - name: "repaymentId"
        in: "path"
        type: "UUID"
        required: true
    responses:
      - status: 200
        description: "Repayment paid"
      - status: 422
        description: "Already paid or insufficient funds"

  - id: "audit-records"
    method: "GET"
    urlPath: "/api/v1/audit/records"
    summary: "Search audit records"
    description: "Append-only audit log query. Requires audit:read permission. Filters by eventType, actorId, entityType, entityId, date range."
    tags: ["audit"]
    authenticated: true
    rateLimit: "—"
    parameters:
      - name: "eventType"
        in: "query"
        type: "string"
        required: false
      - name: "actorId"
        in: "query"
        type: "UUID"
        required: false
      - name: "entityType"
        in: "query"
        type: "string"
        required: false
      - name: "entityId"
        in: "query"
        type: "UUID"
        required: false
      - name: "from"
        in: "query"
        type: "Instant (ISO-8601)"
        required: false
      - name: "to"
        in: "query"
        type: "Instant (ISO-8601)"
        required: false
    responses:
      - status: 200
        description: "Paginated audit records"

  - id: "notifications-records"
    method: "GET"
    urlPath: "/api/v1/notifications/monitoring/records"
    summary: "List notification delivery records"
    description: "Operational monitoring of sent/failed notifications. Filter by status and channel."
    tags: ["notifications"]
    authenticated: true
    rateLimit: "—"
    parameters:
      - name: "status"
        in: "query"
        type: "SENT | FAILED"
        required: false
      - name: "channel"
        in: "query"
        type: "EMAIL | SMS"
        required: false
    responses:
      - status: 200
        description: "Notification records page"

  - id: "notifications-summary"
    method: "GET"
    urlPath: "/api/v1/notifications/monitoring/summary"
    summary: "Notification delivery summary"
    description: "Aggregate counts by status and channel for ops dashboards."
    tags: ["notifications"]
    authenticated: true
    rateLimit: "—"
    responses:
      - status: 200
        description: "Summary counts"
---

# API Schema

> **Base URL (production placeholder):** `https://{{YOUR_DOMAIN_OR_EC2}}`

> **Auth header:** `Authorization: Bearer <access_token>` for protected routes.

> **Idempotency:** All `POST /api/v1/payments/*` mutations require `Idempotency-Key: <UUID>` header.

> **Response envelope:** Success returns `{ data, meta, errors: [] }`; logical failures use HTTP 422 with `errors[{ code, message }]`.

> **JWKS:** Public key available at `GET /.well-known/jwks.json` for token verification by gateways or BFFs.

> **Warning:** Audit and notification monitoring endpoints should be restricted to ADMIN/AUDITOR roles in production API gateways.
