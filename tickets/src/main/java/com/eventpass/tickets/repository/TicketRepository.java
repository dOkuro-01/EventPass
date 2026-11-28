package com.eventpass.tickets.repository;

import com.eventpass.tickets.model.Ticket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByOrdenIdOrderByNumeroEntrada(Long ordenId);
}
