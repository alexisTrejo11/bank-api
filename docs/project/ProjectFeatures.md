# Project Features

## Feature List (`ProjectFeature[]`)

### Feature 1: Identity and Access Management (IAM)

- **ID**: "feature-iam-001"
- **Title**: JWT Authentication with RS256
- **Description**: Complete authentication system with RS256 JWT tokens, refresh tokens stored in Redis, and role-based access control (RBAC).
- **Icon**: "🔐"
- **Category** (`FeatureCategory`): `authentication`
- **Status** (`FeatureStatus`): `stable`
- **Highlights**:
  - RS256 asymmetric key JWT signing
  - Refresh token rotation with Redis storage
  - JWT blocklist for forced logout
  - Role-based permissions (roles → permissions → endpoints)
- **Tech Stack**:
  - Spring Security 6
  - jjwt library
  - Redis for token storage
- **Code Snippet** (`CodeSnippet`):
  - **Language**: "java"
  - **Filename**: "JwtTokenService.java"
  - **Code**:
    ```java
    @Component
    public class JwtTokenService {
        public String generateAccessToken(User user) {
            return Jwts.builder()
                .subject(user.getId().toString())
                .claim("roles", user.getRoles())
                .claim("permissions", user.getPermissions())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, RS256)
                .compact();
        }
    }
    ```
- **GitHub Example URL**: "https://github.com/alexistrejo11/bank-api/blob/main/bank-iam/src/main/java/io/github/alexistrejo11/bank/iam/infrastructure/security/JwtTokenService.java"

---

### Feature 2: Double-Entry Bookkeeping

- **ID**: "feature-accounts-001"
- **Title**: Account Balances via Ledger
- **Description**: Every balance mutation produces two LedgerEntry rows (debit + credit). Balance is always derived from the ledger, never stored as mutable column.
- **Icon**: "📒"
- **Category** (`FeatureCategory`): `database`
- **Status** (`FeatureStatus`): `stable`
- **Highlights**:
  - Immutable ledger entries (insert-only)
  - Balance derived from SUM(ledger entries)
  - Support for multiple account types (checking, savings, loan)
- **Tech Stack**:
  - JPA/Hibernate
  - PostgreSQL
  - Flyway migrations
- **Code Snippet** (`CodeSnippet`):
  - **Language**: "java"
  - **Filename**: "LedgerEntry.java"
  - **Code**:
    ```java
    public record LedgerEntry(
        AccountId accountId,
        LedgerEntryType type, // DEBIT or CREDIT
        Money amount,
        Currency currency,
        Reference reference
    ) {}
    ```

---

### Feature 3: Idempotent Payments

- **ID**: "feature-payments-001"
- **Title**: Transfer Idempotency with Redis
- **Description**: Each transfer is idempotent by idempotencyKey (UUID stored in Redis with TTL). Prevents duplicate transfers on network retries.
- **Icon**: "💸"
- **Category** (`FeatureCategory`): `api`
- **Status** (`FeatureStatus`): `stable`
- **Highlights**:
  - Idempotency key via header (Idempotency-Key)
  - Redis-backed outcome cache
  - State machine: PENDING → PROCESSING → COMPLETED | FAILED | REVERSED
- **Tech Stack**:
  - Spring Data Redis
  - State machine pattern

---

### Feature 4: Loan Origination and Amortization

- **ID**: "feature-loans-001"
- **Title**: Loan Management with Schedule
- **Description**: Complete loan lifecycle from origination to repayment with automatic amortization schedule generation.
- **Icon**: "🏦"
- **Category** (`FeatureCategory`): `api`
- **Status** (`FeatureStatus`): `stable`
- **Highlights**:
  - Loan application and approval workflow
  - Automatic amortization schedule generation
  - Repayment tracking with status (PENDING, PAID, OVERDUE)
  - Integration with accounts for disbursement and repayment ledger entries
- **Tech Stack**:
  - Domain-driven design
  - Event-driven (LoanDisbursedEvent, LoanRepaymentCompletedEvent)

---

### Feature 5: Event-Driven Architecture

