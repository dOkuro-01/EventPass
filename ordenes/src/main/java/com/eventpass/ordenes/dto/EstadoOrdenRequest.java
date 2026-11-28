package com.eventpass.ordenes.dto;

import jakarta.validation.constraints.NotBlank;

public record EstadoOrdenRequest(@NotBlank String estado) {}
