# Observability

Operational guide for logs and metrics. Infrastructure context: [project/generated/ProjectInfrastructure.md](project/generated/ProjectInfrastructure.md). Docker wiring: [docker/MONITORING.md](../docker/MONITORING.md).

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

Loki push is **enabled by default** (`BANK_LOGGING_LOKI_ENABLED=true`). Set `LOKI_URL` in `.env`:

```env
LOKI_URL=http://localhost:3100/loki/api/v1/push
```

For the Docker stack:

```env
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

All files live under `BANK_LOGGING_DIRECTORY` (default `logs/`). The directory is created on first write.

| File | Logger | Console | Loki label |
|------|--------|---------|------------|
| `logs/app.json` | `root` | Yes | `log_type=application` |
| `logs/audit.json` | `AUDIT` | No | `log_type=audit` |
| `logs/access.json` | `ACCESS` | No | `log_type=access` |

- **Local / default profile**: console is human-readable (colored).
- **Docker profile**: console is JSON (for container log collectors).

Rotation: `BANK_LOGGING_MAX_FILE_SIZE_MB`, `BANK_LOGGING_MAX_HISTORY_DAYS`, `BANK_LOGGING_TOTAL_SIZE_CAP_MB` (per file type).

## What not to ship to central monitoring

- Hibernate SQL (unless `BANK_LOGGING_SQL_DEBUG=true` locally)
- Actuator scrape traffic (excluded from access log)
- Debug stubs for email/SMS/Kafka
- Full HTML email bodies (stubs log size only at DEBUG)
