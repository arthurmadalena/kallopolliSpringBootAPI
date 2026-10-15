package io.github.arthurmadalena.kallopolliapi.DTOs;

import java.awt.print.Paper;

public record ColaboradorResponse(Long id, String colaboradorName, String colaboradorTelefone, Paper papel, Long lojaId) {
}
