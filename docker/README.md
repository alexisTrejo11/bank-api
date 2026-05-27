# Docker

All container assets live under this directory.

| File | Purpose |
|------|---------|
| `Dockerfile` | Multi-stage build for `bank-boot` (context: repository root) |
| `compose.yml` | **Deploy** — `app` only; DB/Redis/Kafka from `.env` at repo root |
| `compose.local.yml` | **Local** — full stack (postgres, kafka, redis, prometheus, grafana, nginx) |
| `infra/` | nginx, Prometheus, Grafana, Logstash configs |
| [MONITORING.md](MONITORING.md) | Metrics: app → Prometheus → Grafana; logs: Loki, Promtail, optional ELK |

## Commands

Compose files live only under `docker/` (no `docker-compose.yml` at the repo root).

**`.env` location:** Spring and Compose both use the file at the **repository root** (`cp .env.example .env`).
Compose interpolates `${VAR}` from `.env` next to the compose file. After you run
`./docker/validate-env.sh`, it creates `docker/.env` → `../.env` so you can run
`docker compose` from inside `docker/` without `--env-file`.

### From repository root

```bash
cp .env.example .env   # if needed
./docker/validate-env.sh app      # required vars for deploy (+ docker/.env link)
./docker/validate-env.sh local    # full local stack (+ docker/.env link)

# Production / VPS — app container only
docker compose --env-file .env -f docker/compose.yml up -d --build

# Local full stack (set in .env: BANK_KAFKA_ENABLED=true, BANK_NOTIFICATIONS_DISPATCH_MODE=kafka,
# SPRING_DATA_REDIS_HOST=redis, SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092)
docker compose --env-file .env -f docker/compose.local.yml up -d --build

# Infra only (postgres, redis, kafka) while running the app with Maven
docker compose --env-file .env -f docker/compose.local.yml up -d postgres redis kafka
```

### From `docker/` (after `validate-env.sh`)

```bash
cd docker
docker compose -f compose.local.yml down    # uses docker/.env → ../.env
docker compose -f compose.local.yml up -d --build
```

Without the symlink, pass `--env-file ../.env` explicitly.

Build image without Compose:

```bash
docker build -f docker/Dockerfile -t bank-api:local .
```

Optional TLS certs for nginx:

```bash
./docker/infra/nginx/gen-certs.sh
```

## Layout

```
docker/
├── Dockerfile
├── .dockerignore
├── compose.yml
├── compose.local.yml
├── README.md
└── infra/
    ├── nginx/
    ├── prometheus/
    ├── grafana/
    ├── loki/
    ├── promtail/
    └── logstash/
```

The repository root `.dockerignore` is a symlink to `docker/.dockerignore` (Docker reads ignore rules from the build context root).
