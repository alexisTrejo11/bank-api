# Architecture

## Presentation (API)

REST controllers per domain module — no business logic, only DTO mapping and handler delegation.

### Components

- AuthController — /api/v1/auth
- AccountController — /api/v1/accounts
- TransferController — /api/v1/payments
- LoanController — /api/v1/loans
- AuditController — /api/v1/audit
- NotificationMonitoringController — /api/v1/notifications/monitoring

### Responsibilities

- Validate request DTOs (@Valid)
- Map domain results to ApiResponse<T>
- Apply @RateLimit and OpenAPI annotations

### Technologies

- Spring Web MVC
- springdoc-openapi
- Jakarta Validation

## Application layer

Command/query handlers orchestrate use cases, publish domain events, and coordinate transactions.

### Components

- Command handlers (InitiateTransferHandler, OriginateLoanHandler, …)
- Query handlers (GetAccountBalanceHandler, SearchAuditRecordsHandler, …)
- ApplicationEventPublisher for cross-module events

### Responsibilities

- Transaction boundaries (@Transactional)
- Idempotency checks before side effects
- Publish BankDomainEvent after commit

### Technologies

- Spring @Component handlers
- Spring Modulith event API

## Domain layer

Pure Java aggregates, value objects, and port interfaces — zero Spring/JPA dependencies.

### Components

- Money, AccountId, UserId, TransferId (bank-shared)
- Transfer state machine, Loan amortization rules
- port.in / port.out repository interfaces

### Responsibilities

- Enforce invariants (balance from ledger, transfer states)
- Return Result<T> for expected failures

### Technologies

- Java 21 records
- BigDecimal financial math

## Infrastructure layer

JPA adapters, Redis stores, Kafka producers/consumers, and external notification stubs.

### Components

- JPA entities + repository adapters
- RedisRefreshTokenStore, TransferIdempotencyCache
- KafkaNotificationDispatchIngress
- @TransactionalEventListener cross-module listeners

### Responsibilities

- Implement domain ports
- Map entities ↔ domain models
- Bridge to RDS, Upstash Redis, cloud Kafka

### Technologies

- Spring Data JPA
- Spring Data Redis
- Spring Kafka

## Data & messaging (AWS)

External managed services — not bundled in production compose.

### Components

- Amazon RDS PostgreSQL 16
- Upstash Redis (tokens, idempotency, rate limits)
- Cloud Kafka broker (notification dispatch + pipeline topics)

### Responsibilities

- ACID persistence via Flyway-managed schema
- Shared state across EC2 app restarts
- Decouple notification enqueue from HTTP thread

### Technologies

- Flyway
- PostgreSQL
- Redis
- Kafka

## Observability (EC2)

External monitoring stack running alongside or on the same EC2 host.

### Components

- Prometheus (scrapes /actuator/prometheus)
- Grafana (dashboards, Prometheus + Loki datasources)
- Loki + Promtail (app logs from /app/logs volume)
- Optional ELK profile (Logstash → Elasticsearch → Kibana)

### Responsibilities

- Metrics, dashboards, log aggregation
- audit.json and access.json structured files

### Technologies

- Micrometer
- Logback JSON
- Prometheus / Grafana / Loki

## Design patterns

| Pattern | Category | Description |
| --- | --- | --- |
| 🧩 Modular monolith | Structural | Nine Maven modules with package-level boundaries; any module extractable to microservice by swapping ApplicationEvent for Kafka/SQS. |
| ⬡ Hexagonal (ports & adapters) | Structural | domain/port.out interfaces implemented in infrastructure/persistence/adapter — domain never imports JPA. |
| 📋 CQRS-lite | Behavioral | Separate command and query handlers with dedicated Command/Query records. |
| 📡 Domain events | Behavioral | TransferCompletedEvent, LoanDisbursedEvent, etc. in bank-shared; listeners in accounts, audit, notifications. |
| ✅ Result<T> for failures | Behavioral | Expected domain failures (INSUFFICIENT_FUNDS) return Result.failure instead of exceptions for control flow. |
| 🔑 Idempotency key | Integration | Redis + DB dedup for payment mutations — safe retries from mobile/unstable networks. |

## Scalability strategies

- **Stateless API container** — JWT + external Redis — scale horizontally with additional EC2 instances behind ALB; shared Upstash Redis for tokens and idempotency.
- **RDS PostgreSQL** — Managed backups and storage; connection pooling via HikariCP; Flyway migrations on deploy.
- **Kafka notification pipeline** — Decouple notification dispatch from request thread; consumer group on cloud Kafka handles burst traffic.
- **Rate limiting fail-open** — Redis token bucket protects auth and payments; configurable fail-open if Redis unreachable (logged, not silent).

## Security strategies

- **RS256 JWT + permissions** — Spring Security filter chain; @PreAuthorize on sensitive endpoints; permissions[] in JWT claims.
- **Refresh rotation + blocklist** — Old refresh invalidated on rotate; logout adds jti to Redis blocklist for remaining access token TTL.
- **Append-only audit** — DB trigger prevents audit record mutation; full event JSON in payload for reconstruction.
- **Idempotency on writes** — Payments require Idempotency-Key; prevents duplicate transfers on client retries.
- **CORS configuration** — bank.http.cors.allowed-origins from env — empty by default; set explicitly for frontends.

