package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.ColaboradorPontosResponse;
import io.github.arthurmadalena.kallopolliapi.DTOs.RankingItemResponse;
import io.github.arthurmadalena.kallopolliapi.entities.Periodo;
import io.github.arthurmadalena.kallopolliapi.service.PontuacaoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RankingController {

    private final PontuacaoService service;

    public RankingController(PontuacaoService service) {
        this.service = service;
    }

    @GetMapping("/ranking")
    public List<RankingItemResponse> ranking(
            @RequestParam(defaultValue = "SEMANA") Periodo periodo,
            @RequestParam(required = false) Long lojaId) {
        return service.top(periodo, lojaId);
    }

    @GetMapping("/colaboradores/{id}/pontos")
    public ColaboradorPontosResponse pontos(@PathVariable Long id) {
        return service.resumo(id);
    }
}