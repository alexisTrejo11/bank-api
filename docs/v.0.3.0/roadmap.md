# ROADMAP.md — Step-by-step implementation guide

> This is the single source of truth for implementation order.
> Read this before starting any new feature branch.
> Each phase = one or more PRs into `develop`.

---

## How to use this file

- Every task has a unique ID (e.g. `P0-T1`). Reference it in commit messages and PR descriptions.
- Each task specifies its branch name, dependencies, and acceptance criteria.
- A phase is complete only when all its tasks have passing CI and are merged into `develop`.
- Update `.claude/INFRA.md`, `ARCHITECTURE.md`, `CONVENTIONS.md`, or `DOMAINS.md` if any decision changes during implementation.

---

## Phase 0 — Project scaffold

**Branch:** `feature/project-scaffold`
**Goal:** A compiling multi-module Gradle project with Docker Compose running, CI green.

| ID | Task | Acceptance criteria |
|---|---|---|
| P0-T1 | Initialize Gradle multi-project root | `./gradlew build` succeeds with empty submodules |
| P0-T2 | Create submodule stubs: `shared`, `iam`, `accounts`, `payments`, `loans`, `notifications`, `audit` | Each has `build.gradle` and root package directory |
| P0-T3 | Write multi-stage `Dockerfile` | `docker build .` produces image under 300MB |
| P0-T4 | Write `docker-compose.yml` with all services | `docker compose up` starts postgres + redis without errors |
| P0-T5 | Write `.env.example` and `.gitignore` | No secrets in source; `.env` gitignored |
| P0-T6 | Add GitHub Actions CI workflow | Runs `./gradlew build test` on every push and PR |
| P0-T7 | Write `README.md` with setup instructions | New developer can run the project from scratch |
| P0-T8 | Flyway baseline migration `V1__baseline.sql` | Empty migration establishes baseline; `flywayMigrate` succeeds |

---

## Phase 1 — Shared module

**Branch:** `feature/shared-module`
**Depends on:** Phase 0
**Goal:** All cross-module primitives in place. Every other module can compile against `shared`.

| ID | Task | Acceptance criteria |
|---|---|---|
| P1-T1 | `Money` record with invariant guards | Rejects negative amounts; supports add/subtract; scale=2 always |
| P1-T2 | Typed ID value objects: `AccountId`, `UserId`, `LoanId`, `TransferId` | Each wraps UUID; `of(UUID)` factory; equals/hashCode by value |
| P1-T3 | `Result<T>` sealed interface (Success + Failure) | `isSuccess()`, `map()`, static factories; unit tested |
| P1-T4 | `BankException` base + module exception stubs | `errorCode` field; each module has at least one subclass |
| P1-T5 | `ApiResponse<T>` envelope record | `success(T)`, `failure(code, message)`, `Meta` with timestamp + requestId |
| P1-T6 | `BankDomainEvent` base record | `eventId` (UUID), `occurredAt` (Instant), `eventType` (String) |
| P1-T7 | `GlobalExceptionHandler` skeleton | Handles `BankException` (400), Not Found (404), Validation (422), generic (500) — all return `ProblemDetail` |
| P1-T8 | Unit tests for Money and Result | Edge cases: zero, negative, currency mismatch, failure mapping |

---

## Phase 2 — IAM module

**Branch:** `feature/iam-module`
**Depends on:** Phase 1
**Goal:** Working auth. Every subsequent module is secured by JWT + RBAC from this point.

| ID | Task | Acceptance criteria |
|---|---|---|
| P2-T1 | Flyway migrations: `users`, `roles`, `permissions`, `user_roles`, `role_permissions` | Tables created; FK constraints correct |
| P2-T2 | Seed migration: default roles (CUSTOMER, ADMIN, AUDITOR) + permissions | `R__seed_roles.sql` repeatable |
| P2-T3 | Domain model: `User`, `Role`, `Permission` aggregates | Pure Java; no Spring annotations in `domain/` |
| P2-T4 | JPA entities + repository adapters for IAM | `UserEntity`, `RoleEntity`; adapter implements domain port |
| P2-T5 | JWT service: issue RS256 token with roles + permissions claims | 15min expiry; `jti` claim present; verifiable with public key |
| P2-T6 | Refresh token: store in Redis as `refresh:{userId}:{hash}` TTL 7d | Rotation on refresh; old token invalidated immediately |
| P2-T7 | JWT blocklist: store `jti` in Redis on logout | Blocked tokens rejected by filter even before expiry |
| P2-T8 | `JwtAuthenticationFilter` (OncePerRequestFilter) | Validates RS256 JWT; populates `SecurityContext`; rejects blocklisted tokens |
| P2-T9 | Spring Security config: filter chain, permit `/auth/**`, require auth elsewhere | Public: `/auth/**`, `/.well-known/jwks.json`, `/actuator/health` |
| P2-T10 | `POST /api/v1/auth/register` — RegisterUserCommand + Handler | Creates user; assigns CUSTOMER role; returns JWT |
| P2-T11 | `POST /api/v1/auth/login` | Returns access token + refresh token; 401 on wrong creds |
| P2-T12 | `POST /api/v1/auth/refresh` | Rotates refresh token; returns new access token |
| P2-T13 | `POST /api/v1/auth/logout` | Blocklists JWT jti in Redis |
| P2-T14 | `GET /.well-known/jwks.json` | Returns RSA public key in JWK format |
| P2-T15 | Integration tests: full auth flow | Register → login → call protected endpoint → logout → token rejected |

