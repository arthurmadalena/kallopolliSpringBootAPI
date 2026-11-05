package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.DashboardRhResponse;
import io.github.arthurmadalena.kallopolliapi.repositories.AlertaRepository;
import io.github.arthurmadalena.kallopolliapi.repositories.RelatoRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rh")
public class RhController {

    private final RelatoRepository relatoRepository;
    private final AlertaRepository alertaRepository;

    public RhController(
            RelatoRepository relatoRepository,
            AlertaRepository alertaRepository
    ) {
        this.relatoRepository = relatoRepository;
        this.alertaRepository = alertaRepository;
    }

    @GetMapping("/dashboard")
    public DashboardRhResponse dashboard() {

        return new DashboardRhResponse(
                relatoRepository.countByStatus("AGUARDANDO_CONFIRMACAO"),
                relatoRepository.countByStatus("CONFIRMADO"),
                relatoRepository.countByStatus("CANCELADO_PELO_COLABORADOR"),
                alertaRepository.countByStatus("PENDENTE"),
                alertaRepository.countByStatus("ATENDIDO")
        );
    }
}