package io.github.arthurmadalena.kallopolliapi.DTOs;

public record WebhookResponse(
        String resposta,
        String intencao,
        String categoria,
        String alvo
) {
}
