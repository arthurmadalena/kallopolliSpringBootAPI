package io.github.arthurmadalena.kallopolliapi.DTOs;

import io.github.arthurmadalena.kallopolliapi.entities.Papel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.time.LocalDate;

public record ColaboradorRequest (@NotBlank String colaboradorName,
                                  @NotBlank String colaboradorTelefone,
                                  @NotNull Papel papel,
                                  @NotNull Long lojaId,
                                  Long gestorId,
                                  @NotNull LocalDate dataAdmissao,
                                  String funcao,
                                  Boolean mentorDisponivel)
{

}
