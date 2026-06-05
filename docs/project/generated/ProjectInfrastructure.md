# Infrastructure

## Metrics

| Label | Value | Description |
| --- | --- | --- |
| Container port | 8080 | Spring Boot embedded Tomcat; exposed in Dockerfile HEALTHCHECK |
| Host mapping (deploy) | ${APP_HTTP_PORT}:8080 | docker/compose.yml publishes API port on EC2 (security group) |
| JVM | Java 21 + ZGC | Eclipse Temurin JRE Alpine; JAVA_OPTS=-XX:+UseZGC |
| Image size target | < 300 MB | Multi-stage build — JRE + single fat JAR only in runtime stage |
| Flyway migrations | 15 | V2–V15 + repeatable R__seed_demo_data.sql |

## Cloud services

| Service | Purpose | Est. cost |
| --- | --- | --- |
| Amazon EC2 | Runs bank-api Docker container (compose.yml). Optional Nginx reverse proxy. Hosts Prometheus, Grafana, Loki for external observability. | ~$10–30/mo (t3.micro/small placeholder) |
| Amazon RDS (PostgreSQL 16) | Primary database — IAM, accounts, ledger, transfers, loans, audit, notifications. SPRING_DATASOURCE_* in .env. | ~$15–50/mo (db.t4g.micro placeholder) |
| Upstash Redis | JWT refresh tokens, jti blocklist, transfer idempotency cache, token-bucket rate limits. SPRING_DATA_REDIS_* in .env. | Free tier or ~$10/mo |
| Cloud Kafka instance | Notification dispatch and pipeline topics (bank.notifications.dispatch, bank.notifications.pipeline). App produces and consumes via SPRING_KAFKA_BOOTSTRAP_SERVERS. | Variable (self-hosted VM or managed — placeholder) |
| Prometheus (on EC2) | Scrapes /actuator/prometheus from app. PROMETHEUS_SCRAPE_TARGET points to app host:port. | Included in EC2 cost |
| Grafana + Loki (on EC2) | Dashboards and log aggregation. GRAFANA_PROMETHEUS_URL and LOKI_URL configured per deploy. | Included in EC2 cost |
| SMTP / SMS (optional) | Real notification delivery when not using dev stubs. BANK_NOTIFICATIONS_DEV_REDIRECT_EMAIL for safe testing. | Variable |

## Deployment layers

### Clients

- **REST clients** — Postman, curl, or future web/mobile frontend consuming /api/v1/*
- **Swagger UI** — Interactive API explorer at /swagger-ui.html

### Edge & compute (EC2)

- **Nginx (optional)** — TLS termination and reverse proxy to app:8080 (local: compose.local.yml nginx on :80)
- **Docker — bank-api app** — Built from docker/Dockerfile; SPRING_PROFILES_ACTIVE=docker; restart unless-stopped
- **Observability agents** — Prometheus, Grafana, Loki, Promtail on EC2 (or same compose.local.yml stack locally)

### Managed data & messaging

- **RDS PostgreSQL** — External — not in production compose.yml. Flyway applies schema on startup.
- **Upstash Redis** — External TLS Redis — tokens, idempotency, rate limits
- **Cloud Kafka** — External broker — notification dispatch consumed by bank-notifications module
- **App logs volume** — BANK_LOGGING_DIRECTORY=/app/logs — audit.json, access.json; Promtail ships to Loki

### Local-only (compose.local.yml)

- **Containerized Postgres** — postgres:16-alpine for dev — not used in AWS production deploy
- **Containerized Redis + Kafka** — Local kafka:9092 and redis for parity testing before cloud cutover
- **Optional ELK profile** — docker compose --profile elk — Elasticsearch, Logstash, Kibana

## Docker configuration

### compose.yml (deploy / EC2)

Single app service — Postgres, Redis, and Kafka are external cloud instances configured via repo-root .env.

```yaml
name: bank-api
services:
  app:
    build:
      context: ..
      dockerfile: docker/Dockerfile
    image: bank-api:${BANK_API_IMAGE_TAG}
    env_file:
      - ../.env
    environment:
      SPRING_PROFILES_ACTIVE: docker
    ports:
      - "${APP_HTTP_PORT}:8080"
    restart: unless-stopped
# External: RDS PostgreSQL + Upstash Redis + Cloud Kafka
# Observability: Prometheus/Grafana/Loki on EC2 (see docker/MONITORING.md)
```

### Dockerfile (multi-stage)

JDK 21 Alpine builder runs mvn package; JRE 21 Alpine runtime runs as non-root bank user.

```yaml
FROM eclipse-temurin:21-jdk-alpine AS builder
RUN ./mvnw -pl bank-boot -am package -DskipTests
FROM eclipse-temurin:21-jre-alpine
USER bank
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseZGC"
HEALTHCHECK CMD curl -fsS http://127.0.0.1:8080/actuator/health
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
```

### compose.local.yml (dev full stack)

postgres, redis, kafka, prometheus, grafana, loki, promtail, nginx, app — for local integration testing.

```yaml
services:
  postgres:   # postgres:16-alpine
  redis:      # redis:7-alpine
  kafka:      # confluentinc/cp-kafka:7.5.0
  prometheus: # scrapes app:8080/actuator/prometheus
  grafana:    # dashboards + Loki datasource
  loki:       # log aggregation
  app:        # bank-api Spring Boot
  nginx:      # :80 → app:8080
```

## Additional notes

# Infrastructure

> **Deploy story:** Build image on EC2 or CI → copy `.env` with RDS/Upstash/Kafka endpoints → `./docker/validate-env.sh app` → `docker compose --env-file .env -f docker/compose.yml up -d --build`.

> **AWS `.env` example (from .env.example):**
> ```
> SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds.region.rds.amazonaws.com:5432/at_bank
> SPRING_DATA_REDIS_HOST=your-upstash-host.upstash.io
> SPRING_KAFKA_BOOTSTRAP_SERVERS=your-kafka-host:9092
> BANK_KAFKA_ENABLED=true
> BANK_NOTIFICATIONS_DISPATCH_MODE=kafka
> ```

> **Observability on EC2:** Spring exposes `/actuator/prometheus` only — it does not know Grafana URLs. Prometheus scrapes the app; Grafana reads Prometheus and Loki. See [docker/MONITORING.md](../../../docker/MONITORING.md).

> **EC2 checklist:** Open APP_HTTP_PORT (or 443 behind Nginx), restrict RDS/Redis/Kafka security groups to EC2 SG only, store secrets in SSM or sealed `.env`, set JWT PEM keys for multi-instance deploy.

> **Dangerous:** Never commit `.env` with RDS passwords or Upstash tokens. `R__seed_demo_data.sql` is for demos — review before production.

> **Useful:** Local cutover test — run infra only (`docker compose -f docker/compose.local.yml up -d postgres redis kafka`) and point a local Maven run at those hosts before switching EC2 `.env` to cloud endpoints.

