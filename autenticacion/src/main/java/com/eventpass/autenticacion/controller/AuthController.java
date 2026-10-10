package com.eventpass.autenticacion.controller;
import com.eventpass.autenticacion.dto.*;
import com.eventpass.autenticacion.model.Usuario;
import com.eventpass.autenticacion.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UsuarioService usuarios; private final JwtService jwt;
 public AuthController(UsuarioService usuarios,JwtService jwt){this.usuarios=usuarios;this.jwt=jwt;}
 @PostMapping("/registro") public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(usuarios.registrar(r));}
 @PostMapping("/login") public ResponseEntity<Map<String,Object>> login(@Valid @RequestBody LoginRequest r){Usuario u=usuarios.autenticar(r);return ResponseEntity.ok(Map.of("token",jwt.generar(u),"tipo","Bearer","usuario",UsuarioResponse.from(u)));}
}
