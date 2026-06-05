---
metrics:
  - label: "Container port"
    value: "8080"
    icon: "server"
    description: "Spring Boot embedded Tomcat; exposed in Dockerfile HEALTHCHECK"
  - label: "Host mapping (deploy)"
    value: "${APP_HTTP_PORT}:8080"
    icon: "network"
    description: "docker/compose.yml publishes API port on EC2 (security group)"
  - label: "JVM"
    value: "Java 21 + ZGC"
    icon: "cpu"
    description: "Eclipse Temurin JRE Alpine; JAVA_OPTS=-XX:+UseZGC"
  - label: "Image size target"
    value: "< 300 MB"
    icon: "box"
    description: "Multi-stage build — JRE + single fat JAR only in runtime stage"
  - label: "Flyway migrations"
    value: "15"
    icon: "database"
    description: "V2–V15 + repeatable R__seed_demo_data.sql"

cloudServices:
  - name: "Amazon EC2"
    purpose: "Runs bank-api Docker container (compose.yml). Optional Nginx reverse proxy. Hosts Prometheus, Grafana, Loki for external observability."
    icon: "aws-ec2"
    cost: "~$10–30/mo (t3.micro/small placeholder)"
  - name: "Amazon RDS (PostgreSQL 16)"
    purpose: "Primary database — IAM, accounts, ledger, transfers, loans, audit, notifications. SPRING_DATASOURCE_* in .env."
    icon: "aws-rds"
    cost: "~$15–50/mo (db.t4g.micro placeholder)"
  - name: "Upstash Redis"
    purpose: "JWT refresh tokens, jti blocklist, transfer idempotency cache, token-bucket rate limits. SPRING_DATA_REDIS_* in .env."
    icon: "redis"
    cost: "Free tier or ~$10/mo"
  - name: "Cloud Kafka instance"
    purpose: "Notification dispatch and pipeline topics (bank.notifications.dispatch, bank.notifications.pipeline). App produces and consumes via SPRING_KAFKA_BOOTSTRAP_SERVERS."
    icon: "kafka"
    cost: "Variable (self-hosted VM or managed — placeholder)"
  - name: "Prometheus (on EC2)"
    purpose: "Scrapes /actuator/prometheus from app. PROMETHEUS_SCRAPE_TARGET points to app host:port."
    icon: "prometheus"
    cost: "Included in EC2 cost"
  - name: "Grafana + Loki (on EC2)"
    purpose: "Dashboards and log aggregation. GRAFANA_PROMETHEUS_URL and LOKI_URL configured per deploy."
    icon: "grafana"
    cost: "Included in EC2 cost"
  - name: "SMTP / SMS (optional)"
    purpose: "Real notification delivery when not using dev stubs. BANK_NOTIFICATIONS_DEV_REDIRECT_EMAIL for safe testing."
    icon: "mail"
    cost: "Variable"

deploymentLayers:
  - name: "Clients"
    color: "#4F46E5"
    components:
      - name: "REST clients"
        icon: "terminal"
        description: "Postman, curl, or future web/mobile frontend consuming /api/v1/*"
      - name: "Swagger UI"
        icon: "book-open"
        description: "Interactive API explorer at /swagger-ui.html"

  - name: "Edge & compute (EC2)"
    color: "#059669"
    components:
      - name: "Nginx (optional)"
        icon: "globe"
        description: "TLS termination and reverse proxy to app:8080 (local: compose.local.yml nginx on :80)"
      - name: "Docker — bank-api app"
        icon: "docker"
        description: "Built from docker/Dockerfile; SPRING_PROFILES_ACTIVE=docker; restart unless-stopped"
      - name: "Observability agents"
        icon: "activity"
        description: "Prometheus, Grafana, Loki, Promtail on EC2 (or same compose.local.yml stack locally)"

  - name: "Managed data & messaging"
    color: "#DC2626"
    components:
      - name: "RDS PostgreSQL"
        icon: "database"
        description: "External — not in production compose.yml. Flyway applies schema on startup."
      - name: "Upstash Redis"
        icon: "redis"
        description: "External TLS Redis — tokens, idempotency, rate limits"
      - name: "Cloud Kafka"
        icon: "kafka"
        description: "External broker — notification dispatch consumed by bank-notifications module"
      - name: "App logs volume"
        icon: "folder"
        description: "BANK_LOGGING_DIRECTORY=/app/logs — audit.json, access.json; Promtail ships to Loki"

  - name: "Local-only (compose.local.yml)"
    color: "#D97706"
    components:
      - name: "Containerized Postgres"
        icon: "database"
        description: "postgres:16-alpine for dev — not used in AWS production deploy"
      - name: "Containerized Redis + Kafka"
        icon: "layers"
        description: "Local kafka:9092 and redis for parity testing before cloud cutover"
      - name: "Optional ELK profile"
        icon: "search"
        description: "docker compose --profile elk — Elasticsearch, Logstash, Kibana"

dockerFiles:
  - service: "compose.yml (deploy / EC2)"
    description: "Single app service — Postgres, Redis, and Kafka are external cloud instances configured via repo-root .env."
    content: |
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

  - service: "Dockerfile (multi-stage)"
    description: "JDK 21 Alpine builder runs mvn package; JRE 21 Alpine runtime runs as non-root bank user."
    content: |
      FROM eclipse-temurin:21-jdk-alpine AS builder
      RUN ./mvnw -pl bank-boot -am package -DskipTests
      FROM eclipse-temurin:21-jre-alpine
      USER bank
      EXPOSE 8080
      ENV JAVA_OPTS="-XX:+UseZGC"
      HEALTHCHECK CMD curl -fsS http://127.0.0.1:8080/actuator/health
      ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

  - service: "compose.local.yml (dev full stack)"
    description: "postgres, redis, kafka, prometheus, grafana, loki, promtail, nginx, app — for local integration testing."
    content: |
      services:
        postgres:   # postgres:16-alpine
        redis:      # redis:7-alpine
        kafka:      # confluentinc/cp-kafka:7.5.0
        prometheus: # scrapes app:8080/actuator/prometheus
        grafana:    # dashboards + Loki datasource
        loki:       # log aggregation
        app:        # bank-api Spring Boot
        nginx:      # :80 → app:8080
---

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
