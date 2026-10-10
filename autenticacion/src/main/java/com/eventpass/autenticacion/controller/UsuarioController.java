package com.eventpass.autenticacion.controller;
import com.eventpass.autenticacion.dto.UsuarioResponse;
import com.eventpass.autenticacion.service.UsuarioService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/usuarios")
public class UsuarioController {
 private final UsuarioService service;
 public UsuarioController(UsuarioService service){this.service=service;}
 @GetMapping public List<UsuarioResponse> listar(){return service.listar();}
 @GetMapping("/{id}") public UsuarioResponse buscar(@PathVariable Long id){return service.buscar(id);}
}
