CREATE TABLE jornada (
                         id BIGSERIAL PRIMARY KEY,
                         colaborador_id BIGINT NOT NULL UNIQUE REFERENCES colaborador(id),
                         data_inicio DATE NOT NULL,
                         status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
                         tentativas_sem_resposta INT NOT NULL DEFAULT 0,
                         ultima_mensagem_em TIMESTAMP
);