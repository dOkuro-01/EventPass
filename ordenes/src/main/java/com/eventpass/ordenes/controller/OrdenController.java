package com.eventpass.ordenes.controller;

import com.eventpass.ordenes.dto.OrdenRequest;
import com.eventpass.ordenes.model.Orden;
import com.eventpass.ordenes.service.OrdenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService service;

    
    public OrdenController(OrdenService service) {
        this.service = service;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOrden(@PathVariable Long id) {
        service.eliminarOrden(id);
        return ResponseEntity.noContent().build();
    }
}