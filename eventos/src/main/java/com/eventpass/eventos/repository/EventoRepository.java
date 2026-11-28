package com.eventpass.eventos.repository;
import com.eventpass.eventos.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface EventoRepository extends JpaRepository<Evento,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select e from Evento e where e.id = :id")
 Optional<Evento> findByIdForUpdate(@Param("id") Long id);

 @Modifying @Transactional
 @Query("update Evento e set e.aforoDisponible=e.aforoDisponible-:cantidad where e.id=:id and e.aforoDisponible >= :cantidad and e.estado='PUBLICADO'")
 int descontarAforo(@Param("id") Long id,@Param("cantidad") int cantidad);
}
