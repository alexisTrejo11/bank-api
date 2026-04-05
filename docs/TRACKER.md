# Implementation tracker

Single source of truth for **what is done**. Update checkboxes and PR links when each phase merges.

**Legend:** `[ ]` open | `[x]` done | **PR** = link to merged pull request | **Issue** = GitHub issue number from [ISSUES.md](ISSUES.md)

**Web (after push):** `https://github.com/<owner>/<repo>/blob/develop/docs/TRACKER.md`

---

## Phase 0 — Project skeleton

**Branch:** `feature/project-scaffold` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| P0-G | Gradle: `settings.gradle.kts` (or `.gradle`), root `build.gradle.kts` | [ ] |
| P0-M | Submodule stubs: `shared/build.gradle.kts`, `iam/build.gradle.kts`, … | [ ] |
| P0-D | `docker-compose.yml` (postgres, redis, prometheus, grafana, ELK) | [ ] |
| P0-Y | Root `application.yml` + environment variable wiring | [ ] |
| P0-F | Flyway baseline `V1__init.sql` (may be empty) | [ ] |
| P0-CI | [.github/workflows/ci.yml](../.github/workflows/ci.yml) — build + test on push | [ ] |
| P0-R | [README.md](../README.md) — setup instructions | [ ] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## Phase 1 — Shared module

**Branch:** `feature/shared-module` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| P1-VO | Value objects: `Money`, `AccountId`, `UserId`, `LoanId`, `TransferId` | [x] |
| P1-R | `Result<T>` sealed interface | [x] |
| P1-X | `BankException` hierarchy | [x] |
| P1-E | `BankDomainEvent` base record | [x] |
| P1-A | `ApiResponse<T>` envelope | [x] |
| P1-H | `GlobalExceptionHandler` skeleton | [x] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## Phase 2 — IAM module

**Branch:** `feature/iam-module` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| P2-D | Domain: User, Role, Permission | [ ] |
| P2-F | Flyway migrations for IAM + seed roles | [ ] |
| P2-J | JWT RS256, refresh token, blocklist | [ ] |
| P2-S | Spring Security filter chain, `@PreAuthorize` | [ ] |
| P2-R | REST: `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout` | [ ] |
| P2-T | Unit + integration tests | [ ] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## Phase 3 — Accounts module

**Branch:** `feature/accounts-module` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| P3-A | Account aggregate + `LedgerEntry` | [ ] |
| P3-F | Flyway migrations for accounts/ledger | [ ] |
| P3-P | Double-entry posting service | [ ] |
| P3-R | REST: open account, balance, ledger | [ ] |
| P3-L | `AccountsTransferListener` stub (payments events) | [ ] |
| P3-T | Tests | [ ] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## Phase 4 — Audit module

**Branch:** `feature/audit-module` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| P4-E | `AuditRecord` entity + persistence | [x] |
| P4-F | Append-only DB trigger migration | [x] |
| P4-L | `AuditEventListener` (all `BankDomainEvent`s) | [x] |
| P4-Q | Query endpoint with filters | [x] |
| P4-T | Tests | [x] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## Phase 5 — Payments module

**Branch:** `feature/payments-module` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| P5-A | Transfer aggregate + state machine | [ ] |
| P5-I | Idempotency via Redis | [ ] |
| P5-X | Transfer execution + event publishing | [ ] |
| P5-W | `TransferCompletedEvent` → accounts + audit + notifications | [ ] |
| P5-T | Tests | [ ] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## Phase 6 — Loans module

**Branch:** `feature/loans-module` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| P6-A | Loan aggregate + repayment schedule generation | [ ] |
| P6-O | Origination flow + disbursement event | [ ] |
| P6-R | Repayment endpoint | [ ] |
| P6-T | Tests | [ ] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## Phase 7 — Notifications module

**Branch:** `feature/notifications-module` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| P7-L | Event listeners for notification triggers | [ ] |
| P7-T | Template engine + console stub (dev) | [ ] |
| P7-P | `notification_log` persistence | [ ] |
| P7-S | Tests | [ ] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## Phase 8 — Observability and release

**Branch:** `feature/observability` → then `release/1.0.0` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| P8-M | Custom Micrometer metrics per module | [ ] |
| P8-L | Logback JSON + MDC filter | [ ] |
| P8-G | Grafana dashboard provisioning | [ ] |
| P8-S | Swagger UI customization + `R__seed_demo_data.sql` | [ ] |
| P8-R | `release/1.0.0` → merge to `main` → tag `v1.0.0` | [ ] |

- **PR (to `develop`):** 
- **PR (release to `main`):** 
- **Tag:** `v1.0.0` · **Merged at:** 

---

## Repository paths reference

| Phase | Key paths / artifacts |
|-------|------------------------|
| 0 | `settings.gradle.kts`, `docker-compose.yml`, `bank-boot/src/main/resources/application.yml`, `db/migration/V1__init.sql`, `.github/workflows/ci.yml` |
| 1 | `bank-shared/src/main/java/...` — VO, Result, exceptions, events, `ApiResponse`, `GlobalExceptionHandler` |
| 2 | `iam/...`, JWT/security, `/auth/*`, Flyway IAM |
| 3 | `accounts/...`, ledger, posting, REST, listener stub |
| 4 | `bank-audit/...`, trigger, listener, query API |
| 5 | `payments/...`, Redis idempotency, transfer FSM, events |
| 6 | `loans/...`, schedule, origination, repayment |
| 7 | `notifications/...`, templates, log |
| 8 | metrics, logback, Grafana provisioning, seed SQL, release tag |

Exact packages follow `io.github.alexistrejo11.bank.{module}` (see [README.md](../README.md)).
