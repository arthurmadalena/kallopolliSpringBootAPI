package io.github.arthurmadalena.kallopolliapi.DTOs;

import java.time.LocalDateTime;

public record AlertaResponse(
        Long id,
        Long relatoId,
        String colaborador,
        String descricao,
        String categoria,
        String destinatario,
        String status,
        LocalDateTime criadoEm,
        LocalDateTime prazoLembreteEm,
        LocalDateTime atendidoEm,
        String retorno
) {}