# INFRA.md — Infrastructure, Kafka, nginx & Docker specification

> Read this file before working on any infrastructure task, Docker Compose service,
> nginx config, Kafka topic, or consumer group. All decisions here are final for v1.

---

## Stack overview

| Concern | Technology | Version |
|---|---|---|
| Reverse proxy | nginx | 1.25-alpine |
| Container orchestration | Docker Compose | v2 |
| App runtime | Spring Boot | 3.x on Java 21 |
| Message broker | Apache Kafka | 3.6 |
| Kafka coordination | Zookeeper | 3.8 |
| Primary DB | PostgreSQL | 16-alpine |
| Cache / sessions | Redis | 7-alpine |
| Metrics | Prometheus + Grafana | latest stable |
| Log pipeline | Logstash + Elasticsearch + Kibana | 8.x |

---

## 1. Docker Compose

### 1.1 Service map

| Service name | Image | Internal port | Purpose |
|---|---|---|---|
| `nginx` | `nginx:1.25-alpine` | 80, 443 | Reverse proxy, TLS termination, LB |
| `app` | local build (multi-stage) | 8080–8082 | Spring Boot modular monolith (scale=3) |
| `postgres` | `postgres:16-alpine` | 5432 | Primary relational DB |
| `redis` | `redis:7-alpine` | 6379 | JWT blocklist, idempotency keys, sessions |
| `zookeeper` | `confluentinc/cp-zookeeper:7.5` | 2181 | Kafka cluster coordination |
| `kafka` | `confluentinc/cp-kafka:7.5` | 9092 | Message broker |
| `prometheus` | `prom/prometheus:latest` | 9090 | Metrics scrape + storage |
| `grafana` | `grafana/grafana:latest` | 3000 | Dashboards |
| `elasticsearch` | `elasticsearch:8.11` | 9200 | Log storage |
| `logstash` | `logstash:8.11` | 5044 | Log pipeline (JSON → ES) |
| `kibana` | `kibana:8.11` | 5601 | Log exploration UI |

### 1.2 Requirements

- **R-D1** — `app` service must use a multi-stage Dockerfile. Stage 1: `gradle:8-jdk21` builds the fat jar. Stage 2: `eclipse-temurin:21-jre-alpine` runs it. Final image must be under 300MB.
- **R-D2** — `app` scales to 3 replicas via `deploy.replicas: 3` (Compose v2) or `--scale app=3`. Each instance binds a different host port (8080, 8081, 8082).
- **R-D3** — `postgres` and `redis` must define `healthcheck` blocks. `app` depends on both with `condition: service_healthy`.
- **R-D4** — All secrets (DB password, JWT keys, Redis URL, Kafka bootstrap) injected via `.env` file. `.env` is in `.gitignore`. A `.env.example` with placeholder values is committed.
- **R-D5** — Named volumes for persistent data: `bank_postgres_data`, `bank_kafka_data`, `bank_es_data`.
- **R-D6** — A `docker-compose.override.yml` exists for local dev. It mounts source and enables Spring DevTools. The base `docker-compose.yml` is production-ready.
- **R-D7** — All services run on a single Docker network `bank_network` (bridge driver). No service exposes ports to the host except nginx (443), grafana (3000), kibana (5601), prometheus (9090).

### 1.3 Dockerfile (multi-stage)

```dockerfile
# Stage 1 — build
FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle bootJar --no-daemon

# Stage 2 — runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S bank && adduser -S bank -G bank
COPY --from=build /app/build/libs/*.jar app.jar
USER bank
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-jar", "app.jar"]
```

### 1.4 Healthchecks

```yaml
# postgres
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U $$POSTGRES_USER -d $$POSTGRES_DB"]
  interval: 10s
  timeout: 5s
  retries: 5

# redis
healthcheck:
  test: ["CMD", "redis-cli", "ping"]
  interval: 10s
  timeout: 3s
  retries: 5
```

---

## 2. nginx

### 2.1 Requirements

- **R-N1** — nginx is the single entry point. No client reaches Spring Boot directly.
- **R-N2** — HTTP on port 80 redirects to HTTPS 443 with `301`. No exceptions.
- **R-N3** — TLS terminates at nginx. Self-signed cert for local dev (generated via `openssl`). Spring Boot listens on plain HTTP internally.
- **R-N4** — Load balancing strategy: `least_conn` across 3 app instances.
- **R-N5** — Rate limiting: 30 req/s per IP with burst of 10. Applied to all `/api/` routes. Health endpoint `/actuator/health` is exempt.
- **R-N6** — Security headers set by nginx (never by Spring): `X-Frame-Options DENY`, `X-Content-Type-Options nosniff`, `Strict-Transport-Security max-age=31536000`, `X-XSS-Protection 1; mode=block`.
- **R-N7** — Forwarded headers: `X-Real-IP`, `X-Forwarded-For`, `X-Forwarded-Proto` passed to Spring. Spring must register `ForwardedHeaderFilter` bean.
- **R-N8** — Gzip enabled for `application/json` responses with `gzip_min_length 1024`.
- **R-N9** — nginx config mounted as read-only volume into container. Never baked into image.

