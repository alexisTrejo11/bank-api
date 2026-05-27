# Observability — logging, metrics, and dashboards

Human-readable reference for how the bank API emits logs and metrics. Implementation lives in `bank-boot` (`logback-spring.xml`, filters) and `docker/infra/`.

## Stack overview

| Signal | Local (`docker/compose.local.yml`) | Application config |
|--------|-----------------------------------|--------------------|
| **Metrics** | Prometheus scrapes `app:8080/actuator/prometheus` | `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` |
| **Dashboards** | Grafana → Prometheus + Loki datasources | `GRAFANA_PROMETHEUS_URL`, `GRAFANA_LOKI_URL` |
| **App logs (ops)** | JSON stdout → optional Loki push | `BANK_LOGGING_LOKI_ENABLED`, `LOKI_URL` |
| **Compliance logs** | `logs/audit.json`, `logs/access.json` → Promtail → Loki | `bank.logging.file.*`, loggers `AUDIT` / `ACCESS` |
| **ELK (optional)** | `docker compose --profile elk …` | `BANK_LOGGING_LOGSTASH_ENABLED=true` |

```text
HTTP request
  → RequestCorrelationFilter (MDC: traceId, spanId, requestId, module)
  → JwtAuthenticationFilter (MDC: userId)
  → Controller / domain
  → RequestAccessAuditFilter → ACCESS logger → logs/access.json (not console)

Domain event (after commit)
  → AuditEventListener → AUDIT logger → logs/audit.json (not console)
                      → PostgreSQL audit table

Root logger → JSON console (+ Loki / Logstash when enabled)
```

## Log channels

### 1. Application (`root` logger)

- **Output:** JSON to stdout (`LogstashEncoder`), optional Loki / Logstash TCP.
- **Level policy:** `INFO` for `io.github.alexistrejo11.bank`; `WARN` for Hibernate SQL, Spring, Kafka, Hikari unless `BANK_LOGGING_SQL_DEBUG=true`.
- **Not for:** per-request audit (use `ACCESS`), compliance domain events (use `AUDIT`).

### 2. HTTP access (`ACCESS` logger)

- **Class:** `RequestAccessAuditFilter` (`bank-boot`).
- **Fields:** `method`, `path`, `status`, `durationMs`, `userId`, `clientIp`, `traceId`, `requestId`, `module`.
- **File:** `${BANK_LOGGING_DIRECTORY}/access.json` with size/time rotation (defaults: 50 MB/file, 14 days, 500 MB total cap).
- **Console:** never (`additivity=false`).
- **Skipped paths:** `/actuator/*`, Swagger, OpenAPI.

### 3. Compliance audit (`AUDIT` logger)

- **Writers:** `AuditEventListener`, `AuditKafkaConsumer` (when Kafka enabled).
- **Fields:** `eventCategory=AUDIT`, `eventType`, `actorId`, `entityType`, `entityId`, `eventId`.
- **File:** `${BANK_LOGGING_DIRECTORY}/audit.json` (same rotation as access).
- **Console:** never. Primary store remains the `audit` DB table.

## MDC fields

| Field | Set by | Example |
|-------|--------|---------|
| `traceId` | `RequestCorrelationFilter` (header `X-Trace-Id` or generated) | UUID |
| `spanId` | `RequestCorrelationFilter` | 16-char hex |
| `requestId` | `RequestCorrelationFilter` (header `X-Request-Id` or traceId) | UUID |
| `module` | Path segment after `/api/v1/` | `payments`, `accounts` |
| `userId` | `JwtAuthenticationFilter` when JWT valid | UUID |

## Environment variables

See [`.env.example`](../../../.env.example) section **Logging / observability**.

| Variable | Purpose |
|----------|---------|
| `BANK_LOGGING_DIRECTORY` | Base dir for `audit.json` / `access.json` (`/app/logs` in Docker) |
| `BANK_LOGGING_MAX_FILE_SIZE_MB` | Per-file roll size |
| `BANK_LOGGING_MAX_HISTORY_DAYS` | Retained rolled files |
| `BANK_LOGGING_TOTAL_SIZE_CAP_MB` | Max total size per channel |
| `BANK_LOGGING_ACCESS_ENABLED` | Toggle HTTP access file logging |
| `BANK_LOGGING_SQL_DEBUG` | `true` → Hibernate SQL at DEBUG (local only) |
| `BANK_LOGGING_LOKI_ENABLED` | Push root logs to Loki |
| `LOKI_URL` | e.g. `http://loki:3100/loki/api/v1/push` |
| `BANK_LOGGING_LOGSTASH_ENABLED` | TCP JSON to Logstash (use with `--profile elk`) |

## Logstash pipeline

`docker/infra/logstash/pipeline/bank.conf` drops:

- `DEBUG` / `TRACE`
- Hibernate and Kafka client loggers
- Actuator paths

Tags `compliance` on `AUDIT` / `ACCESS` logger names for index routing.

## AWS deployment notes

- Mount **EBS** or ship `logs/*.json` to CloudWatch Logs / OpenSearch; keep rotation caps to avoid filling disk on ECS tasks.
- Prefer **CloudWatch** or **Grafana Cloud** for metrics; map `/actuator/prometheus` scrape in AMP or ADOT.
- Do not rely on console for audit evidence — use `AUDIT` / `ACCESS` files or DB audit table.
- Set `BANK_LOGGING_LOKI_ENABLED=false` when using a sidecar (Fluent Bit / FireLens) to scrape stdout instead.

## Business-layer logging

- **Application handlers:** minimal logging; failures use `WARN`.
- **Infrastructure stubs** (email/SMS/Kafka stub): `DEBUG` to avoid noise in Loki/Prometheus environments.
- **Domain layer:** no loggers (keep side effects in application/infrastructure).
