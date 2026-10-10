package com.eventpass.autenticacion.dto;
import com.eventpass.autenticacion.model.Usuario;
import java.time.LocalDateTime;
public record UsuarioResponse(Long id,String nombre,String correo,String rol,LocalDateTime fechaCreacion) {
 public static UsuarioResponse from(Usuario u){return new UsuarioResponse(u.getId(),u.getNombre(),u.getCorreo(),u.getRol(),u.getFechaCreacion());}
}
