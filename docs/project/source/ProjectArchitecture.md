---
layers:
  - name: "Presentation (adapters in)"
    description: "Spring Web MVC controllers, request/response DTOs, validation, and OpenAPI annotations. Maps HTTP to application commands/queries."
    color: "#1E88E5"
    expanded: true
    components:
      - "AuthController (/api/v1/auth)"
      - "AccountController (/api/v1/accounts)"
      - "TransferController (/api/v1/payments)"
      - "LoanController (/api/v1/loans)"
      - "AuditController (/api/v1/audit)"
      - "NotificationMonitoringController (/api/v1/notifications/monitoring)"
    responsibilities:
      - "Translate JSON bodies to immutable records and to application-layer commands."
      - "Return ApiResponse<T> envelopes and appropriate HTTP status codes (e.g. 422 for domain failures on transfers)."
    technologies:
      - "Spring Web MVC"
      - "Spring Security (method security via authorities on URL patterns)"
      - "Jakarta Validation"
      - "springdoc-openapi"

  - name: "Application (use cases)"
    description: "Command/query handlers orchestrate domain services and ports. Transaction boundaries live here or on handlers via @Transactional."
    color: "#43A047"
    expanded: true
    components:
      - "OpenAccountHandler, PostTransferToLedgerHandler, loan handlers, payment handlers, IAM handlers"
      - "Query handlers: ledger, balance, audit search, loan detail, notification lists"
    responsibilities:
      - "Enforce use-case-level rules before touching aggregates."
      - "Publish domain or integration events after persistence when required."
    technologies:
      - "Spring @Component / constructor injection"
      - "Spring Modulith event APIs (where used)"

  - name: "Domain"
    description: "Entities, value objects, domain exceptions, and repository interfaces. No Spring Web types."
    color: "#6D4C41"
    expanded: false
    components:
      - "BankAccount, LedgerEntry, LoanAggregate, audit and notification domain models"
      - "AccountRepository, LedgerEntryRepository, etc. (interfaces)"
    responsibilities:
      - "Express invariants (e.g. currency match on transfer, account active)."
      - "Stay free of infrastructure details."
    technologies:
      - "Plain Java 21"

  - name: "Infrastructure (adapters out)"
    description: "JPA entities, Spring Data repositories, Kafka listeners, Redis-backed services, Flyway."
    color: "#8E24AA"
    expanded: false
    components:
      - "Persistence adapters under each module’s infrastructure package"
      - "JwtAuthenticationFilter, SecurityConfig (bank-config)"
      - "Redis token bucket rate limiting (bank-boot)"
    responsibilities:
      - "Map domain models to tables and external brokers."
      - "Implement technical cross-cutting (JWT parsing, rate limit keys)."
    technologies:
      - "Spring Data JPA"
      - "PostgreSQL driver"
      - "Spring Data Redis"
      - "Spring Kafka"

designPatterns:
  - title: "Hexagonal (ports & adapters)"
    emoji: "⬡"
    description: "Domain defines repository ports; infrastructure supplies JPA-backed implementations. Web layer depends on application handlers, not on repositories directly."
    category: "Architecture"
    badge: "core"
  - title: "CQRS-style split"
    emoji: "↔"
    description: "Commands (write) and queries (read) use separate handler types and DTOs, keeping read models optimized for APIs without overloading aggregates."
    category: "Application"
    badge: "patterns"
  - title: "Transactional outbox / domain events (Spring)"
    emoji: "📨"
    description: "Cross-module work uses Spring application events; listeners that must see committed data should align with AFTER_COMMIT semantics where configured."
    category: "Integration"
    badge: "events"
  - title: "Modular monolith"
    emoji: "🧱"
    description: "Spring Modulith enforces module boundaries in documentation and tests so a future split to multiple ECS services is a deployment change, not a rewrite."
    category: "Modulith"
    badge: "spring-modulith"

scalabilityStrategies:
  - title: "Horizontal scale of stateless API tier"
    description: "ECS service desired count > 1 behind ALB; JWTs are self-contained; Redis holds refresh/idempotency state shared across tasks."
  - title: "Read scaling"
    description: "Ledger and audit queries are pageable; RDS read replicas can back heavy read workloads after connection routing is introduced."
  - title: "Kafka for cross-boundary fan-out"
    description: "MSK replaces embedded broker usage from Compose; consumers can be scaled independently for notifications and analytics."

