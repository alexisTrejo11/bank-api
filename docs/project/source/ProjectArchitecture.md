---
layers:
  - name: "Presentation (API)"
    description: "REST controllers per domain module — no business logic, only DTO mapping and handler delegation."
    color: "#6366F1"
    expanded: true
    components:
      - "AuthController — /api/v1/auth"
      - "AccountController — /api/v1/accounts"
      - "TransferController — /api/v1/payments"
      - "LoanController — /api/v1/loans"
      - "AuditController — /api/v1/audit"
      - "NotificationMonitoringController — /api/v1/notifications/monitoring"
    responsibilities:
      - "Validate request DTOs (@Valid)"
      - "Map domain results to ApiResponse<T>"
      - "Apply @RateLimit and OpenAPI annotations"
    technologies:
      - "Spring Web MVC"
      - "springdoc-openapi"
      - "Jakarta Validation"

  - name: "Application layer"
    description: "Command/query handlers orchestrate use cases, publish domain events, and coordinate transactions."
    color: "#F59E0B"
    expanded: true
    components:
      - "Command handlers (InitiateTransferHandler, OriginateLoanHandler, …)"
      - "Query handlers (GetAccountBalanceHandler, SearchAuditRecordsHandler, …)"
      - "ApplicationEventPublisher for cross-module events"
    responsibilities:
      - "Transaction boundaries (@Transactional)"
      - "Idempotency checks before side effects"
      - "Publish BankDomainEvent after commit"
    technologies:
      - "Spring @Component handlers"
      - "Spring Modulith event API"

  - name: "Domain layer"
    description: "Pure Java aggregates, value objects, and port interfaces — zero Spring/JPA dependencies."
    color: "#10B981"
    expanded: true
    components:
      - "Money, AccountId, UserId, TransferId (bank-shared)"
      - "Transfer state machine, Loan amortization rules"
      - "port.in / port.out repository interfaces"
    responsibilities:
      - "Enforce invariants (balance from ledger, transfer states)"
      - "Return Result<T> for expected failures"
    technologies:
      - "Java 21 records"
      - "BigDecimal financial math"

  - name: "Infrastructure layer"
    description: "JPA adapters, Redis stores, Kafka producers/consumers, and external notification stubs."
    color: "#EF4444"
    expanded: true
    components:
      - "JPA entities + repository adapters"
      - "RedisRefreshTokenStore, TransferIdempotencyCache"
      - "KafkaNotificationDispatchIngress"
      - "@TransactionalEventListener cross-module listeners"
    responsibilities:
      - "Implement domain ports"
      - "Map entities ↔ domain models"
      - "Bridge to RDS, Upstash Redis, cloud Kafka"
    technologies:
      - "Spring Data JPA"
      - "Spring Data Redis"
      - "Spring Kafka"

  - name: "Data & messaging (AWS)"
    description: "External managed services — not bundled in production compose."
    color: "#8B5CF6"
    expanded: true
    components:
      - "Amazon RDS PostgreSQL 16"
      - "Upstash Redis (tokens, idempotency, rate limits)"
      - "Cloud Kafka broker (notification dispatch + pipeline topics)"
    responsibilities:
      - "ACID persistence via Flyway-managed schema"
      - "Shared state across EC2 app restarts"
      - "Decouple notification enqueue from HTTP thread"
    technologies:
      - "Flyway"
      - "PostgreSQL"
      - "Redis"
      - "Kafka"

  - name: "Observability (EC2)"
    description: "External monitoring stack running alongside or on the same EC2 host."
    color: "#64748B"
    expanded: false
    components:
      - "Prometheus (scrapes /actuator/prometheus)"
      - "Grafana (dashboards, Prometheus + Loki datasources)"
      - "Loki + Promtail (app logs from /app/logs volume)"
      - "Optional ELK profile (Logstash → Elasticsearch → Kibana)"
    responsibilities:
      - "Metrics, dashboards, log aggregation"
      - "audit.json and access.json structured files"
    technologies:
      - "Micrometer"
      - "Logback JSON"
      - "Prometheus / Grafana / Loki"

