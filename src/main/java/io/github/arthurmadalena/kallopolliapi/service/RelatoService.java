package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.entities.*;
import io.github.arthurmadalena.kallopolliapi.repositories.*;
import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
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

    // Registra o relato, mas ainda não encaminha para ninguém
    @Transactional
    public void abrirPendente(
            ColaboradorEntity autor,
            String descricao,
            String categoria,
            String alvo
    ) {

        RelatoEntity relato = new RelatoEntity();

        relato.setColaborador(autor);
        relato.setDescricao(descricao);
        relato.setCategoria(categoria);
        relato.setStatus("AGUARDANDO_CONFIRMACAO");

        // Se o relato for sobre o gestor, registra quem foi citado
        if ("GESTOR".equalsIgnoreCase(alvo)) {
            relato.setGestorCitado(autor.getGestor());
        }

        relatos.save(relato);
    }

    // Confirma ou cancela o relato
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

        // Se o colaborador responder NÃO, cancela o encaminhamento
        if (!confirmar) {

            relato.setStatus("CANCELADO_PELO_COLABORADOR");
            relatos.save(relato);

            return "Tudo bem. Não vou encaminhar esse relato. " +
                    "Se quiser conversar, continuo por aqui.";
        }

        // Se responder SIM, procura o responsável correto
        ColaboradorEntity destinatario =
                resolverDestinatario(autor, relato.getGestorCitado());

        relato.setDestinatario(destinatario);
        relato.setConfirmadoEm(LocalDateTime.now());
        relato.setStatus("CONFIRMADO");

        relatos.save(relato);

        // Cria o alerta e envia a notificação
        alertas.criar(relato, destinatario);

        return "Obrigado por confirmar. Seu relato foi registrado " +
                "e encaminhado para a pessoa responsável.";
    }

    // Define quem receberá o relato
    private ColaboradorEntity resolverDestinatario(
            ColaboradorEntity autor,
            ColaboradorEntity gestorCitado
    ) {

        ColaboradorEntity candidato;

        if (gestorCitado != null) {

            // Se o relato é sobre o gestor, encaminha ao superior dele
            candidato = gestorCitado.getGestor();

        } else {

            // Caso normal: encaminha ao gestor do colaborador
            candidato = autor.getGestor();
        }

        // Confere se o destinatário não é o próprio gestor citado
        if (candidato != null &&
                (gestorCitado == null ||
                        !candidato.getId().equals(gestorCitado.getId()))) {

            return candidato;
        }

        // Se não houver superior disponível, procura um ADMIN da loja
        if (autor.getLoja() != null) {

            List<ColaboradorEntity> admins =
                    colaboradores.findByPapelAndLojaIdAndAtivoTrue(
                            Papel.ADMIN,
                            autor.getLoja().getId()
                    );

            for (ColaboradorEntity admin : admins) {

                if (gestorCitado == null ||
                        !admin.getId().equals(gestorCitado.getId())) {

                    return admin;
                }
            }
        }

        throw new IllegalStateException(
                "Não existe responsável disponível para receber o relato. " +
                        "Cadastre um ADMIN ou configure o superior do gestor."
        );
    }

    // Verifica se existe relato aguardando confirmação
    public boolean temPendente(Long colaboradorId) {

        return relatos
                .findFirstByColaboradorIdAndStatusOrderByCriadoEmDesc(
                        colaboradorId,
                        "AGUARDANDO_CONFIRMACAO"
                )
                .isPresent();
    }
}