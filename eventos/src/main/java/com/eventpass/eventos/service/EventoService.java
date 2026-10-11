package com.eventpass.eventos.service;

import com.eventpass.eventos.dto.EventoRequest;
import com.eventpass.eventos.model.Evento;
import com.eventpass.eventos.repository.EventoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class EventoService {

    private final EventoRepository repository;

    public EventoService(EventoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Evento> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Evento buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    public Evento crear(EventoRequest datos) {
        Evento evento = new Evento();
        evento.setNombre(datos.nombre());
        evento.setDescripcion(datos.descripcion());
        evento.setLugar(datos.lugar());
        evento.setFechaEvento(datos.fechaEvento());
        evento.setAforoTotal(datos.aforoTotal());
        evento.setAforoDisponible(datos.aforoTotal());
        evento.setEstado(datos.estado() == null || datos.estado().isBlank()
                ? "PUBLICADO" : datos.estado().toUpperCase());
        return repository.save(evento);
    }

    public Evento actualizar(Long id, EventoRequest datos) {
        Evento evento = buscar(id);
        int entradasReservadas = evento.getAforoTotal() - evento.getAforoDisponible();

        if (datos.aforoTotal() < entradasReservadas) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El aforo total no puede ser menor que las entradas ya reservadas");
        }

        evento.setNombre(datos.nombre());
        evento.setDescripcion(datos.descripcion());
        evento.setLugar(datos.lugar());
        evento.setFechaEvento(datos.fechaEvento());
        evento.setAforoTotal(datos.aforoTotal());
        evento.setAforoDisponible(datos.aforoTotal() - entradasReservadas);

        if (datos.estado() != null && !datos.estado().isBlank()) {
            evento.setEstado(datos.estado().toUpperCase());
        }
        return repository.save(evento);
    }

    public Evento reservar(Long id, int cantidad) {
        if (cantidad < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cantidad debe ser mayor que cero");
        }

        int actualizado = repository.descontarAforo(id, cantidad);
        if (actualizado == 0) {
            if (!repository.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Evento no publicado o aforo insuficiente");
        }
        return buscar(id);
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }
        repository.deleteById(id);
    }
}
