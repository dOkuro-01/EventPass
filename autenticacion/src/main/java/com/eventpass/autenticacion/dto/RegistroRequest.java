package com.eventpass.autenticacion.dto;
import jakarta.validation.constraints.*;
public record RegistroRequest(@NotBlank @Size(max=100) String nombre, @NotBlank @Email @Size(max=160) String correo, @NotBlank @Size(min=8,max=72) String password, String rol) {}
