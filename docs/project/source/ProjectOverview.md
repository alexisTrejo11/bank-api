---
problemStatement:
  problemTitle: "Need a compliant, evolvable banking core without premature microservices"
  problemDescription: "Core banking behaviors—authentication, accounts, immutable audit, payments with idempotency, loans, and operational visibility—must stay consistent under failure and retries. The codebase should stay testable and modular so bounded contexts can later split out, without running nine deployables on day one."
  problemList:
    - "Financial writes need atomicity and clear ordering of side-effects (no notifications or audit rows after a rolled-back transaction)."
    - "HTTP retries must not double-post money movements; idempotency and ledger rules must be first-class."
    - "Regulators and operators expect append-only audit trails and searchable evidence, not ad hoc logs only."
    - "Teams want one deployable for velocity, but explicit module seams for a future AWS split (ECS services per context, MSK topics, etc.)."

solution:
  solutionTitle: "Modular monolith on Spring Boot with hexagonal-style ports and domain events"
  solutionList:
    - title: "Single runtime, multiple Maven modules"
      description: "Nine modules compile into one Spring Boot JAR (`bank-boot`). Spring Modulith documents and enforces module relationships so APIs do not degenerate into a big ball of mud."
    - title: "Double-entry ledger and derived balances"
      description: "Account mutations persist balanced DEBIT/CREDIT `LedgerEntry` rows; balances are derived from ledger sums rather than a silently mutable balance column alone."
    - title: "After-commit domain integration"
      description: "Cross-module reactions use Spring’s application events and transactional boundaries so consumers run only after successful commits where configured (see architecture doc)."
    - title: "JWT session model with Redis"
      description: "RS256 JWT access tokens, refresh rotation, and Redis-backed revocation metadata support stateless APIs suitable for ALB → ECS."
    - title: "AWS-shaped operations story"
      description: "Production is documented as ECS Fargate tasks behind an ALB, RDS PostgreSQL, ElastiCache Redis, and MSK Kafka—mirroring what Docker Compose approximates locally."

keyMetrics:
  metricsTitle: "Snapshot metrics (code + docs)"
  metricsList:
    - "9 Maven modules (including bank-config and bank-boot)"
    - "17 HTTP JSON endpoints under /api/v1 (plus actuator and OpenAPI static routes)"
    - "Spring Boot 4.0.5 / Java 21"
    - "PostgreSQL 16 + Redis 7 + Kafka (Compose: ZooKeeper + Confluent broker)"
    - "Rate limiting: optional Redis global + annotated profiles (STRICT, STANDARD, SENSITIVE_OPERATIONS)"
    - "Target production: AWS ECS + RDS + ElastiCache + MSK (documented in infrastructure doc)"

coverImage:
  url: "https://cdn.PLACEHOLDER.example/bank-api/cover-architecture.png"
  alt: "Logical view: client, ALB, ECS tasks, RDS, Redis, MSK"
  credit: "Alexis Trejo (placeholder asset — replace URL when exported diagram is uploaded to S3/CloudFront)"

links:
  github: "https://github.com/alexistrejo11/bank-api"
  demo: "https://api.bank.prod.PLACEHOLDER.example.com/swagger-ui/index.html"
  documentation: "https://github.com/alexistrejo11/bank-api/tree/main/docs"
  dockerHub: null

