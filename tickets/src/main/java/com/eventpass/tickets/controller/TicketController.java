package com.eventpass.tickets.controller;

import com.eventpass.tickets.dto.TicketRequest;
import com.eventpass.tickets.dto.TicketEmissionRequest;
import com.eventpass.tickets.model.Ticket;
import com.eventpass.tickets.service.TicketService;
import com.eventpass.tickets.security.InternalTokenValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final InternalTokenValidator internalTokenValidator;

    public TicketController(TicketService ticketService, InternalTokenValidator internalTokenValidator) {
        this.ticketService = ticketService;
        this.internalTokenValidator = internalTokenValidator;
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

    @PostMapping("/internal/emision")
    public ResponseEntity<List<Ticket>> emitirParaOrden(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @Valid @RequestBody TicketEmissionRequest request) {
        internalTokenValidator.validar(token);
        return ResponseEntity.ok(ticketService.emitirParaOrden(request));
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
