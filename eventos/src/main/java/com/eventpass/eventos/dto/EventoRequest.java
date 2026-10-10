package com.eventpass.eventos.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
public record EventoRequest(@NotBlank @Size(max=150) String nombre,@Size(max=1000) String descripcion,@NotBlank @Size(max=180) String lugar,@NotNull @Future LocalDateTime fechaEvento,@NotNull @Min(1) Integer aforoTotal,@Size(max=20) String estado) {}
