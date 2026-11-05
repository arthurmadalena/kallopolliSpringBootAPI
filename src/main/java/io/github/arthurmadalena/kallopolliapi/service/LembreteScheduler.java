package io.github.arthurmadalena.kallopolliapi.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LembreteScheduler {

    private final AlertaService alertaService;

    public LembreteScheduler(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    // Executa a verificação uma vez por minuto
    @Scheduled(fixedDelay = 60000)
    public void verificarAlertas() {

        alertaService.enviarLembretesVencidos();
    }
}
