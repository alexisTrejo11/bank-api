# Bank API

## Documentation (execution and GitHub)

| Document | Description |
|----------|-------------|
| [docs/GITHUB_STRATEGY.md](docs/GITHUB_STRATEGY.md) | Branches, merges, solo PR flow |
| [docs/PR_CONVENTIONS.md](docs/PR_CONVENTIONS.md) | Conventional Commit titles, PR body, `Closes #` |
| [docs/LABELS.md](docs/LABELS.md) | GitHub label definitions |
| [docs/ISSUES.md](docs/ISSUES.md) | Phase ↔ Issue ↔ branch mapping |
| [docs/TRACKER.md](docs/TRACKER.md) | **Implementation checklist** and PR links |
| [docs/EXECUTION_WORKFLOW.md](docs/EXECUTION_WORKFLOW.md) | Repeatable PR and release steps (Phases 1–8) |
| [docs/GITHUB_SETUP_CHECKLIST.md](docs/GITHUB_SETUP_CHECKLIST.md) | Branch protection, labels, Issues P0–P8 (manual; use if `gh` is unavailable) |

### Build (Maven multi-module)

From the repository root (JDK 21+):

```bash
./mvnw clean verify
```

- **`bank-shared`** — shared kernel (`Money`, IDs, `Result`, `BankException`, `BankDomainEvent`, `ApiResponse`, `GlobalExceptionHandler`).
- **`bank`** — Spring Boot application (`app` module).

---

## Project planning document

### 1. Tech stack decisions

| Concern | Choice | Rationale |
|---|---|---|
| Runtime | Java 21 (LTS) + Spring Boot 3.x | Virtual threads (Project Loom), native compile path for later AWS migration |
| Architecture | Modular monolith | Each domain is a self-contained Maven/Gradle submodule with its own `api`, `domain`, `infrastructure` packages. No shared mutable state across module boundaries — extraction to a microservice later is a rename, not a rewrite |
| API | Spring MVC REST + OpenAPI 3 (SpringDoc) | Contract-first: publish the spec, generate clients |
| Auth | Spring Security 6 + JWT (RS256) + RBAC | Asymmetric keys allow future service-to-service trust; RBAC roles live in the `iam` module |
| Persistence | Spring Data JPA + PostgreSQL 16 | ACID transactions critical for double-entry bookkeeping |
| Migrations | Flyway + seed scripts | Repeatable migrations for demo data; versioned migrations for schema |
| Cache / Sessions | Redis (Spring Data Redis) | JWT blocklist (logout), idempotency keys, rate-limit counters |
| Async (internal) | Spring `ApplicationEventPublisher` | Decoupled module-to-module events without Kafka overhead; swap surface is a single `@EventListener` annotation |
| Metrics | Micrometer → Prometheus → Grafana | Actuator endpoints expose `/metrics` in Prometheus format |
| Logs | Logback (JSON encoder) → Logstash → Elasticsearch → Kibana | Structured logs with `traceId`, `userId`, `moduleId` MDC fields |
| Testing | JUnit 5 + Mockito + Testcontainers + REST Assured | Real DB and Redis in containers for integration tests |
| Build | Maven multi-module (`bank-parent` → `bank-shared`, `bank`) | Shared kernel in `shared/`; runnable app in `app/` (Gradle planned for later phases per roadmap) |
| Containers | Docker Compose | One file for full local stack |

---

### 2. Module breakdown & bounded contexts

Each module lives at `io.github.alexistrejo11.bank.{module}` and follows **hexagonal architecture** (ports & adapters).

```
io.github.alexistrejo11.bank.iam          → auth, users, roles, permissions
io.github.alexistrejo11.bank.accounts     → accounts, balances, double-entry ledger
io.github.alexistrejo11.bank.payments     → transfers, idempotency, FX stubs
io.github.alexistrejo11.bank.loans        → loan origination, schedule, repayment
io.github.alexistrejo11.bank.notifications → email/push dispatch, template engine
io.github.alexistrejo11.bank.audit        → immutable event log, compliance queries
io.github.alexistrejo11.bank.shared       → value objects (Money, Currency, AccountId), exceptions, events
```

**Module internal package structure (apply to every module):**

