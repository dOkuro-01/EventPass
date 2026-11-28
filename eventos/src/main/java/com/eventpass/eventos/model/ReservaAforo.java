package com.eventpass.eventos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservas_aforo",
        uniqueConstraints = @UniqueConstraint(name = "uk_reserva_orden", columnNames = "orden_id"))
@Getter
@Setter
@NoArgsConstructor
public class ReservaAforo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orden_id", nullable = false)
    private Long ordenId;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (fechaReserva == null) {
            fechaReserva = LocalDateTime.now();
        }
    }
}
