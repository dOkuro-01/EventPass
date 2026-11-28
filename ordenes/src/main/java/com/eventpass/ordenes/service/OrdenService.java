package com.eventpass.ordenes.service;

import com.eventpass.ordenes.dto.OrdenRequest;
import com.eventpass.ordenes.model.Orden;
import com.eventpass.ordenes.repository.OrdenRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
@Transactional
public class OrdenService {

    private final OrdenRepository repository;
    private final SqsOrderPublisher publisher;

    public OrdenService(OrdenRepository repository, SqsOrderPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional(readOnly = true)
    public List<Orden> listarOrdenes() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Orden buscarOrdenPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
    }

    public Orden crearOrden(OrdenRequest datos) {
        Orden nuevaOrden = new Orden();
        nuevaOrden.setUsuarioId(datos.usuarioId());
        nuevaOrden.setEventoId(datos.eventoId());
        nuevaOrden.setCantidadEntradas(datos.cantidadEntradas());

        Orden guardada = repository.saveAndFlush(nuevaOrden);
        String pruebaId = publisher.nuevoPruebaId();
        try {
            String messageId = publisher.publicar(guardada, pruebaId);
            System.getLogger(OrdenService.class.getName()).log(System.Logger.Level.INFO,
                    "ENVIADO pruebaId={0} messageId={1}", pruebaId, messageId);
            return guardada;
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo encolar la orden para emisión", exception);
        }
    }

    public Orden actualizarEstado(Long id, String estado) {
        String nuevoEstado = estado == null ? "" : estado.trim().toUpperCase(Locale.ROOT);
        if (!List.of("EMITIDA", "RECHAZADA").contains(nuevoEstado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado final debe ser EMITIDA o RECHAZADA");
        }

        Orden orden = buscarOrdenPorId(id);
        if (nuevoEstado.equals(orden.getEstado())) {
            return orden;
        }
        if (!"PENDIENTE_EMISION".equals(orden.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La orden ya tiene un estado final");
        }

        orden.setEstado(nuevoEstado);
        return repository.save(orden);
    }

    public void eliminarOrden(Long id) {
        Orden orden = buscarOrdenPorId(id);
        repository.delete(orden);
    }
}
