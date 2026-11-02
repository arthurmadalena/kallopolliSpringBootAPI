package io.github.arthurmadalena.kallopolliapi.DTOs;

public record MentorSugestaoResponse(
        Long colaboradorId,
        String nome,
        Long lojaId,
        boolean mesmaLoja,
        int mesesDeCasa,
        int mentoradosAtivos,
        int pontosEngajamento,
        int score
) {}