```
{module}/
  api/          → REST controllers, DTOs, request/response mappers
  application/  → use cases / service layer (orchestration only)
  domain/       → entities, aggregates, repository interfaces (ports)
  infrastructure/
    persistence/  → JPA entities, Spring Data repos (adapter)
    events/       → ApplicationEvent publishers/listeners
    external/     → third-party clients (email, SMS stubs)
```

**Cross-module rule:** modules communicate only via `shared` value objects and `ApplicationEvent`s. No direct Spring bean injection across module boundaries.

---

### 3. Domain model highlights

**Accounts & Balances** — double-entry bookkeeping is non-negotiable. Every balance mutation produces two `LedgerEntry` rows (debit + credit). Balance is always derived from the ledger, never stored as a mutable column.

**Payments** — each transfer is idempotent by `idempotencyKey` (UUID stored in Redis with TTL). State machine: `PENDING → PROCESSING → COMPLETED | FAILED | REVERSED`.

**Loans** — origination creates an amortization schedule. Each installment is a `LoanRepayment` entity. When paid, it fires a `LoanRepaymentCompletedEvent` consumed by Accounts.

**IAM** — `User` → `Role` → `Permission` (many-to-many). Spring Security `UserDetails` loads roles at JWT issuance time. Permissions are fine-grained strings like `accounts:read`, `payments:write`.

**Audit** — every domain event (from every module) is consumed by `AuditEventListener` and persisted as an immutable `AuditRecord`. No update/delete on this table — enforced at DB level via row-level trigger.

---

### 4. API design conventions

- Base path: `/api/v1/{module}/...`
- All responses wrapped in `ApiResponse<T>` envelope: `{ data, meta, errors }`
- Pagination: cursor-based for ledger/audit, offset for lists
- Error contract: RFC 7807 Problem Details (`type`, `title`, `status`, `detail`, `instance`)
- Idempotency header: `Idempotency-Key: <uuid>` required on all POST payment endpoints
- Auth header: `Authorization: Bearer <jwt>`

Key endpoints per module:

| Module | Method | Path | Notes |
|---|---|---|---|
| IAM | POST | `/auth/register` | Creates user + assigns default role |
| IAM | POST | `/auth/login` | Issues JWT (RS256, 15 min) + refresh token (Redis, 7d) |
| IAM | POST | `/auth/refresh` | Rotates refresh token |
| IAM | POST | `/auth/logout` | Blocklists JWT in Redis |
| Accounts | POST | `/accounts` | Open account |
| Accounts | GET | `/accounts/{id}/balance` | Derived from ledger |
| Accounts | GET | `/accounts/{id}/ledger` | Paginated ledger entries |
| Payments | POST | `/transfers` | Idempotent transfer |
| Payments | GET | `/transfers/{id}` | Transfer status |
| Loans | POST | `/loans/apply` | Origination |
| Loans | GET | `/loans/{id}/schedule` | Amortization schedule |
| Loans | POST | `/loans/{id}/repay` | Record a repayment |
| Audit | GET | `/audit/events` | Filtered compliance query |

---

### 5. Data model (key tables)

```sql
-- IAM
users           (id, email, password_hash, status, created_at)
roles           (id, name)
permissions     (id, name)
user_roles      (user_id, role_id)
role_permissions(role_id, permission_id)

-- Accounts
accounts        (id, user_id, type, currency, status, created_at)
ledger_entries  (id, account_id, type[DEBIT|CREDIT], amount, currency,
                 reference_id, reference_type, created_at)

-- Payments
transfers       (id, source_account_id, target_account_id, amount,
                 currency, status, idempotency_key, created_at, updated_at)

-- Loans
loans           (id, account_id, principal, interest_rate, term_months,
                 status, created_at)
loan_repayments (id, loan_id, due_date, amount, paid_at, status)

-- Notifications
notification_log(id, user_id, channel, template_key, payload, sent_at, status)

-- Audit
audit_records   (id, event_type, actor_id, entity_type, entity_id,
                 payload jsonb, created_at)  -- NO UPDATE, NO DELETE
```

Flyway file naming: `V{n}__{description}.sql` · Seed: `R__seed_demo_data.sql`

---

### 6. Security design

