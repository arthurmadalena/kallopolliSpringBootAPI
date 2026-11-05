-- Relatos feitos pelos colaboradores
CREATE TABLE relato (
                        id BIGSERIAL PRIMARY KEY,
                        colaborador_id BIGINT NOT NULL REFERENCES colaborador(id),
                        gestor_citado_id BIGINT REFERENCES colaborador(id),
                        destinatario_id BIGINT REFERENCES colaborador(id),
                        descricao TEXT NOT NULL,
                        categoria VARCHAR(100),
                        status VARCHAR(40) NOT NULL DEFAULT 'AGUARDANDO_CONFIRMACAO',
                        criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
                        confirmado_em TIMESTAMP
);

-- Alertas encaminhados aos responsáveis
CREATE TABLE alerta (
                        id BIGSERIAL PRIMARY KEY,
                        relato_id BIGINT NOT NULL UNIQUE REFERENCES relato(id),
                        destinatario_id BIGINT NOT NULL REFERENCES colaborador(id),
                        status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
                        criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
                        prazo_lembrete_em TIMESTAMP NOT NULL,
                        lembrete_enviado BOOLEAN NOT NULL DEFAULT FALSE,
                        atendido_em TIMESTAMP,
                        retorno TEXT
);

-- Conteúdos utilizados para responder dúvidas
CREATE TABLE conteudo (
                          id BIGSERIAL PRIMARY KEY,
                          titulo VARCHAR(150) NOT NULL,
                          categoria VARCHAR(100) NOT NULL,
                          texto TEXT NOT NULL,
                          ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_relato_colaborador
    ON relato(colaborador_id);

CREATE INDEX idx_alerta_destinatario_status
    ON alerta(destinatario_id, status);

CREATE INDEX idx_alerta_lembrete
    ON alerta(status, prazo_lembrete_em);