---

## Phase 3 — Accounts module

**Branch:** `feature/accounts-module`
**Depends on:** Phase 2
**Goal:** Working account management with double-entry ledger. Balance always derived from entries.

| ID | Task | Acceptance criteria |
|---|---|---|
| P3-T1 | Flyway migrations: `accounts`, `ledger_entries` | Tables with correct types; `ledger_entries` has no UPDATE trigger placeholder |
| P3-T2 | Domain model: `Account` aggregate, `LedgerEntry` entity, `Money` usage | Balance derived method: `SUM(CREDIT) - SUM(DEBIT)` |
| P3-T3 | `LedgerPostingService` domain service | Posts exactly 2 entries (DEBIT + CREDIT) atomically; throws if accounts have mismatched currencies |
| P3-T4 | JPA entities + repository adapters | `AccountEntity`, `LedgerEntryEntity`; adapter implements ports |
| P3-T5 | `POST /api/v1/accounts` — OpenAccountCommand + Handler | Creates account; publishes `AccountOpenedEvent` |
| P3-T6 | `GET /api/v1/accounts/{id}/balance` | Returns derived balance; 404 if not found; 403 if not owner/admin |
| P3-T7 | `GET /api/v1/accounts/{id}/ledger` | Paginated cursor-based; sorted by `created_at DESC` |
| P3-T8 | `GET /api/v1/accounts` | Lists accounts for authenticated user |
| P3-T9 | `AccountsEventListener` stub | Listens to `TransferCompletedEvent` (from ApplicationEvent); posts ledger entries |
| P3-T10 | Integration tests | Open account → check balance = 0 → simulate deposit via direct ledger post → verify balance |

---

## Phase 4 — Audit module

**Branch:** `feature/audit-module`
**Depends on:** Phase 3
**Goal:** Append-only audit log capturing every domain event from every module.

| ID | Task | Acceptance criteria |
|---|---|---|
| P4-T1 | Flyway migration: `audit_records` table + append-only trigger | `BEFORE UPDATE OR DELETE → RAISE EXCEPTION` enforced at DB level |
| P4-T2 | `AuditRecord` JPA entity + repository (insert only) | No `save(update)` — only `saveNew()`; repository has no `update` method |
| P4-T3 | `AuditEventListener` — subscribes to all `BankDomainEvent` subtypes | `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`; writes one `AuditRecord` per event |
| P4-T4 | `GET /api/v1/audit/events` | Filtered by `eventType`, `actorId`, `entityType`, date range; requires `audit:read` permission |
| P4-T5 | Integration tests | Trigger a domain event → verify `audit_records` row inserted; verify UPDATE throws exception |

---

## Phase 5 — Payments module

**Branch:** `feature/payments-module`
**Depends on:** Phase 3, Phase 4
**Goal:** Idempotent transfers with full state machine, publishing events consumed by accounts + audit.

| ID | Task | Acceptance criteria |
|---|---|---|
| P5-T1 | Flyway migration: `transfers` table | Status as VARCHAR; idempotency_key unique index |
| P5-T2 | `Transfer` aggregate with state machine | `PENDING → PROCESSING → COMPLETED | FAILED`; state transitions enforced inside aggregate |
| P5-T3 | Idempotency check via Redis | Key: `idempotency:{key}` TTL 24h; duplicate request returns cached response without re-processing |
| P5-T4 | `TransferFundsCommand` + `TransferFundsHandler` | Validates: same currency, sufficient balance, source ≠ target, amount > 0 |
| P5-T5 | `POST /api/v1/payments/transfers` | Requires `Idempotency-Key` header; requires `payments:write` permission |
| P5-T6 | `GET /api/v1/payments/transfers/{id}` | Returns transfer with current status |
| P5-T7 | `POST /api/v1/payments/transfers/{id}/reverse` | Only `COMPLETED` transfers; creates new reversal transfer |
| P5-T8 | Publish `TransferCompletedEvent` → `AccountsEventListener` | Ledger entries posted; balance updated |
| P5-T9 | Integration tests | Full transfer flow; idempotency (same key twice = one transfer); insufficient funds returns `Result.failure` |

