package io.github.arthurmadalena.kallopolliapi.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.awt.print.Paper;
import java.time.LocalDate;

public record ColaboradorRequest (@NotBlank String colaboradorName,
                                  @NotBlank String colaboradorTelefone,
                                  @NotNull Paper papel,
                                  @NotNull Long lojaId,
                                  Long gestorId,
                                  @NotNull LocalDate dataAdmissao)
{

}
