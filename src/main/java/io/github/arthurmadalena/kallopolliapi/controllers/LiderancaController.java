package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.AlertaResponse;
import io.github.arthurmadalena.kallopolliapi.DTOs.AtendimentoRequest;
import io.github.arthurmadalena.kallopolliapi.service.AlertaService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lideranca")
public class LiderancaController {

    private final AlertaService alertaService;

    public LiderancaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    // Lista os alertas pendentes do responsável
    @GetMapping("/alertas")
    public List<AlertaResponse> listarAlertas(
            @RequestHeader("X-Lideranca-Telefone") String telefone
    ) {

        return alertaService.pendentesPorTelefone(telefone);
    }

    // Registra o atendimento do alerta
    @PostMapping("/alertas/{id}/atender")
    public AlertaResponse atenderAlerta(
            @PathVariable Long id,
            @RequestHeader("X-Lideranca-Telefone") String telefone,
            @Valid @RequestBody AtendimentoRequest request
    ) {

        return alertaService.atender(
                id,
                telefone,
                request
        );
    }
}
