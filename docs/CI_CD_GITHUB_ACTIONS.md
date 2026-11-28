# CI/CD de EventPass: build, SAM y Docker Swarm

El workflow `.github/workflows/eventpass-ci-cd.yml` tiene tres etapas ordenadas:

1. `ci`: compila y ejecuta `mvn verify` en los seis proyectos Maven independientes. Autenticación y Eventos usan Java 17; Órdenes, Tickets, Eureka y API Gateway usan Java 21. Corre en cada pull request y push a `main`.
2. `provision-serverless`: solo después de CI verde y en push a `main`, usa AWS SAM desde el runner EC2 para crear/actualizar SQS, Lambda, CloudWatch y los seis repositorios ECR.
3. `deploy-swarm`: corre en el manager EC2 autohospedado, crea y publica las imágenes etiquetadas con el SHA, despliega el stack Swarm y comprueba HTTP 200 en `/api/eventos`.

## Configuración única de AWS y GitHub

Antes de activar el despliegue, inicia el Learner Lab y registra el runner autohospedado en la EC2 manager, siguiendo la guía de CI/CD. El workflow utiliza el perfil IAM asociado a esa instancia.

Configura los valores siguientes en **GitHub → Settings → Secrets and variables → Actions**:

| Tipo | Nombre | Valor |
| --- | --- | --- |
| Secret | `INTERNAL_API_TOKEN` | Secreto aleatorio de al menos 32 caracteres; debe coincidir entre Lambda y los contenedores |
| Secret | `MYSQL_ROOT_PASSWORD` | Contraseña root de la base MySQL; conserva la que ya tenga el volumen persistente |
| Variable | `AWS_REGION` | Región del laboratorio, por ejemplo `us-east-1` |
| Variable | `EVENTPASS_API_BASE_URL` | URL HTTPS base que permite a Lambda alcanzar el Gateway de EventPass y enruta `/api/...` |

El perfil IAM del manager debe permitir a SAM crear y actualizar los recursos declarados en `aws/sam/template.yaml`, subir el paquete de Lambda a S3, operar CloudFormation y pasar a Lambda únicamente el rol `OrderIssuerRole`. También requiere publicar imágenes en `eventpass/*`, consultar las salidas del stack y obtener el ID de la cuenta para iniciar sesión en ECR. La política de ejemplo `aws/iam/github-actions-deploy-policy.json` muestra los permisos de publicación y consulta; el administrador del laboratorio debe autorizar además los permisos de provisión SAM. Si AWS Academy deniega IAM, CloudFormation u otra acción requerida, el pipeline no puede completar la provisión hasta que se autorice.

## URL que usa Lambda

`EVENTPASS_API_BASE_URL` es el origen HTTPS sin barra final; por ejemplo `https://api.ejemplo.cl`. Esa URL debe aceptar las rutas `/api/eventos/{id}/reservas`, `/api/tickets/internal/emision` y `/api/ordenes/internal/{id}/estado`, y reenviarlas al Gateway de Swarm. Si el Gateway solo está disponible como `http://IP-PUBLICA:8080`, prepara primero un endpoint HTTPS/balanceador y una regla de red adecuada; no guardes tokens en la URL.

## Preparar el manager EC2

Inicializa el manager y workers con [la guía de Docker Swarm](DOCKER_SWARM.md). Instala en el manager Docker Engine/CLI, AWS CLI, AWS SAM CLI y GitHub Actions Runner. Registra el runner en este repositorio con las etiquetas `self-hosted`, `Linux`, `X64` y `eventpass-manager`; instala el servicio con el usuario que puede ejecutar Docker. No compartas el token temporal del runner.

El perfil IAM del manager necesita los permisos anteriores para SAM y ECR. El perfil de cada nodo que pueda ejecutar `ordenes` necesita `sqs:SendMessage` limitado a `laboratorio-pedidos`; el servicio obtiene credenciales de EC2 mediante IMDSv2. Revisa el hop limit de IMDSv2, puertos del Security Group y ruta hacia SQS/ECR antes del primer despliegue.

## Verificación

Después del push revisa **Actions → EventPass CI, AWS SAM y Docker Swarm**. Si CI falla, SAM y Swarm no se ejecutan. Si SAM falla, no se publican imágenes ni se actualiza el stack. Si el runner no está `Idle`, el job `deploy-swarm` queda esperando. Para diagnosticar el manager:

```bash
docker node ls
docker stack services eventpass
docker stack ps --no-trunc eventpass
docker service logs eventpass_api-gateway
```

El workflow espera que el Gateway responda HTTP 200; si falla, comprueba despliegues pendientes, conectividad de base de datos, espacio en disco, y la URL y los secretos configurados en GitHub. No imprimas los valores de secretos en logs.
