-- =============================================================
--  Dendê Eventos – Script de criação do banco de dados
--  Banco: PostgreSQL (produção) | H2 em memória (desenvolvimento)
--  Projeto: dende-eventos-spring-api-manchester
-- =============================================================

-- Para usar com PostgreSQL em produção,
-- configure application.properties com:
--   spring.datasource.url=jdbc:postgresql://localhost:5432/dende_eventos
--   spring.datasource.username=seu_usuario
--   spring.datasource.password=sua_senha
--   spring.jpa.hibernate.ddl-auto=validate   (após primeira subida com create)

-- Para desenvolvimento com H2 em memória, não é necessário rodar este script.
-- O Hibernate cria as tabelas automaticamente via ddl-auto=update.

-- =============================================================
-- BANCO DE DADOS
-- =============================================================
-- No PostgreSQL, crie o banco antes de rodar este script:
--   CREATE DATABASE dende_eventos;
-- Em seguida conecte: \c dende_eventos

-- =============================================================
-- TABELA: eventos
-- =============================================================
CREATE TABLE IF NOT EXISTS eventos (
    id                BIGSERIAL       PRIMARY KEY,
    nome              VARCHAR(100)    NOT NULL,
    descricao         VARCHAR(500),
    data_inicio       TIMESTAMP       NOT NULL,
    data_fim          TIMESTAMP       NOT NULL,
    local             VARCHAR(255)    NOT NULL,
    capacidade_maxima INTEGER         NOT NULL,
    status            VARCHAR(20)     NOT NULL DEFAULT 'ATIVO',  -- ATIVO | CANCELADO | ENCERRADO | ESGOTADO
    criado_em         TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em     TIMESTAMP,

    CONSTRAINT chk_evento_datas
        CHECK (data_fim > data_inicio),
    CONSTRAINT chk_evento_duracao_minima
        CHECK (EXTRACT(EPOCH FROM (data_fim - data_inicio)) / 60 >= 30),
    CONSTRAINT chk_evento_capacidade
        CHECK (capacidade_maxima >= 1),
    CONSTRAINT chk_evento_status
        CHECK (status IN ('ATIVO', 'CANCELADO', 'ENCERRADO', 'ESGOTADO'))
);

-- =============================================================
-- TABELA: participantes
-- =============================================================
CREATE TABLE IF NOT EXISTS participantes (
    id              BIGSERIAL       PRIMARY KEY,
    nome            VARCHAR(100)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    cpf             CHAR(11)        NOT NULL,
    data_inscricao  TIMESTAMP       NOT NULL DEFAULT NOW(),
    evento_id       BIGINT          NOT NULL,

    CONSTRAINT fk_participante_evento
        FOREIGN KEY (evento_id) REFERENCES eventos (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    -- Evita duplicatas de email e CPF por evento
    CONSTRAINT uq_participante_email_evento
        UNIQUE (email, evento_id),
    CONSTRAINT uq_participante_cpf_evento
        UNIQUE (cpf, evento_id)
);

-- =============================================================
-- ÍNDICES para performance
-- =============================================================

-- Listagem de eventos por status (ex: ATIVO, ESGOTADO)
CREATE INDEX IF NOT EXISTS idx_eventos_status
    ON eventos (status);

-- Feed de eventos por data de início (eventos futuros)
CREATE INDEX IF NOT EXISTS idx_eventos_data_inicio
    ON eventos (data_inicio);

-- Busca de participantes por evento
CREATE INDEX IF NOT EXISTS idx_participantes_evento_id
    ON participantes (evento_id);

-- Busca de participante por email (verificação de duplicata)
CREATE INDEX IF NOT EXISTS idx_participantes_email
    ON participantes (email);

-- =============================================================
-- DADOS DE EXEMPLO (opcional — remova em produção)
-- =============================================================

INSERT INTO eventos (nome, descricao, data_inicio, data_fim, local, capacidade_maxima, status, criado_em, atualizado_em)
VALUES
    ('Show do Time Manchester',
     'Grande show de encerramento da temporada do time Manchester.',
     NOW() + INTERVAL '7 days',
     NOW() + INTERVAL '7 days' + INTERVAL '4 hours',
     'Arena Manchester – Rua das Flores, 100, São Paulo/SP',
     500,
     'ATIVO',
     NOW(), NOW()),

    ('Workshop Spring Boot na Prática',
     'Workshop intensivo de Spring Boot com JPA, Swagger e boas práticas.',
     NOW() + INTERVAL '14 days',
     NOW() + INTERVAL '14 days' + INTERVAL '8 hours',
     'Online – Plataforma Dendê',
     100,
     'ATIVO',
     NOW(), NOW()),

    ('Hackathon Dendê 2025',
     'Maratona de programação de 24h para equipes de até 5 pessoas.',
     NOW() + INTERVAL '30 days',
     NOW() + INTERVAL '31 days',
     'Av. Paulista, 1000 – São Paulo/SP',
     200,
     'ATIVO',
     NOW(), NOW());
