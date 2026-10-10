package com.eventpass.autenticacion.dto;
import jakarta.validation.constraints.*;
public record LoginRequest(@NotBlank @Email String correo, @NotBlank String password) {}
