#!/usr/bin/env bash
set -euo pipefail
script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Ejecuta este script con sudo." >&2
  exit 1
fi
bash "$script_dir/install-docker.sh"

: "${SWARM_MANAGER_ADDR:?Define SWARM_MANAGER_ADDR como IP_PRIVADA:2377}"
: "${SWARM_JOIN_TOKEN:?Define SWARM_JOIN_TOKEN con el token worker del manager}"

if [[ "$(docker info --format '{{.Swarm.LocalNodeState}}')" != "active" ]]; then
  docker swarm join \
    --token "$SWARM_JOIN_TOKEN" \
    "$SWARM_MANAGER_ADDR"
else
  echo "Este nodo ya pertenece a un Swarm. No se repitió el join."
fi

unset SWARM_JOIN_TOKEN
docker info --format 'Estado Swarm: {{.Swarm.LocalNodeState}}'
