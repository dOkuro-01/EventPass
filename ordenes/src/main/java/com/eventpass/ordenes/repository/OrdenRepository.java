package com.eventpass.ordenes.repository;

import com.eventpass.ordenes.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;



public interface OrdenRepository extends JpaRepository<Orden, Long> {
   
}