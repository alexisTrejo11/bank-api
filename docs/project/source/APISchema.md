---
type: "REST"
httpEndpoints:
  - id: "ep-auth-register"
    method: "POST"
    urlPath: "/api/v1/auth/register"
    summary: "Register a new user and receive tokens"
    description: "Creates credentials, seeds default authorities, and returns access + refresh token bundle wrapped in ApiResponse."
    tags: ["auth"]
    authenticated: false
    rateLimit: "STRICT profile (default 12 burst @ 0.2 token/s) when bank.rate-limiting.enabled=true"
    requestBody:
      contentType: "application/json"
      schema:
        type: "object"
        required: ["email", "password"]
        properties:
          email: { type: "string", format: "email" }
          password: { type: "string", format: "password", minLength: 8, maxLength: 128 }
      example:
        email: "jdoe@example.com"
        password: "Str0ng#Pass"
    responses:
      - status: 201
        description: "Created — ApiResponse with TokenResponse"
        schema:
          type: "object"
        example:
          success: true
          data:
            accessToken: "eyJhbGciOiJSUzI1NiIs..."
            refreshToken: "opaque-refresh-token"
            tokenType: "Bearer"
            expiresInSeconds: 900
      - status: 400
        description: "Validation error"
        example:
          success: false
          errorCode: "VALIDATION_ERROR"
      - status: 429
        description: "Too many requests (rate limit)"
        example:
          success: false
          errorCode: "RATE_LIMITED"

  - id: "ep-auth-login"
    method: "POST"
    urlPath: "/api/v1/auth/login"
    summary: "Authenticate and receive tokens"
    description: "Validates username/password, returns ApiResponse<TokenResponse>."
    tags: ["auth"]
    authenticated: false
    rateLimit: "STRICT profile when rate limiting enabled"
    requestBody:
      contentType: "application/json"
      schema:
        type: "object"
        required: ["email", "password"]
        properties:
          email: { type: "string", format: "email" }
          password: { type: "string" }
      example:
        email: "jdoe@example.com"
        password: "Str0ng#Pass"
    responses:
      - status: 200
        description: "Success"
        example:
          success: true
          data:
            accessToken: "eyJ..."
            refreshToken: "opaque-refresh-token"
            tokenType: "Bearer"
            expiresInSeconds: 900
      - status: 401
        description: "Invalid credentials"
        example:
          success: false
          errorCode: "UNAUTHORIZED"

  - id: "ep-auth-refresh"
    method: "POST"
    urlPath: "/api/v1/auth/refresh"
    summary: "Rotate refresh token and issue new access token"
    description: "Consumes refresh token from body; prior refresh invalidated according to IAM handler rules."
    tags: ["auth"]
    authenticated: false
    rateLimit: "STRICT profile when rate limiting enabled"
    requestBody:
      contentType: "application/json"
      schema:
        type: "object"
        required: ["refreshToken"]
        properties:
          refreshToken: { type: "string" }
      example:
        refreshToken: "eyJhbGciOiJSUzI1NiIs..."
    responses:
      - status: 200
        description: "New token pair"
        example:
          success: true
          data:
            accessToken: "eyJ..."
            refreshToken: "opaque-refresh-token"
            tokenType: "Bearer"
            expiresInSeconds: 900
      - status: 401
        description: "Invalid or expired refresh"
        example:
          success: false
          errorCode: "UNAUTHORIZED"

  - id: "ep-auth-logout"
    method: "POST"
    urlPath: "/api/v1/auth/logout"
    summary: "Logout and revoke refresh / block access token"
    description: "Requires `Authorization: Bearer <access>`. Returns 204 No Content on success; 401 if header missing or malformed."
    tags: ["auth"]
    authenticated: true
    rateLimit: "STANDARD profile (48 burst @ 0.8/s default) when enabled"
    responses:
      - status: 204
        description: "Logged out"
        example: null
      - status: 401
        description: "Missing Bearer token"
        example: null

  - id: "ep-auth-me"
    method: "GET"
    urlPath: "/api/v1/auth/me"
    summary: "Return current user id and username"
    description: "Uses JwtAuthenticationFilter-populated IamUserPrincipal."
    tags: ["auth"]
    authenticated: true
    rateLimit: "STANDARD profile when enabled"
    responses:
      - status: 200
        description: "ApiResponse<MeResponse>"
        example:
          success: true
          data:
            userId: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
            email: "jdoe@example.com"
      - status: 401
        description: "Missing or invalid JWT"
        example:
          success: false
          errorCode: "UNAUTHORIZED"

  - id: "ep-accounts-open"
    method: "POST"
    urlPath: "/api/v1/accounts"
    summary: "Open a new account for the authenticated customer"
    description: "Requires authority `accounts:write`. Body carries `type` (CHECKING|SAVINGS|...) and ISO currency."
    tags: ["accounts"]
    authenticated: true
    rateLimit: "Global per-IP bucket (64 burst @ 1/s) + nginx 30 r/s zone in Compose"
    requestBody:
      contentType: "application/json"
      schema:
        type: "object"
        required: ["type", "currency"]
        properties:
          type: { type: "string", enum: ["CHECKING", "SAVINGS", "INTERNAL", "LOAN"] }
          currency: { type: "string", minLength: 3, maxLength: 3 }
      example:
        type: "CHECKING"
        currency: "USD"
    responses:
      - status: 200
        description: "ApiResponse<OpenAccountResponse>"
        example:
          success: true
          data:
            accountId: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
            type: "CHECKING"
            currency: "USD"
      - status: 403
        description: "Missing accounts:write"
        example:
          success: false
          errorCode: "FORBIDDEN"

  - id: "ep-accounts-balance"
    method: "GET"
    urlPath: "/api/v1/accounts/{accountId}/balance"
    summary: "Get derived balance for an owned account"
    description: "Requires `accounts:read`; account must belong to caller."
    tags: ["accounts"]
    authenticated: true
    rateLimit: "Global + nginx limits"
    parameters:
      - name: "accountId"
        in: "path"
        type: "string"
        format: "uuid"
        required: true
        description: "Account identifier"
        example: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    responses:
      - status: 200
        description: "ApiResponse<BalanceResponse>"
        example:
          success: true
          data:
            accountId: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
            currency: "USD"
            amount: "1250.00"
      - status: 404
        description: "Account not found or not owned"
        example:
          success: false
          errorCode: "NOT_FOUND"

  - id: "ep-accounts-ledger"
    method: "GET"
    urlPath: "/api/v1/accounts/{accountId}/ledger"
    summary: "Paginated ledger entries"
    description: "Spring `page` (0-based), `size` (default 20), optional `sort` query params apply."
    tags: ["accounts"]
    authenticated: true
    rateLimit: "Global + nginx limits"
    parameters:
      - name: "accountId"
        in: "path"
        type: "string"
        format: "uuid"
        required: true
        description: "Account identifier"
        example: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
      - name: "page"
        in: "query"
        type: "integer"
        required: false
        description: "Page index"
        example: 0
      - name: "size"
        in: "query"
        type: "integer"
        required: false
        description: "Page size"
        example: 20
    responses:
      - status: 200
        description: "ApiResponse<LedgerPageResponse>"
        example:
          success: true
          data:
            content: []
            page: 0
            size: 20
            totalElements: 0

  - id: "ep-payments-transfer"
    method: "POST"
    urlPath: "/api/v1/payments/transfers"
    summary: "Transfer funds between accounts"
    description: "Requires `payments:write`, JSON body, and mandatory `Idempotency-Key` header (UUID). Returns 422 with ApiResponse failure on business rule violations."
    tags: ["payments"]
    authenticated: true
    rateLimit: "STRICT per-user on controller + Redis idempotency"
    parameters:
      - name: "Idempotency-Key"
        in: "header"
        type: "string"
        format: "uuid"
        required: true
        description: "Client-generated idempotency token"
        example: "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
    requestBody:
      contentType: "application/json"
      schema:
        type: "object"
        required: ["sourceAccountId", "targetAccountId", "amount", "currency"]
        properties:
          sourceAccountId: { type: "string", format: "uuid" }
          targetAccountId: { type: "string", format: "uuid" }
          amount: { type: "number" }
          currency: { type: "string" }
      example:
        sourceAccountId: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
        targetAccountId: "c9b1311d-9b3c-4d7e-8a1f-2b3c4d5e6f70"
        amount: 100.5
        currency: "USD"
    responses:
      - status: 200
        description: "Transfer completed"
        example:
          success: true
          data:
            transferId: "11111111-2222-3333-4444-555555555555"
            status: "COMPLETED"
      - status: 422
        description: "Domain validation failure"
        example:
          success: false
          errorCode: "INSUFFICIENT_FUNDS"
          errorMessage: "Not enough balance"

  - id: "ep-payments-reverse"
    method: "POST"
    urlPath: "/api/v1/payments/transfers/{transferId}/reverse"
    summary: "Reverse a prior transfer"
    description: "Requires `payments:write` and `Idempotency-Key` header."
    tags: ["payments"]
    authenticated: true
    rateLimit: "STRICT per-user"
    parameters:
      - name: "transferId"
        in: "path"
        type: "string"
        format: "uuid"
        required: true
        description: "Original transfer id"
        example: "11111111-2222-3333-4444-555555555555"
      - name: "Idempotency-Key"
        in: "header"
        type: "string"
        format: "uuid"
        required: true
        description: "Idempotency token for reversal"
        example: "7ba7b810-9dad-11d1-80b4-00c04fd430c8"
    responses:
      - status: 200
        description: "Reversal completed"
        example:
          success: true
          data:
            transferId: "22222222-3333-4444-5555-666666666666"
            status: "REVERSED"
      - status: 422
        description: "Cannot reverse"
        example:
          success: false
          errorCode: "REVERSAL_NOT_ALLOWED"

  - id: "ep-loans-originate"
    method: "POST"
    urlPath: "/api/v1/loans"
    summary: "Originate a loan"
    description: "Requires `loans:write`. Uses SENSITIVE_OPERATIONS rate profile when limiting is enabled."
    tags: ["loans"]
    authenticated: true
    rateLimit: "SENSITIVE_OPERATIONS (6 burst @ 0.1/s defaults)"
    requestBody:
      contentType: "application/json"
      schema:
        type: "object"
        required: ["checkingAccountId", "principal", "currency", "monthlyInterestRate", "termMonths"]
        properties:
          checkingAccountId: { type: "string", format: "uuid" }
          principal: { type: "number" }
          currency: { type: "string" }
          monthlyInterestRate: { type: "number" }
          termMonths: { type: "integer" }
      example:
        checkingAccountId: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
        principal: 10000
        currency: "USD"
        monthlyInterestRate: 0.005
        termMonths: 12
    responses:
      - status: 200
        description: "ApiResponse<LoanDetailResponse>"
        example:
          success: true
          data:
            loanId: "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
            status: "PENDING_APPROVAL"

  - id: "ep-loans-approve"
    method: "POST"
    urlPath: "/api/v1/loans/{loanId}/approve"
    summary: "Approve a pending loan"
    description: "Requires `loans:write`."
    tags: ["loans"]
    authenticated: true
    rateLimit: "SENSITIVE_OPERATIONS"
    parameters:
      - name: "loanId"
        in: "path"
        type: "string"
        format: "uuid"
        required: true
        description: "Loan identifier"
        example: "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
    responses:
      - status: 200
        description: "Loan approved"
        example:
          success: true
          data:
            loanId: "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
            status: "ACTIVE"

  - id: "ep-loans-get"
    method: "GET"
    urlPath: "/api/v1/loans/{loanId}"
    summary: "Get loan details"
    description: "Requires `loans:read`."
    tags: ["loans"]
    authenticated: true
    rateLimit: "STANDARD when enabled"
    parameters:
      - name: "loanId"
        in: "path"
        type: "string"
        format: "uuid"
        required: true
        description: "Loan identifier"
        example: "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
    responses:
      - status: 200
        description: "ApiResponse<LoanDetailResponse>"
        example:
          success: true
          data:
            loanId: "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
            status: "ACTIVE"

  - id: "ep-loans-pay"
    method: "POST"
    urlPath: "/api/v1/loans/{loanId}/repayments/{repaymentId}/pay"
    summary: "Pay a single scheduled repayment"
    description: "Requires `loans:write`. Returns 422 with failure envelope when business rules fail."
    tags: ["loans"]
    authenticated: true
    rateLimit: "SENSITIVE_OPERATIONS"
    parameters:
      - name: "loanId"
        in: "path"
        type: "string"
        format: "uuid"
        required: true
        description: "Loan identifier"
        example: "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
      - name: "repaymentId"
        in: "path"
        type: "string"
        format: "uuid"
        required: true
        description: "Repayment installment id"
        example: "bbbbbbbb-cccc-dddd-eeee-ffffffffffff"
    responses:
      - status: 200
        description: "Repayment applied"
        example:
          success: true
          data:
            repaymentId: "bbbbbbbb-cccc-dddd-eeee-ffffffffffff"
            status: "PAID"
      - status: 422
        description: "Business rule failure"
        example:
          success: false
          errorCode: "REPAYMENT_NOT_DUE"

  - id: "ep-audit-records"
    method: "GET"
    urlPath: "/api/v1/audit/records"
    summary: "Search audit records"
    description: "Requires `audit:read`. Supports optional filters and pagination."
    tags: ["audit"]
    authenticated: true
    rateLimit: "Global + nginx"
    parameters:
      - name: "eventType"
        in: "query"
        type: "string"
        required: false
        description: "Filter by domain event type string"
        example: "TransferCompleted"
      - name: "actorId"
        in: "query"
        type: "string"
        format: "uuid"
        required: false
        description: "Actor user id"
        example: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
      - name: "entityType"
        in: "query"
        type: "string"
        required: false
        description: "Aggregate or entity type"
        example: "Account"
      - name: "entityId"
        in: "query"
        type: "string"
        format: "uuid"
        required: false
        description: "Entity identifier"
        example: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
      - name: "from"
        in: "query"
        type: "string"
        format: "date-time"
        required: false
        description: "Inclusive lower bound (Instant)"
        example: "2026-01-01T00:00:00Z"
      - name: "to"
        in: "query"
        type: "string"
        format: "date-time"
        required: false
        description: "Inclusive upper bound (Instant)"
        example: "2026-12-31T23:59:59Z"
      - name: "page"
        in: "query"
        type: "integer"
        required: false
        description: "Page index"
        example: 0
      - name: "size"
        in: "query"
        type: "integer"
        required: false
        description: "Page size"
        example: 20
    responses:
      - status: 200
        description: "ApiResponse<AuditRecordsPageResponse>"
        example:
          success: true
          data:
            content: []
            page: 0
            size: 20
            totalElements: 0

  - id: "ep-notifications-records"
    method: "GET"
    urlPath: "/api/v1/notifications/monitoring/records"
    summary: "List notification delivery records"
    description: "Requires `notifications:read`. Optional filters by status and channel."
    tags: ["notifications"]
    authenticated: true
    rateLimit: "Global + nginx"
    parameters:
      - name: "status"
        in: "query"
        type: "string"
        required: false
        description: "NotificationStatus enum name"
        example: "SENT"
      - name: "channel"
        in: "query"
        type: "string"
        required: false
        description: "NotificationChannel enum name"
        example: "EMAIL"
      - name: "page"
        in: "query"
        type: "integer"
        required: false
        description: "Page index"
        example: 0
      - name: "size"
        in: "query"
        type: "integer"
        required: false
        description: "Page size (default 20)"
        example: 20
      - name: "sort"
        in: "query"
        type: "string"
        required: false
        description: "Spring Data sort, default createdAt,DESC"
        example: "createdAt,desc"
    responses:
      - status: 200
        description: "ApiResponse<NotificationRecordsPageResponse>"
        example:
          success: true
          data:
            content: []
            page: 0
            size: 20
            totalElements: 0

  - id: "ep-notifications-summary"
    method: "GET"
    urlPath: "/api/v1/notifications/monitoring/summary"
    summary: "Notification pipeline summary counters"
    description: "Requires `notifications:read`."
    tags: ["notifications"]
    authenticated: true
    rateLimit: "Global + nginx"
    responses:
      - status: 200
        description: "ApiResponse<NotificationSummaryResponse>"
        example:
          success: true
          data:
            total: 0
            pending: 0
            failed: 0

  - id: "ep-actuator-health"
    method: "GET"
    urlPath: "/actuator/health"
    summary: "Liveness/readiness probe"
    description: "Permitted without authentication on dedicated actuator security filter chain — restrict at network layer on AWS (ALB health check, private SG)."
    tags: ["operations"]
    authenticated: false
    rateLimit: "Not subject to annotated limits; ALB hits frequently"
    responses:
      - status: 200
        description: "UP (component details depend on Spring Boot config)"
        example:
          status: "UP"
      - status: 503
        description: "DOWN (database unreachable, etc.)"
        example:
          status: "DOWN"
---

# API schema

## Notes

- **Danger**: `GET /actuator/health` is intentionally **anonymous**; never expose raw actuator endpoints to the public internet without **network-level** protection (private subnet + SG, or IP-restricted target group).
- **Danger**: `POST /api/v1/auth/logout` returns **204** with **no JSON body** — clients must not assume `ApiResponse` envelope on success.
- **Good**: Payments endpoints require **`Idempotency-Key`** as a hard header — document this prominently in consumer SDKs and API Gateway mapping templates.
- **Missing**: This YAML does not yet mirror generated **springdoc** `components.schemas` one-to-one; treat `schema` blobs as summaries until you wire codegen from `v3/api-docs`.
- **Observation**: On AWS, **API Gateway** can enforce usage plans and API keys in front of ALB for partner traffic; this schema describes the Spring app directly behind ALB today.
- **Observation**: All successful JSON payloads shown assume the shared **`ApiResponse<T>`** wrapper used in controllers (some endpoints return `ResponseEntity` without body for 204).
