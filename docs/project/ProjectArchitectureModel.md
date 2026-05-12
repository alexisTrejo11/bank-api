# Architecture model

Derived from [`source/ProjectArchitecture.md`](source/ProjectArchitecture.md). For deployment topology see [InfrastructureModel.md](InfrastructureModel.md).

## Architectural layers

### Presentation (adapters in)

**Role:** Spring Web MVC controllers, request/response DTOs, validation, OpenAPI annotations. Maps HTTP to application commands and queries.

**Controllers (examples)**

- `AuthController` — `/api/v1/auth`  
- `AccountController` — `/api/v1/accounts`  
- `TransferController` — `/api/v1/payments`  
- `LoanController` — `/api/v1/loans`  
- `AuditController` — `/api/v1/audit`  
- `NotificationMonitoringController` — `/api/v1/notifications/monitoring`  

**Responsibilities**

- Translate JSON bodies into immutable records and application-layer commands.  
- Return `ApiResponse<T>` envelopes and correct HTTP semantics (e.g. **422** for domain failures on transfers).

**Technologies:** Spring Web MVC, Spring Security (URL authority rules), Jakarta Validation, springdoc-openapi.

---

### Application (use cases)

**Role:** Command/query handlers orchestrate domain services and ports. `@Transactional` boundaries live on handlers or services as appropriate.

**Examples:** `OpenAccountHandler`, `PostTransferToLedgerHandler`, loan and payment handlers, IAM handlers; query handlers for ledger, balance, audit search, loan detail, notification lists.

**Responsibilities**

- Enforce use-case rules before touching aggregates.  
- Publish domain or integration events after persistence when required.

**Technologies:** Spring `@Component` / constructor injection, Spring Modulith event APIs where used.

---

### Domain

**Role:** Entities, value objects, domain exceptions, repository **interfaces**. No Spring Web types.

**Examples:** `BankAccount`, `LedgerEntry`, `LoanAggregate`, audit and notification domain models; `AccountRepository`, `LedgerEntryRepository`, etc.

**Responsibilities**

- Express invariants (currency match on transfer, account active, …).  
- Stay free of infrastructure details.

**Technologies:** Plain Java 21.

---

### Infrastructure (adapters out)

**Role:** JPA entities, Spring Data repositories, Kafka listeners, Redis-backed services, Flyway migrations.

**Examples:** Persistence adapters per module; `JwtAuthenticationFilter`, `SecurityConfig` in `bank-config`; Redis token-bucket rate limiting wired from `bank-boot`.

**Responsibilities**

- Map domain models to tables and brokers.  
- Implement cross-cutting technical concerns (JWT parsing, rate-limit keys).

**Technologies:** Spring Data JPA, PostgreSQL driver, Spring Data Redis, Spring Kafka.

---

## Design patterns

| Pattern | Emoji | Category | Badge | Summary |
|---------|-------|----------|-------|---------|
| Hexagonal (ports & adapters) | ⬡ | Architecture | core | Domain defines repository ports; infrastructure implements them; web depends on handlers, not repositories. |
| CQRS-style split | ↔ | Application | patterns | Separate command/query handlers and DTOs; read models stay API-friendly. |
| Domain events (Spring) | 📨 | Integration | events | ApplicationEvents; listeners that need committed data should use **AFTER_COMMIT** semantics. |
| Modular monolith | 🧱 | Modulith | spring-modulith | Spring Modulith enforces boundaries so splitting to multiple ECS services is mostly a deployment concern. |

## Scalability strategies

1. **Horizontal scale of stateless API** — ECS desired count > 1 behind ALB; JWT self-contained; Redis holds refresh and idempotency state shared across tasks.  
2. **Read scaling** — Pageable ledger and audit queries; RDS read replicas after routing reads.  
3. **Kafka fan-out** — MSK for durable cross-boundary integration; scale consumers independently.

## Security strategies

1. **RS256 JWT + authority checks** — `SecurityConfig` maps URL patterns to Spring Security authorities from JWT claims.  
2. **Rate limiting** — Optional Redis token buckets: global servlet filter plus `@RateLimit` on sensitive controllers.  
3. **Defense in depth on AWS** — Private subnets for ECS, tight security groups (ALB → task only), Secrets Manager for signing keys, VPC endpoints where appropriate.

## Cache and coordination strategies

| Name | TTL / model | Coverage |
|------|-------------|----------|
| Idempotency outcome cache | ~**24h** (confirm in payments config) | `POST /api/v1/payments/transfers` and reverse |
| JWT refresh / blocklist metadata | ~**7d** (illustrative — verify IAM properties) | Auth and logout flows |
| Rate limit buckets | Token bucket (not classical TTL) | `/api/**` when `bank.rate-limiting.enabled=true` |