### 2.2 nginx.conf skeleton

```nginx
worker_processes auto;

events { worker_connections 1024; }

http {
  limit_req_zone $binary_remote_addr zone=api_limit:10m rate=30r/s;

  gzip on;
  gzip_types application/json text/plain;
  gzip_min_length 1024;

  upstream bank_app {
    least_conn;
    server app:8080;
    server app:8081;
    server app:8082;
  }

  server {
    listen 80;
    return 301 https://$host$request_uri;
  }

  server {
    listen 443 ssl;
    ssl_certificate     /etc/nginx/certs/bank.crt;
    ssl_certificate_key /etc/nginx/certs/bank.key;
    ssl_protocols       TLSv1.2 TLSv1.3;

    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header Strict-Transport-Security "max-age=31536000" always;

    location /api/ {
      limit_req zone=api_limit burst=10 nodelay;
      proxy_pass http://bank_app;
      proxy_set_header Host              $host;
      proxy_set_header X-Real-IP         $remote_addr;
      proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
      proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /actuator/health {
      proxy_pass http://bank_app;
    }
  }
}
```

### 2.3 Self-signed cert generation (local dev)

```bash
mkdir -p infra/nginx/certs
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout infra/nginx/certs/bank.key \
  -out infra/nginx/certs/bank.crt \
  -subj "/CN=localhost/O=BankDev"
```

Certs are mounted into the nginx container. Never committed to git — add `infra/nginx/certs/` to `.gitignore`.

---

## 3. Kafka

### 3.1 Design principles

- **Kafka replaces `ApplicationEventPublisher`** in v2. The internal Spring event bus is used in v1 (local Docker Compose without Kafka). This file defines the target v2 Kafka architecture.
- **Topics are domain-scoped**, not event-type-scoped. One topic per bounded context keeps consumer groups clean and reduces operational overhead.
- **Partition key = domain entity ID**. This guarantees ordering per entity within a partition. A `TransferCompleted` event for `transferId=X` always lands in the same partition as `TransferInitiated` for the same ID.
- **Consumers are smart; producers are dumb emitters**. Producers fire and forget. All business logic lives in consumers.
- **`acks=all` + `enable.idempotence=true`** at producer level. No message is ever silently dropped.
- **Every consumer has a DLQ fallback** after 3 retries.

### 3.2 Topic specifications

| Topic | Partitions | Key | Retention | Purpose |
|---|---|---|---|---|
| `bank.transfers` | 3 | `transferId` | 7 days | Transfer lifecycle events |
| `bank.accounts` | 3 | `accountId` | 7 days | Account open/freeze/snapshot events |
| `bank.loans` | 3 | `loanId` | 7 days | Loan lifecycle events |
| `bank.notifications` | 3 | `userId` | 3 days | Notification dispatch events |
| `bank.audit` | 3 | `entityId` | 30 days | All domain events fan-in here |
| `bank.dlq` | 1 | `originalTopic` | 30 days | Dead letter queue for all failed consumers |

Replication factor = 1 for local Docker Compose. Set to 3 when migrating to AWS MSK.

### 3.3 Events per topic

#### `bank.transfers`
| Event | Producer | Payload fields |
|---|---|---|
| `TransferInitiatedEvent` | payments module | transferId, sourceAccountId, targetAccountId, amount, currency, idempotencyKey, timestamp |
| `TransferCompletedEvent` | payments module | transferId, sourceAccountId, targetAccountId, amount, currency, timestamp |
| `TransferFailedEvent` | payments module | transferId, reason, timestamp |
| `TransferReversedEvent` | payments module | transferId, originalTransferId, amount, timestamp |

#### `bank.loans`
| Event | Producer | Payload fields |
|---|---|---|
| `LoanApprovedEvent` | loans module | loanId, accountId, principal, interestRate, termMonths, timestamp |
| `LoanDisbursedEvent` | loans module | loanId, accountId, amount, timestamp |
| `LoanRepaymentCompletedEvent` | loans module | loanId, repaymentId, amount, paidAt |
| `LoanPaidOffEvent` | loans module | loanId, accountId, timestamp |

