package io.github.arthurmadalena.kallopolliapi.DTOs;

import jakarta.validation.constraints.NotBlank;

public record LojaRequest(@NotBlank String lojaName, @NotBlank String lojaCity) {
}