---

## Phase 6 — Loans module

**Branch:** `feature/loans-module`
**Depends on:** Phase 3, Phase 4
**Goal:** Loan origination, amortization schedule, repayment flow.

| ID | Task | Acceptance criteria |
|---|---|---|
| P6-T1 | Flyway migration: `loans`, `loan_repayments` tables | Status enums as VARCHAR; schedule generated at insert time |
| P6-T2 | `Loan` aggregate + `LoanRepayment` child entity | `termMonths` installments generated using fixed-payment formula; all amounts in `BigDecimal` |
| P6-T3 | Amortization calculator domain service | `M = P * [r(1+r)^n] / [(1+r)^n - 1]`; unit tested with known values |
| P6-T4 | `ApplyForLoanCommand` + `ApplyForLoanHandler` | Creates loan in `PENDING_APPROVAL`; auto-approves for demo (no underwriting in v1) |
| P6-T5 | `POST /api/v1/loans/apply` | Returns loan + full repayment schedule |
| P6-T6 | `GET /api/v1/loans/{id}/schedule` | Returns paginated repayment installments with status |
| P6-T7 | `POST /api/v1/loans/{id}/repay` | Marks installment PAID; publishes `LoanRepaymentCompletedEvent` |
| P6-T8 | Auto-transition to `PAID_OFF` | When all repayments = PAID, loan status transitions; `LoanPaidOffEvent` published |
| P6-T9 | Integration tests | Apply → get schedule → repay all → loan is PAID_OFF; double-repay returns `Result.failure` |

---

## Phase 7 — Notifications module

**Branch:** `feature/notifications-module`
**Depends on:** Phase 5, Phase 6
**Goal:** Async notification dispatch for all financial events. Fire-and-forget; failures go to DLQ.

| ID | Task | Acceptance criteria |
|---|---|---|
| P7-T1 | Flyway migration: `notification_log` table | Stores channel, templateKey, userId, status, sentAt |
| P7-T2 | Template engine: map `templateKey` → message body | Simple string interpolation; templates in `resources/templates/` |
| P7-T3 | `ConsoleNotificationAdapter` (dev stub) | Logs to console with `[NOTIFICATION]` prefix; no real SMTP |
| P7-T4 | `NotificationEventListener` | Listens: `TransferCompletedEvent`, `LoanApprovedEvent`, `LoanRepaymentCompletedEvent`, `LoanPaidOffEvent`, `TransferFailedEvent` |
| P7-T5 | Writes `notification_log` on every dispatch attempt | Status = SENT or FAILED; never throws — always completes |
| P7-T6 | Integration tests | Trigger transfer event → verify `notification_log` row inserted with SENT status |

---

# WE ARE HERE

## Phase 8 — Infra layer (nginx + Kafka + Observability)

**Branch series:** see `INFRA.md` section 6
**Depends on:** Phase 7 complete
**Goal:** Full production-ready local stack with Kafka replacing ApplicationEvents, nginx in front, ELK + Grafana live.

**Docker / remote host:** Run the full stack with **`docker compose up -d --build`** — see **[DOCKER.md](DOCKER.md)** (multi-stage `Dockerfile`, 11-service `docker-compose.yml`, nginx, Prometheus, Grafana, ELK, `.env.example`).