mediaGallery:
  title: "Media gallery (placeholder assets)"
  description: "Replace URLs with S3 or CloudFront paths after you capture screenshots and diagrams from the AWS environment."
  items:
    - type: "image"
      url: "https://cdn.PLACEHOLDER.example/bank-api/gallery-01-modulith.png"
      thumbnail: "https://cdn.PLACEHOLDER.example/bank-api/gallery-01-modulith-thumb.png"
      title: "Spring Modulith module graph"
      description: "Placeholder for a screenshot of the Modulith documentation or build-time module graph."
      alt: "Module graph placeholder"
      category: "architecture"
    - type: "image"
      url: "https://cdn.PLACEHOLDER.example/bank-api/gallery-02-swagger.png"
      thumbnail: "https://cdn.PLACEHOLDER.example/bank-api/gallery-02-swagger-thumb.png"
      title: "Swagger UI on ALB"
      description: "Placeholder for Swagger UI against the production or staging ALB (consider IP restriction)."
      alt: "Swagger UI placeholder"
      category: "screenshot"
    - type: "image"
      url: "https://cdn.PLACEHOLDER.example/bank-api/gallery-03-grafana.png"
      thumbnail: "https://cdn.PLACEHOLDER.example/bank-api/gallery-03-grafana-thumb.png"
      title: "Grafana JVM dashboard"
      description: "Placeholder for Grafana (Compose today; Amazon Managed Grafana or self-hosted on AWS later)."
      alt: "Grafana dashboard placeholder"
      category: "screenshot"
    - type: "video"
      url: "https://cdn.PLACEHOLDER.example/bank-api/walkthrough.mp4"
      thumbnail: "https://cdn.PLACEHOLDER.example/bank-api/walkthrough-thumb.png"
      title: "Architecture walkthrough"
      description: "Placeholder for a short Loom or S3-hosted MP4 explaining request and event flows."
      alt: "Walkthrough video placeholder"
      category: "demo"

mediaItems:
  - type: "image"
    url: "https://cdn.PLACEHOLDER.example/bank-api/media-arch.png"
    thumbnail: "https://cdn.PLACEHOLDER.example/bank-api/media-arch-thumb.png"
    title: "AWS reference architecture"
    description: "ALB, ECS service, RDS, ElastiCache, MSK — export from draw.io or Lucidchart."
    alt: "AWS diagram placeholder"
    category: "architecture"
  - type: "image"
    url: "https://cdn.PLACEHOLDER.example/bank-api/media-seq-transfer.png"
    thumbnail: ""
    title: "Transfer sequence"
    description: "Placeholder sequence: client → ALB → TransferController → domain → events → ledger listener."
    alt: "Sequence diagram placeholder"
    category: "diagram"
  - type: "image"
    url: "https://cdn.PLACEHOLDER.example/bank-api/media-er.png"
    thumbnail: ""
    title: "Persistence overview"
    description: "Placeholder ER or schema map (Flyway migrations are source of truth)."
    alt: "ER placeholder"
    category: "diagram"

metrics:
  - label: "Maven modules"
    value: "9"
    description: "Shared kernel, IAM, accounts, audit, payments, loans, notifications, config, boot"
    icon: "📦"
    unit: "count"
    trend: "stable"
    threshold: null
  - label: "Versioned REST paths"
    value: "17"
    description: "Controller-mapped /api/v1 routes (excluding actuator and OpenAPI assets)"
    icon: "🌐"
    unit: "endpoints"
    trend: "up"
    threshold: null
  - label: "Java LTS"
    value: "21"
    description: "Temurin JDK in Docker build; virtual-thread-ready stack"
    icon: "☕"
    unit: "version"
    trend: "stable"
    threshold: null
  - label: "Spring Boot"
    value: "4.0.5"
    description: "Parent BOM version from bank-parent POM"
    icon: "🍃"
    unit: "version"
    trend: "stable"
    threshold: null
---

# Overview

## Notes

- **Danger**: `docker-compose.yml` exposes nginx on **port 80 (HTTP)** with `limit_req` — there is **no TLS** in that file. For AWS, terminate TLS at the ALB with ACM; do not copy the Compose TLS story to production verbatim.
- **Danger**: Global `failOpen` on rate limiting favors availability when Redis is sick; document whether you ever want `failOpen: false` for fraud-sensitive endpoints.
- **Good**: `SecurityConfig` maps each banking route to fine-grained authorities (`accounts:read`, `payments:write`, etc.), which transfers cleanly to IAM-style policies or Cognito groups later.
- **Missing**: All `cdn.PLACEHOLDER.example` URLs are fictional — upload real assets to S3 and invalidate CloudFront when ready.
- **Observation**: Compose uses **Confluent Kafka + ZooKeeper**, not Redpanda; MSK is the natural AWS counterpart for the same client code paths.
- **Observation**: `bank-config` holds cross-cutting security beans; keep dependency rules one-way (feature modules should not depend on `bank-config` types from domain packages if you later extract services).