- **JWT claims:** `sub` (userId), `roles[]`, `permissions[]`, `iat`, `exp`
- **Key rotation:** RSA key pair loaded from env; public key exposed at `/.well-known/jwks.json` for future service-to-service trust
- **Refresh token:** stored in Redis as `refresh:{userId}:{tokenHash}` with TTL
- **JWT blocklist:** on logout, hash stored in Redis as `blocklist:{jti}` with remaining TTL
- **RBAC enforcement:** `@PreAuthorize("hasAuthority('payments:write')")` at controller level
- **HTTPS:** enforced at the reverse proxy (nginx in Docker Compose); app listens on HTTP internally
- **Secrets:** all credentials injected via environment variables; never in `application.yml`

---

### 7. Internal event flow

```
Payments module
  └─ TransferCompletedEvent (ApplicationEvent)
       ├─► AccountsEventListener  → posts two LedgerEntry rows
       ├─► AuditEventListener     → writes AuditRecord
       └─► NotificationEventListener → queues email to sender/receiver

Loans module
  └─ LoanRepaymentCompletedEvent
       ├─► AccountsEventListener  → posts ledger entries
       └─► AuditEventListener     → writes AuditRecord
```

All listeners annotated `@Async` + `@TransactionalEventListener(phase = AFTER_COMMIT)` so side-effects only fire on successful commit.

---

### 8. Docker Compose services

| Service | Image | Purpose |
|---|---|---|
| `app` | custom Spring Boot image | The modular monolith |
| `postgres` | `postgres:16-alpine` | Primary DB |
| `redis` | `redis:7-alpine` | Cache, sessions, idempotency keys |
| `prometheus` | `prom/prometheus` | Scrapes `/actuator/prometheus` |
| `grafana` | `grafana/grafana` | Dashboards (provisioned via config) |
| `elasticsearch` | `elasticsearch:8.x` | Log storage |
| `logstash` | `logstash:8.x` | Parses JSON logs from app |
| `kibana` | `kibana:8.x` | Log exploration |

Health checks on all services; `app` depends on `postgres` and `redis` being healthy.

---

### 9. Testing strategy

| Layer | Tool | Scope |
|---|---|---|
| Unit | JUnit 5 + Mockito | Domain logic, use cases (no Spring context) |
| Integration | `@SpringBootTest` + Testcontainers | Full HTTP → DB → Redis round-trips |
| Module isolation | `@WebMvcTest` + MockBean | Controller slice, verifies security rules |
| Data | `@DataJpaTest` + Testcontainers | Repository queries, Flyway migrations |
| API | REST Assured (inside integration tests) | Validates OpenAPI contract adherence |

Naming convention: `{Class}Test` for unit, `{Class}IT` for integration. Testcontainers reuses the same container per test class via `@Container static`.

---

### 10. Observability checklist

**Metrics to expose (custom `MeterRegistry` beans):**
- `bank.transfer.total` (counter, tagged by status)
- `bank.transfer.amount` (distribution summary)
- `bank.account.balance` (gauge per account type)
- `bank.loan.delinquency_rate` (gauge)

**Structured log MDC fields:** `traceId`, `spanId`, `userId`, `module`, `requestId`

**Grafana dashboards to provision:**
- JVM health (heap, GC, threads)
- HTTP request rates and latency by endpoint
- Transfer volume and failure rate
- Active loans overview

---

### 11. AWS migration path (future)

Since the monolith is modular from day one, the migration path when you need it:

1. Extract one module at a time into a standalone Spring Boot service
2. Replace `ApplicationEvent` internal bus with SQS/SNS or Kafka
3. Each service gets its own RDS instance (schema already isolated)
4. Add an API Gateway (AWS API GW or Spring Cloud Gateway) in front
5. Redis → ElastiCache, Prometheus/Grafana → CloudWatch + Managed Grafana, ELK → OpenSearch

---

### 12. Suggested implementation order

1. Project scaffolding (Gradle multi-project, Docker Compose skeleton, Flyway baseline)
2. `iam` module — auth, JWT, RBAC (everything else depends on this)
3. `accounts` module — account CRUD, double-entry ledger
4. `audit` module — event listener infrastructure
5. `payments` module — transfers, idempotency, state machine
6. `loans` module — origination, schedule, repayment
7. `notifications` module — templates, async dispatch
8. Observability wiring (Prometheus metrics, JSON logs → ELK)
9. Seed data + Swagger UI polish for portfolio demo

---

This gives you a complete implementation blueprint. Each section maps directly to a coding sprint. When you're ready to start any specific module or layer, just say which one and we'll go deep on the implementation details.
