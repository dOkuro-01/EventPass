package com.eventpass.tickets.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${app.jwt.secret:Q2xhdmVEZURlbW9QYXJhSmF2YU5vYmVDb25Eb2NrZXIyMDI2IQ==}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms:300000}")
    private long jwtExpirationMs;

    public String generarToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validarToken(String token, String username) {
        final String usernameToken = extraerUsername(token);
        return (usernameToken.equals(username) && !isTokenExpired(token));
    }

    public String extraerUsername(String token) {
        return extraerClaims(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extraerExpiracion(token).before(new Date());
    }

    private Date extraerExpiracion(String token) {
        return extraerClaims(token, Claims::getExpiration);
    }

    private <T> T extraerClaims(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosLosClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}