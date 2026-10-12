package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.LojaRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.LojaResponse;
import io.github.arthurmadalena.kallopolliapi.service.LojaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lojas")
public class LojaController {
    private final LojaService service;

    public LojaController(LojaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LojaResponse criar(@Valid @RequestBody LojaRequest req) {
        return service.criar(req);
    }

    @GetMapping
    public List<LojaResponse> listar() {
        return service.listar();
    }

}
