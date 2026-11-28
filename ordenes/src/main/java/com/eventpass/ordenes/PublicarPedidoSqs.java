package com.eventpass.ordenes;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/** Publica un mensaje de prueba para seguirlo desde Java hasta CloudWatch Logs. */
public final class PublicarPedidoSqs {

    private PublicarPedidoSqs() {}

    public static void main(String[] args) throws Exception {
        long pedidoId = args.length == 0 ? 42 : Long.parseLong(args[0]);
        String queueUrl = System.getenv("PEDIDOS_QUEUE_URL");
        if (queueUrl == null || queueUrl.isBlank()) {
            throw new IllegalStateException("Falta PEDIDOS_QUEUE_URL");
        }

        String pruebaId = UUID.randomUUID().toString();
        String body = new ObjectMapper().writeValueAsString(Map.of(
                "evento", "PEDIDO_CREADO",
                "pedidoId", pedidoId,
                "pruebaId", pruebaId,
                "modo", "prueba"));

        try (SqsClient sqs = SqsClient.create()) {
            String messageId = sqs.sendMessage(SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(body)
                            .build())
                    .messageId();
            System.out.printf("ENVIADO pruebaId=%s messageId=%s%n", pruebaId, messageId);
        }
    }
}
