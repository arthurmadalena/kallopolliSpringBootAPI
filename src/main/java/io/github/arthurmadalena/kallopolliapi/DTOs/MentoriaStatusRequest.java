package io.github.arthurmadalena.kallopolliapi.DTOs;

import io.github.arthurmadalena.kallopolliapi.entities.StatusMentoria;
import jakarta.validation.constraints.NotNull;

public record MentoriaStatusRequest(@NotNull StatusMentoria status) {}