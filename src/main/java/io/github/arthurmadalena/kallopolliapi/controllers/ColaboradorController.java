package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.ColaboradorRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.ColaboradorResponse;
import io.github.arthurmadalena.kallopolliapi.service.ColaboradorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {
    private final ColaboradorService service;

    public ColaboradorController(ColaboradorService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ColaboradorResponse criar(@Valid @RequestBody ColaboradorRequest req) {
        return service.criar(req);
    }

    @GetMapping
    public List<ColaboradorResponse> listar() {
        return service.listar();
    }
}
