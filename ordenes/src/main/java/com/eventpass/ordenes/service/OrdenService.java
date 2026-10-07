package com.eventpass.ordenes.service;

import com.eventpass.ordenes.dto.OrdenRequest;
import com.eventpass.ordenes.model.Orden;
import com.eventpass.ordenes.repository.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
@Transactional
public class OrdenService {

    private final OrdenRepository repository;

    public OrdenService(OrdenRepository repository) {
        this.repository = repository;
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
        
        return repository.save(nuevaOrden);
    }

    public void eliminarOrden(Long id) {
        Orden orden = buscarOrdenPorId(id);
        repository.delete(orden);
    }
}