securityStrategies:
  - title: "RS256 JWT + authority checks"
    description: "Asymmetric keys; `SecurityConfig` maps each banking route pattern to Spring Security authorities derived from JWT claims."
  - title: "Rate limiting"
    description: "Optional Redis token buckets: global filter plus `@RateLimit` profiles on sensitive controllers."
  - title: "Defense in depth on AWS"
    description: "Private subnets for ECS tasks, security groups allowing only ALB → task port, Secrets Manager for signing keys, and VPC endpoints to reduce public data-plane exposure."

cacheStrategies:
  - name: "Idempotency outcome cache"
    description: "Payment commands require `Idempotency-Key`; outcomes are cached in Redis to deduplicate retries (TTL aligned with product policy)."
    ttl: "24h (typical product default — confirm in payments module configuration)"
    coverage: "POST /api/v1/payments/transfers and reverse"
  - name: "JWT refresh / blocklist metadata"
    description: "Refresh tokens and revocation lists backed by Redis for fast lookups on every authenticated request path."
    ttl: "7d refresh TTL (illustrative — verify IAM properties)"
    coverage: "Authentication and logout flows"
  - name: "Rate limit buckets"
    description: "Per-IP and per-user token buckets stored in Redis when `bank.rate-limiting.enabled=true`."
    ttl: "n/a (sliding token bucket, not a classical TTL cache)"
    coverage: "/api/** when rate limiting enabled"

architectureFeatures:
  - title: "Single deployable, many bounded contexts"
    emoji: "📦"
    description: "Operational simplicity today; optional extraction of loans/payments workers to separate ECS services later."
  - title: "Flyway as schema authority"
    emoji: "🪶"
    description: "Database migrations live with the application lifecycle; RDS upgrades coordinated with migration compatibility."
  - title: "Observability hooks"
    emoji: "📈"
    description: "Actuator exposes health and metrics; in AWS, scrape via ADOT or CloudWatch agent instead of only Prometheus sidecars."

architectureDiagram:
  legendItems:
    - type: "client"
      label: "Client / partner API"
      color: "#90CAF9"
      icon: "💻"
    - type: "gateway"
      label: "ALB / API front door"
      color: "#FFCC80"
      icon: "🚪"
    - type: "service"
      label: "ECS Fargate (bank-boot)"
      color: "#A5D6A7"
      icon: "⚙️"
    - type: "database"
      label: "RDS PostgreSQL"
      color: "#CE93D8"
      icon: "🐘"
    - type: "queue"
      label: "MSK Kafka"
      color: "#FFF59D"
      icon: "📨"
    - type: "monitoring"
      label: "CloudWatch / observability"
      color: "#B0BEC5"
      icon: "📊"
  nodes:
    - id: "client"
      label: "HTTPS clients"
      type: "client"
      x: 80
      y: 120
      status: "healthy"
      traffic: 100
    - id: "alb"
      label: "Application Load Balancer"
      type: "gateway"
      x: 280
      y: 120
      status: "healthy"
      traffic: 100
    - id: "ecs"
      label: "ECS service (bank-api tasks)"
      type: "service"
      x: 480
      y: 120
      status: "healthy"
      traffic: 95
    - id: "rds"
      label: "RDS PostgreSQL"
      type: "database"
      x: 520
      y: 280
      status: "healthy"
      traffic: 60
    - id: "redis"
      label: "ElastiCache Redis"
      type: "database"
      x: 320
      y: 280
      status: "healthy"
      traffic: 55
    - id: "msk"
      label: "MSK cluster"
      type: "queue"
      x: 720
      y: 280
      status: "healthy"
      traffic: 40
    - id: "cw"
      label: "CloudWatch"
      type: "monitoring"
      x: 120
      y: 280
      status: "healthy"
      traffic: 20
  connections:
    - id: "c1"
      from: "client"
      to: "alb"
      label: "HTTPS"
      protocol: "TLS"
      isActive: true
    - id: "c2"
      from: "alb"
      to: "ecs"
      label: "HTTP"
      protocol: "HTTP/1.1"
      isActive: true
    - id: "c3"
      from: "ecs"
      to: "rds"
      label: "JDBC"
      protocol: "TCP"
      isActive: true
    - id: "c4"
      from: "ecs"
      to: "redis"
      label: "Redis protocol"
      protocol: "TCP"
      isActive: true
    - id: "c5"
      from: "ecs"
      to: "msk"
      label: "produce/consume"
      protocol: "Kafka"
      isActive: true
    - id: "c6"
      from: "ecs"
      to: "cw"
      label: "metrics/logs"
      protocol: "OTLP / CW API"
      isActive: true

