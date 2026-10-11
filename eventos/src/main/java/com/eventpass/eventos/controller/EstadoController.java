package com.eventpass.eventos.controller;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class EstadoController {
    @GetMapping("/api/publico/estado")
    public Map<String, String> estado() { return Map.of("estado", "OK", "servicio", "eventos"); }
}
