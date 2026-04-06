# API Schema

- **Type**: `REST` (with OpenAPI/Swagger documentation)

---

## HTTP Endpoints (`ApiEndpoint[]`)

### Endpoint 1: Register User

- **ID**: "api-iam-001"
- **Method**: `POST`
- **URL Path**: `/api/v1/auth/register`
- **Summary**: "Register a new user"
- **Description**: "Creates a new user with default role. Returns user details with generated ID."
- **Tags**: `["iam", "auth"]`
- **Authenticated**: `false`
- **Rate Limit**: "10 req/min (IP)"

#### Request Body (`ApiRequestBody`)

- **Content Type**: "application/json"
- **Schema**: 
  ```json
  {
    "email": "string",
    "password": "string",
    "firstName": "string",
    "lastName": "string"
  }
  ```
- **Example**:
  ```json
  {
    "email": "user@example.com",
    "password": "securePassword123",
    "firstName": "John",
    "lastName": "Doe"
  }
  ```

#### Responses (`ApiResponse[]`)

- **Status**: 201
- **Description**: "User created successfully"
- **Schema**: 
  ```json
  {
    "data": { "userId": "uuid", "email": "string" }
  }
  ```

- **Status**: 400
- **Description**: "Validation error"
- **Schema**: "RFC 7807 ProblemDetail"

---

### Endpoint 2: Login

- **ID**: "api-iam-002"
- **Method**: `POST`
- **URL Path**: `/api/v1/auth/login`
- **Summary**: "Authenticate user"
- **Description**: "Authenticates user with email/password, returns JWT access token and refresh token."
- **Tags**: `["iam", "auth"]`
- **Authenticated**: `false`
- **Rate Limit**: "5 req/min (IP)"

#### Request Body (`ApiRequestBody`)

- **Content Type**: "application/json"
- **Schema**:
  ```json
  {
    "email": "string",
    "password": "string"
  }
  ```
- **Example**:
  ```json
  {
    "email": "user@example.com",
    "password": "securePassword123"
  }
  ```

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "Authentication successful"
- **Schema**:
  ```json
  {
    "data": {
      "accessToken": "string",
      "refreshToken": "string",
      "expiresIn": 900
    }
  }
  ```

- **Status**: 401
- **Description**: "Invalid credentials"

---

### Endpoint 3: Refresh Token

- **ID**: "api-iam-003"
- **Method**: `POST`
- **URL Path**: `/api/v1/auth/refresh`
- **Summary**: "Refresh access token"
- **Description**: "Exchanges refresh token for new access token."
- **Tags**: `["iam", "auth"]`
- **Authenticated**: `false`
- **Rate Limit**: "10 req/min (IP)"

#### Request Body (`ApiRequestBody`)

- **Content Type**: "application/json"
- **Schema**:
  ```json
  {
    "refreshToken": "string"
  }
  ```

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "Token refreshed"
- **Schema**:
  ```json
  {
    "data": {
      "accessToken": "string",
      "expiresIn": 900
    }
  }
  ```

---

### Endpoint 4: Logout

- **ID**: "api-iam-004"
- **Method**: `POST`
- **URL Path**: `/api/v1/auth/logout`
- **Summary**: "Logout user"
- **Description**: "Invalidates current JWT by adding to blocklist."
- **Tags**: `["iam", "auth"]`
- **Authenticated**: `true`
- **Rate Limit**: "10 req/min"

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "Logged out successfully"

---

### Endpoint 5: Create Account

- **ID**: "api-accounts-001"
- **Method**: `POST`
- **URL Path**: `/api/v1/accounts`
- **Summary**: "Open a new account"
- **Description**: "Creates a new account for the authenticated user."
- **Tags**: `["accounts"]`
- **Authenticated**: `true`
- **Rate Limit**: "5 req/min"

#### Request Body (`ApiRequestBody`)

- **Content Type**: "application/json"
- **Schema**:
  ```json
  {
    "type": "CHECKING | SAVINGS",
    "currency": "USD"
  }
  ```

#### Responses (`ApiResponse[]`)

- **Status**: 201
- **Description**: "Account created"
- **Schema**:
  ```json
  {
    "data": {
      "accountId": "uuid",
      "type": "CHECKING",
      "currency": "USD",
      "status": "ACTIVE"
    }
  }
  ```

---

### Endpoint 6: Get Account Balance

- **ID**: "api-accounts-002"
- **Method**: `GET`
- **URL Path**: `/api/v1/accounts/{id}/balance`
- **Summary**: "Get account balance"
- **Description**: "Returns derived balance from ledger entries."
- **Tags**: `["accounts"]`
- **Authenticated**: `true`
- **Rate Limit**: "30 req/min"

#### Parameters (`ApiParameter[]`)

