
package io.github.arthurmadalena.kallopolliapi.DTOs;

public record ConteudoResponse(
        Long id,
        String titulo,
        String categoria,
        String texto,
        boolean ativo
) {
}
