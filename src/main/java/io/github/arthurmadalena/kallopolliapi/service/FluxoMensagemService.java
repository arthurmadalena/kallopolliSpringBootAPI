package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.DTOs.MensagemProcessadaRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.WebhookRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.WebhookResponse;
import io.github.arthurmadalena.kallopolliapi.entities.ColaboradorEntity;
import io.github.arthurmadalena.kallopolliapi.entities.DirecaoMensagem;
import io.github.arthurmadalena.kallopolliapi.entities.MensagemEntity;
import io.github.arthurmadalena.kallopolliapi.ia.ServicoIA;
import io.github.arthurmadalena.kallopolliapi.repositories.ColaboradorRepository;
import io.github.arthurmadalena.kallopolliapi.repositories.MensagemRepository;
import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
import io.github.arthurmadalena.kallopolliapi.whatsapp.EnviadorMensagem;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;

@Service
public class FluxoMensagemService {

    private static final String RESPOSTA_PADRAO =
            "Entendi! Estou por aqui se precisar de qualquer coisa.";

    private final ColaboradorRepository colaboradorRepository;
    private final MensagemRepository mensagemRepository;
    private final ServicoIA servicoIA;
    private final EnviadorMensagem enviador;
    private final PontuacaoService pontuacaoService;
    private final RelatoService relatoService;

    public FluxoMensagemService(
            ColaboradorRepository colaboradorRepository,
            MensagemRepository mensagemRepository,
            ServicoIA servicoIA,
            EnviadorMensagem enviador,
            PontuacaoService pontuacaoService,
            RelatoService relatoService
    ) {
        this.colaboradorRepository = colaboradorRepository;
        this.mensagemRepository = mensagemRepository;
        this.servicoIA = servicoIA;
        this.enviador = enviador;
        this.pontuacaoService = pontuacaoService;
        this.relatoService = relatoService;
    }

    // Simulador local: usa o stub da IA
    @Transactional
    public WebhookResponse processar(WebhookRequest req) {

        ColaboradorEntity colaborador = buscarPorTelefone(req.telefone());

        var ia = servicoIA.analisar(
                colaborador.getId(),
                req.texto()
        );

        WebhookResponse resposta = processarClassificada(
                new MensagemProcessadaRequest(
                        req.telefone(),
                        req.texto(),
                        ia.intencao(),
                        ia.categoria(),
                        ia.alvo(),
                        ia.respostaSugerida()
                )
        );

        enviador.enviar(
                colaborador.getColaboradorTelefone(),
                resposta.resposta()
        );

        return resposta;
    }

    // Fluxo real: o Python já classificou a mensagem
    @Transactional
    public WebhookResponse processarClassificada(
            MensagemProcessadaRequest req
    ) {

        ColaboradorEntity colaborador =
                buscarPorTelefone(req.telefone());

        // 1. Salva a mensagem recebida
        MensagemEntity entrada = salvar(
                colaborador,
                DirecaoMensagem.ENTRADA,
                req.texto()
        );

        entrada.setIntencao(req.intencao());
        entrada.setCategoria(req.categoria());
        entrada.setAlvo(req.alvo());

        mensagemRepository.save(entrada);

        // 2. Registra a pontuação pela interação
        pontuacaoService.registrarRespostaIA(
                colaborador,
                req.texto()
        );

        String textoNormalizado = normalizar(req.texto());

        String resposta;

        // 3. Verifica se existe um relato esperando SIM ou NÃO
        if (relatoService.temPendente(colaborador.getId())) {

            if (ehSim(textoNormalizado)) {

                resposta = relatoService.confirmarPendente(
                        colaborador,
                        true
                );

            } else if (ehNao(textoNormalizado)) {

                resposta = relatoService.confirmarPendente(
                        colaborador,
                        false
                );

            } else {

                resposta = "Seu relato ainda está aguardando confirmação. " +
                        "Responda SIM para encaminhar ou NÃO para cancelar.";
            }

        } else {

            // 4. Aplica as regras de negócio conforme a classificação da IA
            String intencao = req.intencao() == null
                    ? "CONVERSA"
                    : req.intencao().trim().toUpperCase(Locale.ROOT);

            switch (intencao) {

                case "RECLAMACAO", "RECLAMAÇÃO" -> {

                    // Cria relato pendente, sem enviar alerta ainda
                    relatoService.abrirPendente(
                            colaborador,
                            req.texto(),
                            req.categoria(),
                            req.alvo()
                    );

                    resposta = "Sinto muito que isso esteja acontecendo. " +
                            "Quero encaminhar seu relato para a pessoa responsável. " +
                            "Você confirma que deseja registrar e compartilhar " +
                            "esse relato? Responda SIM para confirmar ou NÃO para cancelar.";
                }

                case "DUVIDA", "DÚVIDA" -> {

                    resposta = "Recebi sua dúvida! Em breve vou consultar " +
                            "os materiais de treinamento para te ajudar.";
                }

                default -> {

                    resposta = (
                            req.respostaSugerida() != null &&
                                    !req.respostaSugerida().isBlank()
                    )
                            ? req.respostaSugerida()
                            : RESPOSTA_PADRAO;
                }
            }
        }

        // 5. Salva a resposta que será devolvida ao Python ou ao simulador
        salvar(
                colaborador,
                DirecaoMensagem.SAIDA,
                resposta
        );

        return new WebhookResponse(
                resposta,
                req.intencao(),
                req.categoria(),
                req.alvo()
        );
    }

    private ColaboradorEntity buscarPorTelefone(String telefone) {

        return colaboradorRepository
                .findByColaboradorTelefone(telefone)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Colaborador não encontrado."
                        )
                );
    }

    private MensagemEntity salvar(
            ColaboradorEntity colaborador,
            DirecaoMensagem direcao,
            String texto
    ) {

        MensagemEntity mensagem = new MensagemEntity();

        mensagem.setColaborador(colaborador);
        mensagem.setDirecao(direcao);
        mensagem.setTexto(texto);

        return mensagemRepository.save(mensagem);
    }

    // Remove acentos e padroniza o texto
    private String normalizar(String texto) {

        return Normalizer.normalize(
                        texto == null ? "" : texto,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private boolean ehSim(String texto) {

        return texto.equals("sim") ||
                texto.equals("s") ||
                texto.startsWith("sim ");
    }

    private boolean ehNao(String texto) {

        return texto.equals("nao") ||
                texto.equals("n") ||
                texto.startsWith("nao ");
    }
}