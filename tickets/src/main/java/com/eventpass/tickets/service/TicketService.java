package com.eventpass.tickets.service;

import com.eventpass.tickets.model.Ticket;
import com.eventpass.tickets.repository.TicketRepository;
import com.eventpass.tickets.dto.TicketEmissionRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> listarTodos() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> buscarPorId(Long id) {
        return ticketRepository.findById(id);
    }

    public Ticket guardar(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Transactional
    public List<Ticket> emitirParaOrden(TicketEmissionRequest request) {
        List<Ticket> existentes = ticketRepository.findByOrdenIdOrderByNumeroEntrada(request.ordenId());
        if (existentes.stream().anyMatch(ticket ->
                !request.eventoId().equals(ticket.getEventoId())
                        || !request.usuarioId().equals(ticket.getUsuarioId()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La orden ya tiene tickets asociados a otros datos");
        }
        if (existentes.size() > request.cantidadEntradas()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La cantidad solicitada no coincide con los tickets emitidos");
        }

        Set<Integer> numerosExistentes = new HashSet<>();
        existentes.forEach(ticket -> numerosExistentes.add(ticket.getNumeroEntrada()));

        List<Ticket> nuevos = new ArrayList<>();
        for (int numero = 1; numero <= request.cantidadEntradas(); numero++) {
            if (numerosExistentes.contains(numero)) {
                continue;
            }
            Ticket ticket = new Ticket();
            ticket.setOrdenId(request.ordenId());
            ticket.setUsuarioId(request.usuarioId());
            ticket.setNumeroEntrada(numero);
            ticket.setEventoId(request.eventoId());
            ticket.setCodigoAcceso(UUID.randomUUID().toString().toUpperCase(Locale.ROOT));
            ticket.setTipo("GENERAL");
            ticket.setPrecio(0.0);
            ticket.setEstado("EMITIDO");
            nuevos.add(ticket);
        }

        if (!nuevos.isEmpty()) {
            ticketRepository.saveAllAndFlush(nuevos);
        }
        return ticketRepository.findByOrdenIdOrderByNumeroEntrada(request.ordenId());
    }

    public void eliminar(Long id) {
        ticketRepository.deleteById(id);
    }
}
