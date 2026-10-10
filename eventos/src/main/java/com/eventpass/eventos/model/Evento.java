package com.eventpass.eventos.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="eventos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Evento {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=150) private String nombre;
 @Column(length=1000) private String descripcion;
 @Column(nullable=false,length=180) private String lugar;
 @Column(name="fecha_evento",nullable=false) private LocalDateTime fechaEvento;
 @Column(name="aforo_total",nullable=false) private Integer aforoTotal;
 @Column(name="aforo_disponible",nullable=false) private Integer aforoDisponible;
 @Column(nullable=false,length=20) private String estado;
 @Version private Long version;
 @PrePersist void prePersist(){if(estado==null)estado="PUBLICADO";if(aforoDisponible==null)aforoDisponible=aforoTotal;}
}
