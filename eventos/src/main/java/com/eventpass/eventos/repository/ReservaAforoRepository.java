package com.eventpass.eventos.repository;

import com.eventpass.eventos.model.ReservaAforo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaAforoRepository extends JpaRepository<ReservaAforo, Long> {
    Optional<ReservaAforo> findByOrdenId(Long ordenId);
}
