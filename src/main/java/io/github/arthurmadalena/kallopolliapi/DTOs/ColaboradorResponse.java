package io.github.arthurmadalena.kallopolliapi.DTOs;

import io.github.arthurmadalena.kallopolliapi.entities.Papel;



public record ColaboradorResponse(Long id,
                                  String colaboradorName,
                                  String colaboradorTelefone,
                                  Papel papel,
                                  Long lojaId,
                                  String funcao,
                                  boolean mentorDisponivel) {
}
