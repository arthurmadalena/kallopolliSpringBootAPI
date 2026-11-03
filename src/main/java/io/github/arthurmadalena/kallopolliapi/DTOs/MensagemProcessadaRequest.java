package io.github.arthurmadalena.kallopolliapi.DTOs;

import jakarta.validation.constraints.NotBlank;

public record MensagemProcessadaRequest(
        @NotBlank String telefone,
        @NotBlank String texto,
        @NotBlank String intencao,
        String categoria,
        String alvo,
        String respostaSugerida
) {}
