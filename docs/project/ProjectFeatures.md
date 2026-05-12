# Project features

Derived from [`source/ProjectFeature.md`](source/ProjectFeature.md). Each row is one portfolio **feature** with category, status, and highlights.

## Summary table

| ID | Title | Category | Status | Icon |
|----|-------|----------|--------|------|
| feat-auth-jwt | JWT authentication and refresh | authentication | stable | 🔐 |
| feat-accounts-ledger | Accounts, balances, and ledger | database | stable | 🏦 |
| feat-payments-idempotent | Idempotent fund transfers | api | stable | 💸 |
| feat-loans-lifecycle | Loan origination and repayments | api | stable | 📜 |
| feat-audit-search | Immutable audit search API | security | stable | 🕵️ |
| feat-notifications-monitoring | Notification delivery monitoring | monitoring | **beta** | 📬 |
| feat-rate-limiting | Redis token-bucket rate limiting | performance | stable | 🚦 |
| feat-spring-modulith | Spring Modulith module boundaries | integration | stable | 🧩 |
| feat-openapi | OpenAPI 3 and Swagger UI | api | stable | 📘 |

---

## feat-auth-jwt — JWT authentication and refresh

**Description:** Register accepts **email + password**; login uses the same identifiers; **RS256** access tokens plus **opaque** refresh tokens; logout invalidates refresh metadata and supports JWT blocklist behavior where implemented.

**Highlights**

- Public `/register`, `/login`, `/refresh` skip JWT filter per `SecurityWebPaths`.  
- JWKS at `/.well-known/jwks.json` for verifiers and API gateways.  
- Strict Redis token-bucket limits on auth endpoints when rate limiting is enabled.

**Tech:** JJWT, Spring Security 6, Redis.  
**Metric:** Auth endpoints — **5**.

---

## feat-accounts-ledger — Accounts, balances, and ledger

**Description:** Customers open **CHECKING** / **SAVINGS** accounts; balances derive from **ledger sums**; paginated ledger exposes immutable **DEBIT/CREDIT** history for the owning user.

**Highlights**

- Open account maps to `AccountType` and ISO currency validation.  
- Ledger queries use `PageResult` → `LedgerPageResponse`.  
- Transfers append paired ledger rows via application/domain services.

**Tech:** Spring Data JPA, PostgreSQL, Flyway.

**Snippet (`OpenAccountRequest.java`):**

```java
public record OpenAccountRequest(
    @NotNull AccountType type,
    @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "[A-Za-z]{3}") String currency) {}
```

---

## feat-payments-idempotent — Idempotent fund transfers

**Description:** `POST /api/v1/payments/transfers` requires header **`Idempotency-Key`** (UUID). Handler stores outcomes in **Redis** so retries do not double-settle.

**Highlights**

- **422** with `ApiResponse` failure envelope on domain errors.  
- Reverse transfer endpoint mirrors idempotency.  
- Controller-level **strict** per-user rate limiting.

**Tech:** Redis, Spring Web MVC.  
**Metric:** Transfer verbs — **2**.

---

## feat-loans-lifecycle — Loan origination and repayments

**Description:** Originate amortizing loans against owned checking accounts, approve, fetch detail, pay installments; failures use domain **`Result`**.

**Highlights**

- Sensitive-operation rate profile on writes.  
- Separate read authority for `GET /api/v1/loans/{id}`.  
- Repayments tie into ledger through application services.

**Tech:** Java 21 records, Spring validation.

---

## feat-audit-search — Immutable audit search API

**Description:** Auditors query append-only records with filters (event type, actor, entity, time window); pageable for SIEM / OpenSearch export.

**Highlights**

- `audit:read` authority.  
- Spring `Pageable` for export pipelines.

**Tech:** PostgreSQL, Spring Data.

---

## feat-notifications-monitoring — Notification delivery monitoring

**Description:** Operations inspect notification records (**status**, **channel**) and a **summary** snapshot — good CloudWatch dashboard inputs.

**Highlights**

- Filters by `NotificationStatus` and `NotificationChannel`.  
- Default sort `createdAt` descending.

**Tech:** Spring MVC, Kafka where dispatch uses broker.  
**Status:** **beta** until MSK load tests and DLQ story are proven.

---

## feat-rate-limiting — Redis token-bucket rate limiting

**Description:** Global servlet filter plus `@RateLimit` interceptor; profiles **STANDARD**, **STRICT**, **SENSITIVE_OPERATIONS**.

**Highlights**

- **`failOpen` defaults to true** — Redis outage does not hard-block traffic.  
- Capacities/refill rates from `RateLimitingProperties`.

**Tech:** Redis, Spring Boot `@ConfigurationProperties`.

---

## feat-spring-modulith — Spring Modulith module boundaries

**Description:** Build/tests verify legal module dependencies — rehearsal for splitting to multiple ECS services.

**Highlights**

- `spring-modulith-starter-test` documentation and tests.  
- In-process events until MSK bridge expands.

**Tech:** Spring Modulith **2.0.5**.

---

## feat-openapi — OpenAPI 3 and Swagger UI

**Description:** springdoc exposes `/v3/api-docs` and `/swagger-ui/**`, **permitAll** for DX — on AWS, protect with VPN, IP allow list, or ALB rule.

**Highlights**

- `BankApiOperation` keys for stable operationIds.  
- Works behind nginx / ALB `X-Forwarded-*` headers.

**Tech:** springdoc-openapi **2.6.0**.

---

## Cross-feature notes

- **Risk:** Swagger **permitAll** — do not leave a production ALB wide open without additional controls.  
- **Risk:** `failOpen=true` on rate limits — throttles may disappear if Redis is down; evaluate per profile.  
- **Good:** Payments and loans return **domain-shaped** errors via `ApiResponse.failure`.  
- **Gap:** No Pact / Spring Cloud Contract row yet.  
- **Beta:** Notification monitoring until Kafka + DLQ path is load-tested on MSK.

## Related

- [APISchema.md](APISchema.md) — REST mapping  
- [ProjectArchitectureModel.md](ProjectArchitectureModel.md) — how features sit in layers  
