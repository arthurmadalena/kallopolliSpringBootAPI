package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.MensagemHistoricoResponse;
import io.github.arthurmadalena.kallopolliapi.repositories.MensagemRepository;
import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
import io.github.arthurmadalena.kallopolliapi.repositories.ColaboradorRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colaboradores")
public class HistoricoMensagemController {

    private final MensagemRepository mensagemRepository;
    private final ColaboradorRepository colaboradorRepository;

    public HistoricoMensagemController(
            MensagemRepository mensagemRepository,
            ColaboradorRepository colaboradorRepository
    ) {
        this.mensagemRepository = mensagemRepository;
        this.colaboradorRepository = colaboradorRepository;
    }

    @GetMapping("/{id}/mensagens")
    public List<MensagemHistoricoResponse> listar(
            @PathVariable Long id
    ) {

        colaboradorRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Colaborador não encontrado."
                        )
                );

        return mensagemRepository
                .findByColaboradorIdOrderByCriadaEmAsc(id)
                .stream()
                .map(mensagem -> new MensagemHistoricoResponse(
                        mensagem.getId(),
                        mensagem.getDirecao(),
                        mensagem.getTexto(),
                        mensagem.getIntencao(),
                        mensagem.getCategoria(),
                        mensagem.getAlvo(),
                        mensagem.getCriadaEm()
                ))
                .toList();
    }
}
