package com.eventpass.tickets.controller;

import com.eventpass.tickets.dto.TicketRequest;
import com.eventpass.tickets.model.Ticket;
import com.eventpass.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> listarTickets() {
        return ResponseEntity.ok(ticketService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> buscarPorId(@PathVariable Long id) {
        return ticketService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Ticket> crearTicket(@Valid @RequestBody TicketRequest request) {
        Ticket nuevoTicket = new Ticket();
        nuevoTicket.setEventoId(request.getEventoId());
        nuevoTicket.setTipo(request.getTipo());
        nuevoTicket.setPrecio(request.getPrecio());
        nuevoTicket.setEstado(request.getEstado());

        Ticket ticketGuardado = ticketService.guardar(nuevoTicket);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketGuardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long id) {
        if (ticketService.buscarPorId(id).isPresent()) {
            ticketService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}