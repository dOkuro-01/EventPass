package com.eventpass.ordenes.service;

import com.eventpass.ordenes.model.Orden;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class SqsOrderPublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public SqsOrderPublisher(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @Value("${app.sqs.queue-url:}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    public String publicar(Orden orden, String pruebaId) {
        if (!StringUtils.hasText(queueUrl)) {
            throw new IllegalStateException("Falta configurar PEDIDOS_QUEUE_URL");
        }

        var message = new PedidoCreadoMessage(
                "PEDIDO_CREADO",
                orden.getId(),
                orden.getUsuarioId(),
                orden.getEventoId(),
                orden.getCantidadEntradas(),
                pruebaId);

        try {
            String body = objectMapper.writeValueAsString(message);
            return sqsClient.sendMessage(SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(body)
                            .build())
                    .messageId();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo serializar el mensaje de la orden", exception);
        }
    }

    public String nuevoPruebaId() {
        return UUID.randomUUID().toString();
    }

    public record PedidoCreadoMessage(
            String evento,
            Long pedidoId,
            Long usuarioId,
            Long eventoId,
            Integer cantidadEntradas,
            String pruebaId) {}
}
