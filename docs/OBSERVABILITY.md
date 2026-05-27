# Observability

Operational guide for logs and metrics. Detailed design: [project/source/Observability.md](project/source/Observability.md). Docker wiring: [docker/MONITORING.md](../docker/MONITORING.md).

## Quick start (local stack)

```bash
./docker/validate-env.sh local
docker compose --env-file .env -f docker/compose.local.yml up -d --build
```

| UI | URL |
|----|-----|
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |
| Loki | http://localhost:3100 |

Suggested `.env` for local observability:

```env
BANK_LOGGING_LOKI_ENABLED=true
LOKI_URL=http://loki:3100/loki/api/v1/push
GRAFANA_LOKI_URL=http://loki:3100
LOKI_PORT=3100
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,prometheus
```

Optional ELK:

```bash
docker compose --env-file .env -f docker/compose.local.yml --profile elk up -d
# Set BANK_LOGGING_LOGSTASH_ENABLED=true and LOGSTASH_HOST=logstash in .env
```

## Log files (on disk)

| File | Logger | Console |
|------|--------|---------|
| `logs/audit.json` | `AUDIT` | No |
| `logs/access.json` | `ACCESS` | No |
| stdout | `root` | Yes (JSON) |

Rotation is configured via `BANK_LOGGING_MAX_FILE_SIZE_MB`, `BANK_LOGGING_MAX_HISTORY_DAYS`, and `BANK_LOGGING_TOTAL_SIZE_CAP_MB`.

## What not to ship to central monitoring

- Hibernate SQL (unless `BANK_LOGGING_SQL_DEBUG=true` locally)
- Actuator scrape traffic (excluded from access log)
- Debug stubs for email/SMS/Kafka
- Full HTML email bodies (stubs log size only at DEBUG)
