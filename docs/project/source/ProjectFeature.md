---
features:
  - id: "feat-auth-jwt"
    title: "JWT authentication and refresh"
    description: "Register accepts email+password; login uses the same identifiers; RS256 access tokens plus opaque refresh tokens; logout invalidates refresh metadata and supports JWT blocklist semantics where implemented."
    icon: "🔐"
    category: "authentication"
    status: "stable"
    highlights:
      - "Public `/register`, `/login`, `/refresh` paths skip JWT filter per `SecurityWebPaths`"
      - "JWKS published at `/.well-known/jwks.json` for verifiers and API gateways"
      - "Strict Redis token-bucket rate limits on auth endpoints when rate limiting is enabled"
    techStack:
      - "JJWT"
      - "Spring Security 6"
      - "Redis"
    metrics:
      - label: "Auth endpoints"
        value: "5"
        trend: "stable"
        icon: "🎫"

  - id: "feat-accounts-ledger"
    title: "Accounts, balances, and ledger"
    description: "Customers open CHECKING/SAVINGS accounts; balances derive from ledger sums; paginated ledger exposes immutable DEBIT/CREDIT history scoped to the owning user."
    icon: "🏦"
    category: "database"
    status: "stable"
    highlights:
      - "Open account command maps to domain `AccountType` and ISO currency validation"
      - "Ledger queries return `PageResult` mapped to `LedgerPageResponse`"
      - "Transfer side-effects append paired ledger rows via domain services"
    techStack:
      - "Spring Data JPA"
      - "PostgreSQL"
      - "Flyway"
    codeSnippet:
      language: "java"
      filename: "OpenAccountRequest.java"
      code: |
        public record OpenAccountRequest(
            @NotNull AccountType type,
            @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "[A-Za-z]{3}") String currency) {}

  - id: "feat-payments-idempotent"
    title: "Idempotent fund transfers"
    description: "POST `/payments/transfers` requires `Idempotency-Key` (UUID). Handler records outcomes in Redis so safe retries from unstable mobile networks do not double-settle."
    icon: "💸"
    category: "api"
    status: "stable"
    highlights:
      - "422 Unprocessable Content with `ApiResponse` error envelope on domain failures"
      - "Reverse transfer endpoint mirrors idempotency requirements"
      - "Controller-level strict per-user rate limiting"
    techStack:
      - "Redis"
      - "Spring Web MVC"
    metrics:
      - label: "Transfer verbs"
        value: "2"
        trend: "stable"
        icon: "↔️"

  - id: "feat-loans-lifecycle"
    title: "Loan origination and repayments"
    description: "Originate amortizing loans against owned checking accounts, approve pending loans, fetch detail, and pay scheduled repayments with domain `Result` for failures."
    icon: "📜"
    category: "api"
    status: "stable"
    highlights:
      - "Sensitive-operation rate profile on writes"
      - "Separate read authority for `GET /loans/{id}`"
      - "Repayment payment maps to ledger movements through application services"
    techStack:
      - "Java 21 records"
      - "Spring validation"

  - id: "feat-audit-search"
    title: "Immutable audit search API"
    description: "Auditors query append-only audit records with filters for event type, actor, entity, and time window; results are pageable for export to SIEM or OpenSearch."
    icon: "🕵️"
    category: "security"
    status: "stable"
    highlights:
      - "Dedicated `audit:read` authority"
      - "Spring `Pageable` for CSV/Parquet export pipelines"
    techStack:
      - "PostgreSQL"
      - "Spring Data"

  - id: "feat-notifications-monitoring"
    title: "Notification delivery monitoring"
    description: "Operations users inspect notification records (status, channel) and a summarized pipeline health snapshot — ideal metrics source for CloudWatch dashboards."
    icon: "📬"
    category: "monitoring"
    status: "beta"
    highlights:
      - "Filters by `NotificationStatus` and `NotificationChannel` enums"
      - "Descending sort on `createdAt` by default"
    techStack:
      - "Spring MVC"
      - "Kafka (where notification dispatch uses broker)"

  - id: "feat-rate-limiting"
    title: "Redis token-bucket rate limiting"
    description: "Global servlet filter plus `@RateLimit` interceptor apply configurable profiles (STANDARD, STRICT, SENSITIVE_OPERATIONS) backed by Redis counters."
    icon: "🚦"
    category: "performance"
    status: "stable"
    highlights:
      - "`failOpen` defaults to true — Redis outages do not hard-fail traffic"
      - "Profiles map to capacities/refill rates in `RateLimitingProperties`"
    techStack:
      - "Redis"
      - "Spring Boot configuration properties"

  - id: "feat-spring-modulith"
    title: "Spring Modulith module boundaries"
    description: "Module structure is verified in build/tests so packages cannot accidentally create illegal dependencies between bounded contexts — practice for eventual ECS split."
    icon: "🧩"
    category: "integration"
    status: "stable"
    highlights:
      - "Documentation and tests from `spring-modulith-starter-test`"
      - "Event-based integration stays within JVM until MSK bridge is expanded"
    techStack:
      - "Spring Modulith 2.0.5"

  - id: "feat-openapi"
    title: "OpenAPI 3 and Swagger UI"
    description: "springdoc exposes `/v3/api-docs` and `/swagger-ui/**`, permitted anonymously for developer experience; on AWS, protect via IP allow list or VPN."
    icon: "📘"
    category: "api"
    status: "stable"
    highlights:
      - "`BankApiOperation` keys centralize operationIds"
      - "Works behind nginx `X-Forwarded-*` headers in Compose and ALB in AWS"
    techStack:
      - "springdoc-openapi 2.6.0"
---

# Project features

## Notes

- **Danger**: Swagger UI is **permitAll** in `SecurityConfig` — for a public internet deployment, front it with **VPN**, **IP allow list**, or **Basic auth at ALB** unless you intend a fully public API catalog.
- **Danger**: Rate limiting `failOpen=true` means attackers could theoretically bypass throttles during Redis incidents — weigh against `failOpen=false` for high-risk profiles only.
- **Good**: Payments + loans both surface **domain-shaped** errors via `ApiResponse.failure` instead of leaking stack traces to clients.
- **Missing**: Automated contract tests (Pact / Spring Cloud Contract) are not represented here yet; add a feature row when they exist.
- **Observation**: Notification feature marked **beta** until Kafka-enabled paths and DLQ handling are proven under load tests on MSK.