| ID | Task | File / Location | Acceptance criteria |
|---|---|---|---|
| P8-T1 | Multi-stage Dockerfile | `Dockerfile` | `docker build .` < 300MB |
| P8-T2 | Full `docker-compose.yml` with all 11 services | `docker-compose.yml` | `docker compose up` — all healthy |
| P8-T3 | `docker-compose.override.yml` for dev | `docker-compose.override.yml` | Hot-reload works with DevTools |
| P8-T4 | nginx.conf with upstream + TLS + rate limit | `infra/nginx/nginx.conf` | HTTP redirects to HTTPS; LB across 3 instances |
| P8-T5 | Self-signed cert generation script | `infra/nginx/gen-certs.sh` | Cert generated; mounted into nginx container |
| P8-T6 | `ForwardedHeaderFilter` bean in Spring | `WebConfig.java` | `X-Real-IP` visible in request context |
| P8-T7 | `KafkaTopicConfig` — 6 `NewTopic` beans | `KafkaTopicConfig.java` | Topics created on startup; visible in Kafka UI |
| P8-T8 | Kafka producer config: `acks=all`, idempotent | `application.yml` | Producer sends with correct guarantees |
| P8-T9 | Replace `ApplicationEventPublisher` → `KafkaTemplate` in all module publishers | All `infrastructure/events/publisher/` classes | Events appear in Kafka topics |
| P8-T10 | `accounts-cg` Kafka consumer | `AccountsKafkaConsumer.java` | Ledger entries posted on transfer/loan events |
| P8-T11 | `audit-cg` Kafka consumer | `AuditKafkaConsumer.java` | `AuditRecord` written for every consumed event |
| P8-T12 | `notifications-cg` Kafka consumer | `NotificationsKafkaConsumer.java` | Notification dispatched and logged |
| P8-T13 | `dlq-monitor-cg` + `/admin/dlq` endpoint | `DlqMonitorConsumer.java` | Failed events logged at ERROR; endpoint returns list |
| P8-T14 | `DefaultErrorHandler` wired to `bank.dlq` on all consumers | `KafkaConsumerConfig.java` | After 3 retries, message lands in DLQ topic |
| P8-T15 | Prometheus scrape config | `infra/prometheus/prometheus.yml` | Metrics visible at `localhost:9090` |
| P8-T16 | Grafana datasource provisioning | `infra/grafana/provisioning/datasources/` | Prometheus auto-wired; no manual setup |
| P8-T17 | Grafana dashboard provisioning (4 dashboards) | `infra/grafana/provisioning/dashboards/` | JVM, HTTP, Kafka lag, Business metrics visible |
| P8-T18 | Logback JSON config with LogstashEncoder | `src/main/resources/logback-spring.xml` | All logs output as JSON to stdout |
| P8-T19 | `MDCFilter` — sets requestId, userId, module | `MDCFilter.java` | Every log line carries MDC fields |
| P8-T20 | Logstash pipeline config | `infra/logstash/pipeline/bank.conf` | Logs appear in Kibana index `bank-logs-*` |
| P8-T21 | Custom Micrometer metrics in each module | each module's handler/service | All 7 metrics from `INFRA.md §4.1` registered and visible in Grafana |

---

## Phase 9 — Seed data + Swagger UI polish

**Branch:** `feature/demo-polish`
**Depends on:** Phase 8
**Goal:** Portfolio-ready demo. Anyone can clone, run `docker compose up`, and explore a live system.

| ID | Task | Acceptance criteria |
|---|---|---|
| P9-T1 | `R__seed_demo_data.sql` — 3 users, roles, 5 accounts | Repeatable migration; seed data present after `docker compose up` |
| P9-T2 | Seed 10 historical transfers | Ledger has entries; balances are non-zero |
| P9-T3 | Seed 2 active loans with partial repayments | Schedule visible; one loan partially paid |
| P9-T4 | SpringDoc OpenAPI config — title, version, security scheme | Swagger UI at `/swagger-ui.html`; JWT auth button present |
| P9-T5 | OpenAPI docs without bloating controllers | `@BankApiOperation` + `BankApiDocumentationCatalog` (summary/description/tag/security); DTOs use `@Schema` for Swagger schemas |
| P9-T6 | `README.md` final update | Includes: architecture diagram link, module list, how to run, demo credentials, Swagger URL, Grafana URL, Kibana URL |
| P9-T7 | `release/1.0.0` branch → merge to `main` → tag `v1.0.0` | Tagged release on GitHub; CI green on `main` |

---

## Release checklist before `v1.0.0` tag

- [x] All integration tests passing on `develop` (verified: `./mvnw verify` green)
- [ ] `docker compose up` starts all services healthy
- [ ] Swagger UI accessible at `http://localhost/swagger-ui.html` (or `https://` if TLS enabled in nginx)
- [ ] Grafana dashboards populated at `http://localhost:3000`
- [ ] Kibana index `bank-logs-*` has entries at `http://localhost:5601`
- [ ] Kafka topics visible; DLQ is empty
- [x] Seed data present (`R__seed_demo_data.sql`); demo credentials documented in README
- [ ] No secrets in any committed file
- [ ] `.env.example` up to date with all required variables
- [ ] All 8 GitHub Issues closed and linked to merged PRs