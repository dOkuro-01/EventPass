package com.eventpass.eventos.repository;
import com.eventpass.eventos.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
public interface EventoRepository extends JpaRepository<Evento,Long> {
 @Modifying @Transactional
 @Query("update Evento e set e.aforoDisponible=e.aforoDisponible-:cantidad where e.id=:id and e.aforoDisponible >= :cantidad and e.estado='PUBLICADO'")
 int descontarAforo(@Param("id") Long id,@Param("cantidad") int cantidad);
}
