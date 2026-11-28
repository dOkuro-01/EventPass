# SQS, Lambda y CloudWatch para EventPass

Este flujo separa la aceptación de una orden de la emisión de sus tickets:

1. `POST /api/ordenes` guarda la orden como `PENDIENTE_EMISION` y publica `PEDIDO_CREADO` en SQS.
2. Lambda consume la orden y solicita una reserva idempotente al microservicio de eventos.
3. Si hay aforo, Lambda pide al microservicio de tickets que emita un código por entrada y marca la orden `EMITIDA`.
4. Si no hay aforo o el evento no existe, la orden queda `RECHAZADA` y no se crean tickets.
5. Lambda escribe en CloudWatch el `messageId` de SQS, el `pruebaId` y el resultado. No registra el cuerpo completo ni el identificador del usuario.

Los endpoints internos requieren `X-Internal-Token`. Usa el mismo valor para `INTERNAL_API_TOKEN` en EventPass y para el parámetro `InternalApiToken` de Lambda. No guardes ese valor en GitHub ni en el repositorio.

## Recursos de AWS

La plantilla SAM de `aws/sam/template.yaml` crea en `us-east-1`:

- La cola estándar `laboratorio-pedidos`, con visibilidad de 60 segundos, retención de cuatro días y cifrado SSE-SQS.
- La función `laboratorio-receptor-pedidos`, con runtime Python 3.13, timeout de 10 segundos y una asignación SQS de un mensaje por lote.
- Un rol de Lambda con permisos limitados para leer y borrar mensajes de esa cola y escribir en su grupo de CloudWatch.
- El grupo `/aws/lambda/laboratorio-receptor-pedidos`, con retención de 14 días.

La identidad que despliega la plantilla necesita permisos para crear SQS, Lambda, IAM, CloudWatch Logs y la asignación de origen de eventos. Si Learner Lab deniega `iam:CreateRole` o `iam:PassRole`, la plantilla no puede saltarse esa restricción: solicita un rol autorizado o usa el flujo de consola con el rol que entregue el administrador.

Despliega la plantilla desde la raíz del repositorio:

```bash
sam build --template-file aws/sam/template.yaml
sam deploy --guided --template-file .aws-sam/build/template.yaml
```

Selecciona `us-east-1`, nombre de stack `eventpass-serverless` y `CAPABILITY_IAM`. En `EventPassApiBaseUrl`, indica la URL HTTPS de API Gateway que enruta `/api/...` hacia el Gateway de EventPass. En `InternalApiToken`, usa un secreto aleatorio de al menos 32 caracteres y no guardes los parámetros en `samconfig.toml`. Si la cola ya existe, no intentes crear otra con el mismo nombre; importa la cola en la infraestructura o adapta la plantilla a su ARN.

Al finalizar, copia el valor de `PedidosQueueUrl` en el `.env` de EC2:

```dotenv
AWS_REGION=us-east-1
PEDIDOS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/ID_CUENTA/laboratorio-pedidos
INTERNAL_API_TOKEN=el-mismo-secreto-aleatorio
```

Asocia al perfil IAM de la EC2 la política de `aws/iam/ec2-sqs-send-policy.json`, reemplazando `ID_CUENTA` por la cuenta del laboratorio. Limita el permiso a `sqs:SendMessage` en esa cola. La aplicación usa la cadena de credenciales predeterminada del SDK de AWS; no agregues claves de acceso al código ni a `.env`.

## Prueba de envío desde Java

En la EC2, desde una sesión con Java 21 y Maven:

```bash
export AWS_REGION=us-east-1
export PEDIDOS_QUEUE_URL='https://sqs.us-east-1.amazonaws.com/ID_CUENTA/laboratorio-pedidos'
mvn -f ordenes/pom.xml \
  -Dexec.mainClass=com.eventpass.ordenes.PublicarPedidoSqs \
  -Dexec.args=42 compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

La terminal imprime `ENVIADO`, `pruebaId` y el `messageId` aceptado por SQS. El mensaje de modo `prueba` solo registra recepción en Lambda y no modifica datos de órdenes, aforo o tickets.

## Confirmación en CloudWatch

En Lambda, abre `laboratorio-receptor-pedidos` → Monitor → View CloudWatch logs. También puedes usar:

```bash
aws logs tail /aws/lambda/laboratorio-receptor-pedidos \
  --region us-east-1 --since 15m --format short
```

Busca la misma pareja `pruebaId` y `messageId` impresa por Java. Para probar el flujo de negocio, crea una orden mediante el Gateway con un token JWT válido y revisa el log `TICKETS_EMITIDOS` o `PEDIDO_RECHAZADO` correspondiente. Los mensajes con error técnico se reintentan por SQS; las reservas y la emisión son idempotentes para tolerar reentregas.

## Evento simulado en el editor Lambda

El evento de prueba debe incluir `modo: prueba` para verificar los logs sin invocar la API:

```json
{
  "Records": [
    {
      "messageId": "mensaje-simulado",
      "body": "{\"evento\":\"PEDIDO_CREADO\",\"pedidoId\":42,\"pruebaId\":\"prueba-editor\",\"modo\":\"prueba\"}"
    }
  ]
}
```
