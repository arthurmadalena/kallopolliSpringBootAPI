
package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.DTOs.JornadaResponse;
import io.github.arthurmadalena.kallopolliapi.entities.ColaboradorEntity;
import io.github.arthurmadalena.kallopolliapi.entities.JornadaEntity;
import io.github.arthurmadalena.kallopolliapi.entities.StatusJornada;
import io.github.arthurmadalena.kallopolliapi.repositories.JornadaRepository;
import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
import io.github.arthurmadalena.kallopolliapi.whatsapp.EnviadorMensagem;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JornadaService {

    public static final int DURACAO_DIAS = 90;
    private static final int MAX_TENTATIVAS = 3;

    private final JornadaRepository repository;
    private final EnviadorMensagem enviador;

    public JornadaService(
            JornadaRepository repository,
            EnviadorMensagem enviador
    ) {
        this.repository = repository;
        this.enviador = enviador;
    }

    @Transactional
    public JornadaEntity criarPara(ColaboradorEntity colaborador) {

        var existente = repository.findByColaboradorId(colaborador.getId());

        if (existente.isPresent()) {
            return existente.get();
        }

        var jornada = new JornadaEntity();
        jornada.setColaborador(colaborador);
        jornada.setDataInicio(colaborador.getDataAdmissao());
        jornada.setStatus(StatusJornada.ATIVA);

        return repository.save(jornada);
    }

    @Transactional(readOnly = true)
    public JornadaResponse buscarPorColaborador(Long colaboradorId) {

        var jornada = repository.findByColaboradorId(colaboradorId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Jornada não encontrada."
                        )
                );

        return toResponse(jornada);
    }

    @Transactional(readOnly = true)
    public List<JornadaResponse> listar() {

        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public int calcularDiaAtual(JornadaEntity jornada) {

        long dias = ChronoUnit.DAYS.between(
                jornada.getDataInicio(),
                LocalDate.now()
        ) + 1;

        if (dias < 1) {
            return 0;
        }

        return (int) Math.min(dias, DURACAO_DIAS);
    }

    // Executado pelo agendador para enviar mensagens de acompanhamento.
    @Transactional
    public void processarCadencia() {

        List<JornadaEntity> jornadas =
                repository.findByStatus(StatusJornada.ATIVA);

        LocalDateTime agora = LocalDateTime.now();

        for (JornadaEntity jornada : jornadas) {

            int dia = calcularDiaAtual(jornada);

            if (dia < 1) {
                continue;
            }

            if (dia > DURACAO_DIAS) {
                jornada.setStatus(StatusJornada.CONCLUIDA);
                repository.save(jornada);
                continue;
            }

            if (jornada.getTentativasSemResposta() >= MAX_TENTATIVAS) {
                continue;
            }

            if (!deveEnviar(jornada, dia, agora)) {
                continue;
            }

            String mensagem = montarMensagem(dia);

            enviador.enviar(
                    jornada.getColaborador().getColaboradorTelefone(),
                    mensagem
            );

            jornada.setUltimoEnvioCadencia(agora);
            jornada.setTentativasSemResposta(
                    jornada.getTentativasSemResposta() + 1
            );

            repository.save(jornada);
        }
    }

    private boolean deveEnviar(
            JornadaEntity jornada,
            int dia,
            LocalDateTime agora
    ) {

        if (jornada.getUltimoEnvioCadencia() == null) {
            return true;
        }

        long horasDesdeUltimoEnvio = ChronoUnit.HOURS.between(
                jornada.getUltimaMensagemEm(),
                agora
        );

        // Evita repetir mensagens no mesmo dia.
        if (horasDesdeUltimoEnvio < 20) {
            return false;
        }

        int intervaloDias;

        if (dia <= 10) {
            intervaloDias = 1;
        } else if (dia <= 30) {
            intervaloDias = 3;
        } else {
            intervaloDias = 7;
        }

        long diasDesdeUltimoEnvio = ChronoUnit.DAYS.between(
                jornada.getUltimaMensagemEm().toLocalDate(),
                agora.toLocalDate()
        );

        return diasDesdeUltimoEnvio >= intervaloDias;
    }

    private String montarMensagem(int dia) {

        if (dia == 1) {
            return "Olá! Seja bem-vindo(a) à Kallopolli! " +
                    "Estamos aqui para acompanhar seus primeiros 90 dias. " +
                    "Como está sendo seu início?";
        }

        if (dia == 7) {
            return "Você completou sua primeira semana! " +
                    "Como está sua adaptação à equipe e à rotina?";
        }

        if (dia == 30) {
            return "Você chegou ao seu primeiro mês na Kallopolli! " +
                    "Tem alguma dúvida ou dificuldade que gostaria de compartilhar?";
        }

        if (dia == 60) {
            return "Você está completando 60 dias! " +
                    "Como está sua experiência até aqui? " +
                    "Queremos ouvir você.";
        }

        if (dia == 90) {
            return "Parabéns pelos seus 90 dias na Kallopolli! " +
                    "Obrigado por fazer parte da equipe. " +
                    "Como você avalia sua jornada até agora?";
        }

        return "Olá! Estamos passando para saber como você está. " +
                "Como está sua experiência na Kallopolli hoje?";
    }

    // Uma mensagem recebida significa que o colaborador voltou a interagir.
    @Transactional
    public void registrarResposta(Long colaboradorId) {

        repository.findByColaboradorId(colaboradorId)
                .ifPresent(jornada -> {
                    jornada.setTentativasSemResposta(0);
                    jornada.setUltimaMensagemEm(LocalDateTime.now());
                    repository.save(jornada);
                });
    }

    private JornadaResponse toResponse(JornadaEntity jornada) {

        return new JornadaResponse(
                jornada.getId(),
                jornada.getColaborador().getId(),
                jornada.getColaborador().getColaboradorName(),
                jornada.getDataInicio(),
                calcularDiaAtual(jornada),
                jornada.getStatus(),
                jornada.getTentativasSemResposta()
        );
    }
}