- **Name**: "id"
- **In**: "path"
- **Type**: "uuid"
- **Required**: `true`
- **Description**: "Account ID"

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "Balance retrieved"
- **Schema**:
  ```json
  {
    "data": {
      "accountId": "uuid",
      "amount": "1000.00",
      "currency": "USD"
    }
  }
  ```

---

### Endpoint 7: Get Account Ledger

- **ID**: "api-accounts-003"
- **Method**: `GET`
- **URL Path**: `/api/v1/accounts/{id}/ledger`
- **Summary**: "Get account ledger entries"
- **Description**: "Returns paginated ledger entries for an account."
- **Tags**: `["accounts"]`
- **Authenticated**: `true`
- **Rate Limit**: "30 req/min"

#### Parameters (`ApiParameter[]`)

- **Name**: "id"
- **In**: "path"
- **Type**: "uuid"
- **Required**: `true`
- **Description**: "Account ID"

- **Name**: "page"
- **In**: "query"
- **Type**: "integer"
- **Required**: `false`
- **Description**: "Page number (default: 0)"

- **Name**: "size"
- **In**: "query"
- **Type**: "integer"
- **Required**: `false`
- **Description**: "Page size (default: 20)"

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "Ledger entries"
- **Schema**:
  ```json
  {
    "data": {
      "content": [
        {
          "id": "uuid",
          "type": "DEBIT | CREDIT",
          "amount": "100.00",
          "currency": "USD",
          "createdAt": "2026-01-01T00:00:00Z"
        }
      ],
      "totalElements": 100,
      "totalPages": 5
    }
  }
  ```

---

### Endpoint 8: Initiate Transfer

- **ID**: "api-payments-001"
- **Method**: `POST`
- **URL Path**: `/api/v1/transfers`
- **Summary**: "Transfer funds between accounts"
- **Description**: "Initiates an idempotent transfer. Requires Idempotency-Key header."
- **Tags**: `["payments"]`
- **Authenticated**: `true`
- **Rate Limit**: "Strict (12 req/min)"

#### Parameters (`ApiParameter[]`)

- **Name**: "Idempotency-Key"
- **In**: "header"
- **Type**: "uuid"
- **Required**: `true`
- **Description**: "Unique idempotency key"
- **Example**: "550e8400-e29b-41d4-a716-446655440000"

#### Request Body (`ApiRequestBody`)

- **Content Type**: "application/json"
- **Schema**:
  ```json
  {
    "sourceAccountId": "uuid",
    "targetAccountId": "uuid",
    "amount": "100.00",
    "currency": "USD"
  }
  ```

#### Responses (`ApiResponse[]`)

- **Status**: 201
- **Description**: "Transfer initiated"
- **Schema**:
  ```json
  {
    "data": {
      "transferId": "uuid",
      "status": "PENDING | PROCESSING | COMPLETED | FAILED"
    }
  }
  ```

- **Status**: 422
- **Description**: "Insufficient funds or idempotency key conflict"

---

### Endpoint 9: Get Transfer Status

- **ID**: "api-payments-002"
- **Method**: `GET`
- **URL Path**: `/api/v1/transfers/{id}`
- **Summary**: "Get transfer status"
- **Description**: "Returns current status of a transfer."
- **Tags**: `["payments"]`
- **Authenticated**: `true`
- **Rate Limit**: "30 req/min"

#### Parameters (`ApiParameter[]`)

- **Name**: "id"
- **In**: "path"
- **Type**: "uuid"
- **Required**: `true`
- **Description**: "Transfer ID"

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "Transfer status"
- **Schema**:
  ```json
  {
    "data": {
      "transferId": "uuid",
      "sourceAccountId": "uuid",
      "targetAccountId": "uuid",
      "amount": "100.00",
      "currency": "USD",
      "status": "COMPLETED",
      "createdAt": "2026-01-01T00:00:00Z"
    }
  }
  ```

---

### Endpoint 10: Apply for Loan

- **ID**: "api-loans-001"
- **Method**: `POST`
- **URL Path**: `/api/v1/loans/apply`
- **Summary**: "Apply for a loan"
- **Description**: "Creates a loan application with requested amount and term."
- **Tags**: `["loans"]`
- **Authenticated**: `true`
- **Rate Limit**: "Sensitive (6 req/min)"

#### Request Body (`ApiRequestBody`)

- **Content Type**: "application/json"
- **Schema**:
  ```json
  {
    "accountId": "uuid",
    "principal": "10000.00",
    "currency": "USD",
    "interestRate": "5.5",
    "termMonths": 36
  }
  ```

#### Responses (`ApiResponse[]`)

- **Status**: 201
- **Description**: "Loan application created"
- **Schema**:
  ```json
  {
    "data": {
      "loanId": "uuid",
      "status": "PENDING"
    }
  }
  ```

