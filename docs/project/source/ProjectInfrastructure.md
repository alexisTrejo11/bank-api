---
metrics:
  - label: "Compose services"
    value: "11"
    icon: "🐳"
    description: "zookeeper, kafka, postgres, redis, elasticsearch, logstash, kibana, prometheus, grafana, app, nginx (see docker-compose.yml)"
  - label: "Actuator health"
    value: "1"
    icon: "❤️"
    description: "Dockerfile HEALTHCHECK curls http://127.0.0.1:8080/actuator/health inside the container"
  - label: "Spring profiles"
    value: "4+"
    icon: "⚙️"
    description: "default, test, postgres, docker (bank-boot YAML); extend for `aws` when you add account-specific property files"
  - label: "Hikari default pool"
    value: "10"
    icon: "🔗"
    description: "Spring Boot default maximum pool size unless overridden in YAML"
  - label: "Nginx zone rate"
    value: "30 r/s"
    icon: "🚦"
    description: "limit_req_zone in infra/nginx/nginx.conf for /api/"
  - label: "App global bucket (when enabled)"
    value: "64 @ 1/s"
    icon: "🪣"
    description: "RateLimitingProperties.GlobalBucket defaults: capacity 64, refillPerSecond 1.0"
  - label: "STRICT profile default"
    value: "12 @ 0.2/s"
    icon: "🔐"
    description: "Auth endpoints use RateLimitProfile.STRICT unless overridden in config"
  - label: "SENSITIVE_OPERATIONS default"
    value: "6 @ 0.1/s"
    icon: "🏦"
    description: "Loan write endpoints use SENSITIVE_OPERATIONS profile"

cloudServices:
  - name: "Amazon ECS (Fargate)"
    purpose: "Run `bank-boot` container tasks; auto scaling on CPU/memory; attach to private subnets"
    icon: "🐳"
    cost: "Depends on vCPU/GB and task count (on-demand Fargate pricing in your region)"
  - name: "Application Load Balancer"
    purpose: "TLS termination with ACM, HTTP health checks to `/actuator/health`, sticky sessions off for stateless API"
    icon: "⚖️"
    cost: "LCU-hours + processed bytes"
  - name: "Amazon RDS for PostgreSQL"
    purpose: "System of record for accounts, payments, loans, audit, notifications tables"
    icon: "🐘"
    cost: "Instance + storage + Multi-AZ multiplier"
  - name: "Amazon ElastiCache for Redis"
    purpose: "Shared idempotency, JWT refresh metadata, rate limiting buckets, optional caches"
    icon: "💾"
    cost: "Node type × replica count"
  - name: "Amazon MSK"
    purpose: "Managed Kafka for notification pipeline and future event-driven extraction"
    icon: "📨"
    cost: "Broker hours + storage + data transfer"
  - name: "AWS Secrets Manager"
    purpose: "JWT signing keys, DB master credentials, third-party API keys"
    icon: "🔑"
    cost: "Per-secret monthly + API calls"
  - name: "Amazon CloudWatch"
    purpose: "Logs, metrics, alarms (replace or complement Prometheus/Grafana from Compose)"
    icon: "📊"
    cost: "Ingestion + storage + alarms"
  - name: "Amazon S3"
    purpose: "Static documentation assets, exported Grafana JSON, forensic exports"
    icon: "📦"
    cost: "Storage + requests"
  - name: "AWS WAF (optional)"
    purpose: "Rate-based rules and OWASP CRS on ALB for public endpoints"
    icon: "🛡️"
    cost: "Web ACL + rule + request charges"

