# Project Features

## JWT authentication & RBAC

RS256 JWT with roles (CUSTOMER, ADMIN, AUDITOR) and fine-grained permissions. Refresh tokens stored in Upstash Redis with rotation on refresh; jti blocklist on logout.

| Property | Value |
| --- | --- |
| ID | jwt-auth-rbac |
| Category | authentication |
| Status | stable |
| Icon | shield-lock |

### Highlights

- POST /api/v1/auth/register, login, refresh, logout, me
- Access TTL 15 min, refresh TTL 7 days
- JWKS endpoint at /.well-known/jwks.json
- Redis-backed refresh store enabled in docker/AWS profile

### Tech stack

- Spring Security
- jjwt 0.12.5
- bank-iam
- bank-config

### Metrics

| Label | Value | Trend |
| --- | --- | --- |
| Access token TTL | 15 min | stable |
| Refresh token TTL | 7 days | stable |

### Code snippet

_bank-config/.../RedisRefreshTokenStore.java_

```java
@ConditionalOnProperty(name = "bank.iam.redis.enabled", havingValue = "true")
public class RedisRefreshTokenStore implements RefreshTokenStore {
    public void store(String refreshToken, UserId userId, Duration ttl) {
        String key = "iam:refresh:" + Sha256.hex(refreshToken);
        redis.opsForValue().set(key, userId.value().toString(), ttl);
    }
}
```

## Double-entry ledger accounts

CHECKING, SAVINGS, and LOAN accounts. Balance always computed from ledger entries — never stored as a mutable field.

| Property | Value |
| --- | --- |
| ID | double-entry-accounts |
| Category | api |
| Status | stable |
| Icon | wallet |

### Highlights

- Open account, balance, paginated ledger
- Ledger entries on transfers, loan disbursement, repayments
- FROZEN/CLOSED accounts reject mutations

### Tech stack

- bank-accounts
- Spring Data JPA
- Flyway

### Metrics

| Label | Value | Trend |
| --- | --- | --- |
| Account types | 3 | stable |

## Transfers with idempotency

PENDING → PROCESSING → COMPLETED state machine. Redis idempotency cache (24h) plus DB dedup. Publishes TransferCompletedEvent for ledger and notifications.

| Property | Value |
| --- | --- |
| ID | payments-transfers |
| Category | api |
| Status | stable |
| Icon | arrow-left-right |

### Highlights

- Idempotency-Key header required
- Same-currency transfers only (no FX v1)
- Reversal for COMPLETED transfers
- Result<T> pattern for domain errors → HTTP 422

### Tech stack

- bank-payments
- Upstash Redis

### Metrics

| Label | Value | Trend |
| --- | --- | --- |
| Idempotency TTL | 24h | stable |

### Code snippet

_bank-payments/.../InitiateTransferHandler.java_

```java
Optional<Result<TransferResponse>> cached = idempotencyCache.get(userId, idempotencyKey);
if (cached.isPresent()) {
    return cached.get();
}
if (sourceAccountId.equals(targetAccountId)) {
    return persistFailure(command, currencyRaw, "SELF_TRANSFER", "...");
}
```

## Loan origination & amortization

Fixed monthly payment schedule at origination. Approval creates LOAN account and disburses to checking. Repayments debit checking via domain events.

| Property | Value |
| --- | --- |
| ID | loans-amortization |
| Category | api |
| Status | stable |
| Icon | percent |

### Highlights

- Originate → approve → pay installments workflow
- Statuses: PENDING_APPROVAL, ACTIVE, PAID_OFF, DEFAULTED
- BigDecimal interest — never float/double

### Tech stack

- bank-loans
- bank-accounts (event listeners)

### Metrics

| Label | Value | Trend |
| --- | --- | --- |
| Repayment statuses | 3 | stable |

## Append-only audit trail

Every BankDomainEvent captured as immutable AuditRecord (JSONB payload). DB trigger prevents UPDATE/DELETE.

| Property | Value |
| --- | --- |
| ID | append-only-audit |
| Category | security |
| Status | stable |
| Icon | clipboard-list |

### Highlights

- AuditEventListener subscribes to all domain events
- UUID v7 time-ordered IDs for range queries
- GET /api/v1/audit/records with filters
- Dedicated audit.json log file (ACCESS/AUDIT loggers)

