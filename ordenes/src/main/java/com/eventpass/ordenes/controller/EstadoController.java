package com.eventpass.ordenes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class EstadoController {

    @GetMapping("/api/publico/estado")
    public Map<String, String> estado() {
        return Map.of("estado", "OK", "servicio", "ordenes");
    }
}