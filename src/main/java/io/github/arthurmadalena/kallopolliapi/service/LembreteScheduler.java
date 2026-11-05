
package io.github.arthurmadalena.kallopolliapi.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LembreteScheduler {

    private final AlertaService alertaService;
    private final JornadaService jornadaService;

    public LembreteScheduler(
            AlertaService alertaService,
            JornadaService jornadaService
    ) {
        this.alertaService = alertaService;
        this.jornadaService = jornadaService;
    }

    // Verifica alertas vencidos a cada minuto.
    @Scheduled(fixedDelay = 60000)
    public void verificarAlertas() {
        alertaService.enviarLembretesVencidos();
    }

    // Verifica a cadência de acompanhamento a cada hora.
    @Scheduled(fixedDelay = 3600000)
    public void verificarJornadas() {
        jornadaService.processarCadencia();
    }
}

