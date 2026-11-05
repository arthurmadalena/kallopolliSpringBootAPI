package io.github.arthurmadalena.kallopolliapi.DTOs;

import io.github.arthurmadalena.kallopolliapi.entities.DirecaoMensagem;

import java.time.LocalDateTime;

public record MensagemHistoricoResponse(
        Long id,
        DirecaoMensagem direcao,
        String texto,
        String intencao,
        String categoria,
        String alvo,
        LocalDateTime criadaEm
) {
}