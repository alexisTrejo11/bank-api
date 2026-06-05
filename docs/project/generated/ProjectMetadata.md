# Bank API

Minimalist modular-monolith banking REST API — IAM, double-entry accounts, transfers with idempotency, loans, append-only audit, and async notifications. Deployed on AWS EC2 with RDS PostgreSQL, Upstash Redis, and Kafka consumed from a cloud broker; observability stack (Prometheus, Grafana, Loki) runs on EC2.

| Field | Value |
| --- | --- |
| Project ID | bank-api |
| Version | 0.3.0 |
| Language | Java |
| Framework | Spring Boot |
| Category | backend |
| Status | stable |
| Featured | Yes |
| Repository | https://github.com/alexisTrejo11/bank-api |
| Live demo | https://{{YOUR_DOMAIN_OR_EC2}}/actuator/health |
| Created | 2025-01-01T00:00:00.000Z |
| Updated | 2026-06-05T00:00:00.000Z |

## Tech stack

- Java 21
- Spring Boot 4.0.5
- Spring Modulith 2.0.5
- Spring Security + JWT (RS256)
- PostgreSQL 16 (Amazon RDS)
- Redis (Upstash — tokens, idempotency, rate limits)
- Apache Kafka (cloud instance — notification dispatch)
- Flyway
- Spring Data JPA
- Spring Kafka
- springdoc-openapi (Swagger UI)
- Docker (multi-stage JRE image)
- Prometheus + Grafana + Loki

## Additional notes

# Project Metadata

> **Portfolio note:** Replace `{{YOUR_DOMAIN_OR_EC2}}` in `liveDemoUrl` with your real EC2 public DNS or ALB hostname once published.

> **Warning:** JWT keys are ephemeral when `BANK_SECURITY_JWT_*_PEM` env vars are unset — always set PEM keys in production on AWS.

