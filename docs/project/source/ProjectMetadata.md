---
projectId: "bank-api"
featured: true
name: "Bank API"
language: "Java"
category: "backend"
framework: "Spring Boot"
version: "0.0.1-SNAPSHOT"
repositoryUrl: "https://github.com/alexistrejo11/bank-api"
liveDemoUrl: "https://api.bank.prod.PLACEHOLDER.example.com/swagger-ui/index.html"
description: "Modular monolith banking API with double-entry ledger, JWT (RS256) and RBAC, idempotent payments, loan origination, append-only audit, and notification monitoring. Packaged as a single Spring Boot app (bank-boot) with nine Maven modules, DDD-style boundaries, and Spring Modulith verification. Documented below as if production runs on AWS (ECS, RDS, ElastiCache, MSK); local and Docker Compose stacks remain the day-to-day dev path."
techStack:
  - "Java 21"
  - "Spring Boot 4.0.5"
  - "Spring Modulith 2.0.5"
  - "PostgreSQL 16"
  - "Redis 7"
  - "Apache Kafka (Spring Kafka; Confluent images in Compose)"
  - "Flyway"
  - "JJWT 0.12.5"
  - "SpringDoc OpenAPI 2.6.0"
  - "Docker / Docker Compose"
  - "Maven"
  - "JUnit 5 & Spring Boot Test"
  - "Testcontainers (where ITs are enabled)"
  - "Prometheus & Grafana (Compose)"
  - "Elasticsearch, Logstash, Kibana 8.x (Compose)"
status: "deployed"
createdAt: "2025-01-01T00:00:00.000Z"
updatedAt: "2026-05-12T12:00:00.000Z"
---

# Project metadata

## Notes

- **Danger — secrets**: JWT private keys, DB passwords, and Kafka-related settings belong in AWS Secrets Manager or SSM Parameter Store in production. Never commit a populated `.env`; only `.env.example` belongs in git.
- **Danger — actuator exposure**: `SecurityConfig` permits all requests under `/actuator/**`. On AWS, restrict network access (security groups, IP allow lists, or private subnets) and avoid exposing broad actuator endpoints publicly.
- **Danger — financial invariants**: Transfers depend on Redis-backed idempotency keys and paired ledger rows. Losing Redis without a safe operational story can allow duplicate transfers on retries; losing invariant checks in code can break reconciliation.
- **Good**: Nine modules (`bank-shared`, `bank-iam`, `bank-accounts`, `bank-audit`, `bank-payments`, `bank-loans`, `bank-notifications`, `bank-config`, `bank-boot`) keep boundaries explicit; `bank-boot` is the runnable aggregate.
- **Good**: Rate limiting is Redis token-bucket based (`bank.rate-limiting.*`), with `failOpen` defaulting to true so Redis outages favor availability over strict throttling (document operational trade-off).
- **Missing / placeholder**: `liveDemoUrl` uses a deliberate `PLACEHOLDER` host — replace with the real public ALB or CloudFront origin when DNS is live.
- **Observation**: `status` is set to `deployed` to match the portfolio narrative (target AWS production). If the repo is still pre-cutover, flip to `develop` until go-live.
- **Observation**: Package directory `io.github.alexisTrejo11` in `bank-config` (capital T) differs from other modules’ `alexistrejo11`; harmless at runtime but worth normalizing in a future refactor for consistency.