- **ID**: "feature-events-001"
- **Title**: Cross-Module Communication via Events
- **Description**: Modules communicate only via ApplicationEvents (Spring's event publishing). AFTER_COMMIT phase ensures side-effects only fire on successful commit.
- **Icon**: "📡"
- **Category** (`FeatureCategory`): `messaging`
- **Status** (`FeatureStatus`): `stable`
- **Highlights**:
  - TransferCompletedEvent triggers: ledger posting, audit record, notification
  - LoanDisbursedEvent triggers: ledger entry, audit record
  - LoanRepaymentCompletedEvent triggers: ledger entry, audit record
  - Async + TransactionalEventListener for decoupled processing
- **Tech Stack**:
  - Spring ApplicationEventPublisher
  - @Async + @TransactionalEventListener

---

### Feature 6: Audit Trail

- **ID**: "feature-audit-001"
- **Title**: Immutable Audit Records
- **Description**: Every domain event is consumed by AuditEventListener and persisted as immutable AuditRecord. No update/delete allowed on this table.
- **Icon**: "📋"
- **Category** (`FeatureCategory`): `monitoring`
- **Status** (`FeatureStatus`): `stable`
- **Highlights**:
  - Append-only audit_records table
  - DB trigger for enforce append-only
  - JSONB payload for event data
  - Queryable by actorId, eventType, entityId
- **Tech Stack**:
  - PostgreSQL JSONB
  - Flyway trigger migration

---

### Feature 7: Notifications

- **ID**: "feature-notifications-001"
- **Title**: Async Email and SMS Dispatch
- **Description**: Asynchronous notification dispatch with template rendering (Thymeleaf) for emails and stub for SMS.
- **Icon**: "📧"
- **Category** (`FeatureCategory`): `messaging`
- **Status** (`FeatureStatus`): `stable`
- **Highlights**:
  - Thymeleaf HTML email templates
  - Twilio SMS stub (configurable)
  - Kafka-ready dispatch mode (v0.2.0+)
  - Notification log tracking (PENDING, SENT, FAILED)
- **Tech Stack**:
  - Thymeleaf
  - Spring Kafka (optional)

---

### Feature 8: Rate Limiting

- **ID**: "feature-rate-limit-001"
- **Title**: Per-User and Global Rate Limiting
- **Description**: Redis-backed token bucket rate limiting with global IP limit and per-user profiles (standard, strict, sensitive_operations).
- **Icon**: "🚦"
- **Category** (`FeatureCategory`): `security`
- **Status** (`FeatureStatus`): `beta`
- **Highlights**:
  - Global per-IP bucket (64 requests/sec default)
  - Profile-based limits (standard: 48/min, strict: 12/min, sensitive: 6/min)
  - 429 responses with Retry-After header
  - @RateLimit annotation for controller-level control
- **Tech Stack**:
  - Spring Data Redis
  - Bucket4j-style token bucket algorithm

---

### Feature 9: OpenAPI Documentation

- **ID**: "feature-swagger-001"
- **Title**: Swagger/OpenAPI UI
- **Description**: Auto-generated API documentation via SpringDoc OpenAPI with Swagger UI.
- **Icon**: "📖"
- **Category** (`FeatureCategory`): `api`
- **Status** (`FeatureStatus`): `beta`
- **Highlights**:
  - Available at /swagger-ui.html
  - OpenAPI schema at /api-docs
  - JWT authentication integration
- **Tech Stack**:
  - springdoc-openapi-starter-webmvc-ui

---

### Feature 10: Observability

- **ID**: "feature-observability-001"
- **Title**: Metrics and Health Endpoints
- **Description**: Spring Boot Actuator with Prometheus metrics, health probes, and JSON logging ready for ELK stack.
- **Icon**: "📊"
- **Category** (`FeatureCategory`): `monitoring`
- **Status** (`FeatureStatus`): `beta`
- **Highlights**:
  - /actuator/prometheus endpoint
  - /actuator/health with liveness/readiness probes
  - JSON log format via logstash-logback-encoder
  - Spring Modulith for module visualization
- **Tech Stack**:
  - Micrometer + Prometheus
  - Spring Boot Actuator
  - Logstash encoder

---

### Feature 11: Externalized Configuration

- **ID**: "feature-config-001"
- **Title**: Environment-Based Configuration
- **Description**: All configuration via environment variables with .env file support. No secrets in Git.
- **Icon**: "⚙️"
- **Category** (`FeatureCategory`): `api`
- **Status** (`FeatureStatus`): `stable`
- **Highlights**:
  - .env.example for local development
  - DotEnvEnvironmentPostProcessor for .env loading
  - Profile-specific configs (default, test, postgres, docker)
- **Tech Stack**:
  - Spring Boot
  - dotenv-java library