deploymentLayers:
  - name: "Local developer"
    color: "#4CAF50"
    components:
      - name: "H2 or local PostgreSQL"
        icon: "🗄️"
        description: "Fast feedback; H2 for zero-deps runs; postgres profile for parity with Docker schema"
      - name: "Maven wrapper"
        icon: "📦"
        description: "`./mvnw -pl bank-boot spring-boot:run` from repository root"
      - name: "Optional local Redis"
        icon: "💾"
        description: "Needed for full auth + idempotency behavior outside docker"

  - name: "Docker Compose (parity / demo host)"
    color: "#2196F3"
    components:
      - name: "PostgreSQL 16"
        icon: "🐘"
        description: "Primary database with health-checked startup; credentials from `.env`"
      - name: "Redis 7"
        icon: "💾"
        description: "Sessions, idempotency, rate limits when enabled"
      - name: "ZooKeeper + Kafka 7.5"
        icon: "📨"
        description: "Confluent images; broker `kafka:9092` on internal network"
      - name: "nginx 1.25"
        icon: "🌐"
        description: "Reverse proxy on port 80 → `app:8080`; rate limit zone `30r/s` for `/api/`"
      - name: "Prometheus + Grafana"
        icon: "📈"
        description: "Metrics stack; Grafana provisions dashboards from `infra/grafana/provisioning`"
      - name: "Elasticsearch + Logstash + Kibana"
        icon: "🔍"
        description: "ELK 8.x pipeline; Logstash mounts `infra/logstash/pipeline`"

  - name: "AWS production (target)"
    color: "#FF9800"
    components:
      - name: "ECS Fargate service"
        icon: "🐳"
        description: "Same container image as local Docker build; environment from task definition + Secrets Manager"
      - name: "ALB + ACM"
        icon: "🔐"
        description: "Public HTTPS; forward `X-Forwarded-*` headers — Spring forwarded header strategy must stay enabled"
      - name: "RDS PostgreSQL Multi-AZ"
        icon: "🐘"
        description: "Automated backups, parameter groups tuned for JDBC batch sizes used by JPA"
      - name: "ElastiCache Redis replication group"
        icon: "💾"
        description: "Multi-AZ with automatic failover for coordination state"
      - name: "MSK"
        icon: "📨"
        description: "Private SASL/SCRAM or IAM auth; security groups only from ECS tasks"
      - name: "CloudWatch + (optional) OpenSearch"
        icon: "📊"
        description: "Central logs/metrics; OpenSearch if you need Compose-like ad hoc search"

dockerFiles:
  - service: "bank-api (multi-stage)"
    description: "Root Dockerfile builds `bank-boot` fat JAR with Maven inside Temurin JDK 21 Alpine, runs as non-root `bank` user with ZGC and curl-based healthcheck."
    content: |
      # Multi-stage build — final image target < ~300MB (JRE + single fat JAR).
      # Build from repo root: docker build -t bank-api:local .

      FROM eclipse-temurin:21-jdk-alpine AS builder
      WORKDIR /build
      RUN apk add --no-cache bash
      COPY mvnw mvnw.cmd ./
      COPY .mvn .mvn
      COPY pom.xml ./
      COPY bank-shared/pom.xml bank-shared/
      COPY bank-iam/pom.xml bank-iam/
      COPY bank-accounts/pom.xml bank-accounts/
      COPY bank-audit/pom.xml bank-audit/
      COPY bank-payments/pom.xml bank-payments/
      COPY bank-loans/pom.xml bank-loans/
      COPY bank-notifications/pom.xml bank-notifications/
      COPY bank-boot/pom.xml bank-boot/
      RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline -DskipTests || true
      COPY . .
      RUN ./mvnw -q -B -pl bank-boot -am package -DskipTests

      FROM eclipse-temurin:21-jre-alpine
      WORKDIR /app
      RUN apk add --no-cache curl \
      	&& addgroup -S bank && adduser -S bank -G bank
      COPY --from=builder /build/bank-boot/target/bank-boot-*.jar app.jar
      USER bank
      EXPOSE 8080
      ENV JAVA_OPTS="-XX:+UseZGC"
      HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
      	CMD curl -fsS http://127.0.0.1:8080/actuator/health >/dev/null || exit 1
      ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
---

# Infrastructure

## Notes

- **Danger**: Compose **nginx listens on port 80 without TLS**; this is acceptable for local/VPS demos only. Production on AWS must use **ALB + ACM** (or CloudFront) for TLS.
- **Danger**: Grafana defaults (`GF_SECURITY_ADMIN_PASSWORD`) in Compose are weak; rotate for any shared host and never expose Grafana publicly without auth hardening.
- **Good**: `app` service `expose`s port 8080 only — not published on the host; **nginx is the intended entry** (plus published tool ports like 9090/3000/5601 per compose).
- **Good**: Dockerfile runs as non-root and uses a **HEALTHCHECK** compatible with ECS health checks (map to ALB target group health check path `/actuator/health`).
- **Missing**: No `bank.conf` verification in this doc run — confirm `infra/logstash/pipeline` exists before relying on Logstash in CI.
- **Observation**: `BANK_KAFKA_ENABLED` defaults to `false` in compose environment; domain events may still be in-process while Kafka is used selectively (e.g. notifications) — verify `application-docker.yaml` for exact flags.
- **Observation**: The builder stage copies only a subset of `pom.xml` files before `dependency:go-offline`; **`bank-config/pom.xml` is not in that list** (modules that transitively need it still resolve after `COPY . .`). If go-offline ever fails in CI, add `COPY bank-config/pom.xml bank-config/` alongside the others.
