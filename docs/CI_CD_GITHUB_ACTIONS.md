# CI/CD de EventPass con GitHub Actions

El workflow `.github/workflows/eventpass-ci-cd.yml` verifica los seis proyectos Maven independientes en máquinas `ubuntu-latest`. Usa Java 17 para Autenticación y Eventos, y Java 21 para Órdenes, Tickets, Eureka y el Gateway. Al llegar un cambio a `main`, ejecuta `verify` antes de desplegar con Docker Compose en el runner de EC2.

## Preparar la EC2 una vez

La instancia necesita Docker Engine, Docker Compose v2, Buildx, Git, curl y Java 21. En GitHub abre **Settings → Actions → Runners → New self-hosted runner**, elige Linux y X64 y sigue los comandos que GitHub genera para la versión vigente del runner. Esos comandos incluyen un token temporal: introdúcelo solo en la terminal de EC2 y no lo guardes en el repositorio ni en capturas.

Configura el runner con la URL de este repositorio y la etiqueta `eventpass-ec2`:

```bash
./config.sh \
  --url https://github.com/dOkuro-01/EventPass \
  --token TOKEN_TEMPORAL_DE_GITHUB \
  --name eventpass-ec2 \
  --labels eventpass-ec2
```

Instálalo como servicio con el usuario que ejecuta Docker y comprueba en GitHub que aparezca `Idle` con las etiquetas `self-hosted`, `Linux`, `X64` y `eventpass-ec2`:

```bash
sudo ./svc.sh install ec2-user
sudo ./svc.sh start
sudo ./svc.sh status
```

Mantén el repositorio confiable: el runner tiene acceso amplio al host por Docker y el job de despliegue solo se ejecuta con `push` a `main`, nunca con un `pull_request`.

## Archivo de entorno de despliegue

El job conserva el volumen MySQL usando el nombre de proyecto Compose `eventpass`. Mantén el archivo de entorno fuera del checkout en `~/EventPass/.env`. Parte de `.env.example`, conserva la contraseña que corresponde al volumen MySQL existente y añade la URL real de SQS y el secreto compartido de Lambda. No subas `.env` a GitHub.

El workflow ejecuta `docker compose config --quiet`, reconstruye y espera los servicios y luego comprueba que `http://localhost:8080/api/eventos` responda HTTP 200. La regla del grupo de seguridad debe permitir el acceso de los clientes al Gateway según el uso previsto.

## Ejecución

Al hacer push a `main`, revisa **Actions → EventPass CI y despliegue EC2**. El job de despliegue queda en cola si el runner está apagado u offline. Si CI falla, el job de EC2 no se ejecuta. Si el Gateway no responde, revisa el primer error de `docker compose`, `docker compose ps`, `docker compose logs api-gateway eventos` y el espacio libre de la instancia.
