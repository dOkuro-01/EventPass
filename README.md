# EventPass

EventPass separa la aceptación de órdenes de la emisión de entradas. El sistema usa seis aplicaciones Maven: autenticación, eventos, órdenes, tickets, Eureka y API Gateway.

## Flujo asíncrono de emisión

Órdenes guarda el pedido como `PENDIENTE_EMISION` y lo publica en Amazon SQS. Lambda reserva aforo de forma idempotente en Eventos, solicita un ticket con código de acceso por cada entrada a Tickets y marca la orden como `EMITIDA`. Si no existe aforo, la orden queda `RECHAZADA`. CloudWatch registra el `messageId` y el `pruebaId` sin imprimir el cuerpo completo del mensaje.

Consulta [la guía de SQS, Lambda y CloudWatch](docs/SQS_LAMBDA_CLOUDWATCH.md) para crear los recursos y hacer el recorrido de prueba. Consulta [la guía de CI/CD](docs/CI_CD_GITHUB_ACTIONS.md) para preparar el runner de EC2.

## Desarrollo local

1. Copia `.env.example` a `.env` y define una contraseña segura para MySQL.
2. Ejecuta `docker compose up --build -d`.
3. Para crear órdenes y emitir tickets en modo AWS, configura `AWS_REGION`, `PEDIDOS_QUEUE_URL` e `INTERNAL_API_TOKEN` como se describe en la guía de SQS. La instancia EC2 debe usar un perfil IAM con permiso limitado para enviar mensajes.

El workflow de GitHub Actions verifica cada módulo Maven por separado porque el repositorio no tiene un POM raíz. El despliegue usa un runner autohospedado en EC2 etiquetado `eventpass-ec2`.