dataFlow:
  requestFlow:
    - number: 1
      title: "TLS at ALB"
      description: "Client hits ACM certificate on ALB; optional AWS WAF inspection for OWASP rules."
      icon: "🔐"
    - number: 2
      title: "JWT authentication filter"
      description: "Unless path is in allowlist (register/login/refresh, swagger, JWKS), JwtAuthenticationFilter validates Bearer JWT and builds the security context."
      icon: "🎫"
    - number: 3
      title: "Controller → handler"
      description: "Controller builds command/query object; application handler enforces rules and calls domain repositories."
      icon: "🎯"
    - number: 4
      title: "Transaction commit"
      description: "Successful flush to PostgreSQL commits the unit of work; failures roll back and skip downstream listeners that depend on commit."
      icon: "💾"
    - number: 5
      title: "Response envelope"
      description: "ApiResponse wraps success payload or standardized error codes for API consumers and BFFs."
      icon: "📤"
  eventFlow:
    - number: 1
      title: "Domain publishes application events"
      description: "Handlers publish Spring application events representing something that already happened in the domain."
      icon: "📣"
    - number: 2
      title: "After-commit listeners"
      description: "Listeners for audit, ledger posting, or notifications should use transactional event semantics so they never observe phantom reads from rolled-back work."
      icon: "⏱️"
    - number: 3
      title: "Kafka (MSK) for durable fan-out"
      description: "Where enabled, notifications or analytics can consume from MSK topics populated by producers in the monolith or future outbox workers."
      icon: "📨"
    - number: 4
      title: "Downstream idempotent consumers"
      description: "Consumers deduplicate by business keys or message IDs to stay safe under at-least-once delivery."
      icon: "♻️"

techDecisions:
  decisions:
    - title: "Modular monolith vs microservices day one"
      problem: "Small team needs correctness and velocity; microservices add network and ops overhead."
      solution: "Single Spring Boot binary with enforced module boundaries (Spring Modulith) and clear packages per bounded context."
      alternatives:
        - "Full microservices mesh from day zero"
        - "Unstructured monolith without module rules"
      outcome: "Faster delivery with an extraction path to ECS services per context when load or team boundaries justify it."
      icon: "⚖️"
    - title: "PostgreSQL everywhere"
      problem: "Financial data needs ACID and strong consistency for ledger rows."
      solution: "PostgreSQL as the system of record with Flyway migrations versioned with the app."
      alternatives:
        - "NoSQL primary store for balances"
        - "Multi-database polyglot persistence"
      outcome: "Simpler operations and straightforward RDS HA patterns (Multi-AZ)."
      icon: "🐘"
    - title: "Redis for cross-request coordination"
      problem: "Idempotency, refresh sessions, JWT revocation metadata, and rate limits need sub-millisecond shared state."
      solution: "ElastiCache Redis cluster accessed from all ECS tasks."
      alternatives:
        - "DynamoDB for idempotency only"
        - "In-memory only (breaks multi-task correctness)"
      outcome: "Predictable deduplication and throttling semantics at scale."
      icon: "💾"
---

# Architecture

## Notes

- **Danger**: The diagram is **logical** — coordinates are for rendering only, not production network topology.
- **Danger**: `SecurityConfig` permits **all** `/actuator/**` traffic on its own filter chain; treat actuator as sensitive infrastructure surface area on AWS.
- **Good**: `PostTransferToLedgerHandler` shows the **paired** `DEBIT`/`CREDIT` `LedgerEntry` construction in one transaction — the invariant to preserve when refactoring.
- **Missing**: Explicit **outbox table** is not described here; if you move to MSK-only integration, consider outbox pattern for exactly-once publication semantics.
- **Observation**: Align listener `@TransactionalEventListener` phases in code review whenever a new integration listener is added — this is the main foot-gun for “ghost side-effects.”
- **Observation**: `bank-config` uses package `io.github.alexisTrejo11` (typo casing) — harmless but confusing when navigating; fix in a dedicated hygiene PR.
