package com.eventpass.autenticacion.service;

import com.eventpass.autenticacion.dto.LoginRequest;
import com.eventpass.autenticacion.dto.RegistroRequest;
import com.eventpass.autenticacion.dto.UsuarioResponse;
import com.eventpass.autenticacion.model.Usuario;
import com.eventpass.autenticacion.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class UsuarioService {

    private static final Set<String> ROLES_VALIDOS = Set.of("COMPRADOR", "STAFF", "ADMIN");

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public UsuarioResponse registrar(RegistroRequest datos) {
        if (repository.existsByCorreoIgnoreCase(datos.correo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        String rol = datos.rol() == null || datos.rol().isBlank()
                ? "COMPRADOR" : datos.rol().toUpperCase(Locale.ROOT);
        if (!ROLES_VALIDOS.contains(rol)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rol inválido: COMPRADOR, STAFF o ADMIN");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(datos.nombre().trim());
        usuario.setCorreo(datos.correo().trim().toLowerCase(Locale.ROOT));
        usuario.setPasswordHash(passwordEncoder.encode(datos.password()));
        usuario.setRol(rol);
        return UsuarioResponse.from(repository.save(usuario));
    }

    @Transactional(readOnly = true)
    public Usuario autenticar(LoginRequest datos) {
        Usuario usuario = repository.findByCorreoIgnoreCase(datos.correo())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(datos.password(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        return usuario;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(UsuarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return repository.findById(id).map(UsuarioResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}