designPatterns:
  - title: "Modular monolith"
    emoji: "🧩"
    description: "Nine Maven modules with package-level boundaries; any module extractable to microservice by swapping ApplicationEvent for Kafka/SQS."
    category: "Structural"
    badge: "Core"
  - title: "Hexagonal (ports & adapters)"
    emoji: "⬡"
    description: "domain/port.out interfaces implemented in infrastructure/persistence/adapter — domain never imports JPA."
    category: "Structural"
    badge: "DDD"
  - title: "CQRS-lite"
    emoji: "📋"
    description: "Separate command and query handlers with dedicated Command/Query records."
    category: "Behavioral"
    badge: "Application"
  - title: "Domain events"
    emoji: "📡"
    description: "TransferCompletedEvent, LoanDisbursedEvent, etc. in bank-shared; listeners in accounts, audit, notifications."
    category: "Behavioral"
    badge: "Events"
  - title: "Result<T> for failures"
    emoji: "✅"
    description: "Expected domain failures (INSUFFICIENT_FUNDS) return Result.failure instead of exceptions for control flow."
    category: "Behavioral"
    badge: "API"
  - title: "Idempotency key"
    emoji: "🔑"
    description: "Redis + DB dedup for payment mutations — safe retries from mobile/unstable networks."
    category: "Integration"
    badge: "Payments"

scalabilityStrategies:
  - title: "Stateless API container"
    description: "JWT + external Redis — scale horizontally with additional EC2 instances behind ALB; shared Upstash Redis for tokens and idempotency."
  - title: "RDS PostgreSQL"
    description: "Managed backups and storage; connection pooling via HikariCP; Flyway migrations on deploy."
  - title: "Kafka notification pipeline"
    description: "Decouple notification dispatch from request thread; consumer group on cloud Kafka handles burst traffic."
  - title: "Rate limiting fail-open"
    description: "Redis token bucket protects auth and payments; configurable fail-open if Redis unreachable (logged, not silent)."

securityStrategies:
  - title: "RS256 JWT + permissions"
    description: "Spring Security filter chain; @PreAuthorize on sensitive endpoints; permissions[] in JWT claims."
  - title: "Refresh rotation + blocklist"
    description: "Old refresh invalidated on rotate; logout adds jti to Redis blocklist for remaining access token TTL."
  - title: "Append-only audit"
    description: "DB trigger prevents audit record mutation; full event JSON in payload for reconstruction."
  - title: "Idempotency on writes"
    description: "Payments require Idempotency-Key; prevents duplicate transfers on client retries."
  - title: "CORS configuration"
    description: "bank.http.cors.allowed-origins from env — empty by default; set explicitly for frontends."

cacheStrategies:
  - name: "IAM refresh tokens"
    description: "Redis key iam:refresh:{sha256(token)} → userId, TTL 7 days"
    ttl: "7 days"
    coverage: "Refresh token validation and rotation"
  - name: "JWT blocklist"
    description: "Redis key blocklist:{jti} for revoked access tokens"
    ttl: "Remaining token lifetime"
    coverage: "Logout and suspend flows"
  - name: "Transfer idempotency"
    description: "Cached Result<TransferResponse> per userId + idempotencyKey"
    ttl: "24h"
    coverage: "POST /payments/transfers and /reverse"
  - name: "Rate limit buckets"
    description: "Lua token-bucket script — atomic refill + consume per IP/user key"
    ttl: "Rolling window"
    coverage: "Auth, payments, loans sensitive ops"

architectureFeatures:
  - title: "Spring Modulith"
    emoji: "📦"
    description: "Formal module boundaries with event-based integration; supports future extraction without package rewrites."
  - title: "Flyway migrations"
    emoji: "🗃️"
    description: "15 versioned SQL migrations + repeatable seed; ddl-auto=validate in production."
  - title: "Standard ApiResponse"
    emoji: "📨"
    description: "Uniform { data, meta, errors } JSON for all REST endpoints."
  - title: "Actuator observability"
    emoji: "📊"
    description: "health, info, prometheus endpoints exposed per MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE."