---

### Endpoint 11: Get Loan Schedule

- **ID**: "api-loans-002"
- **Method**: `GET`
- **URL Path**: `/api/v1/loans/{id}/schedule`
- **Summary**: "Get loan amortization schedule"
- **Description**: "Returns the amortization schedule with installment details."
- **Tags**: `["loans"]`
- **Authenticated**: `true`
- **Rate Limit**: "30 req/min"

#### Parameters (`ApiParameter[]`)

- **Name**: "id"
- **In**: "path"
- **Type**: "uuid"
- **Required**: `true`
- **Description**: "Loan ID"

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "Amortization schedule"
- **Schema**:
  ```json
  {
    "data": {
      "loanId": "uuid",
      "installments": [
        {
          "number": 1,
          "dueDate": "2026-02-01",
          "amount": "302.46",
          "principal": "252.46",
          "interest": "50.00",
          "status": "PENDING"
        }
      ]
    }
  }
  ```

---

### Endpoint 12: Repay Loan

- **ID**: "api-loans-003"
- **Method**: `POST`
- **URL Path**: `/api/v1/loans/{id}/repay`
- **Summary**: "Record a loan repayment"
- **Description**: "Records a payment towards a loan installment."
- **Tags**: `["loans"]`
- **Authenticated**: `true`
- **Rate Limit**: "Sensitive (6 req/min)"

#### Request Body (`ApiRequestBody`)

- **Content Type**: "application/json"
- **Schema**:
  ```json
  {
    "installmentNumber": 1,
    "amount": "302.46"
  }
  ```

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "Repayment recorded"
- **Schema**:
  ```json
  {
    "data": {
      "repaymentId": "uuid",
      "status": "COMPLETED",
      "paidAt": "2026-02-01T12:00:00Z"
    }
  }
  ```

---

### Endpoint 13: Get Audit Events

- **ID**: "api-audit-001"
- **Method**: `GET`
- **URL Path**: `/api/v1/audit/events`
- **Summary**: "Query audit events"
- **Description**: "Returns filtered audit records for compliance."
- **Tags**: `["audit"]`
- **Authenticated**: `true`
- **Rate Limit**: "30 req/min"

#### Parameters (`ApiParameter[]`)

- **Name**: "actorId"
- **In**: "query"
- **Type**: "uuid"
- **Required**: `false`
- **Description**: "Filter by actor"

- **Name**: "eventType"
- **In**: "query"
- **Type**: "string"
- **Required**: `false**
- **Description**: "Filter by event type"

- **Name**: "entityId"
- **In**: "query"
- **Type**: "uuid"
- **Required**: `false`
- **Description**: "Filter by entity"

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "Audit records"
- **Schema**:
  ```json
  {
    "data": {
      "content": [
        {
          "id": "uuid",
          "eventType": "TransferCompletedEvent",
          "actorId": "uuid",
          "entityType": "Transfer",
          "entityId": "uuid",
          "payload": {},
          "createdAt": "2026-01-01T00:00:00Z"
        }
      ]
    }
  }
  ```

---

### Endpoint 14: JWKS

- **ID**: "api-iam-005"
- **Method**: `GET`
- **URL Path**: `/.well-known/jwks.json`
- **Summary**: "Get JWT public keys"
- **Description**: "Returns JSON Web Key Set for JWT validation."
- **Tags**: `["iam"]`
- **Authenticated**: `false`
- **Rate Limit**: "N/A"

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "JWKS"
- **Schema**:
  ```json
  {
    "keys": [
      {
        "kty": "RSA",
        "use": "sig",
        "alg": "RS256",
        "n": "...",
        "e": "AQAB"
      }
    ]
  }
  ```

---

### Endpoint 15: Health Check

- **ID**: "api-health-001"
- **Method**: `GET`
- **URL Path**: `/actuator/health`
- **Summary**: "Application health"
- **Description**: "Returns health status of application and dependencies."
- **Tags**: `["actuator"]`
- **Authenticated**: `false`
- **Rate Limit**: "N/A"

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "Health status"
- **Schema**:
  ```json
  {
    "status": "UP",
    "components": {
      "db": { "status": "UP" },
      "redis": { "status": "UP" }
    }
  }
  ```

---

### Endpoint 16: Prometheus Metrics

- **ID**: "api-metrics-001"
- **Method**: `GET`
- **URL Path**: `/actuator/prometheus`
- **Summary**: "Prometheus metrics endpoint"
- **Description**: "Exposes metrics in Prometheus format."
- **Tags**: `["actuator"]`
- **Authenticated**: `false`
- **Rate Limit**: "N/A"

#### Responses (`ApiResponse[]`)

- **Status**: 200
- **Description**: "Prometheus metrics"
