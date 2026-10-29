package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.WebhookRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.WebhookResponse;
import io.github.arthurmadalena.kallopolliapi.service.FluxoMensagemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private final FluxoMensagemService fluxo;

    public WebhookController(FluxoMensagemService fluxo) {
        this.fluxo = fluxo;
    }

    @PostMapping("/whatsapp")
    public WebhookResponse receber(@Valid @RequestBody WebhookRequest req) {
        return fluxo.processar(req);
    }
}