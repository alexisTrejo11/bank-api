# Project metadata

Summary card for the Bank API repository. Structured twin: [`source/ProjectMetadata.md`](source/ProjectMetadata.md).

## Identity

| Field | Value |
|--------|--------|
| **Project ID** | `bank-api` |
| **Name** | Bank API |
| **Featured (portfolio)** | Yes |
| **Category** | Backend |
| **Language** | Java |
| **Framework** | Spring Boot **4.0.5** |
| **Version** | **0.0.1-SNAPSHOT** (parent POM) |
| **Status** | **Deployed** (documentation narrative aligned with target AWS production; flip to *develop* in source if pre–go-live) |
| **Created (doc)** | 2025-01-01 |
| **Updated (doc)** | 2026-05-12 |

## Elevator pitch

Modular monolith banking API with **double-entry ledger**, **JWT (RS256) and RBAC**, **idempotent payments**, **loan origination**, **append-only audit**, and **notification monitoring**. Everything ships as one Spring Boot application (**`bank-boot`**) built from **nine** Maven modules with DDD-style boundaries and **Spring Modulith** checks. Operations are documented as **AWS** (ECS, RDS, ElastiCache, MSK); **Docker Compose** remains the usual local and demo stack.

## Tech stack

| Layer | Technologies |
|--------|----------------|
| Runtime | Java **21** |
| Application framework | Spring Boot **4.0.5** |
| Modularity | Spring Modulith **2.0.5** |
| Data | PostgreSQL **16**, Flyway |
| Coordination | Redis **7** (refresh, revocation metadata, idempotency, rate limits) |
| Messaging | Apache Kafka (Spring Kafka; **Confluent** images in Compose; **MSK** on AWS) |
| Security / tokens | Spring Security 6, **JJWT 0.12.5** |
| API description | **SpringDoc OpenAPI 2.6.0** (Swagger UI) |
| Build | Maven (`mvnw`) |
| Test | JUnit 5, Spring Boot Test, Testcontainers where ITs exist |
| Observability (Compose) | Prometheus, Grafana, ELK 8.x |
| Container | Multi-stage Dockerfile (Temurin 21, non-root user, ZGC) |

## Repository and demo URLs

| Link | URL |
|------|-----|
| **Repository** | [https://github.com/alexistrejo11/bank-api](https://github.com/alexistrejo11/bank-api) |
| **Live demo / Swagger (placeholder)** | `https://api.bank.prod.PLACEHOLDER.example.com/swagger-ui/index.html` — replace with real ALB or CloudFront origin after DNS cutover |

## Maven modules (nine)

1. **`bank-shared`** — shared kernel (IDs, `ApiResponse`, auth primitives, rate-limit annotations, etc.)  
2. **`bank-iam`** — registration, login, refresh, logout, `/me`  
3. **`bank-accounts`** — open account, balance, ledger  
4. **`bank-audit`** — searchable audit records  
5. **`bank-payments`** — transfers and reversals with idempotency  
6. **`bank-loans`** — originate, approve, detail, repay installments  
7. **`bank-notifications`** — monitoring APIs for delivery records  
8. **`bank-config`** — cross-cutting security (`SecurityConfig`, JWT filter)  
9. **`bank-boot`** — runnable fat JAR, Flyway, Actuator, rate-limit wiring  

## Operational and security notes

- **Secrets**: Production keys and passwords belong in **AWS Secrets Manager** or **SSM Parameter Store**. Never commit a filled `.env`.  
- **Actuator**: `/actuator/**` is broadly permitted in a dedicated security filter chain — restrict by **network** (private subnets, security groups) on AWS.  
- **Financial invariants**: Transfers assume **Redis idempotency** and **paired ledger rows**. Operational loss of Redis or bugs in invariants are high severity.  
- **Rate limiting**: Redis token buckets with **`failOpen` defaulting to true** — availability over strict throttling when Redis is unhealthy; document the fraud/abuse trade-off.  
- **Placeholder demo URL**: Replace `PLACEHOLDER` with your real public hostname when ready.  
- **Housekeeping**: `bank-config` uses Java package `io.github.alexisTrejo11` (capital **T**); other modules use `alexistrejo11` — consider renaming in a hygiene PR.

## Related docs

- [Project overview](ProjectOverview.md) — problem, solution, metrics  
- [Infrastructure](InfrastructureModel.md) — Compose vs AWS  
- [API reference](APISchema.md) — HTTP surface  
