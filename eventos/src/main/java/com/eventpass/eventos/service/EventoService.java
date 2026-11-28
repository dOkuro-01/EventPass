package com.eventpass.eventos.service;

import com.eventpass.eventos.dto.EventoRequest;
import com.eventpass.eventos.model.Evento;
import com.eventpass.eventos.repository.EventoRepository;
import com.eventpass.eventos.model.ReservaAforo;
import com.eventpass.eventos.repository.ReservaAforoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class EventoService {

    private final EventoRepository repository;
    private final ReservaAforoRepository reservas;

    public EventoService(EventoRepository repository, ReservaAforoRepository reservas) {
        this.repository = repository;
        this.reservas = reservas;
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

    public ReservaAforo reservarParaOrden(Long eventoId, Long ordenId, Integer cantidad) {
        if (ordenId == null || cantidad == null || cantidad < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar una orden y una cantidad mayor que cero");
        }

        var existente = reservas.findByOrdenId(ordenId);
        if (existente.isPresent()) {
            ReservaAforo reserva = existente.get();
            if (!reserva.getEventoId().equals(eventoId) || !reserva.getCantidad().equals(cantidad)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La orden ya reservó un aforo diferente");
            }
            return reserva;
        }

        Evento evento = repository.findByIdForUpdate(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Evento no encontrado"));
        if (!"PUBLICADO".equals(evento.getEstado()) || evento.getAforoDisponible() < cantidad) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Evento no publicado o aforo insuficiente");
        }

        evento.setAforoDisponible(evento.getAforoDisponible() - cantidad);
        repository.save(evento);

        ReservaAforo reserva = new ReservaAforo();
        reserva.setOrdenId(ordenId);
        reserva.setEventoId(eventoId);
        reserva.setCantidad(cantidad);
        return reservas.saveAndFlush(reserva);
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }
        repository.deleteById(id);
    }
}
