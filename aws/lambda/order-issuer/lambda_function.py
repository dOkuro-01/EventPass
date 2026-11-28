import json
import os
from urllib.error import HTTPError
from urllib.request import Request, urlopen


def _log(action, **fields):
    print(json.dumps({"accion": action, **fields}, separators=(",", ":")), flush=True)


def _api_request(method, path, payload):
    base_url = os.environ["EVENTPASS_API_BASE_URL"].rstrip("/")
    token = os.environ["INTERNAL_API_TOKEN"]
    body = json.dumps(payload).encode("utf-8")
    request = Request(
        f"{base_url}{path}",
        data=body,
        method=method,
        headers={
            "Content-Type": "application/json",
            "X-Internal-Token": token,
        },
    )
    timeout = float(os.environ.get("API_TIMEOUT_SECONDS", "4"))
    try:
        with urlopen(request, timeout=timeout) as response:
            content = response.read()
            return response.status, json.loads(content) if content else None
    except HTTPError as error:
        content = error.read()
        try:
            response_body = json.loads(content) if content else None
        except json.JSONDecodeError:
            response_body = None
        return error.code, response_body


def _actualizar_estado(pedido_id, estado):
    status, _ = _api_request(
        "PUT",
        f"/api/ordenes/internal/{pedido_id}/estado",
        {"estado": estado},
    )
    if status < 200 or status >= 300:
        raise RuntimeError(f"No se pudo actualizar el estado de la orden: HTTP {status}")


def lambda_handler(event, context):
    for record in event["Records"]:
        body = json.loads(record["body"])
        message_id = record["messageId"]
        prueba_id = body.get("pruebaId")

        # Mensaje de humo de la guía: confirma SQS -> Lambda -> CloudWatch sin tocar datos.
        if body.get("modo") == "prueba":
            _log(
                "MENSAJE_RECIBIDO",
                messageId=message_id,
                pedidoId=body.get("pedidoId"),
                pruebaId=prueba_id,
                modo="prueba",
            )
            continue

        pedido_id = int(body["pedidoId"])
        evento_id = int(body["eventoId"])
        usuario_id = int(body["usuarioId"])
        cantidad = int(body["cantidadEntradas"])
        if cantidad < 1:
            raise ValueError("cantidadEntradas debe ser mayor que cero")

        status, _ = _api_request(
            "POST",
            f"/api/eventos/{evento_id}/reservas",
            {"ordenId": pedido_id, "cantidad": cantidad},
        )
        if status in (404, 409):
            _actualizar_estado(pedido_id, "RECHAZADA")
            _log(
                "PEDIDO_RECHAZADO",
                messageId=message_id,
                pedidoId=pedido_id,
                pruebaId=prueba_id,
                motivo="EVENTO_NO_DISPONIBLE_O_AFORO_INSUFICIENTE",
            )
            continue
        if status < 200 or status >= 300:
            raise RuntimeError(f"No se pudo reservar aforo: HTTP {status}")

        status, tickets = _api_request(
            "POST",
            "/api/tickets/internal/emision",
            {
                "ordenId": pedido_id,
                "usuarioId": usuario_id,
                "eventoId": evento_id,
                "cantidadEntradas": cantidad,
            },
        )
        if status < 200 or status >= 300:
            raise RuntimeError(f"No se pudieron emitir los tickets: HTTP {status}")

        _actualizar_estado(pedido_id, "EMITIDA")
        _log(
            "TICKETS_EMITIDOS",
            messageId=message_id,
            pedidoId=pedido_id,
            pruebaId=prueba_id,
            cantidadTickets=len(tickets or []),
        )

    return {"procesados": len(event.get("Records", []))}
