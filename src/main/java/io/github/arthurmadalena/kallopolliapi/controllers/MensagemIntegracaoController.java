package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.MensagemProcessadaRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.WebhookResponse;
import io.github.arthurmadalena.kallopolliapi.service.FluxoMensagemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/mensagens")
public class MensagemIntegracaoController {

    private final FluxoMensagemService fluxo;
    private final String chaveEsperada;

    public MensagemIntegracaoController(
            FluxoMensagemService fluxo,
            @Value("${app.integracao.api-key}") String chaveEsperada) {
        this.fluxo = fluxo;
        this.chaveEsperada = chaveEsperada;
    }

    @PostMapping("/processadas")
    public WebhookResponse receber(
            @RequestHeader(value = "X-API-Key", required = false) String chave,
            @Valid @RequestBody MensagemProcessadaRequest req) {

        if (!chaveValida(chave)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chave inválida");
        }
        return fluxo.processarClassificada(req);
    }

    private boolean chaveValida(String chave) {
        if (chave == null) return false;
        return MessageDigest.isEqual(
                chave.getBytes(StandardCharsets.UTF_8),
                chaveEsperada.getBytes(StandardCharsets.UTF_8));
    }
}