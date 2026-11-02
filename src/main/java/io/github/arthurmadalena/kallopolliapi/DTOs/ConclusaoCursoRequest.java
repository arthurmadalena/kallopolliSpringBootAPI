package io.github.arthurmadalena.kallopolliapi.DTOs;

import jakarta.validation.constraints.NotNull;

public record ConclusaoCursoRequest(@NotNull Long colaboradorId) {}