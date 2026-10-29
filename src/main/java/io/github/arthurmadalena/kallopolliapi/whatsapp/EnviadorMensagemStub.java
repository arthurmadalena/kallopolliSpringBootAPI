package io.github.arthurmadalena.kallopolliapi.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EnviadorMensagemStub implements EnviadorMensagem {
    private static final Logger log = LoggerFactory.getLogger(EnviadorMensagemStub.class);

    @Override
    public void enviar(String telefone, String texto) {
        log.info("[WHATSAPP FAKE] para {}: {}", telefone, texto);
    }
}
