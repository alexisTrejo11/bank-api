---
projectId: "bank-api"
featured: true
name: "Bank API"
language: "Java"
category: "backend"
framework: "Spring Boot"
version: "0.4.0"
repositoryUrl: "https://github.com/alexisTrejo11/bank-api"
liveDemoUrl: "https://{{YOUR_DOMAIN_OR_EC2}}/actuator/health"
description: "Minimalist modular-monolith banking REST API — IAM, double-entry accounts, transfers with idempotency, loans, append-only audit, and async notifications. Deployed on AWS EC2 with RDS PostgreSQL, Upstash Redis, and Kafka consumed from a cloud broker; observability stack (Prometheus, Grafana, Loki) runs on EC2."
techStack:
  - "Java 21"
  - "Spring Boot 4.0.5"
  - "Spring Modulith 2.0.5"
  - "Spring Security + JWT (RS256)"
  - "PostgreSQL 16 (Amazon RDS)"
  - "Redis (Upstash — tokens, idempotency, rate limits)"
  - "Apache Kafka (cloud instance — notification dispatch)"
  - "Flyway"
  - "Spring Data JPA"
  - "Spring Kafka"
  - "springdoc-openapi (Swagger UI)"
  - "Docker (multi-stage JRE image)"
  - "Prometheus + Grafana + Loki"
status: "stable"
createdAt: "2025-01-01T00:00:00.000Z"
updatedAt: "2026-06-05T00:00:00.000Z"
---

# Project Metadata

> **Portfolio note:** Replace `{{YOUR_DOMAIN_OR_EC2}}` in `liveDemoUrl` with your real EC2 public DNS or ALB hostname once published.

> **Warning:** JWT keys are ephemeral when `BANK_SECURITY_JWT_*_PEM` env vars are unset — always set PEM keys in production on AWS.
