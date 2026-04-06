# Docker — run the stack off your laptop

This matches **Phase 8** in [roadmap.md](roadmap.md): multi-stage image, full Compose stack, nginx entry, Prometheus + Grafana, ELK pipeline files.

## Prerequisites

- Docker Engine **24+** and Docker Compose **v2**
- On **Linux**, Elasticsearch may require `sudo sysctl -w vm.max_map_count=262144` (see [Elasticsearch docs](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html))

## One-time setup

```bash
cp .env.example .env
# Set POSTGRES_PASSWORD and matching SPRING_DATASOURCE_PASSWORD if you override defaults.
# Optional: set BANK_SECURITY_JWT_* PEMs for stable JWT keys across restarts (see .env.example).
```

## Build and run everything

From the repository root:

```bash
docker compose up -d --build
```

- **API (via nginx):** `http://localhost` (paths under `/api/`, Swagger under `/swagger-ui.html`)
- **Grafana:** `http://localhost:3000` (default admin / password from `GRAFANA_ADMIN_PASSWORD` in `.env`, default `admin`)
- **Prometheus:** `http://localhost:9090`
- **Kibana:** `http://localhost:5601` (index pattern `bank-logs-*` after logs are shipped)
- **Logstash:** TCP **5044** inside the network (configure an appender separately if you want logs in ES)

The **Spring Boot** service is named **`app`** and is **not** published on the host by default; use **nginx** on port **80** as the entrypoint.

## TLS (optional)

Self-signed material for nginx HTTPS:

```bash
./infra/nginx/gen-certs.sh
# Then add a second server block in infra/nginx/nginx.conf listening on 443 and mount ./infra/nginx/certs
```

Generated files live under `infra/nginx/certs/` (gitignored).

## Domain-event Kafka (`BANK_KAFKA_ENABLED`)

Compose sets **`BANK_KAFKA_ENABLED`** (default **`false`**). While `false`, the app uses the legacy **`ApplicationEventPublisher`** path so accounts, audit, and notifications keep working without migrating every handler to Kafka.

Set **`BANK_KAFKA_ENABLED=true`** in `.env` only after domain events are published through **`DomainEventPublisher`** and Kafka consumers are enabled (Phase 8 Java work).

## Image size

The **Dockerfile** targets a small runtime image (JRE Alpine + single fat JAR). Check size with:

```bash
docker images bank-api:local
```

## Troubleshooting

- **`app` exits / Flyway errors:** ensure Postgres credentials in `.env` match `SPRING_DATASOURCE_*` used by the container (Compose wires `SPRING_DATASOURCE_URL` to `postgres:5432` automatically).
- **Kafka not ready:** `docker compose logs -f kafka` — the `app` service waits for Kafka’s healthcheck.
- **Elasticsearch OOM:** lower `ES_JAVA_OPTS` in `docker-compose.yml` or give Docker more memory.
