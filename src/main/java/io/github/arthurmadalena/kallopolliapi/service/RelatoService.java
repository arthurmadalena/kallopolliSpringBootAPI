
package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.entities.*;
import io.github.arthurmadalena.kallopolliapi.repositories.*;
import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
import io.github.arthurmadalena.kallopolliapi.shared.RegraNegocioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RelatoService {

    private final RelatoRepository relatos;
    private final ColaboradorRepository colaboradores;
    private final AlertaService alertas;

    public RelatoService(
            RelatoRepository relatos,
            ColaboradorRepository colaboradores,
            AlertaService alertas
    ) {
        this.relatos = relatos;
        this.colaboradores = colaboradores;
        this.alertas = alertas;
    }

    @Transactional
    public void abrirPendente(
            ColaboradorEntity autor,
            String descricao,
            String categoria,
            String alvo
    ) {

        if (autor == null) {
            throw new RegraNegocioException("Colaborador não identificado.");
        }

        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("O relato não pode estar vazio.");
        }

        boolean relataSobreGestor =
                alvo != null && alvo.trim().equalsIgnoreCase("GESTOR");

        if (relataSobreGestor && autor.getGestor() == null) {
            throw new RegraNegocioException(
                    "Não é possível encaminhar um relato sobre gestor: " +
                            "não existe gestor cadastrado para este colaborador."
            );
        }

        RelatoEntity relato = new RelatoEntity();

        relato.setColaborador(autor);
        relato.setDescricao(descricao.trim());
        relato.setCategoria(categoria);
        relato.setStatus("AGUARDANDO_CONFIRMACAO");

        if (relataSobreGestor) {
            relato.setGestorCitado(autor.getGestor());
        }

        relatos.save(relato);
    }

    @Transactional
    public String confirmarPendente(
            ColaboradorEntity autor,
            boolean confirmar
    ) {

        RelatoEntity relato = relatos
                .findFirstByColaboradorIdAndStatusOrderByCriadoEmDesc(
                        autor.getId(),
                        "AGUARDANDO_CONFIRMACAO"
                )
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Não existe relato aguardando confirmação."
                        )
                );

        if (!confirmar) {

            relato.setStatus("CANCELADO_PELO_COLABORADOR");
            relatos.save(relato);

            return "Tudo bem. Não vou encaminhar esse relato. " +
                    "Se quiser conversar, continuo por aqui.";
        }

        ColaboradorEntity destinatario =
                resolverDestinatario(autor, relato.getGestorCitado());

        relato.setDestinatario(destinatario);
        relato.setConfirmadoEm(LocalDateTime.now());
        relato.setStatus("CONFIRMADO");

        relatos.save(relato);

        alertas.criar(relato, destinatario);

        return "Obrigado por confirmar. Seu relato foi registrado " +
                "e encaminhado para a pessoa responsável.";
    }

    private ColaboradorEntity resolverDestinatario(
            ColaboradorEntity autor,
            ColaboradorEntity gestorCitado
    ) {

        ColaboradorEntity candidato;

        if (gestorCitado != null) {
            candidato = gestorCitado.getGestor();
        } else {
            candidato = autor.getGestor();
        }

        if (candidato != null
                && candidato.isAtivo()
                && (gestorCitado == null
                || !candidato.getId().equals(gestorCitado.getId()))) {
            return candidato;
        }

        if (autor.getLoja() != null) {

            List<ColaboradorEntity> admins =
                    colaboradores.findByPapelAndLojaIdAndAtivoTrue(
                            Papel.ADMIN,
                            autor.getLoja().getId()
                    );

            for (ColaboradorEntity admin : admins) {

                if (gestorCitado == null
                        || !admin.getId().equals(gestorCitado.getId())) {
                    return admin;
                }
            }
        }

        throw new RegraNegocioException(
                "Não existe responsável disponível para receber o relato. " +
                        "Cadastre um ADMIN ativo ou configure o superior do gestor."
        );
    }

    @Transactional(readOnly = true)
    public boolean temPendente(Long colaboradorId) {

        return relatos
                .findFirstByColaboradorIdAndStatusOrderByCriadoEmDesc(
                        colaboradorId,
                        "AGUARDANDO_CONFIRMACAO"
                )
                .isPresent();
    }
}
