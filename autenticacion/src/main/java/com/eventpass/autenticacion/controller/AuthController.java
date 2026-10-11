package com.eventpass.autenticacion.controller;

import com.eventpass.autenticacion.dto.LoginRequest;
import com.eventpass.autenticacion.dto.RegistroRequest;
import com.eventpass.autenticacion.dto.UsuarioResponse;
import com.eventpass.autenticacion.model.Usuario;
import com.eventpass.autenticacion.service.JwtService;
import com.eventpass.autenticacion.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarios;
    private final JwtService jwtService;

    public AuthController(UsuarioService usuarios, JwtService jwtService) {
        this.usuarios = usuarios;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest datos) {
        UsuarioResponse usuario = usuarios.registrar(datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest datos) {
        Usuario usuario = usuarios.autenticar(datos);
        Map<String, Object> respuesta = Map.of(
                "token", jwtService.generar(usuario),
                "tipo", "Bearer",
                "usuario", UsuarioResponse.from(usuario));
        return ResponseEntity.ok(respuesta);
    }
}
