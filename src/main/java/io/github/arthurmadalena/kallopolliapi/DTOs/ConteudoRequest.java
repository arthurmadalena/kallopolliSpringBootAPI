
package io.github.arthurmadalena.kallopolliapi.DTOs;

import jakarta.validation.constraints.NotBlank;

public record ConteudoRequest(
        @NotBlank String titulo,
        @NotBlank String categoria,
        @NotBlank String texto
) {
}

