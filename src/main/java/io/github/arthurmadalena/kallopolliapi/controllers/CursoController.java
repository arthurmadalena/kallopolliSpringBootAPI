package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.ColaboradorPontosResponse;
import io.github.arthurmadalena.kallopolliapi.DTOs.ConclusaoCursoRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.CursoRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.CursoResponse;
import io.github.arthurmadalena.kallopolliapi.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService service;

    public CursoController(CursoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CursoResponse criar(@Valid @RequestBody CursoRequest req) {
        return service.criar(req);
    }

    @GetMapping
    public List<CursoResponse> listar() {
        return service.listar();
    }

    @PostMapping("/{id}/conclusao")
    public ColaboradorPontosResponse concluir(@PathVariable Long id,
                                              @Valid @RequestBody ConclusaoCursoRequest req) {
        return service.concluir(id, req.colaboradorId());
    }
}