### Tech stack

- bank-audit
- PostgreSQL JSONB

### Metrics

| Label | Value | Trend |
| --- | --- | --- |
| Mutability | append-only | stable |

## Async notifications (direct + Kafka)

Fire-and-forget email/SMS on transfer and loan events. In AWS/docker profile, dispatch-mode=kafka enqueues to cloud Kafka; consumer in bank-notifications processes pipeline.

| Property | Value |
| --- | --- |
| ID | kafka-notifications |
| Category | messaging |
| Status | stable |
| Icon | bell |

### Highlights

- Templates: transfer.completed, loan.approved, loan.paid_off, etc.
- KafkaNotificationDispatchIngress when dispatch-mode=kafka
- Monitoring API: /api/v1/notifications/monitoring/*
- Dev stubs log to console; production uses SMTP/SMS config

### Tech stack

- bank-notifications
- Spring Kafka
- Cloud Kafka broker

### Code snippet

_bank-notifications/.../KafkaNotificationDispatchIngress.java_

```java
@ConditionalOnProperty(name = "bank.notifications.dispatch-mode", havingValue = "kafka")
public void submit(DispatchNotificationCommand command) {
    String json = objectMapper.writeValueAsString(NotificationDispatchMessage.from(command));
    kafkaTemplate.send(dispatchTopic, json);
}
```

## Redis token-bucket rate limiting

Global filter + @RateLimit annotation on controllers. Lua script for atomic refill/consume. Fail-open on Redis errors when configured.

| Property | Value |
| --- | --- |
| ID | redis-rate-limiting |
| Category | performance |
| Status | stable |
| Icon | speedometer |

### Highlights

- Profiles: standard, strict, sensitive_operations
- Enabled in docker/AWS profile (bank.rate-limiting.enabled=true)
- Scopes: PER_IP, PER_USER

### Tech stack

- bank-boot/infrastructure/ratelimit
- Upstash Redis
- Lua token-bucket script

### Metrics

| Label | Value | Trend |
| --- | --- | --- |
| Strict profile | 12 tokens | stable |

## OpenAPI & standardized responses

springdoc-openapi with @BankApiOperation annotations. ApiResponse<T> envelope with data, meta.timestamp, and errors[].

| Property | Value |
| --- | --- |
| ID | openapi-docs |
| Category | api |
| Status | stable |
| Icon | book |

### Highlights

- /swagger-ui.html and /api-docs
- GlobalExceptionHandler maps domain exceptions
- RequestAccessAuditFilter for HTTP access logs

### Tech stack

- springdoc-openapi
- bank-shared/shared_kernel/api

### Metrics

| Label | Value | Trend |
| --- | --- | --- |
| Documented version | 0.3.0 | stable |

## Docker + AWS production deploy

Multi-stage Temurin 21 JRE image (~300MB). compose.yml runs app only on EC2; RDS, Upstash Redis, and cloud Kafka via .env. Observability on EC2.

| Property | Value |
| --- | --- |
| ID | docker-aws-deploy |
| Category | integration |
| Status | stable |
| Icon | docker |

### Highlights

- docker/compose.yml — single app container on EC2
- docker/compose.local.yml — full local stack
- Flyway migrations on startup (ddl-auto=validate)
- Actuator health + prometheus endpoints

### Tech stack

- Docker
- Amazon EC2
- Amazon RDS
- Upstash Redis

## Additional notes

# Project Features

> **Stable:** Core banking flows (auth, accounts, payments, loans, audit) are implemented and covered by integration tests in bank-boot.

> **AWS profile:** Set `SPRING_PROFILES_ACTIVE=docker`, point `SPRING_DATASOURCE_*` to RDS, `SPRING_DATA_REDIS_*` to Upstash, `SPRING_KAFKA_BOOTSTRAP_SERVERS` to your cloud Kafka host, and `BANK_KAFKA_ENABLED=true`.

> **Warning:** Email/SMS are stubbed in dev — configure real SMTP/Twilio env vars before expecting live notifications in production.

> **Useful:** Run `./docker/validate-env.sh app` before deploy to ensure all required `.env` variables are set.

