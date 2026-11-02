package io.github.arthurmadalena.kallopolliapi.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CursoRequest(@NotBlank String titulo, @NotNull @Min(1) Integer pontos) {}