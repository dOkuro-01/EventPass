package com.eventpass.autenticacion.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="usuarios", uniqueConstraints=@UniqueConstraint(columnNames="correo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Usuario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=100) private String nombre;
 @Column(nullable=false,length=160,unique=true) private String correo;
 @Column(name="password_hash",nullable=false,length=100) private String passwordHash;
 @Column(nullable=false,length=30) private String rol;
 @Column(nullable=false) private LocalDateTime fechaCreacion;
 @PrePersist void prePersist(){ if(fechaCreacion==null) fechaCreacion=LocalDateTime.now(); if(rol==null||rol.isBlank()) rol="COMPRADOR"; }
}
