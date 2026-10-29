package io.github.arthurmadalena.kallopolliapi.DTOs;

import jakarta.validation.constraints.NotBlank;

public record WebhookRequest(
        @NotBlank String telefone,
        @NotBlank String texto
) {
}
