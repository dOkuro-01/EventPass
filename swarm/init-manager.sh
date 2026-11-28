#!/usr/bin/env bash
set -euo pipefail
script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Ejecuta este script con sudo." >&2
  exit 1
fi
bash "$script_dir/install-docker.sh"

private_ip="${SWARM_ADVERTISE_ADDR:-}"
if [[ -z "$private_ip" ]]; then
  token=$(curl -fsS -X PUT \
    -H 'X-aws-ec2-metadata-token-ttl-seconds: 60' \
    http://169.254.169.254/latest/api/token)
  private_ip=$(curl -fsS \
    -H "X-aws-ec2-metadata-token: $token" \
    http://169.254.169.254/latest/meta-data/local-ipv4)
fi

if [[ "$(docker info --format '{{.Swarm.LocalNodeState}}')" != "active" ]]; then
  docker swarm init --advertise-addr "$private_ip"
fi

node_id=$(docker info --format '{{.Swarm.NodeID}}')
docker node update --label-add eventpass.db=true "$node_id"
echo "Manager listo en $private_ip. Copia el siguiente comando solo a las EC2 worker autorizadas:"
docker swarm join-token worker
