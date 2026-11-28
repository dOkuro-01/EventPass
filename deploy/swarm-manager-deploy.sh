#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="${1:-$HOME/EventPass/.env}"
if [[ "$env_file" != "-" ]]; then
  if [[ ! -r "$env_file" ]]; then
    echo "No se puede leer el archivo de entorno: $env_file" >&2
    exit 1
  fi
  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -z "$line" || "$line" == \\#* ]] && continue
    if [[ "$line" =~ ^([A-Z_][A-Z0-9_]*)=(.*)$ ]]; then
      export "${BASH_REMATCH[1]}=${BASH_REMATCH[2]}"
    else
      echo "Línea inválida en el archivo de entorno: se esperaba CLAVE=VALOR." >&2
      exit 1
    fi
  done < "$env_file"
fi

: "${MYSQL_ROOT_PASSWORD:?Falta MYSQL_ROOT_PASSWORD}"
: "${INTERNAL_API_TOKEN:?Falta INTERNAL_API_TOKEN}"
: "${AWS_REGION:?Falta AWS_REGION}"
: "${PEDIDOS_QUEUE_URL:?Falta PEDIDOS_QUEUE_URL}"
: "${ECR_REGISTRY:?Falta ECR_REGISTRY}"
: "${IMAGE_TAG:?Falta IMAGE_TAG}"

if [[ "$(docker info --format '{{.Swarm.ControlAvailable}}')" != "true" ]]; then
  echo "Este comando debe ejecutarse en un nodo manager Docker Swarm." >&2
  exit 1
fi

docker stack config --compose-file "$repo_root/deploy/swarm-stack.yml" > /dev/null

# Retira el Compose previo sin borrar el volumen persistente eventpass_mysql_data.
docker compose \
  --project-name eventpass \
  --file "$repo_root/docker-compose.yml" \
  down --remove-orphans

docker stack deploy \
  --with-registry-auth \
  --prune \
  --compose-file "$repo_root/deploy/swarm-stack.yml" \
  eventpass

for attempt in {1..60}; do
  gateway_container=$(docker ps \
    --filter 'label=com.docker.swarm.service.name=eventpass_api-gateway' \
    --filter status=running --format '{{.ID}}' | head -n 1)
  if [[ -n "$gateway_container" ]] && docker exec "$gateway_container" \
    wget -q -O /dev/null -T 5 http://127.0.0.1:8080/api/eventos; then
    echo "Docker Swarm desplegado; API Gateway respondió HTTP 200."
    docker stack services eventpass
    exit 0
  fi
  sleep 5
done

echo "El API Gateway no respondió HTTP 200; estado actual:" >&2
docker stack services eventpass >&2 || true
docker stack ps --no-trunc eventpass >&2 || true
exit 1
