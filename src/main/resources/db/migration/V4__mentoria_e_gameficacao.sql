ALTER TABLE colaborador ADD COLUMN funcao VARCHAR(50);
ALTER TABLE colaborador ADD COLUMN mentor_disponivel BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE mentoria (
    id BIGSERIAL PRIMARY KEY,
    mentor_id BIGINT NOT NULL REFERENCES colaborador(id),
    mentorado_id BIGINT NOT NULL REFERENCES colaborador(id),
    status VARCHAR(20) NOT NULL DEFAULT 'SUGERIDA',
    criada_em TIMESTAMP NOT NULL DEFAULT NOW(),
    decidida_em TIMESTAMP
);

CREATE TABLE curso (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    pontos INT NOT NULL DEFAULT 10
);

CREATE TABLE conclusao_curso (
    id BIGSERIAL PRIMARY KEY,
    colaborador_id BIGINT NOT NULL REFERENCES colaborador(id),
    curso_id BIGINT NOT NULL REFERENCES curso(id),
    concluido_em TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (colaborador_id, curso_id)
);

CREATE TABLE pontuacao_evento (
    id BIGSERIAL PRIMARY KEY,
    colaborador_id BIGINT NOT NULL REFERENCES colaborador(id),
    tipo VARCHAR(30) NOT NULL,
    pontos INT NOT NULL,
    referencia VARCHAR(100),
    criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pontuacao_colaborador ON pontuacao_evento (colaborador_id, criado_em);