## Architecture features (highlights)

| Title | Emoji | Description |
|-------|--------|-------------|
| Single deployable, many bounded contexts | 📦 | Operational simplicity; optional extraction of workers to separate ECS services. |
| Flyway as schema authority | 🪶 | Migrations ship with the app; coordinate with RDS upgrades. |
| Observability hooks | 📈 | Actuator health/metrics; on AWS prefer CloudWatch / ADOT vs only Prometheus sidecars. |

## Logical diagram (AWS-shaped)

**Legend**

| Type | Label | Color (source) | Icon |
|------|-------|----------------|------|
| client | Client / partner API | #90CAF9 | 💻 |
| gateway | ALB / API front door | #FFCC80 | 🚪 |
| service | ECS Fargate (bank-boot) | #A5D6A7 | ⚙️ |
| database | RDS PostgreSQL | #CE93D8 | 🐘 |
| database | ElastiCache Redis | (same legend type in tooling) | 💾 |
| queue | MSK Kafka | #FFF59D | 📨 |
| monitoring | CloudWatch | #B0BEC5 | 📊 |

**Nodes (labels are logical — x/y are for renderers only)**

| ID | Label | Type | Status |
|----|-------|------|--------|
| client | HTTPS clients | client | healthy |
| alb | Application Load Balancer | gateway | healthy |
| ecs | ECS service (bank-api tasks) | service | healthy |
| rds | RDS PostgreSQL | database | healthy |
| redis | ElastiCache Redis | database | healthy |
| msk | MSK cluster | queue | healthy |
| cw | CloudWatch | monitoring | healthy |

**Connections**

| From | To | Label | Protocol |
|------|-----|--------|----------|
| client | alb | HTTPS | TLS |
| alb | ecs | HTTP | HTTP/1.1 |
| ecs | rds | JDBC | TCP |
| ecs | redis | Redis protocol | TCP |
| ecs | msk | produce/consume | Kafka |
| ecs | cw | metrics/logs | OTLP / CW API |

## Data flows

### Request path (synchronous)

1. **TLS at ALB** — ACM certificate; optional AWS WAF.  
2. **JWT authentication filter** — Allowlist: register/login/refresh, swagger, JWKS; otherwise validate Bearer JWT.  
3. **Controller → handler** — Build command/query; handler applies rules and uses repositories.  
4. **Transaction commit** — PostgreSQL commit; rollback prevents dependent side-effects.  
5. **Response envelope** — `ApiResponse` for success or standardized error codes.

### Event path (asynchronous / integration)

1. **Domain publishes application events** — Something already happened in the domain.  
2. **After-commit listeners** — Audit, ledger posting, notifications must not read uncommitted phantom state.  
3. **Kafka (MSK)** — Durable fan-out for notifications and analytics.  
4. **Idempotent consumers** — At-least-once safe via business keys or message IDs.

## Architecture decision records (lite)

### Modular monolith vs microservices on day one

| | |
|--|--|
| **Problem** | Small team needs correctness and velocity; microservices add network and ops overhead. |
| **Solution** | Single Spring Boot binary with Spring Modulith-enforced module boundaries. |
| **Alternatives** | Full microservices mesh from day zero; unstructured monolith. |
| **Outcome** | Faster delivery with a path to per-context ECS services when justified. |

### PostgreSQL everywhere

| | |
|--|--|
| **Problem** | Financial data needs ACID and strong consistency for ledger rows. |
| **Solution** | PostgreSQL as system of record; Flyway migrations versioned with the app. |
| **Alternatives** | NoSQL primary for balances; polyglot persistence. |
| **Outcome** | Simpler operations; standard RDS Multi-AZ HA. |

### Redis for cross-request coordination

| | |
|--|--|
| **Problem** | Idempotency, refresh sessions, revocation metadata, and rate limits need fast shared state. |
| **Solution** | ElastiCache Redis cluster shared by all ECS tasks. |
| **Alternatives** | DynamoDB for idempotency only; in-memory only (breaks multi-task correctness). |
| **Outcome** | Predictable deduplication and throttling at scale. |

## Notes and risks

- Diagram coordinates are **not** network topology.  
- **`/actuator/**`** is permitted on a dedicated filter chain — treat as sensitive on AWS.  
- **Outbox table** is not described; consider it if you need exactly-once publication to MSK.  
- **`bank-config` package typo** (`alexisTrejo11` vs `alexistrejo11`) — cosmetic but confusing; fix in a hygiene PR.

## Related

- [Project features](ProjectFeatures.md) — what the architecture delivers  
- [API schema](APISchema.md) — HTTP surface  