## Cache strategies

| Name | TTL | Coverage | Description |
| --- | --- | --- | --- |
| IAM refresh tokens | 7 days | Refresh token validation and rotation | Redis key iam:refresh:{sha256(token)} → userId, TTL 7 days |
| JWT blocklist | Remaining token lifetime | Logout and suspend flows | Redis key blocklist:{jti} for revoked access tokens |
| Transfer idempotency | 24h | POST /payments/transfers and /reverse | Cached Result<TransferResponse> per userId + idempotencyKey |
| Rate limit buckets | Rolling window | Auth, payments, loans sensitive ops | Lua token-bucket script — atomic refill + consume per IP/user key |

## Architecture highlights

### 📦 Spring Modulith

Formal module boundaries with event-based integration; supports future extraction without package rewrites.

### 🗃️ Flyway migrations

15 versioned SQL migrations + repeatable seed; ddl-auto=validate in production.

### 📨 Standard ApiResponse

Uniform { data, meta, errors } JSON for all REST endpoints.

### 📊 Actuator observability

health, info, prometheus endpoints exposed per MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE.

## Architecture diagram

### Legend

| Type | Label |
| --- | --- |
| client | Client |
| gateway | Gateway |
| service | API service |
| database | Database |
| queue | Queue / cache |
| monitoring | Monitoring |

### Nodes

| ID | Label | Type | Status |
| --- | --- | --- | --- |
| client | API clients / Postman | client | healthy |
| ec2-nginx | EC2 — Nginx (optional) | gateway | healthy |
| bank-app | Bank API (Docker on EC2) | service | healthy |
| rds | Amazon RDS PostgreSQL | database | healthy |
| upstash | Upstash Redis | queue | healthy |
| kafka | Cloud Kafka broker | queue | healthy |
| prometheus | Prometheus (EC2) | monitoring | healthy |
| grafana | Grafana + Loki (EC2) | monitoring | healthy |

### Connections

| From | To | Label | Protocol |
| --- | --- | --- | --- |
| client | ec2-nginx | HTTPS | TLS |
| ec2-nginx | bank-app | Proxy :8080 | HTTP |
| bank-app | rds | JDBC / Flyway | PostgreSQL |
| bank-app | upstash | Tokens / idempotency / limits | Redis TLS |
| bank-app | kafka | Notification dispatch | Kafka |
| kafka | bank-app | Consumer group | Kafka |
| prometheus | bank-app | Scrape metrics | HTTP /actuator/prometheus |
| grafana | prometheus | Dashboards | HTTP |

### Mermaid overview

```mermaid
flowchart LR
    client([API clients / Postman])
    ec2-nginx{EC2 — Nginx (optional)}
    bank-app[Bank API (Docker on EC2)]
    rds[(Amazon RDS PostgreSQL)]
    upstash[/Upstash Redis/]
    kafka[/Cloud Kafka broker/]
    prometheus>Prometheus (EC2)]
    grafana>Grafana + Loki (EC2)]
    client -->|HTTPS| ec2-nginx
    ec2-nginx -->|Proxy :8080| bank-app
    bank-app -->|JDBC / Flyway| rds
    bank-app -->|Tokens / idempotency / limits| upstash
    bank-app -->|Notification dispatch| kafka
    kafka -->|Consumer group| bank-app
    prometheus -->|Scrape metrics| bank-app
    grafana -->|Dashboards| prometheus
```

## Data flow

### Request flow

1. **HTTP request** — Client sends REST call with Bearer JWT (except public auth routes).
2. **Security + rate limit** — JwtAuthenticationFilter validates RS256 token; GlobalRateLimitFilter and @RateLimit check Redis token bucket.
3. **Handler execution** — Controller delegates to application handler; domain rules enforced; @Transactional wraps persistence.
4. **Persistence & cache** — JPA writes to RDS; Redis updated for idempotency or rate counters.
5. **Events & response** — AFTER_COMMIT listeners update ledger, audit, notifications; ApiResponse JSON returned.

### Event flow

1. **Domain event published** — e.g. TransferCompletedEvent after successful transfer commit.
2. **Cross-module listeners** — accounts posts ledger entries; audit appends AuditRecord; notifications builds DispatchNotificationCommand.
3. **Kafka enqueue (AWS)** — When dispatch-mode=kafka, KafkaNotificationDispatchIngress publishes to bank.notifications.dispatch topic on cloud broker.
4. **Notification delivery** — Kafka consumer processes pipeline; email/SMS sent or stubbed; status logged in notification_log table.

## Additional notes

# Architecture

> **AWS alignment:** Production runs `docker/compose.yml` (app only) on EC2. RDS, Upstash Redis, and cloud Kafka are reached via `.env` — see `.env.example` AWS comment block.

> **Local parity:** `docker/compose.local.yml` bundles postgres, redis, kafka, prometheus, grafana, loki, nginx so developers exercise the same integration paths before deploy.

> **Important:** Domain modules communicate via `ApplicationEvent` (AFTER_COMMIT) and optionally Kafka for notifications — never direct imports of another module's JPA entities.

> **Warning:** Ephemeral JWT keys when PEM env vars unset — multi-instance EC2 deploy requires `BANK_SECURITY_JWT_PRIVATE_KEY_PEM` and `BANK_SECURITY_JWT_PUBLIC_KEY_PEM`.

