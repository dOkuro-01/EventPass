package com.eventpass.eventos.controller;
import com.eventpass.eventos.dto.EventoRequest;
import com.eventpass.eventos.model.Evento;
import com.eventpass.eventos.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/eventos")
public class EventoController {
 private final EventoService service;
 public EventoController(EventoService service){this.service=service;}
 @GetMapping public List<Evento> listar(){return service.listar();}
 @GetMapping("/{id}") public Evento buscar(@PathVariable Long id){return service.buscar(id);}
 @PostMapping public ResponseEntity<Evento> crear(@Valid @RequestBody EventoRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));}
 @PutMapping("/{id}") public Evento actualizar(@PathVariable Long id,@Valid @RequestBody EventoRequest r){return service.actualizar(id,r);}
 @PostMapping("/{id}/reservar-aforo") public Evento reservar(@PathVariable Long id,@RequestBody Map<String,Integer> body){Integer cantidad=body.get("cantidad");if(cantidad==null)throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"Debe indicar cantidad");return service.reservar(id,cantidad);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id){service.eliminar(id);return ResponseEntity.noContent().build();}
}
