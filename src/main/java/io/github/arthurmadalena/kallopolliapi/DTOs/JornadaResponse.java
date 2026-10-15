package io.github.arthurmadalena.kallopolliapi.DTOs;

import io.github.arthurmadalena.kallopolliapi.entities.StatusJornada;

import java.time.LocalDate;

public record JornadaResponse(
        Long id,
        Long colaboradorId,
        String colaboradorName,
        LocalDate dataInicio,
        int diaAtual,
        StatusJornada status,
        int tentativasSemResposta
) {}
