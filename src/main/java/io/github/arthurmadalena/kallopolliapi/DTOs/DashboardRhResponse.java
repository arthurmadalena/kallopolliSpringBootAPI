package io.github.arthurmadalena.kallopolliapi.DTOs;

public record DashboardRhResponse(
        long relatosAguardandoConfirmacao,
        long relatosConfirmados,
        long relatosCancelados,
        long alertasPendentes,
        long alertasAtendidos
) {
}