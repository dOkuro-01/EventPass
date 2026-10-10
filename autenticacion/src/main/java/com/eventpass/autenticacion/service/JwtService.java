package com.eventpass.autenticacion.service;
import com.eventpass.autenticacion.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
@Service
public class JwtService {
 private final SecretKey key; private final long expirationMs;
 public JwtService(org.springframework.core.env.Environment env){
  String secret=env.getProperty("app.jwt.secret","EventPassClaveDemostracionParaJWT2026_ClaveSegura");
  byte[] bytes=secret.getBytes(StandardCharsets.UTF_8); this.key=Keys.hmacShaKeyFor(bytes.length>=32?bytes:java.util.Arrays.copyOf(bytes,32));
  this.expirationMs=env.getProperty("app.jwt.expiration-ms",Long.class,3600000L);
 }
 public String generar(Usuario u){Date ahora=new Date();return Jwts.builder().setSubject(u.getId().toString()).claim("correo",u.getCorreo()).claim("rol",u.getRol()).setIssuedAt(ahora).setExpiration(new Date(ahora.getTime()+expirationMs)).signWith(key,SignatureAlgorithm.HS256).compact();}
}
