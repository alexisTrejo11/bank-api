# Implementation tracker — v0.2.0

Single checklist for **release 0.2.0** infrastructure and cross-cutting work. See [ROADMAP.md](ROADMAP.md) for rationale and acceptance notes.

**Legend:** `[ ]` open | `[x]` done | **PR** = merged pull request link

**Web (after push):** `https://github.com/<owner>/<repo>/blob/develop/docs/v0.2.0/TRACKER.md`

---

## V2 — Data and migrations

**Branch:** `feature/v0.2-postgres-flyway` (suggested) · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| V2-DB | PostgreSQL datasource profile(s) + Compose service wired to app | [ ] |
| V2-F | Flyway migrations validated on PostgreSQL; document H2 vs PG differences | [ ] |
| V2-T | CI / Testcontainers still green | [ ] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## V2 — Redis

**Branch:** `feature/v0.2-redis` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| V2-R-C | Connection config from environment; health indicator | [ ] |
| V2-R-I | IAM: refresh + JWT blocklist using Redis in target profile(s) | [ ] |
| V2-R-P | Payments: Redis idempotency enabled for same profile(s) | [ ] |
| V2-R-D | README / ROADMAP: how to run with and without Redis | [ ] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## V2 — Notifications + Kafka

**Branch:** `feature/v0.2-kafka-notifications` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| V2-K-B | Kafka in Docker Compose; Spring Kafka config | [ ] |
| V2-K-P | Publish/consume design implemented in `bank-notifications` | [ ] |
| V2-K-O | Observability: consumer lag / error metrics or logs | [ ] |
| V2-K-T | Tests (Testcontainers Kafka or embedded) | [ ] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## V2 — HTTP surface (Security, CORS, rate limit)

**Branch:** `feature/v0.2-http-hardening` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| V2-S | Spring Security refactor: explicit chains, documented public routes | [ ] |
| V2-C | CORS from configurable properties / env | [ ] |
| V2-L | Rate limiting (Redis) for selected endpoints | [ ] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## V2 — Observability and secrets

**Branch:** `feature/v0.2-observability-secrets` · **Issue:** #___

| ID | Track item | Done |
|----|------------|------|
| V2-O-L | JSON structured application logging + MDC | [ ] |
| V2-O-A | Audit-friendly log channel or field convention for ELK | [ ] |
| V2-O-E | `.env.example` + docs; `.gitignore` for `.env` | [ ] |
| V2-O-R | Release tag `v0.2.0` after merge to `main` (per your release process) | [ ] |

- **PR:** 
- **Merged at / SHA on `develop`:** 

---

## Repository paths reference

| Theme | Likely touch points |
|-------|---------------------|
| Postgres / Flyway | `bank-boot/src/main/resources/application*.yaml`, Flyway scripts, `docker-compose` |
| Redis | `bank-iam`, `bank-payments`, `bank-boot` config |
| Kafka | `bank-notifications`, `bank-boot`, Compose |
| Security / CORS / limits | `bank-iam`, `bank-boot` |
| Logging | `bank-boot` `logback-spring.xml` (or equivalent), shared MDC filter |
| Env | repo root `.env.example`, `README.md`, `.gitignore` |
