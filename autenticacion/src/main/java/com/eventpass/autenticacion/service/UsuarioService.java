package com.eventpass.autenticacion.service;
import com.eventpass.autenticacion.dto.*;
import com.eventpass.autenticacion.model.Usuario;
import com.eventpass.autenticacion.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@Service
public class UsuarioService {
 private final UsuarioRepository repo; private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
 private static final Set<String> ROLES=Set.of("COMPRADOR","STAFF","ADMIN");
 public UsuarioService(UsuarioRepository repo){this.repo=repo;}
 @Transactional public UsuarioResponse registrar(RegistroRequest r){
  if(repo.existsByCorreoIgnoreCase(r.correo())) throw new ResponseStatusException(HttpStatus.CONFLICT,"El correo ya está registrado");
  String rol=(r.rol()==null||r.rol().isBlank())?"COMPRADOR":r.rol().toUpperCase(Locale.ROOT);
  if(!ROLES.contains(rol)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Rol inválido: COMPRADOR, STAFF o ADMIN");
  Usuario u=new Usuario();u.setNombre(r.nombre().trim());u.setCorreo(r.correo().trim().toLowerCase(Locale.ROOT));u.setPasswordHash(encoder.encode(r.password()));u.setRol(rol);
  return UsuarioResponse.from(repo.save(u));
 }
 public Usuario autenticar(LoginRequest r){Usuario u=repo.findByCorreoIgnoreCase(r.correo()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales inválidas"));
  if(!encoder.matches(r.password(),u.getPasswordHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales inválidas"); return u; }
 public List<UsuarioResponse> listar(){return repo.findAll().stream().map(UsuarioResponse::from).toList();}
 public UsuarioResponse buscar(Long id){return repo.findById(id).map(UsuarioResponse::from).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuario no encontrado"));}
}
