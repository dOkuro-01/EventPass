#!/usr/bin/env bash
set -euo pipefail

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Ejecuta este script con sudo." >&2
  exit 1
fi

if command -v dnf >/dev/null 2>&1; then
  # Amazon Linux 2023 ships curl-minimal; installing the full curl package
  # conflicts with it. Install curl-minimal only when curl is not available.
  dnf install -y docker
  if ! command -v curl >/dev/null 2>&1; then
    dnf install -y curl-minimal
  fi
elif command -v apt-get >/dev/null 2>&1; then
  apt-get update
  apt-get install -y docker.io curl
else
  echo "Distribución no soportada: instala Docker Engine y vuelve a ejecutar." >&2
  exit 1
fi

systemctl enable --now docker
docker version
