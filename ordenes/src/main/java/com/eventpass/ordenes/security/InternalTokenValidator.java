package com.eventpass.ordenes.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InternalTokenValidator {

    private final String expectedToken;

    public InternalTokenValidator(@Value("${app.internal-token:}") String expectedToken) {
        this.expectedToken = expectedToken;
    }

    public void validar(String presentedToken) {
        if (expectedToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El acceso interno no está configurado");
        }
        if (presentedToken == null || !MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                presentedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token interno inválido");
        }
    }
}
