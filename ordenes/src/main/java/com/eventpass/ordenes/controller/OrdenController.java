package com.eventpass.ordenes.controller;

import com.eventpass.ordenes.dto.OrdenRequest;
import com.eventpass.ordenes.dto.EstadoOrdenRequest;
import com.eventpass.ordenes.model.Orden;
import com.eventpass.ordenes.service.OrdenService;
import com.eventpass.ordenes.security.InternalTokenValidator;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService service;
    private final InternalTokenValidator internalTokenValidator;

    public OrdenController(OrdenService service, InternalTokenValidator internalTokenValidator) {
        this.service = service;
        this.internalTokenValidator = internalTokenValidator;
    }

    @GetMapping
    public List<Orden> listarOrdenes() {
        return service.listarOrdenes();
    }

    @GetMapping("/{id}")
    public Orden buscarOrdenPorId(@PathVariable Long id) {
        return service.buscarOrdenPorId(id);
    }

    @PostMapping
    public ResponseEntity<Orden> crearOrden(@Valid @RequestBody OrdenRequest datos) {
        Orden creada = service.crearOrden(datos);
        return ResponseEntity.created(URI.create("/api/ordenes/" + creada.getId())).body(creada);
    }

    @PutMapping("/internal/{id}/estado")
    public Orden actualizarEstado(
            @PathVariable Long id,
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @Valid @RequestBody EstadoOrdenRequest request) {
        internalTokenValidator.validar(token);
        return service.actualizarEstado(id, request.estado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOrden(@PathVariable Long id) {
        service.eliminarOrden(id);
        return ResponseEntity.noContent().build();
    }
}
