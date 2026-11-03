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

@Service
public class FluxoMensagemService {

    private static final String RESPOSTA_PADRAO = "Entendi! Estou por aqui se precisar de qualquer coisa.";

    private final ColaboradorRepository colaboradorRepository;
    private final MensagemRepository mensagemRepository;
    private final ServicoIA servicoIA;
    private final EnviadorMensagem enviador;
    private final PontuacaoService pontuacaoService; // remova se não colou o bloco de ranking

    public FluxoMensagemService(ColaboradorRepository colaboradorRepository,
                                MensagemRepository mensagemRepository,
                                ServicoIA servicoIA,
                                EnviadorMensagem enviador,
                                PontuacaoService pontuacaoService) { // remova se não colou o bloco de ranking
        this.colaboradorRepository = colaboradorRepository;
        this.mensagemRepository = mensagemRepository;
        this.servicoIA = servicoIA;
        this.enviador = enviador;
        this.pontuacaoService = pontuacaoService; // remova se não colou o bloco de ranking
    }

    // ------------------------------------------------------------------
    // SIMULADOR (Postman): usa o stub de IA e envia pelo EnviadorMensagem.
    // Serve para testar sem o Python e sem o WhatsApp fake.
    // ------------------------------------------------------------------
    public WebhookResponse processar(WebhookRequest req) {
        var colaborador = buscarPorTelefone(req.telefone());
        var ia = servicoIA.analisar(colaborador.getId(), req.texto());

        var resposta = processarClassificada(new MensagemProcessadaRequest(
                req.telefone(), req.texto(),
                ia.intencao(), ia.categoria(), ia.alvo(), ia.respostaSugerida()));

        enviador.enviar(colaborador.getColaboradorTelefone(), resposta.resposta());
        return resposta;
    }

    // ------------------------------------------------------------------
    // FLUXO REAL: o Python já classificou a mensagem.
    // Aqui moram as regras de negócio. Quem chama é o Python.
    // ------------------------------------------------------------------
    public WebhookResponse processarClassificada(MensagemProcessadaRequest req) {
        var colaborador = buscarPorTelefone(req.telefone());

        // 1. Salva a mensagem recebida já com a classificação
        var entrada = salvar(colaborador, DirecaoMensagem.ENTRADA, req.texto());
        entrada.setIntencao(req.intencao());
        entrada.setCategoria(req.categoria());
        entrada.setAlvo(req.alvo());
        mensagemRepository.save(entrada);

        // 2. Pontos por responder (1 vez por dia)
        pontuacaoService.registrarRespostaIA(colaborador, req.texto()); // remova se não colou o bloco de ranking

        // 3. Regras de negócio sobre o JSON
        switch (req.intencao()) {
            case "RECLAMACAO" -> {
                // PRÓXIMA ETAPA: pedir confirmação, criar relato e alerta
                // (alvo = GESTOR -> encaminhar ao nível acima)
            }
            case "DUVIDA" -> {
                // PRÓXIMA ETAPA: resposta com base no material de treinamento
            }
            default -> {
                // CONVERSA: só segue a conversa
            }
        }

        // 4. Registra a resposta e devolve o texto final
        var texto = (req.respostaSugerida() != null && !req.respostaSugerida().isBlank())
                ? req.respostaSugerida()
                : RESPOSTA_PADRAO;

        salvar(colaborador, DirecaoMensagem.SAIDA, texto);

        return new WebhookResponse(texto, req.intencao(), req.categoria(), req.alvo());
    }

    private ColaboradorEntity buscarPorTelefone(String telefone) {
        return colaboradorRepository.findByColaboradorTelefone(telefone)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado"));
    }

    private MensagemEntity salvar(ColaboradorEntity colaborador, DirecaoMensagem direcao, String texto) {
        var m = new MensagemEntity();
        m.setColaborador(colaborador);
        m.setDirecao(direcao);
        m.setTexto(texto);
        return mensagemRepository.save(m);
    }
}