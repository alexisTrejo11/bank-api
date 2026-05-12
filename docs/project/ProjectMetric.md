# Project metrics

Consolidated metrics from the overview and infrastructure models. Numeric “traffic” values on diagram nodes in source are **illustrative only** (for UI renderers), not measured production QPS.

## Product snapshot (overview)

| Label | Value | Description | Trend |
|-------|--------|-------------|--------|
| Maven modules | **9** | `bank-shared` … `bank-boot` | Stable |
| Versioned REST paths | **17** | `/api/v1/*` controller routes (excluding actuator and static OpenAPI assets) | Up |
| Java LTS | **21** | Temurin in Docker; virtual-thread-ready | Stable |
| Spring Boot | **4.0.5** | Parent BOM | Stable |

### Snapshot bullets (from overview key metrics)

- Nine Maven modules including **`bank-config`** and **`bank-boot`**.  
- **17** HTTP JSON endpoints under `/api/v1` (plus Actuator and OpenAPI routes).  
- **PostgreSQL 16**, **Redis 7**, **Kafka** (Compose: ZooKeeper + Confluent broker).  
- **Rate limiting**: optional Redis **global** bucket plus annotated profiles (**STRICT**, **STANDARD**, **SENSITIVE_OPERATIONS**).  
- **Target production**: **AWS ECS + RDS + ElastiCache + MSK** (see [Infrastructure](InfrastructureModel.md)).

## Infrastructure and operations (infra model)

| Label | Value | Meaning |
|-------|--------|---------|
| Compose services | **11** | zookeeper, kafka, postgres, redis, elasticsearch, logstash, kibana, prometheus, grafana, app, nginx |
| Actuator health checks (image) | **1** | Dockerfile `HEALTHCHECK` → `GET /actuator/health` |
| Spring profiles (illustrative) | **4+** | default, test, postgres, docker — add `aws` when you split config by account |
| Hikari default max pool | **10** | Spring Boot default unless overridden |
| Nginx `/api/` zone | **30 r/s** | `limit_req_zone` in `infra/nginx/nginx.conf` |
| Global app bucket (when limiting on) | **64 @ 1/s** | `RateLimitingProperties.GlobalBucket` defaults |
| STRICT profile default | **12 @ 0.2/s** | Auth endpoints (`RateLimitProfile.STRICT`) |
| SENSITIVE_OPERATIONS default | **6 @ 0.1/s** | Loan write endpoints |

## How to use this page

- For **SLAs and SLOs**, replace placeholders with values from CloudWatch or your APM tool once AWS is live.  
- For **portfolio JSON**, the same numbers live under `metrics` in [`source/ProjectOverview.md`](source/ProjectOverview.md) and [`source/ProjectInfrastructure.md`](source/ProjectInfrastructure.md).
