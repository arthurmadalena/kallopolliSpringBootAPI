package io.github.arthurmadalena.kallopolliapi.DTOs;

import io.github.arthurmadalena.kallopolliapi.entities.StatusMentoria;

import java.time.LocalDateTime;

public record MentoriaResponse(
        Long id,
        Long mentorId,
        String mentorNome,
        Long mentoradoId,
        String mentoradoNome,
        StatusMentoria status,
        LocalDateTime criadaEm
) {}
