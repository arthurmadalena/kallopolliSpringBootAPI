package io.github.arthurmadalena.kallopolliapi.DTOs;

import jakarta.validation.constraints.NotBlank;

public record AtendimentoRequest(
        @NotBlank String retorno
) {}