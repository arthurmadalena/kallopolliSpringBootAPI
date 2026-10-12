CREATE TABLE loja (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL
);

CREATE TABLE colaborador (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(20) NOT NULL UNIQUE,
    papel VARCHAR(20) NOT NULL,
    loja_id BIGINT NOT NULL REFERENCES loja(id),
    gestor_id BIGINT REFERENCES colaborador(id),
    data_admissao DATE NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);