architectureDiagram:
  legendItems:
    - type: "client"
      label: "Client"
      color: "#6366F1"
      icon: "monitor"
    - type: "gateway"
      label: "Gateway"
      color: "#10B981"
      icon: "shield"
    - type: "service"
      label: "API service"
      color: "#F59E0B"
      icon: "server"
    - type: "database"
      label: "Database"
      color: "#EF4444"
      icon: "database"
    - type: "queue"
      label: "Queue / cache"
      color: "#8B5CF6"
      icon: "layers"
    - type: "monitoring"
      label: "Monitoring"
      color: "#64748B"
      icon: "activity"

  nodes:
    - id: "client"
      label: "API clients / Postman"
      type: "client"
      x: 80
      y: 120
      connections: ["ec2-nginx"]
      status: "healthy"
      traffic: 50
    - id: "ec2-nginx"
      label: "EC2 — Nginx (optional)"
      type: "gateway"
      x: 280
      y: 120
      connections: ["bank-app"]
      status: "healthy"
      traffic: 50
    - id: "bank-app"
      label: "Bank API (Docker on EC2)"
      type: "service"
      x: 480
      y: 120
      connections: ["rds", "upstash", "kafka", "prometheus"]
      status: "healthy"
      traffic: 45
    - id: "rds"
      label: "Amazon RDS PostgreSQL"
      type: "database"
      x: 700
      y: 60
      connections: []
      status: "healthy"
      traffic: 40
    - id: "upstash"
      label: "Upstash Redis"
      type: "queue"
      x: 700
      y: 180
      connections: []
      status: "healthy"
      traffic: 35
    - id: "kafka"
      label: "Cloud Kafka broker"
      type: "queue"
      x: 480
      y: 260
      connections: ["bank-app"]
      status: "healthy"
      traffic: 20
    - id: "prometheus"
      label: "Prometheus (EC2)"
      type: "monitoring"
      x: 280
      y: 260
      connections: ["grafana"]
      status: "healthy"
      traffic: 10
    - id: "grafana"
      label: "Grafana + Loki (EC2)"
      type: "monitoring"
      x: 80
      y: 260
      connections: []
      status: "healthy"
      traffic: 8

  connections:
    - id: "c1"
      from: "client"
      to: "ec2-nginx"
      label: "HTTPS"
      protocol: "TLS"
      isActive: true
    - id: "c2"
      from: "ec2-nginx"
      to: "bank-app"
      label: "Proxy :8080"
      protocol: "HTTP"
      isActive: true
    - id: "c3"
      from: "bank-app"
      to: "rds"
      label: "JDBC / Flyway"
      protocol: "PostgreSQL"
      isActive: true
    - id: "c4"
      from: "bank-app"
      to: "upstash"
      label: "Tokens / idempotency / limits"
      protocol: "Redis TLS"
      isActive: true
    - id: "c5"
      from: "bank-app"
      to: "kafka"
      label: "Notification dispatch"
      protocol: "Kafka"
      isActive: true
    - id: "c6"
      from: "kafka"
      to: "bank-app"
      label: "Consumer group"
      protocol: "Kafka"
      isActive: true
    - id: "c7"
      from: "prometheus"
      to: "bank-app"
      label: "Scrape metrics"
      protocol: "HTTP /actuator/prometheus"
      isActive: true
    - id: "c8"
      from: "grafana"
      to: "prometheus"
      label: "Dashboards"
      protocol: "HTTP"
      isActive: true

