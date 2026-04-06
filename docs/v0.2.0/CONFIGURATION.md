# Externalized configuration and secrets (v0.2.0)

## Principles

- **No secrets in Git** — passwords, PEM keys, and tokens come from the **OS environment** or a local **`.env`** file (see [`.env.example`](../../.env.example)).
- **Spring Boot relaxed binding** — `SPRING_DATASOURCE_PASSWORD`, `spring.datasource.password`, and `SPRING_DATASOURCE_PASSWORD` are equivalent for binding; placeholders in YAML use the canonical names you set in the environment.
- **`.env` loading** — `bank-boot` registers `DotEnvEnvironmentPostProcessor`, which loads `./.env` with **lowest precedence** so real deployment variables and `application-*.yml` override file entries.

## Local setup

1. Copy [`.env.example`](../../.env.example) to `.env` at the repo root.
2. Adjust values; use strong passwords outside sandboxes.
3. For **default H2** development, leave **`SPRING_DATASOURCE_*` unset** in `.env` (or commented) so `application.yaml` defaults apply.
4. For **`postgres` or `docker` Spring profiles**, set **`SPRING_DATASOURCE_URL`**, **`SPRING_DATASOURCE_USERNAME`**, and **`SPRING_DATASOURCE_PASSWORD`** — YAML has **no defaults** for the password in those profiles.

## Docker Compose

Compose reads `.env` for variable substitution. **`POSTGRES_PASSWORD`** is required (see `docker-compose.yml`). Align **`SPRING_DATASOURCE_PASSWORD`** with the database password when the app connects to that Postgres instance.

## JWT keys

If **`BANK_SECURITY_JWT_PRIVATE_KEY_PEM`** and **`BANK_SECURITY_JWT_PUBLIC_KEY_PEM`** are unset, the IAM module generates an **ephemeral RSA key pair** (tokens invalid after restart). For production or multi-instance deployments, set PEM strings (PKCS#8 private, SPKI public, including headers; newlines may be `\n` in `.env`).

## Reference tables

| Area | Primary environment keys |
|------|---------------------------|
| JDBC | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` |
| Redis | `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`, `SPRING_DATA_REDIS_PASSWORD` |
| Kafka | `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SPRING_KAFKA_CONSUMER_GROUP_ID` |
| JWT | `BANK_SECURITY_JWT_PRIVATE_KEY_PEM`, `BANK_SECURITY_JWT_PUBLIC_KEY_PEM` |
| Dotenv path | `BANK_DOTENV_PATH` (optional directory containing `.env`) |

| Spring profile | Config file | Notes |
|----------------|-------------|--------|
| *(default)* | `application.yaml` | H2 + safe defaults for URLs; empty datasource password default |
| `test` | `application-test.yaml` | Isolated H2; used by `mvn verify` |
| `postgres` | `application-postgres.yaml` | JDBC settings **only** from env |
| `docker` | `application-docker.yaml` | JDBC, Redis host, Kafka bootstrap **from env** |
