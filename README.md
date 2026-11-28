# EventPass

EventPass separa la aceptación de órdenes de la emisión de entradas. El sistema usa seis aplicaciones Maven: autenticación, eventos, órdenes, tickets, Eureka y API Gateway.

## Flujo asíncrono de emisión

Órdenes guarda el pedido como `PENDIENTE_EMISION` y lo publica en Amazon SQS. Lambda reserva aforo de forma idempotente en Eventos, solicita un ticket con código de acceso por cada entrada a Tickets y marca la orden como `EMITIDA`. Si no existe aforo, la orden queda `RECHAZADA`. CloudWatch registra el `messageId` y el `pruebaId` sin imprimir el cuerpo completo del mensaje.

Consulta [la guía de SQS, Lambda y CloudWatch](docs/SQS_LAMBDA_CLOUDWATCH.md) para el flujo de emisión. [CI/CD](docs/CI_CD_GITHUB_ACTIONS.md) automatiza build, pruebas, provisión SAM y despliegue. [Docker Swarm](docs/DOCKER_SWARM.md) documenta manager, workers, red y stack distribuido.

## Desarrollo local

1. Copia `.env.example` a `.env` y define una contraseña segura para MySQL.
2. Ejecuta `docker compose up --build -d`.
3. Para crear órdenes y emitir tickets en modo AWS, configura `AWS_REGION`, `PEDIDOS_QUEUE_URL` e `INTERNAL_API_TOKEN` como se describe en la guía de SQS. La EC2 debe usar un perfil IAM con permiso limitado para enviar mensajes.

El workflow verifica cada módulo Maven por separado porque el repositorio no tiene un POM raíz. En `main`, crea/actualiza SQS, Lambda, CloudWatch y ECR mediante SAM, publica las seis imágenes y despliega el stack en el runner Swarm manager etiquetado `eventpass-manager`. AWS CLI y SAM usan el perfil IAM de la EC2; los secretos y variables de aplicación se describen en la guía de CI/CD.
