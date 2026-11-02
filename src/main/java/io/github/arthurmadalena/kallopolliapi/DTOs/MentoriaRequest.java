package io.github.arthurmadalena.kallopolliapi.DTOs;

import jakarta.validation.constraints.NotNull;

public record MentoriaRequest(@NotNull Long mentorId, @NotNull Long mentoradoId) {}