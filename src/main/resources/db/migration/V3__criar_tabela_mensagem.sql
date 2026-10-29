CREATE TABLE mensagem (
                          id BIGSERIAL PRIMARY KEY,
                          colaborador_id BIGINT NOT NULL REFERENCES colaborador(id),
                          direcao VARCHAR(10) NOT NULL,
                          texto TEXT NOT NULL,
                          intencao VARCHAR(30),
                          categoria VARCHAR(30),
                          alvo VARCHAR(30),
                          criada_em TIMESTAMP NOT NULL DEFAULT NOW()
);