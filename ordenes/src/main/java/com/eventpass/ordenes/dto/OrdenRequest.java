package com.eventpass.ordenes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrdenRequest(
    @NotNull(message = "El ID del usuario es obligatorio")
    Long usuarioId,

    @NotNull(message = "El ID del evento es obligatorio")
    Long eventoId,

    @NotNull(message = "La cantidad de entradas es obligatoria")
    @Min(value = 1, message = "Debe solicitar al menos 1 entrada")
    Integer cantidadEntradas
) {}