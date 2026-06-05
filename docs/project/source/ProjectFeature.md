---
features:
  - id: "jwt-auth-rbac"
    title: "JWT authentication & RBAC"
    description: "RS256 JWT with roles (CUSTOMER, ADMIN, AUDITOR) and fine-grained permissions. Refresh tokens stored in Upstash Redis with rotation on refresh; jti blocklist on logout."
    icon: "shield-lock"
    category: "authentication"
    status: "stable"
    highlights:
      - "POST /api/v1/auth/register, login, refresh, logout, me"
      - "Access TTL 15 min, refresh TTL 7 days"
      - "JWKS endpoint at /.well-known/jwks.json"
      - "Redis-backed refresh store enabled in docker/AWS profile"
    techStack:
      - "Spring Security"
      - "jjwt 0.12.5"
      - "bank-iam"
      - "bank-config"
    metrics:
      - label: "Access token TTL"
        value: "15 min"
        trend: "stable"
        icon: "clock"
      - label: "Refresh token TTL"
        value: "7 days"
        trend: "stable"
        icon: "refresh"
    codeSnippet:
      language: "java"
      filename: "bank-config/.../RedisRefreshTokenStore.java"
      code: |
        @ConditionalOnProperty(name = "bank.iam.redis.enabled", havingValue = "true")
        public class RedisRefreshTokenStore implements RefreshTokenStore {
            public void store(String refreshToken, UserId userId, Duration ttl) {
                String key = "iam:refresh:" + Sha256.hex(refreshToken);
                redis.opsForValue().set(key, userId.value().toString(), ttl);
            }
        }

  - id: "double-entry-accounts"
    title: "Double-entry ledger accounts"
    description: "CHECKING, SAVINGS, and LOAN accounts. Balance always computed from ledger entries — never stored as a mutable field."
    icon: "wallet"
    category: "api"
    status: "stable"
    highlights:
      - "Open account, balance, paginated ledger"
      - "Ledger entries on transfers, loan disbursement, repayments"
      - "FROZEN/CLOSED accounts reject mutations"
    techStack:
      - "bank-accounts"
      - "Spring Data JPA"
      - "Flyway"
    metrics:
      - label: "Account types"
        value: "3"
        trend: "stable"
        icon: "layers"

  - id: "payments-transfers"
    title: "Transfers with idempotency"
    description: "PENDING → PROCESSING → COMPLETED state machine. Redis idempotency cache (24h) plus DB dedup. Publishes TransferCompletedEvent for ledger and notifications."
    icon: "arrow-left-right"
    category: "api"
    status: "stable"
    highlights:
      - "Idempotency-Key header required"
      - "Same-currency transfers only (no FX v1)"
      - "Reversal for COMPLETED transfers"
      - "Result<T> pattern for domain errors → HTTP 422"
    techStack:
      - "bank-payments"
      - "Upstash Redis"
    metrics:
      - label: "Idempotency TTL"
        value: "24h"
        trend: "stable"
        icon: "key"
    codeSnippet:
      language: "java"
      filename: "bank-payments/.../InitiateTransferHandler.java"
      code: |
        Optional<Result<TransferResponse>> cached = idempotencyCache.get(userId, idempotencyKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        if (sourceAccountId.equals(targetAccountId)) {
            return persistFailure(command, currencyRaw, "SELF_TRANSFER", "...");
        }

  - id: "loans-amortization"
    title: "Loan origination & amortization"
    description: "Fixed monthly payment schedule at origination. Approval creates LOAN account and disburses to checking. Repayments debit checking via domain events."
    icon: "percent"
    category: "api"
    status: "stable"
    highlights:
      - "Originate → approve → pay installments workflow"
      - "Statuses: PENDING_APPROVAL, ACTIVE, PAID_OFF, DEFAULTED"
      - "BigDecimal interest — never float/double"
    techStack:
      - "bank-loans"
      - "bank-accounts (event listeners)"
    metrics:
      - label: "Repayment statuses"
        value: "3"
        trend: "stable"
        icon: "calendar"

  - id: "append-only-audit"
    title: "Append-only audit trail"
    description: "Every BankDomainEvent captured as immutable AuditRecord (JSONB payload). DB trigger prevents UPDATE/DELETE."
    icon: "clipboard-list"
    category: "security"
    status: "stable"
    highlights:
      - "AuditEventListener subscribes to all domain events"
      - "UUID v7 time-ordered IDs for range queries"
      - "GET /api/v1/audit/records with filters"
      - "Dedicated audit.json log file (ACCESS/AUDIT loggers)"
    techStack:
      - "bank-audit"
      - "PostgreSQL JSONB"
    metrics:
      - label: "Mutability"
        value: "append-only"
        trend: "stable"
        icon: "lock"

  - id: "kafka-notifications"
    title: "Async notifications (direct + Kafka)"
    description: "Fire-and-forget email/SMS on transfer and loan events. In AWS/docker profile, dispatch-mode=kafka enqueues to cloud Kafka; consumer in bank-notifications processes pipeline."
    icon: "bell"
    category: "messaging"
    status: "stable"
    highlights:
      - "Templates: transfer.completed, loan.approved, loan.paid_off, etc."
      - "KafkaNotificationDispatchIngress when dispatch-mode=kafka"
      - "Monitoring API: /api/v1/notifications/monitoring/*"
      - "Dev stubs log to console; production uses SMTP/SMS config"
    techStack:
      - "bank-notifications"
      - "Spring Kafka"
      - "Cloud Kafka broker"
    codeSnippet:
      language: "java"
      filename: "bank-notifications/.../KafkaNotificationDispatchIngress.java"
      code: |
        @ConditionalOnProperty(name = "bank.notifications.dispatch-mode", havingValue = "kafka")
        public void submit(DispatchNotificationCommand command) {
            String json = objectMapper.writeValueAsString(NotificationDispatchMessage.from(command));
            kafkaTemplate.send(dispatchTopic, json);
        }

  - id: "redis-rate-limiting"
    title: "Redis token-bucket rate limiting"
    description: "Global filter + @RateLimit annotation on controllers. Lua script for atomic refill/consume. Fail-open on Redis errors when configured."
    icon: "speedometer"
    category: "performance"
    status: "stable"
    highlights:
      - "Profiles: standard, strict, sensitive_operations"
      - "Enabled in docker/AWS profile (bank.rate-limiting.enabled=true)"
      - "Scopes: PER_IP, PER_USER"
    techStack:
      - "bank-boot/infrastructure/ratelimit"
      - "Upstash Redis"
      - "Lua token-bucket script"
    metrics:
      - label: "Strict profile"
        value: "12 tokens"
        trend: "stable"
        icon: "gauge"

  - id: "openapi-docs"
    title: "OpenAPI & standardized responses"
    description: "springdoc-openapi with @BankApiOperation annotations. ApiResponse<T> envelope with data, meta.timestamp, and errors[]."
    icon: "book"
    category: "api"
    status: "stable"
    highlights:
      - "/swagger-ui.html and /api-docs"
      - "GlobalExceptionHandler maps domain exceptions"
      - "RequestAccessAuditFilter for HTTP access logs"
    techStack:
      - "springdoc-openapi"
      - "bank-shared/shared_kernel/api"
    metrics:
      - label: "Documented version"
        value: "0.3.0"
        trend: "stable"
        icon: "tag"

  - id: "docker-aws-deploy"
    title: "Docker + AWS production deploy"
    description: "Multi-stage Temurin 21 JRE image (~300MB). compose.yml runs app only on EC2; RDS, Upstash Redis, and cloud Kafka via .env. Observability on EC2."
    icon: "docker"
    category: "integration"
    status: "stable"
    highlights:
      - "docker/compose.yml — single app container on EC2"
      - "docker/compose.local.yml — full local stack"
      - "Flyway migrations on startup (ddl-auto=validate)"
      - "Actuator health + prometheus endpoints"
    techStack:
      - "Docker"
      - "Amazon EC2"
      - "Amazon RDS"
      - "Upstash Redis"
---

# Project Features

> **Stable:** Core banking flows (auth, accounts, payments, loans, audit) are implemented and covered by integration tests in bank-boot.

> **AWS profile:** Set `SPRING_PROFILES_ACTIVE=docker`, point `SPRING_DATASOURCE_*` to RDS, `SPRING_DATA_REDIS_*` to Upstash, `SPRING_KAFKA_BOOTSTRAP_SERVERS` to your cloud Kafka host, and `BANK_KAFKA_ENABLED=true`.

> **Warning:** Email/SMS are stubbed in dev — configure real SMTP/Twilio env vars before expecting live notifications in production.

> **Useful:** Run `./docker/validate-env.sh app` before deploy to ensure all required `.env` variables are set.
