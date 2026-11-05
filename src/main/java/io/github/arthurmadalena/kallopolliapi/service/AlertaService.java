package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.DTOs.AlertaResponse;
import io.github.arthurmadalena.kallopolliapi.DTOs.AtendimentoRequest;
import io.github.arthurmadalena.kallopolliapi.entities.AlertaEntity;
import io.github.arthurmadalena.kallopolliapi.entities.ColaboradorEntity;
import io.github.arthurmadalena.kallopolliapi.entities.RelatoEntity;
import io.github.arthurmadalena.kallopolliapi.repositories.AlertaRepository;
import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
import io.github.arthurmadalena.kallopolliapi.whatsapp.EnviadorMensagem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository repository;
    private final EnviadorMensagem enviador;

    public AlertaService(
            AlertaRepository repository,
            EnviadorMensagem enviador
    ) {
        this.repository = repository;
        this.enviador = enviador;
    }

    // Cria o alerta e avisa o responsável
    @Transactional
    public AlertaEntity criar(RelatoEntity relato, ColaboradorEntity destinatario) {

        if (destinatario == null) {
            throw new IllegalStateException(
                    "Não existe responsável configurado para receber o alerta."
            );
        }

        // Proteção: o gestor citado nunca pode receber o próprio alerta
        if (relato.getGestorCitado() != null &&
                relato.getGestorCitado().getId().equals(destinatario.getId())) {

            throw new IllegalStateException(
                    "O gestor citado não pode receber o próprio alerta."
            );
        }

        AlertaEntity alerta = new AlertaEntity();

        alerta.setRelato(relato);
        alerta.setDestinatario(destinatario);
        alerta.setStatus("PENDENTE");

        // O responsável terá 24 horas para atender
        alerta.setPrazoLembreteEm(LocalDateTime.now().plusHours(24));

        alerta = repository.save(alerta);

        // Envia a notificação pelo WhatsApp Fake (ou pelo stub)
        enviador.enviar(
                destinatario.getColaboradorTelefone(),
                "Você recebeu um novo alerta do Parceiro dos 90 dias. " +
                        "Acesse o painel da liderança para consultar e atender o alerta #" +
                        alerta.getId()
        );

        return alerta;
    }

    // Lista alertas pendentes de um responsável
    @Transactional(readOnly = true)
    public List<AlertaResponse> pendentesPorTelefone(String telefone) {

        return repository
                .findByDestinatarioColaboradorTelefoneAndStatusOrderByCriadoEmDesc(
                        telefone,
                        "PENDENTE"
                )
                .stream()
                .map(this::converter)
                .toList();
    }

    // Lista todos os alertas
    @Transactional(readOnly = true)
    public List<AlertaResponse> listarTodos() {

        return repository.findAllByOrderByCriadoEmDesc()
                .stream()
                .map(this::converter)
                .toList();
    }

    // Lista alertas pendentes
    @Transactional(readOnly = true)
    public List<AlertaResponse> listarPendentes() {

        return repository.findByStatusOrderByCriadoEmDesc("PENDENTE")
                .stream()
                .map(this::converter)
                .toList();
    }

    // Lista alertas já atendidos
    @Transactional(readOnly = true)
    public List<AlertaResponse> listarAtendidos() {

        return repository.findByStatusOrderByCriadoEmDesc("ATENDIDO")
                .stream()
                .map(this::converter)
                .toList();
    }

    // Registra o atendimento de um alerta
    @Transactional
    public AlertaResponse atender(
            Long id,
            String telefoneResponsavel,
            AtendimentoRequest request
    ) {

        AlertaEntity alerta = repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Alerta não encontrado.")
                );

        // Confere se o alerta pertence ao responsável que está atendendo
        if (!alerta.getDestinatario()
                .getColaboradorTelefone()
                .equals(telefoneResponsavel)) {

            throw new RecursoNaoEncontradoException(
                    "Alerta não encontrado para este responsável."
            );
        }

        if (!"PENDENTE".equals(alerta.getStatus())) {
            throw new IllegalStateException(
                    "Este alerta já foi atendido."
            );
        }

        alerta.setStatus("ATENDIDO");
        alerta.setRetorno(request.retorno());
        alerta.setAtendidoEm(LocalDateTime.now());

        return converter(repository.save(alerta));
    }

    // Verifica se existem alertas que passaram de 24 horas
    @Transactional
    public void enviarLembretesVencidos() {

        List<AlertaEntity> vencidos =
                repository.findByStatusAndPrazoLembreteEmBeforeAndLembreteEnviadoFalse(
                        "PENDENTE",
                        LocalDateTime.now()
                );

        for (AlertaEntity alerta : vencidos) {

            enviador.enviar(
                    alerta.getDestinatario().getColaboradorTelefone(),
                    "Lembrete: o alerta #" + alerta.getId() +
                            " ainda está aguardando atendimento. " +
                            "Acesse o painel e registre seu retorno."
            );

            alerta.setLembreteEnviado(true);
            repository.save(alerta);
        }
    }

    private AlertaResponse converter(AlertaEntity alerta) {

        RelatoEntity relato = alerta.getRelato();

        return new AlertaResponse(
                alerta.getId(),
                relato.getId(),
                relato.getColaborador().getColaboradorName(),
                relato.getDescricao(),
                relato.getCategoria(),
                alerta.getDestinatario().getColaboradorName(),
                alerta.getStatus(),
                alerta.getCriadoEm(),
                alerta.getPrazoLembreteEm(),
                alerta.getAtendidoEm(),
                alerta.getRetorno()
        );
    }
}