# CI/CD de EventPass: build, SAM y Docker Swarm

El workflow `.github/workflows/eventpass-ci-cd.yml` tiene tres etapas ordenadas:

1. `ci`: compila y ejecuta `mvn verify` en los seis proyectos Maven independientes. Autenticación y Eventos usan Java 17; Órdenes, Tickets, Eureka y API Gateway usan Java 21. Corre en cada pull request y push a `main`.
2. `provision-serverless`: solo después de CI verde y en push a `main`, usa AWS OIDC y AWS SAM para crear/actualizar SQS, Lambda, CloudWatch y los seis repositorios ECR.
3. `deploy-swarm`: corre en el manager EC2 autohospedado, crea y publica las imágenes etiquetadas con el SHA, despliega el stack Swarm y comprueba HTTP 200 en `/api/eventos`.

## Configuración única de AWS y GitHub

El navegador abierto al momento de preparar el cambio estaba en AWS Academy Canvas, no en la consola AWS. Antes de activar el despliegue, inicia el Learner Lab y configura el acceso OIDC del repositorio. AWS IAM debe tener el proveedor `token.actions.githubusercontent.com` con audiencia `sts.amazonaws.com`. Usa `aws/iam/github-actions-oidc-trust-policy.json` como política de confianza para dos roles separados y reemplaza `ID_CUENTA`.

Configura los valores siguientes en **GitHub → Settings → Secrets and variables → Actions**:

| Tipo | Nombre | Valor |
| --- | --- | --- |
| Secret | `AWS_GITHUB_SAM_ROLE_ARN` | ARN del rol OIDC que permite desplegar el stack SAM y crear ECR |
| Secret | `AWS_GITHUB_DEPLOY_ROLE_ARN` | ARN del rol OIDC que permite publicar en los seis repositorios ECR y consultar la salida de SAM |
| Secret | `INTERNAL_API_TOKEN` | Secreto aleatorio de al menos 32 caracteres; debe coincidir entre Lambda y los contenedores |
| Secret | `MYSQL_ROOT_PASSWORD` | Contraseña root de la base MySQL; conserva la que ya tenga el volumen persistente |
| Variable | `AWS_REGION` | Región del laboratorio, por ejemplo `us-east-1` |
| Variable | `EVENTPASS_API_BASE_URL` | URL HTTPS base que permite a Lambda alcanzar el Gateway de EventPass y enruta `/api/...` |

No guardes access keys de IAM de larga duración en GitHub. `configure-aws-credentials` obtiene credenciales temporales mediante OIDC. Restringe la confianza de ambos roles a `repo:dOkuro-01/EventPass:ref:refs/heads/main`; el job de despliegue no se ejecuta en pull requests.

El rol SAM debe permitir crear/actualizar los recursos declarados en `aws/sam/template.yaml`, subir el paquete de Lambda a S3, crear y leer cambios de CloudFormation y pasar a Lambda el rol `OrderIssuerRole`. El rol de despliegue necesita `ecr:GetAuthorizationToken`, carga de capas e imágenes en `eventpass/*`, `sts:GetCallerIdentity` y lectura de salidas del stack `eventpass-serverless`. Si AWS Academy no permite crear el proveedor OIDC, roles, políticas o recursos SAM, el pipeline no puede aprovisionar hasta que el administrador del laboratorio habilite esos permisos.

Para el rol de despliegue, `aws/iam/github-actions-deploy-policy.json` entrega una política de ECR push acotada a `eventpass/*` y de lectura al stack. Reemplaza `ID_CUENTA`; ajusta la región si no usas `us-east-1`. El rol SAM necesita permisos de CloudFormation, Lambda, SQS, Logs, ECR, subida a la ubicación de artefactos SAM y `iam:PassRole` únicamente al rol Lambda de este stack. Mantén esos permisos separados de los permisos de instancia EC2.

## URL que usa Lambda

`EVENTPASS_API_BASE_URL` es el origen HTTPS sin barra final; por ejemplo `https://api.ejemplo.cl`. Esa URL debe aceptar las rutas `/api/eventos/{id}/reservas`, `/api/tickets/internal/emision` y `/api/ordenes/internal/{id}/estado`, y reenviarlas al Gateway de Swarm. Si el Gateway solo está disponible como `http://IP-PUBLICA:8080`, prepara primero un endpoint HTTPS/balanceador y una regla de red adecuada; no guardes tokens en la URL.

## Preparar el manager EC2

Inicializa el manager y workers con [la guía de Docker Swarm](DOCKER_SWARM.md). Instala en el manager Docker Engine/CLI, AWS CLI y GitHub Actions Runner. Registra el runner en este repositorio con las etiquetas `self-hosted`, `Linux`, `X64` y `eventpass-manager`; instala el servicio con el usuario que puede ejecutar Docker. No compartas el token temporal del runner.

Asocia a los nodos Swarm un perfil IAM con el permiso mínimo `sqs:SendMessage` para `laboratorio-pedidos`; el servicio de órdenes obtiene credenciales de EC2 mediante IMDSv2. Revisa el hop limit de IMDSv2, puertos del Security Group y ruta hacia SQS/ECR antes del primer despliegue.

## Verificación

Después del push revisa **Actions → EventPass CI, AWS SAM y Docker Swarm**. Si CI falla, SAM y Swarm no se ejecutan. Si SAM falla, no se publican imágenes ni se actualiza el stack. Si el runner no está `Idle`, el job `deploy-swarm` queda esperando. Para diagnosticar el manager:

```bash
docker node ls
docker stack services eventpass
docker stack ps --no-trunc eventpass
docker service logs eventpass_api-gateway
```

El workflow espera que el Gateway responda HTTP 200; si falla, comprueba despliegues pendientes, conectividad de base de datos, espacio en disco, y la URL y los secretos configurados en GitHub. No imprimas los valores de secretos en logs.