dataFlow:
  requestFlow:
    - number: 1
      title: "HTTP request"
      description: "Client sends REST call with Bearer JWT (except public auth routes)."
      icon: "send"
    - number: 2
      title: "Security + rate limit"
      description: "JwtAuthenticationFilter validates RS256 token; GlobalRateLimitFilter and @RateLimit check Redis token bucket."
      icon: "filter"
    - number: 3
      title: "Handler execution"
      description: "Controller delegates to application handler; domain rules enforced; @Transactional wraps persistence."
      icon: "cog"
    - number: 4
      title: "Persistence & cache"
      description: "JPA writes to RDS; Redis updated for idempotency or rate counters."
      icon: "database"
    - number: 5
      title: "Events & response"
      description: "AFTER_COMMIT listeners update ledger, audit, notifications; ApiResponse JSON returned."
      icon: "reply"

  eventFlow:
    - number: 1
      title: "Domain event published"
      description: "e.g. TransferCompletedEvent after successful transfer commit."
      icon: "zap"
    - number: 2
      title: "Cross-module listeners"
      description: "accounts posts ledger entries; audit appends AuditRecord; notifications builds DispatchNotificationCommand."
      icon: "inbox"
    - number: 3
      title: "Kafka enqueue (AWS)"
      description: "When dispatch-mode=kafka, KafkaNotificationDispatchIngress publishes to bank.notifications.dispatch topic on cloud broker."
      icon: "upload"
    - number: 4
      title: "Notification delivery"
      description: "Kafka consumer processes pipeline; email/SMS sent or stubbed; status logged in notification_log table."
      icon: "mail"

techDecisions:
  - title: "Modular monolith vs microservices"
    problem: "Portfolio project needs credible domain boundaries without Kubernetes operational cost."
    solution: "Maven multi-module monolith with ApplicationEvent + optional Kafka; single Docker image on EC2."
    alternatives:
      - "Microservice per bank-* module"
      - "Serverless Lambda per endpoint"
    outcome: "Fast iteration locally; clean extraction path documented in .agents/architecture.md."
    icon: "layers"
  - title: "PostgreSQL on RDS"
    problem: "Financial data requires ACID, relational integrity, and append-only audit with triggers."
    solution: "Amazon RDS PostgreSQL 16; Flyway migrations; Hibernate ddl-auto=validate."
    alternatives:
      - "H2 (local dev only)"
      - "Document store for ledger"
    outcome: "Production-grade persistence; same SQL dialect in local compose.local.yml."
    icon: "database"
  - title: "Upstash Redis"
    problem: "JWT refresh, blocklist, idempotency, and rate limits need shared state across EC2 restarts."
    solution: "External Upstash Redis — not containerized in production compose.yml."
    alternatives:
      - "In-container Redis"
      - "Database-only idempotency"
    outcome: "Survives container redeploys; works with multiple EC2 instances behind ALB."
    icon: "redis"
  - title: "Cloud Kafka for notifications"
    problem: "Notification dispatch should not block HTTP threads or be lost on app restart."
    solution: "Kafka broker on cloud instance; BANK_KAFKA_ENABLED=true and dispatch-mode=kafka in docker profile."
    alternatives:
      - "Direct ApplicationEvent only (v0.1.0 default)"
      - "Amazon SQS"
    outcome: "Same code path testable locally via compose.local.yml kafka service."
    icon: "kafka"
  - title: "EC2 observability stack"
    problem: "Need metrics and logs without managed CloudWatch-only setup for portfolio demo."
    solution: "Prometheus scrapes app on EC2; Grafana dashboards; Loki for log aggregation; optional ELK profile."
    alternatives:
      - "CloudWatch only"
      - "Datadog SaaS"
    outcome: "Full visibility documented in docker/MONITORING.md and docs/OBSERVABILITY.md."
    icon: "activity"
---

# Architecture

> **AWS alignment:** Production runs `docker/compose.yml` (app only) on EC2. RDS, Upstash Redis, and cloud Kafka are reached via `.env` — see `.env.example` AWS comment block.

> **Local parity:** `docker/compose.local.yml` bundles postgres, redis, kafka, prometheus, grafana, loki, nginx so developers exercise the same integration paths before deploy.

> **Important:** Domain modules communicate via `ApplicationEvent` (AFTER_COMMIT) and optionally Kafka for notifications — never direct imports of another module's JPA entities.

> **Warning:** Ephemeral JWT keys when PEM env vars unset — multi-instance EC2 deploy requires `BANK_SECURITY_JWT_PRIVATE_KEY_PEM` and `BANK_SECURITY_JWT_PUBLIC_KEY_PEM`.
