package com.eventpass.tickets.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ticket_orden_numero", columnNames = {"orden_id", "numero_entrada"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventoId;

    @Column(name = "orden_id")
    private Long ordenId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "numero_entrada")
    private Integer numeroEntrada;

    @Column(name = "codigo_acceso", unique = true, length = 36)
    private String codigoAcceso;

    @Column(nullable = false)
    private String tipo; 

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private String estado; 
}
