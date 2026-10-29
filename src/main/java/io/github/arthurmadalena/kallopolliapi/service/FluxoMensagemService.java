package io.github.arthurmadalena.kallopolliapi.service;

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

    private final ColaboradorRepository colaboradorRepository;
    private final MensagemRepository mensagemRepository;
    private final ServicoIA servicoIA;
    private final EnviadorMensagem enviador;

    public FluxoMensagemService(ColaboradorRepository colaboradorRepository,
                                MensagemRepository mensagemRepository,
                                ServicoIA servicoIA,
                                EnviadorMensagem enviador) {
        this.colaboradorRepository = colaboradorRepository;
        this.mensagemRepository = mensagemRepository;
        this.servicoIA = servicoIA;
        this.enviador = enviador;
    }

    public WebhookResponse processar(WebhookRequest req) {
        var colaborador = colaboradorRepository.findByColaboradorTelefone(req.telefone())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado"));

        // 1. Salva a mensagem recebida (antes de chamar a IA, para não perder se ela falhar)
        var entrada = salvar(colaborador, DirecaoMensagem.ENTRADA, req.texto());

        // 2. Pergunta para a IA
        var ia = servicoIA.analisar(colaborador.getId(), req.texto());

        // 3. Guarda a classificação na mensagem recebida
        entrada.setIntencao(ia.intencao());
        entrada.setCategoria(ia.categoria());
        entrada.setAlvo(ia.alvo());
        mensagemRepository.save(entrada);

        // 4. Regras de negócio sobre o JSON
        switch (ia.intencao()) {
            case "RECLAMACAO" -> {
                // PRÓXIMA ETAPA: pedir confirmação, criar relato e alerta
                // (alvo = GESTOR -> encaminhar ao nível acima)
            }
            case "DUVIDA" -> {
                // PRÓXIMA ETAPA: responder com base no material de treinamento
            }
            default -> {
                // CONVERSA: só segue a conversa
            }
        }

        // 5. Responde ao colaborador e registra a saída
        enviador.enviar(colaborador.getColaboradorTelefone(), ia.respostaSugerida());
        salvar(colaborador, DirecaoMensagem.SAIDA, ia.respostaSugerida());

        return new WebhookResponse(ia.respostaSugerida(), ia.intencao(), ia.categoria(), ia.alvo());
    }

    private MensagemEntity salvar(ColaboradorEntity colaborador, DirecaoMensagem direcao, String texto) {
        var m = new MensagemEntity();
        m.setColaborador(colaborador);
        m.setDirecao(direcao);
        m.setTexto(texto);
        return mensagemRepository.save(m);
    }
}