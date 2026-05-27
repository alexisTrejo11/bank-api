#!/usr/bin/env bash
# Generate self-signed TLS material for local / staging (never commit certs/).
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
mkdir -p "${DIR}/certs"
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "${DIR}/certs/bank.key" \
  -out "${DIR}/certs/bank.crt" \
  -subj "/CN=localhost/O=BankDev"
echo "Wrote ${DIR}/certs/bank.crt and bank.key — mount into nginx for HTTPS (optional)."