#### `bank.accounts`
| Event | Producer | Payload fields |
|---|---|---|
| `AccountOpenedEvent` | accounts module | accountId, userId, type, currency, timestamp |
| `AccountFrozenEvent` | accounts module | accountId, reason, timestamp |
| `BalanceSnapshotEvent` | accounts module | accountId, balance, currency, snapshotAt |

#### `bank.notifications`
| Event | Producer | Payload fields |
|---|---|---|
| `NotificationRequestedEvent` | any module | userId, templateKey, params (map), channel (EMAIL/PUSH) |

#### `bank.audit`
All events from all topics are also published here by each module's Kafka producer. The `audit-cg` consumer is the single writer to `audit_records`.

### 3.4 Consumer groups

#### `accounts-cg`
- Consumes: `bank.transfers`, `bank.loans`
- Responsibility: posts ledger entries (double-entry) for every transfer and loan event
- Idempotency guard: check if `LedgerEntry` with `referenceId = eventId` already exists before inserting
- On failure: retry 3x with 2s backoff → publish to `bank.dlq`

```java
@KafkaListener(topics = {"bank.transfers", "bank.loans"}, groupId = "accounts-cg")
public void handle(ConsumerRecord<String, BankDomainEvent> record) { ... }
```

#### `audit-cg`
- Consumes: `bank.transfers`, `bank.loans`, `bank.accounts`, `bank.notifications`
- Responsibility: writes one `AuditRecord` per event — append only, never update
- Must be `@Async` — never block the consumer thread
- On failure: retry 3x → `bank.dlq` (audit loss is acceptable but must be tracked)

#### `notifications-cg`
- Consumes: `bank.notifications`
- Responsibility: routes by `templateKey` to email/push handler, writes `notification_log`
- On failure after 3 retries: publish to `bank.dlq`, do NOT retry indefinitely (notification loss is tolerable)

#### `dlq-monitor-cg`
- Consumes: `bank.dlq`
- Responsibility: structured ERROR log with `originalTopic`, `consumerGroup`, `errorMessage`, `payload`
- Exposes: `GET /admin/dlq` paginated list of dead letters
- Exposes: `POST /admin/dlq/{id}/replay` to manually re-publish to original topic

### 3.5 Producer configuration (Spring Kafka)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 1
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false
      properties:
        spring.json.trusted.packages: "io.github.alexistrejo11.bank.*"
```

### 3.6 Error handler (apply to every consumer)

```java
@Bean
public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, ?> template) {
    var recoverer = new DeadLetterPublishingRecoverer(template,
        (record, ex) -> new TopicPartition("bank.dlq", 0));
    var backoff = new FixedBackOff(2000L, 3L);
    return new DefaultErrorHandler(recoverer, backoff);
}
```

### 3.7 Topic init on startup

```java
@Configuration
public class KafkaTopicConfig {
    @Bean public NewTopic transfers()     { return TopicBuilder.name("bank.transfers").partitions(3).replicas(1).build(); }
    @Bean public NewTopic accounts()      { return TopicBuilder.name("bank.accounts").partitions(3).replicas(1).build(); }
    @Bean public NewTopic loans()         { return TopicBuilder.name("bank.loans").partitions(3).replicas(1).build(); }
    @Bean public NewTopic notifications() { return TopicBuilder.name("bank.notifications").partitions(3).replicas(1).build(); }
    @Bean public NewTopic audit()         { return TopicBuilder.name("bank.audit").partitions(3).replicas(1).build(); }
    @Bean public NewTopic dlq()           { return TopicBuilder.name("bank.dlq").partitions(1).replicas(1).build(); }
}
```

---

## 4. Observability

### 4.1 Metrics (Prometheus + Grafana)

- **R-O1** — Spring Actuator exposes `/actuator/prometheus`. Prometheus scrapes every 15s.
- **R-O2** — Grafana datasource and dashboards are provisioned via YAML files in `infra/grafana/provisioning/`. No manual setup.
- **R-O3** — Four dashboards provisioned: JVM health, HTTP request metrics, Kafka consumer lag, Business metrics.
- **R-O4** — Custom metrics registered per module:

| Metric | Type | Tags | Module |
|---|---|---|---|
| `bank.transfer.total` | Counter | `status` | payments |
| `bank.transfer.amount` | DistributionSummary | `currency` | payments |
| `bank.account.opened.total` | Counter | `type` | accounts |
| `bank.loan.active.count` | Gauge | — | loans |
| `bank.loan.delinquency.rate` | Gauge | — | loans |
| `bank.notification.sent.total` | Counter | `channel`, `status` | notifications |
| `bank.dlq.messages.total` | Counter | `originalTopic` | audit |

### 4.2 Structured logging (ELK)

- **R-O5** — Logback uses `LogstashEncoder` (dependency: `logstash-logback-encoder`). All output is JSON to stdout.
- **R-O6** — Every log line must carry these MDC fields: `traceId`, `spanId`, `userId`, `module`, `requestId`.
- **R-O7** — `MDCFilter` (OncePerRequestFilter) sets `requestId` (UUID), `userId` (from JWT SecurityContext), `module` (derived from request path prefix). Cleared in `finally`.
- **R-O8** — Never log: raw passwords, full JWT tokens, card numbers, account numbers in full, PII beyond `userId`.
- **R-O9** — Logstash pipeline: TCP input port 5044, JSON codec, date filter on `timestamp` field, output to Elasticsearch index `bank-logs-{YYYY.MM.dd}`.

### 4.3 Logback config skeleton

```xml
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <customFields>{"app":"bank-system"}</customFields>
    </encoder>
  </appender>
  <root level="INFO">
    <appender-ref ref="STDOUT"/>
  </root>
