# Clúster y despliegue Docker Swarm

## Arquitectura

El clúster tiene un manager EC2 y uno o más workers EC2 en la misma VPC. El workflow de GitHub Actions corre en el manager, crea y publica las seis imágenes en ECR, y ejecuta `docker stack deploy`. Swarm distribuye los servicios por la red overlay y publica el API Gateway en el puerto 8080 mediante routing mesh.

MySQL conserva sus datos en un volumen local y queda fijado al manager, que se etiqueta como nodo de datos. No elimines ni reemplaces ese nodo/volumen sin respaldar MySQL. Para alta disponibilidad de la base de datos se requiere un servicio de base de datos compartido; el volumen local no replica los datos entre EC2.

## Security Group de los nodos

Usa reglas entre miembros del mismo Security Group para tráfico interno del clúster. No abras los puertos de control de Swarm a `0.0.0.0/0`.

| Puerto | Protocolo | Origen | Uso |
| --- | --- | --- | --- |
| 2377 | TCP | Security Group del clúster | Control manager/worker; entra al manager |
| 7946 | TCP y UDP | Security Group del clúster | Descubrimiento de nodos |
| 4789 | UDP | Security Group del clúster | Red overlay/VXLAN |
| 22 | TCP | IP administrativa | SSH |
| 8080 | TCP | Clientes autorizados | API Gateway por routing mesh |

## Inicializar un manager

Conecta por SSH a la EC2 que será manager. Los scripts soportan Amazon Linux con `dnf` y Ubuntu/Debian con `apt-get`. El script usa IMDSv2 para elegir la IP privada; puedes fijarla explícitamente con `SWARM_ADVERTISE_ADDR`.

```bash
git clone https://github.com/dOkuro-01/EventPass.git
cd EventPass
sudo bash swarm/init-manager.sh
```

El script instala y activa Docker, inicializa Swarm si hace falta y etiqueta el manager para hospedar MySQL. Copia el comando `docker swarm join --token ...` que imprime y entrégalo solo a las EC2 worker autorizadas. El token permite incorporar nodos al clúster.

## Incorporar workers

En cada EC2 worker, usa la IP privada del manager y el token worker recién generado:

```bash
git clone https://github.com/dOkuro-01/EventPass.git
cd EventPass
sudo env \
  SWARM_MANAGER_ADDR=10.0.1.10:2377 \
  SWARM_JOIN_TOKEN='TOKEN_WORKER_DEL_MANAGER' \
  bash swarm/init-worker.sh
```

Comprueba en el manager que todos los nodos estén `Ready` y `Active`:

```bash
sudo docker node ls
sudo docker node inspect self --format '{{ .Spec.Labels }}'
```

Si el manager reinicia, Swarm conserva el estado local. Guarda de forma segura el token de recuperación del manager (`docker swarm unlock-key`, si se activa autolock). No lo incluyas en GitHub ni en el repositorio.

## Secretos y permisos de AWS

El rol de instancia de cada nodo que pueda ejecutar `ordenes` necesita `sqs:SendMessage` limitado a la cola `laboratorio-pedidos`; usa `aws/iam/ec2-sqs-send-policy.json`. El rol OIDC de GitHub para publicar imágenes está descrito en `aws/iam/github-actions-deploy-policy.json`. El SDK Java usa el proveedor de credenciales de EC2, no claves AWS dentro del contenedor. Habilita IMDSv2 y configura el hop limit en 2 para que los contenedores puedan obtener credenciales; no desactives IMDSv2.

El entorno de despliegue se entrega temporalmente al script desde GitHub Actions. El script no escribe `.env` dentro del checkout y elimina su archivo temporal al terminar. Mantén iguales `MYSQL_ROOT_PASSWORD` e `INTERNAL_API_TOKEN` entre los despliegues; conserva la contraseña del volumen MySQL que ya exista.

## Despliegue

En `main`, tras pasar `verify` y la provisión SAM, GitHub publica las imágenes `eventpass/*:<commit>` en ECR. El runner manager se identifica con la etiqueta `eventpass-manager`. El paso final ejecuta:

```bash
docker stack deploy --with-registry-auth --prune \
  --compose-file deploy/swarm-stack.yml eventpass
```

`--with-registry-auth` permite que los workers descarguen las imágenes privadas de ECR. Consulta el estado y errores con:

```bash
docker stack services eventpass
docker stack ps --no-trunc eventpass
docker service logs eventpass_api-gateway
```

La comprobación del pipeline requiere que `http://localhost:8080/api/eventos` responda HTTP 200. En AWS, restringe el puerto 8080 en el Security Group a los clientes previstos o publica el servicio detrás de un balanceador HTTPS.
