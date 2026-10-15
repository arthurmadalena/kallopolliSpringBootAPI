package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.JornadaResponse;
import io.github.arthurmadalena.kallopolliapi.service.JornadaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jornadas")
public class JornadaController {

    private final JornadaService service;

    public JornadaController(JornadaService service) {
        this.service = service;
    }

    @GetMapping
    public List<JornadaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/colaborador/{colaboradorId}")
    public JornadaResponse buscarPorColaborador(@PathVariable Long colaboradorId) {
        return service.buscarPorColaborador(colaboradorId);
    }
}