</configuration>
```

---

## 5. AWS migration checklist (future)

When moving from Docker Compose to AWS, apply these changes in order:

1. Replace `kafka` container → Amazon MSK (set replication factor to 3)
2. Replace `postgres` container → Amazon RDS PostgreSQL Multi-AZ
3. Replace `redis` container → Amazon ElastiCache Redis cluster
4. Replace `nginx` container → AWS Application Load Balancer (ALB handles TLS via ACM)
5. Replace `prometheus` + `grafana` → Amazon Managed Grafana + CloudWatch datasource
6. Replace ELK stack → Amazon OpenSearch Service
7. Replace Docker Compose `scale` → ECS Fargate task definition with desired count = 3
8. Move `.env` secrets → AWS Secrets Manager + Parameter Store, injected via ECS task role

---

## 6. Implementation order (infra phase)

This is the recommended PR sequence for `feature/infra-layer`:

| Step | Task | Branch |
|---|---|---|
| 1 | Multi-stage Dockerfile + basic compose | `feature/infra-docker-scaffold` |
| 2 | nginx config + self-signed TLS + LB | `feature/infra-nginx` |
| 3 | Kafka + Zookeeper + topic init bean | `feature/infra-kafka-setup` |
| 4 | Replace ApplicationEvent → KafkaTemplate | `feature/infra-kafka-producers` |
| 5 | Consumer groups: accounts-cg, audit-cg | `feature/infra-kafka-consumers` |
| 6 | notifications-cg + dlq-monitor-cg | `feature/infra-kafka-dlq` |
| 7 | Prometheus + Grafana provisioning | `feature/infra-metrics` |
| 8 | Logstash + ES + Kibana + MDC filter | `feature/infra-logging` |

Each step is a self-contained PR into `develop`. Do not merge a later step until the previous one has passing CI.

---

## 7. File structure for infra code

```
infra/
  nginx/
    nginx.conf
    certs/           ← gitignored, generated locally
  kafka/
    topics.sh        ← manual topic creation script (backup for KafkaTopicConfig)
  prometheus/
    prometheus.yml
  grafana/
    provisioning/
      datasources/
        prometheus.yml
        elasticsearch.yml
      dashboards/
        jvm.json
        http.json
        kafka.json
        business.json
  logstash/
    pipeline/
      bank.conf
  postgres/
    init/
      01-extensions.sql   ← enable uuid-ossp, pgcrypto
docker-compose.yml
docker-compose.override.yml
.env.example
Dockerfile
```

---

## 8. Non-negotiable rules (infra layer)

1. nginx is the only container that exposes ports to the host on 443. All other ports are internal.
2. TLS always terminates at nginx. Spring Boot never handles TLS.
3. Kafka partition key is always the domain entity ID — never null, never random UUID.
4. Every Kafka consumer must have a `DefaultErrorHandler` wired to `bank.dlq`. No silent failures.
5. `acks=all` and `enable.idempotence=true` on every Kafka producer. Non-negotiable for financial data.
6. Secrets never in source code or committed `.env`. Always `.env.example` with placeholders.
7. Grafana and Kibana are provisioned via config files — zero manual setup after `docker compose up`.
8. The DLQ topic `bank.dlq` has retention of 30 days. Dead letters must never be auto-deleted quickly.