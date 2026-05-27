# Infrastructure model

Human-readable infrastructure documentation derived from [`source/ProjectInfrastructure.md`](source/ProjectInfrastructure.md). See also [ProjectArchitectureModel.md](ProjectArchitectureModel.md) for where this stack sits in the logical architecture.

## Infra metrics (quick reference)

| Metric | Value | Detail |
|--------|--------|--------|
| Docker Compose services (local) | **10+** | zookeeper, kafka, postgres, redis, prometheus, grafana, loki, promtail, app, nginx (+ optional ELK profile) |
| Container healthcheck | **1×** `GET /actuator/health` | Dockerfile `HEALTHCHECK` — map to ECS / ALB target group health checks |
| Spring profiles | **4+** | default, test, postgres, docker; add `aws` per account when needed |
| Hikari max pool (default) | **10** | Override via Spring datasource properties |
| Nginx `/api/` rate | **30 r/s** | `limit_req_zone` + burst in `infra/nginx/nginx.conf` |
| Global Redis bucket (when enabled) | **64 @ 1/s** | `bank.rate-limiting.global` defaults |
| STRICT profile | **12 @ 0.2/s** | Auth routes |
| SENSITIVE_OPERATIONS | **6 @ 0.1/s** | Loan writes |

## AWS cloud services (target production)

| Service | Purpose | Cost note (illustrative) |
|---------|---------|---------------------------|
| **Amazon ECS (Fargate)** | Run `bank-boot` tasks; autoscaling on CPU/memory; private subnets | Fargate vCPU/GB-hour × task count |
| **Application Load Balancer** | TLS (ACM), health checks to `/actuator/health`, stateless API | LCU-hours + bytes |
| **Amazon RDS (PostgreSQL)** | System of record | Instance + storage; Multi-AZ multiplier |
| **Amazon ElastiCache (Redis)** | Idempotency, refresh/revocation metadata, rate limits | Node type × replicas |
| **Amazon MSK** | Kafka-compatible streaming for notifications / future extraction | Broker hours + storage + egress |
| **AWS Secrets Manager** | JWT keys, DB passwords, third-party secrets | Per-secret monthly + API |
| **Amazon CloudWatch** | Logs, metrics, alarms | Ingestion + storage + alarms |
| **Amazon S3** | Static assets, exports, optional log archive | GB-month + requests |
| **AWS WAF (optional)** | OWASP CRS, rate-based rules on ALB | Web ACL + rules + requests |

## Deployment layers

### 1. Local developer

| Component | Description |
|-----------|-------------|
| **H2 or local PostgreSQL** | Fast feedback; H2 for zero-deps; `postgres` profile for schema parity |
| **Maven wrapper** | `./mvnw -pl bank-boot spring-boot:run` from repo root |
| **Optional Redis** | Full auth, idempotency, and rate limiting behavior |

### 2. Docker Compose (parity / demo host)

| Component | Description |
|-----------|-------------|
| **PostgreSQL 16** | Primary DB; credentials from `.env`; health-checked |
| **Redis 7** | Sessions coordination, idempotency, rate limits when enabled |
| **ZooKeeper + Kafka 7.5** | Confluent images; broker `kafka:9092` on internal network |
| **nginx 1.25** | Reverse proxy **port 80 → `app:8080`**; `30 r/s` zone on `/api/` |
| **Prometheus + Grafana** | Metrics; dashboards under `infra/grafana/provisioning` |
| **Loki + Promtail** | App compliance files under `logs/` + JSON stdout; Grafana explores logs |
| **Elasticsearch + Logstash + Kibana** | Optional Compose profile `elk`; pipeline filters noisy levels |

### 3. AWS production (target)

| Component | Description |
|-----------|-------------|
| **ECS Fargate service** | Same image as local Docker build; env from task definition + Secrets Manager |
| **ALB + ACM** | Public HTTPS; preserve `X-Forwarded-*` — Spring forwarded-header strategy must remain correct |
| **RDS PostgreSQL Multi-AZ** | Automated backups; parameter groups aligned with JPA batch usage |
| **ElastiCache Redis** | Replication group with failover for shared coordination state |
| **MSK** | Private auth (SASL/IAM); security groups only from ECS tasks |
| **CloudWatch + optional OpenSearch** | Central observability; OpenSearch if you need ELK-like search in AWS |

## Dockerfile reference (`bank-api` multi-stage)

The canonical file is **[docker/Dockerfile](../../docker/Dockerfile)**. Summary:

- **Builder:** `eclipse-temurin:21-jdk-alpine`, copies `mvnw` and module `pom.xml` files for dependency prefetch, then full `COPY . .`, then `./mvnw -pl bank-boot -am package -DskipTests`.  
- **Runtime:** `eclipse-temurin:21-jre-alpine`, non-root user `bank`, `JAVA_OPTS` defaults to **ZGC**, exposes **8080**, **HEALTHCHECK** curls `/actuator/health`.  
- **Build command:** `docker build -t bank-api:local .` from repo root.

> **Note:** The prefetch layer does not copy **`bank-config/pom.xml`** today; `dependency:go-offline` may be partial until `COPY . .`. If CI fails, add `COPY bank-config/pom.xml bank-config/` to the prefetch list.

## Operational warnings

- **TLS:** Compose nginx is **HTTP only** on port **80**. Production on AWS must use **ALB + ACM** (or CloudFront).  
- **Grafana defaults:** Rotate weak admin passwords before exposing Grafana on any shared host.  
- **Network:** The `app` container **exposes** 8080 to the Compose network but does not publish it on the host — **nginx** is the API entry point (plus published tool ports such as 9090, 3000, 5601).  
- **Kafka flag:** `BANK_KAFKA_ENABLED` defaults toward **false** in compose; some flows stay in-process — confirm `application-docker.yaml` when documenting MSK cutover.  
- **Prometheus:** Static scrape targets may list a single `app:8080`; on ECS use **service discovery** or an agent sidecar pattern.

## Related documentation

- [Project metadata](ProjectMetadata.md) — stack list  
- [API schema](APISchema.md) — public vs internal routes  
- [Project metric](ProjectMetric.md) — merged metric tables  
