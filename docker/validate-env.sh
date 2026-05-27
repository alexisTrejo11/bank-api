#!/usr/bin/env bash
# Fail fast if required .env variables are missing.
# Usage (from repo root):
#   ./docker/validate-env.sh app      # deploy / docker/compose.yml
#   ./docker/validate-env.sh local    # docker/compose.local.yml (+ monitoring)
#
# Then run Compose with --env-file .env (see docker/README.md), e.g.:
#   docker compose --env-file .env -f docker/compose.local.yml up -d --build
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCKER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${ENV_FILE:-$ROOT/.env}"

if [[ ! -f "$ENV_FILE" ]]; then
	echo "Missing $ENV_FILE — copy from .env.example" >&2
	exit 1
fi

# Compose loads .env next to the compose file (docker/). Symlink repo-root .env so
# `cd docker && docker compose …` works without passing --env-file every time.
compose_env_link="$DOCKER_DIR/.env"
if [[ ! -e "$compose_env_link" ]]; then
	ln -sf ../.env "$compose_env_link"
fi

load_env() {
	while IFS= read -r line || [[ -n "$line" ]]; do
		[[ "$line" =~ ^[[:space:]]*# ]] && continue
		[[ -z "${line//[[:space:]]/}" ]] && continue
		local key="${line%%=*}"
		local val="${line#*=}"
		key="${key#"${key%%[![:space:]]*}"}"
		key="${key%"${key##*[![:space:]]}"}"
		val="${val#"${val%%[![:space:]]*}"}"
		val="${val%"${val##*[![:space:]]}"}"
		if [[ "$val" == \"*\" && "$val" == *\" ]]; then
			val="${val:1:${#val}-2}"
		fi
		export "${key}=${val}"
	done < "$ENV_FILE"
}

load_env

require() {
	local name=$1
	if [[ -z "${!name:-}" ]]; then
		echo "Missing required env: $name (set in $ENV_FILE)" >&2
		exit 1
	fi
}

MODE="${1:-app}"

APP_VARS=(
	SPRING_DATASOURCE_URL
	SPRING_DATASOURCE_USERNAME
	SPRING_DATASOURCE_PASSWORD
	SPRING_DATA_REDIS_HOST
	SPRING_DATA_REDIS_PORT
	SPRING_KAFKA_BOOTSTRAP_SERVERS
	SPRING_KAFKA_CONSUMER_GROUP_ID
	MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE
	APP_HTTP_PORT
	BANK_API_IMAGE_TAG
	BANK_KAFKA_ENABLED
)

LOCAL_VARS=(
	POSTGRES_DB
	POSTGRES_USER
	POSTGRES_PASSWORD
	POSTGRES_PORT
	NGINX_HTTP_PORT
	PROMETHEUS_SCRAPE_TARGET
	PROMETHEUS_METRICS_PATH
	GRAFANA_PROMETHEUS_URL
	PROMETHEUS_PORT
	GRAFANA_PORT
	GRAFANA_ADMIN_USER
	GRAFANA_ADMIN_PASSWORD
)

for v in "${APP_VARS[@]}"; do
	require "$v"
done

if [[ "$MODE" == "local" ]]; then
	for v in "${LOCAL_VARS[@]}"; do
		require "$v"
	done
fi

echo "OK: required variables set for mode=${MODE}"
