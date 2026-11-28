package com.eventpass.eventos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservaAforoRequest(
        @NotNull Long ordenId,
        @NotNull @Min(1) Integer cantidad) {}
