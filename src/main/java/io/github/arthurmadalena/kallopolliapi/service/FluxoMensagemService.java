
package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.DTOs.MensagemProcessadaRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.WebhookRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.WebhookResponse;

import io.github.arthurmadalena.kallopolliapi.entities.ColaboradorEntity;
import io.github.arthurmadalena.kallopolliapi.entities.ConteudoEntity;
import io.github.arthurmadalena.kallopolliapi.entities.DirecaoMensagem;
import io.github.arthurmadalena.kallopolliapi.entities.MensagemEntity;

import io.github.arthurmadalena.kallopolliapi.ia.ServicoIA;

import io.github.arthurmadalena.kallopolliapi.repositories.ColaboradorRepository;
import io.github.arthurmadalena.kallopolliapi.repositories.MensagemRepository;

import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
import io.github.arthurmadalena.kallopolliapi.shared.RegraNegocioException;

import io.github.arthurmadalena.kallopolliapi.whatsapp.EnviadorMensagem;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
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
    private final ConteudoService conteudoService;
    private final JornadaService jornadaService;

    public FluxoMensagemService(
            ColaboradorRepository colaboradorRepository,
            MensagemRepository mensagemRepository,
            ServicoIA servicoIA,
            EnviadorMensagem enviador,
            PontuacaoService pontuacaoService,
            RelatoService relatoService,
            ConteudoService conteudoService,
            JornadaService jornadaService
    ) {
        this.colaboradorRepository = colaboradorRepository;
        this.mensagemRepository = mensagemRepository;
        this.servicoIA = servicoIA;
        this.enviador = enviador;
        this.pontuacaoService = pontuacaoService;
        this.relatoService = relatoService;
        this.conteudoService = conteudoService;
        this.jornadaService = jornadaService;
    }

    // Simulador local: utiliza o stub de IA.
    @Transactional
    public WebhookResponse processar(WebhookRequest req) {

        ColaboradorEntity colaborador = buscarPorTelefone(req.telefone());

        var ia = servicoIA.analisar(
                colaborador.getId(),
                req.texto()
        );

        MensagemProcessadaRequest processada =
                new MensagemProcessadaRequest(
                        req.telefone(),
                        req.texto(),
                        ia.intencao(),
                        ia.categoria(),
                        ia.alvo(),
                        ia.respostaSugerida()
                );

        return processarClassificada(processada);
    }

    // Fluxo principal: recebe a classificação feita pela IA em Python.
    @Transactional
    public WebhookResponse processarClassificada(
            MensagemProcessadaRequest req
    ) {

        ColaboradorEntity colaborador =
                buscarPorTelefone(req.telefone());

        if (!colaborador.isAtivo()) {
            throw new RegraNegocioException(
                    "Este colaborador está inativo e não pode utilizar o atendimento."
            );
        }

        // Registra que o colaborador voltou a interagir.
        jornadaService.registrarResposta(colaborador.getId());

        // Salva a mensagem recebida.
        MensagemEntity entrada = salvar(
                colaborador,
                DirecaoMensagem.ENTRADA,
                req.texto()
        );

        entrada.setIntencao(req.intencao());
        entrada.setCategoria(req.categoria());
        entrada.setAlvo(req.alvo());

        mensagemRepository.save(entrada);

        // Registra a interação para a gamificação.
        pontuacaoService.registrarRespostaIA(
                colaborador,
                req.texto()
        );

        String textoNormalizado = normalizar(req.texto());

        String resposta;

        // Primeiro verifica se há um relato aguardando SIM ou NÃO.
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

            String intencao = req.intencao() == null
                    ? "CONVERSA"
                    : req.intencao().trim().toUpperCase(Locale.ROOT);

            switch (intencao) {

                case "RECLAMACAO", "RECLAMAÇÃO" -> {

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

                    resposta = responderComConteudo(req.categoria());
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

        // Salva a resposta no histórico.
        salvar(
                colaborador,
                DirecaoMensagem.SAIDA,
                resposta
        );

        WebhookResponse retorno = new WebhookResponse(
                resposta,
                req.intencao(),
                req.categoria(),
                req.alvo()
        );

        // Executa o envio pelo componente configurado.
        enviador.enviar(
                colaborador.getColaboradorTelefone(),
                resposta
        );

        return retorno;
    }

    private String responderComConteudo(String categoria) {

        List<ConteudoEntity> conteudos =
                conteudoService.buscarPorCategoria(categoria);

        if (conteudos.isEmpty()) {
            conteudos = conteudoService.listarAtivos();
        }

        if (conteudos.isEmpty()) {
            return "Ainda não encontrei um material específico sobre " +
                    "essa dúvida. Vou registrar sua necessidade para que " +
                    "a equipe possa orientar você.";
        }

        StringBuilder texto = new StringBuilder();

        texto.append(
                "Encontrei estas informações nos materiais de treinamento:\n\n"
        );

        int limite = Math.min(conteudos.size(), 3);

        for (int i = 0; i < limite; i++) {

            ConteudoEntity conteudo = conteudos.get(i);

            texto.append("• ")
                    .append(conteudo.getTitulo())
                    .append("\n")
                    .append(conteudo.getTexto())
                    .append("\n\n");
        }

        return texto.toString().trim();
    }

    private ColaboradorEntity buscarPorTelefone(String telefone) {

        if (telefone == null || telefone.isBlank()) {
            throw new RecursoNaoEncontradoException(
                    "Telefone não informado."
            );
        }

        String telefoneNormalizado = telefone.replaceAll("\\D", "");

        return colaboradorRepository
                .findByColaboradorTelefone(telefoneNormalizado)
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

        return texto.equals("sim")
                || texto.equals("s")
                || texto.startsWith("sim ");
    }

    private boolean ehNao(String texto) {

        return texto.equals("nao")
                || texto.equals("n")
                || texto.startsWith("nao ");
    }
}
