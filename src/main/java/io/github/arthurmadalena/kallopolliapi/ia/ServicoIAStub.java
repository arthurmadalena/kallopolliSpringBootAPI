package io.github.arthurmadalena.kallopolliapi.ia;

import org.springframework.stereotype.Service;

@Service
public class ServicoIAStub implements ServicoIA {

    @Override
    public RespostaIA analisar(Long colaboradorId, String texto) {
        var t = texto.toLowerCase();

        if (t.contains("gerente") || t.contains("chefe") || t.contains("gestor")) {
            return new RespostaIA("RECLAMACAO", "CONFLITO", "GESTOR",
                    "Sinto muito que isso tenha acontecido. Posso avisar alguém do RH?");
        }
        if (t.contains("cansad") || t.contains("sobrecarreg") || t.contains("puxado")) {
            return new RespostaIA("CONVERSA", "CANSACO", "NENHUM",
                    "Dá para sentir que o dia foi pesado. Quer me contar mais?");
        }
        if (t.contains("?") || t.contains("dúvida") || t.contains("duvida") || t.contains("como")) {
            return new RespostaIA("DUVIDA", "GERAL", "NENHUM",
                    "Boa pergunta! Em breve vou responder com base no material de treinamento.");
        }
        return new RespostaIA("CONVERSA", "NENHUM", "NENHUM",
                "Entendi! Estou por aqui se precisar de qualquer coisa.");
    }
}