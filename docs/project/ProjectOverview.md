# Project overview

Narrative overview derived from [`source/ProjectOverview.md`](source/ProjectOverview.md). For identity and stack list see [Project metadata](ProjectMetadata.md). For links see [Project links](ProjectLinks.md).

## Problem statement

**Title:** Need a compliant, evolvable banking core without premature microservices.

**Context:** Core banking behaviors—authentication, accounts, immutable audit, payments with idempotency, loans, and operational visibility—must stay consistent under failure and retries. The codebase should remain testable and modular so bounded contexts can be split out later, without operating nine separate deployables on day one.

**Pain points**

1. Financial writes need **atomicity** and a clear ordering of **side-effects** (no notifications or audit rows after a rolled-back transaction).  
2. **HTTP retries** must not double-post money movements; **idempotency** and **ledger** rules must be first-class.  
3. Regulators and operators expect **append-only audit** trails and searchable evidence, not logs alone.  
4. Teams want **one deployable** for velocity, but explicit **module seams** for a future AWS split (ECS services per context, MSK topics, etc.).

## Solution

**Title:** Modular monolith on Spring Boot with hexagonal-style ports and domain events.

| Pillar | Description |
|--------|-------------|
| **Single runtime, multiple Maven modules** | Nine modules compile into one Spring Boot JAR (`bank-boot`). Spring Modulith documents and enforces module relationships. |
| **Double-entry ledger and derived balances** | Mutations persist balanced DEBIT/CREDIT `LedgerEntry` rows; balances derive from ledger sums. |
| **After-commit domain integration** | Cross-module reactions use Spring application events and transactional boundaries so consumers align with successful commits where configured. |
| **JWT session model with Redis** | RS256 access tokens, refresh rotation, Redis-backed revocation metadata — suitable for ALB → stateless ECS tasks. |
| **AWS-shaped operations** | Production is described as ECS Fargate behind ALB, RDS PostgreSQL, ElastiCache Redis, and MSK — analogous to Docker Compose locally. |

## Key metrics (narrative)

**Section title (source):** Snapshot metrics (code + docs).

- Nine Maven modules (including `bank-config` and `bank-boot`).  
- Seventeen HTTP JSON endpoints under `/api/v1` (plus Actuator and OpenAPI static routes).  
- Spring Boot **4.0.5** / Java **21**.  
- PostgreSQL **16** + Redis **7** + Kafka (Compose: ZooKeeper + Confluent broker).  
- Rate limiting: optional Redis global + annotated profiles.  
- Target production: **AWS ECS + RDS + ElastiCache + MSK** (see [Infrastructure](InfrastructureModel.md)).

## Cover image (placeholder)

| Field | Value |
|--------|--------|
| **URL** | `https://cdn.PLACEHOLDER.example/bank-api/cover-architecture.png` |
| **Alt** | Logical view: client, ALB, ECS tasks, RDS, Redis, MSK |
| **Credit** | Alexis Trejo — replace URL after uploading a real diagram to S3/CloudFront |

## Risks, gaps, and observations

- **Compose uses HTTP on port 80** at nginx — **no TLS** in the checked stack. On AWS, terminate TLS at the **ALB** with **ACM**.  
- **Rate limit `failOpen`**: when Redis is unhealthy, traffic may proceed without throttling — document whether fraud-sensitive endpoints should ever use `failOpen=false`.  
- **Good:** `SecurityConfig` maps routes to fine-grained **authorities** (`accounts:read`, `payments:write`, …), which maps cleanly to IAM policies or Cognito groups.  
- **Missing:** All `cdn.PLACEHOLDER.example` URLs are fictional until you upload real media.  
- **Observation:** Compose uses **Confluent Kafka + ZooKeeper**, not Redpanda; **MSK** is the natural AWS counterpart.  
- **Observation:** `bank-config` holds cross-cutting security; keep dependencies one-way when extracting microservices later.

## Related documentation

| Page | Topic |
|------|--------|
| [ProjectArchitectureModel.md](ProjectArchitectureModel.md) | Layers, diagrams, ADRs |
| [ProjectFeatures.md](ProjectFeatures.md) | Feature catalog |
| [APISchema.md](APISchema.md) | REST reference |
| [MediaGallerySection.md](MediaGallerySection.md) | Visual placeholders |
