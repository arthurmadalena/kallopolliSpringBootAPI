package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.MentorSugestaoResponse;
import io.github.arthurmadalena.kallopolliapi.DTOs.MentoriaRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.MentoriaResponse;
import io.github.arthurmadalena.kallopolliapi.DTOs.MentoriaStatusRequest;
import io.github.arthurmadalena.kallopolliapi.service.MentoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rh/mentorias")
public class MentoriaController {

    private final MentoriaService service;

    public MentoriaController(MentoriaService service) {
        this.service = service;
    }

    @GetMapping("/sugestoes/{colaboradorId}")
    public List<MentorSugestaoResponse> sugerir(@PathVariable Long colaboradorId) {
        return service.sugerir(colaboradorId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MentoriaResponse criar(@Valid @RequestBody MentoriaRequest req) {
        return service.criar(req);
    }

    @PatchMapping("/{id}/status")
    public MentoriaResponse atualizarStatus(@PathVariable Long id,
                                            @Valid @RequestBody MentoriaStatusRequest req) {
        return service.atualizarStatus(id, req.status());
    }

    @GetMapping
    public List<MentoriaResponse> listar() {
        return service.listar();
    }
}
