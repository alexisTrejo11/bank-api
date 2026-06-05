---
problemStatement:
  problemTitle: "Learning banking domains without microservice overhead"
  problemDescription: "Building a credible banking backend requires double-entry ledgers, idempotent payments, loan amortization, audit trails, and secure auth — but a full microservice mesh is heavy for a portfolio or teaching project. Teams also need a path from local Docker to a real cloud deploy without rewriting the domain model."
  problemList:
    - "Monolithic tutorials skip idempotency, ledger integrity, and append-only audit"
    - "JWT + RBAC + refresh rotation is often bolted on without Redis-backed revocation"
    - "Cross-module side effects (transfers → ledger → notifications) are hard to test in isolation"
    - "Local dev stacks differ from production (H2 vs PostgreSQL, in-process events vs Kafka)"
    - "Observability (metrics, structured logs, audit files) is rarely wired end-to-end"

solution:
  solutionTitle: "Modular monolith with clean boundaries and AWS-ready deploy"
  solutionList:
    - title: "Domain modules with hexagonal layering"
      description: "Nine Maven modules (shared, iam, accounts, payments, loans, audit, notifications, config, boot) — controllers stay thin; handlers own use cases; domain has zero Spring imports."
    - title: "Financial correctness by design"
      description: "Balances derived from ledger entries (never stored), BigDecimal money, transfer state machine, loan amortization schedule, and Result<T> for domain failures."
    - title: "Production integrations enabled in docker profile"
      description: "Redis for refresh tokens, JWT blocklist, payment idempotency, and token-bucket rate limits; Kafka ingress for notification dispatch when BANK_KAFKA_ENABLED=true."
    - title: "Deployed minimalist stack on AWS"
      description: "Single EC2 instance runs the app container (docker/compose.yml); RDS PostgreSQL and Upstash Redis are external; Kafka broker is a cloud instance the app consumes from; Prometheus/Grafana/Loki for observability on EC2."
    - title: "OpenAPI-first API surface"
      description: "REST under /api/v1/* with Swagger UI at /swagger-ui.html and standardized ApiResponse envelope."

keyMetrics:
  metricsTitle: "Platform snapshot"
  metricsList:
    - "9 Maven modules — bank-shared through bank-boot"
    - "REST API v1 under /api/v1/ (auth, accounts, payments, loans, audit, notifications)"
    - "15 Flyway migrations (V2–V15 + repeatable seed)"
    - "Docker image: Eclipse Temurin 21 JRE Alpine, ZGC, healthcheck on /actuator/health"
    - "Local full stack: postgres, redis, kafka, prometheus, grafana, loki, nginx via compose.local.yml"

links:
  github: "https://github.com/alexisTrejo11/bank-api"
  demo: "https://{{YOUR_DOMAIN_OR_EC2}}/actuator/health"
  documentation: "https://{{YOUR_DOMAIN_OR_EC2}}/swagger-ui.html"
  dockerHub: null

mediaGallery:
  title: "Bank API — product views"
  description: "Screenshots and diagrams for portfolio presentation. Replace placeholder URLs with Swagger UI, Grafana dashboard, or architecture exports from your AWS deploy."
  items:
    - type: "image"
      url: "https://placehold.co/1200x630/1E3A5F/ffffff?text=Bank+API"
      thumbnail: "https://placehold.co/400x210/1E3A5F/ffffff?text=Bank+API"
      title: "API cover"
      description: "Modular monolith banking REST API — IAM, accounts, payments, loans"
      alt: "Bank API branding placeholder"
      category: "screenshot"
    - type: "image"
      url: "https://placehold.co/1200x800/2563EB/ffffff?text=Swagger+OpenAPI"
      thumbnail: "https://placehold.co/400x267/2563EB/ffffff?text=OpenAPI"
      title: "OpenAPI documentation"
      description: "Interactive API schema via springdoc-openapi"
      alt: "OpenAPI Swagger UI placeholder"
      category: "demo"

mediaItems:
  - type: "image"
    url: "https://placehold.co/800x500/059669/ffffff?text=AWS+Architecture"
    thumbnail: "https://placehold.co/320x200/059669/ffffff?text=AWS"
    title: "AWS deployment"
    description: "EC2 app container connecting to RDS, Upstash Redis, cloud Kafka, and on-EC2 Prometheus/Grafana/Loki"
    alt: "AWS architecture placeholder"
    category: "architecture"
  - type: "image"
    url: "https://placehold.co/800x500/DC2626/ffffff?text=Grafana+Dashboard"
    thumbnail: "https://placehold.co/320x200/DC2626/ffffff?text=Grafana"
    title: "Observability"
    description: "Metrics from /actuator/prometheus scraped by Prometheus; logs shipped to Loki"
    alt: "Grafana dashboard placeholder"
    category: "diagram"

metrics:
  - label: "Maven modules"
    value: "9"
    description: "shared, iam, accounts, audit, payments, loans, notifications, config, boot"
  - label: "API version"
    value: "v1"
    description: "Primary prefix /api/v1/"
  - label: "Java runtime"
    value: "21"
    description: "Eclipse Temurin JDK/JRE in Docker multi-stage build"
  - label: "Auth"
    value: "JWT RS256"
    description: "15 min access, 7 day refresh in Redis, jti blocklist on logout"
---

# Overview

> **Audience:** Developers learning domain-driven design, financial APIs, and pragmatic AWS deployment without Kubernetes complexity.

> **AWS deploy (current):** Application container on **EC2** (`docker/compose.yml`), database on **Amazon RDS PostgreSQL**, cache/idempotency on **Upstash Redis**, **Kafka** consumed from an external cloud broker for notification pipeline, and **Prometheus + Grafana + Loki** on EC2 for external observability. See [ProjectInfrastructure.md](../generated/ProjectInfrastructure.md).

> **Useful:** Local full stack mirrors production wiring via `docker/compose.local.yml` — set `BANK_KAFKA_ENABLED=true` and `BANK_NOTIFICATIONS_DISPATCH_MODE=kafka` to exercise the same paths as AWS.

> **Warning:** This is an educational/portfolio API — not licensed banking software. Do not use for real customer funds without formal security, compliance, and penetration review.
