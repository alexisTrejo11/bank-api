# AGENT.md — Bank System

## Project identity
- **Artifact:** `io.github.alexistrejo11.bank`
- **Language:** Java 25 (use virtual threads, records, sealed classes where appropriate)
- **Framework:** Spring Boot 4.x (Spring MVC, Spring Security 6, Spring Data JPA)
- **Build:** Maven multi-module (parent POM + one submodule per domain + `bank-boot`)
- **Architecture:** Modular monolith with hexagonal internals (ports & adapters)
- **Deployment:** Docker Compose (local); AWS-ready structure

## Context files — read these before touching any module
- **Architecture decisions & module map** → `.agents/architecture.md`
- **Coding conventions & patterns** → `.agents/conventions.md`
- **Domain model & business rules** → `.agents/domains.md`
- **Planned infra / v0.2.0** → `docs/v0.2.0/ROADMAP.md` and `docs/v0.2.0/TRACKER.md`

## Build commands
```bash
./mvnw clean verify                    # full build + tests (from repo root)
./mvnw test                            # unit tests only
./mvnw -pl bank-boot spring-boot:run   # runnable application module
docker compose up --build              # full local stack (when compose is present)
docker compose up -d postgres redis kafka   # infra for v0.2.0 (see README + docs/v0.2.0/DATABASE.md)
```

## Module list
| Submodule | Root package | Purpose |
|---|---|---|
| `bank-iam` | `io.github.alexistrejo11.bank.iam` | Auth, users, roles, permissions |
| `bank-accounts` | `io.github.alexistrejo11.bank.accounts` | Accounts, balances, double-entry ledger |
| `bank-payments` | `io.github.alexistrejo11.bank.payments` | Transfers, idempotency, state machine |
| `bank-loans` | `io.github.alexistrejo11.bank.loans` | Origination, schedule, repayment |
| `bank-notifications` | `io.github.alexistrejo11.bank.notifications` | Email/push dispatch |
| `bank-audit` | `io.github.alexistrejo11.bank.audit` | Immutable compliance event log |
| `bank-shared` | `io.github.alexistrejo11.bank.shared` | Value objects, events, Result<T>, exceptions |
| `bank-boot` | `io.github.alexistrejo11.bank` | Spring Boot assembly / integration adapters |

## Non-negotiable rules (apply everywhere)
1. **No cross-module Spring bean injection.** Modules communicate only via `shared` value objects and `ApplicationEvent`s.
2. **Domain layer has zero Spring annotations.** Entities, aggregates, and domain services are plain Java.
3. **Every balance mutation goes through the ledger.** Never update a balance column directly.
4. **All POST payment/transfer endpoints require `Idempotency-Key` header.**
5. **Secrets live in environment variables or a local `.env` file only** (loaded with lowest precedence by `DotEnvEnvironmentPostProcessor` in `bank-boot`). Never commit secrets. See `docs/v0.2.0/CONFIGURATION.md` and `.env.example`.
6. **`audit_records` table is append-only.** No UPDATE or DELETE ever — enforced by DB trigger.
7. **Use `Result<T>` for recoverable business errors.** Use exceptions for unexpected/infrastructure failures.