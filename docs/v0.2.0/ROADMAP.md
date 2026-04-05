# Roadmap — release 0.2.0

This document captures **planned** improvements on top of the modular monolith delivered for **v0.1.0**. It does not replace domain rules in `.agents/domains.md`; it describes infrastructure and cross-cutting concerns.

**Baseline (today, v0.1.0):** Maven multi-module Spring Boot app, **H2** (PostgreSQL compatibility mode) with **Flyway** enabled, JWT and idempotency paths prepared with **Redis dependencies** but `bank.iam.redis.enabled` / `bank.payments.redis-idempotency` still **off** in default `application.yaml`. Inter-module integration uses **in-process `ApplicationEvent`s**.

**Target (v0.2.0):** Production-shaped persistence, optional messaging for notifications, hardened HTTP surface, observable logs, and secrets kept out of source control.

---

## 1. PostgreSQL + Flyway

- **Goal:** Use **PostgreSQL** as the primary datasource for non-test profiles (local Docker and shared dev/staging), with **Flyway** owning schema history (already on the classpath via `bank-boot`).
- **Scope:** Profile-specific `application-*.yaml` (e.g. `docker`, `prod`) with `jdbc:postgresql://…`, credentials from environment; CI integration tests may keep Testcontainers/H2 where appropriate.
- **Migrations:** Continue `V{n}__*.sql` under the existing Flyway location; validate PostgreSQL-specific types (e.g. JSONB for audit payloads) match what H2 test profile emulates or document deltas.
- **Acceptance:** `docker compose up postgres` + app profile connects, migrates cleanly, existing ITs still pass.

---

## 2. Redis (confirm and enable)

- **Goal:** Treat Redis as **ready for use**: refresh tokens, JWT blocklist, and payment **idempotency** backed by Redis in the profile that runs against real infra.
- **Scope:** Spring Data Redis configuration, connection from env (`spring.data.redis.*`), toggle defaults so **docker/local full stack** runs with Redis **on**; document fallbacks for pure H2 dev if needed.
- **Acceptance:** With Redis up, login/refresh/logout and idempotent `POST /transfers` behave as designed under load smoke tests.

---

## 3. Kafka for notifications

- **Goal:** Decouple notification **dispatch** from the request thread and from synchronous `ApplicationEvent` handling by publishing consumption triggers to **Kafka** (or consuming domain events published to a topic—choose one clear pattern).
- **Scope:** `bank-notifications` (and `bank-boot`): producer and/or consumer config, topic naming convention, serialization (JSON with schema version in headers or envelope), failure handling (DLQ or retry policy), local Compose service.
- **Acceptance:** A domain outcome still completes even if the broker is slow; notification side effects are eventually processed and reflected in `notification_log` (or documented metrics).

---

## 4. Spring Security — deeper configuration

- **Goal:** Explicit, reviewable security: multiple filter chains if needed (e.g. actuator vs API), method security defaults, session statelessness, JWT resource-server style documentation, optional **multiple issuers** or **opaque token** path only if required later.
- **Scope:** Central `SecurityFilterChain` beans in IAM/boot, extracted constants for public paths, tests for negative cases (wrong audience, expired, missing permission).
- **Acceptance:** Security configuration is readable in one place; integration tests cover main happy and deny paths.

---

## 5. CORS

- **Goal:** **Configurable** allowed origins, methods, and headers per environment (no `*` in production with credentials).
- **Scope:** `CorsConfigurationSource` bean or Spring Security `cors()` with properties such as `bank.http.cors.allowed-origins` from env.
- **Acceptance:** Browser clients from configured dev origins work; undisclosed origins receive no CORS approval in prod profile.

---

## 6. Rate limiting

- **Goal:** Protect auth and sensitive **POST** endpoints from abuse (per IP / per user / per route), aligned with `.agents/architecture.md` (Redis-backed counters).
- **Scope:** Filter or gateway-style component, 429 responses with `Retry-After` where practical, exemptions for health/actuator as needed.
- **Acceptance:** Load test shows threshold enforcement without breaking legitimate traffic patterns documented in README.

---

## 7. Application and audit logging (ELK-ready)

- **Goal:** **Structured logs** (JSON) suitable for **Elasticsearch** ingestion via Filebeat/Logstash: consistent fields, correlation IDs, log level policy.
- **Scope:** Logback (or Log4j2) JSON encoder, MDC population for `traceId`, `requestId`, `userId`, `module`; optional **separate audit stream** (e.g. dedicated logger appender for `AUDIT` events) with a stable schema for later ELK mapping.
- **Acceptance:** Sample log lines parse as JSON; audit entries include enough fields to search by `actorId`, `eventType`, `entityId` without logging secrets.

---

## 8. `.env` and sensitive configuration

- **Goal:** **No secrets in Git**; local and container runs load from **environment** or `.env` (Compose `env_file`, IDE run configs, or `export`).
- **Scope:** Document required variables (DB URL, Redis, Kafka, JWT keys, mail); add `.env.example` with dummy values; ensure `.gitignore` covers `.env`; Spring uses `SPRING_APPLICATION_JSON` or individual `BANK_*` / `SPRING_*` variables only.
- **Acceptance:** A new developer can copy `.env.example` → `.env`, start Compose, and run the app without editing committed YAML secrets.

---

## Suggested GitHub labels (optional)

Add labels such as `version:0.2.0`, `module:infra`, `module:notifications` for filtering. Reuse [v0.1.0/LABELS.md](../v0.1.0/LABELS.md) patterns.

---

## Related files

| Area | Location |
|------|----------|
| Implementation checklist | [TRACKER.md](TRACKER.md) |
| Architecture (updated for 0.2.0 direction) | [.agents/architecture.md](../../.agents/architecture.md) |
| Agent entrypoint | [.agents/agent.md](../../.agents/agent.md) |
