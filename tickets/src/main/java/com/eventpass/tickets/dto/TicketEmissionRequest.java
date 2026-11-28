package com.eventpass.tickets.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TicketEmissionRequest(
        @NotNull Long ordenId,
        @NotNull Long usuarioId,
        @NotNull Long eventoId,
        @NotNull @Min(1) Integer cantidadEntradas) {}
