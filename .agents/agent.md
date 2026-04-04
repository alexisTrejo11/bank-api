# AGENT.md — Bank System

## Project identity
- **Artifact:** `io.github.alexistrejo11.bank`
- **Language:** Java 25 (use virtual threads, records, sealed classes where appropriate)
- **Framework:** Spring Boot 4.x (Spring MVC, Spring Security 6, Spring Data JPA)
- **Build:** Gradle multi-project (one submodule per domain + one `infra` module)
- **Architecture:** Modular monolith with hexagonal internals (ports & adapters)
- **Deployment:** Docker Compose (local); AWS-ready structure

## Context files — read these before touching any module
- **Architecture decisions & module map** → `.claude/ARCHITECTURE.md`
- **Coding conventions & patterns** → `.claude/CONVENTIONS.md`
- **Domain model & business rules** → `.claude/DOMAINS.md`

## Build commands
```bash
mvn clean verify                       # full build + tests
mvn test                               # unit tests only
mvn verify -Pintegration               # Testcontainers integration tests
mvn -pl iam spring-boot:run            # run a single module (dev)
docker compose up --build              # full local stack
docker compose up postgres redis       # infra only (for local Spring run)
mvn flyway:migrate                     # run pending migrations
mvn springdoc-openapi:generate         # regenerate OpenAPI spec
```

## Module list
| Submodule | Root package | Purpose |
|---|---|---|
| `iam` | `io.github.alexistrejo11.bank.iam` | Auth, users, roles, permissions |
| `accounts` | `io.github.alexistrejo11.bank.accounts` | Accounts, balances, double-entry ledger |
| `payments` | `io.github.alexistrejo11.bank.payments` | Transfers, idempotency, state machine |
| `loans` | `io.github.alexistrejo11.bank.loans` | Origination, schedule, repayment |
| `notifications` | `io.github.alexistrejo11.bank.notifications` | Email/push dispatch |
| `audit` | `io.github.alexistrejo11.bank.audit` | Immutable compliance event log |
| `shared` | `io.github.alexistrejo11.bank.shared` | Value objects, events, Result<T>, exceptions |

## Non-negotiable rules (apply everywhere)
1. **No cross-module Spring bean injection.** Modules communicate only via `shared` value objects and `ApplicationEvent`s.
2. **Domain layer has zero Spring annotations.** Entities, aggregates, and domain services are plain Java.
3. **Every balance mutation goes through the ledger.** Never update a balance column directly.
4. **All POST payment/transfer endpoints require `Idempotency-Key` header.**
5. **Secrets live in environment variables only.** Never in `application.yml` or source code.
6. **`audit_records` table is append-only.** No UPDATE or DELETE ever — enforced by DB trigger.
7. **Use `Result<T>` for recoverable business errors.** Use exceptions for unexpected/infrastructure failures.