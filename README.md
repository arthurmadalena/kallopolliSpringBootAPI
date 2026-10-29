# Parceiro dos 90 dias — API

API central (Spring Boot) do **Parceiro dos 90 dias**, um assistente de IA que acompanha pelo WhatsApp os colaboradores novos da **Kallopolli** (franquia com restaurantes no Rio Grande do Sul) durante os 3 primeiros meses de contrato.

O assistente **complementa** o acompanhamento presencial da liderança: conversa com a pessoa no momento certo, tira dúvidas, percebe sinais de risco (cansaço, conflito, desmotivação, vontade de sair) e avisa o líder certo no mesmo dia.

> **Status:** protótipo em desenvolvimento (Entrega 2). A IA e o WhatsApp ainda funcionam por *stubs* (simuladores), descritos abaixo.

---

## Contexto

Na Kallopolli, muitas pessoas deixam o emprego nos primeiros meses, e boa parte está no primeiro emprego. Os principais problemas identificados:

- O momento certo da conversa nem sempre bate com a agenda do gerente.
- Falta um canal contínuo para quem acabou de chegar tirar dúvidas ou desabafar.
- O sinal de risco chega tarde ao líder.
- Muitas saídas acontecem sem aviso.

---

## Arquitetura

O **Spring Boot é o maestro**: recebe as mensagens, consulta a IA, aplica as regras de negócio e é o **único** a escrever no banco de dados. A IA é uma API externa que apenas devolve uma resposta categorizada em JSON.

```
WhatsApp (ou App Fake)
        |
        v
  API Spring Boot  ------>  API Python / FastAPI (IA)
        |                         |
        |  <---- JSON (intenção, categoria, alvo, resposta sugerida)
        |
        +--> aplica as regras de negócio
        +--> salva mensagens, relatos e alertas no PostgreSQL
        +--> responde ao colaborador pelo WhatsApp
```

| Bloco | Responsabilidade | Tecnologia |
|---|---|---|
| **API central** (este repositório) | Cadastros, jornada, mensagens, regras de fluxo, alertas, painel do RH | Java + Spring Boot |
| **IA** | Classificar a intenção e sugerir a resposta | Python + FastAPI |
| **Banco de dados** | Persistência | PostgreSQL |
| **WhatsApp** | Entrada e saída de mensagens | App Fake (simula a API oficial) |

### Contrato com a IA (provisório)

A IA recebe a mensagem do colaborador e devolve:

```json
{
  "intencao": "RECLAMACAO",
  "categoria": "CONFLITO",
  "alvo": "GESTOR",
  "respostaSugerida": "Sinto muito que isso tenha acontecido. Posso avisar alguém do RH?"
}
```

Os valores de `intencao`, `categoria` e `alvo` ainda serão fechados com a equipe de IA.

---

## O que já funciona

- **Loja** e **Colaborador**: cadastro e listagem, com hierarquia de gestor (`gestor_id`).
- **Jornada de 90 dias**: criada automaticamente para cada colaborador com papel `COLABORADOR`; o dia atual é calculado a partir da data de admissão.
- **Webhook de mensagens**: recebe a mensagem, identifica o colaborador pelo telefone, salva o histórico, consulta a IA (stub), registra a classificação e responde.
- **Tratamento de erros padronizado** (404 e 400 em formato `ProblemDetail`).
- **Migrations versionadas** com Flyway.

### Simulados por enquanto (stubs)

| Peça | Hoje | Depois |
|---|---|---|
| `ServicoIA` | Classifica por palavras-chave | Chamada HTTP à API FastAPI |
| `EnviadorMensagem` | Imprime no log | Envio ao App Fake / WhatsApp |

Ambos ficam atrás de interfaces, então a troca não afeta o resto do sistema.

---

## Tecnologias

- Java e Spring Boot (Web, Data JPA, Validation)
- PostgreSQL 16
- Flyway (migrations)
- Docker / Docker Compose (banco local)
- Maven

---

## Como rodar localmente

### Pré-requisitos

- JDK instalado (a versão do projeto está no `pom.xml`)
- Docker Desktop **aberto**
- Maven (ou o wrapper `mvnw` que já vem no projeto)

### 1. Subir o banco

Na raiz do projeto:

```bash
docker compose up -d
```

O `docker-compose.yml` sobe um PostgreSQL 16 na porta **5433** do seu computador (a 5432 costuma estar ocupada por um Postgres instalado localmente):

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: parceiro90
      POSTGRES_USER: parceiro
      POSTGRES_PASSWORD: parceiro
    ports:
      - "5433:5432"
```

### 2. Configuração

Em `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/parceiro90
    username: parceiro
    password: parceiro
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
```

> As credenciais acima são **apenas para desenvolvimento local**. Em qualquer ambiente compartilhado, use variáveis de ambiente (por exemplo `${DB_PASSWORD}`) e nunca envie senhas reais ao repositório.

### 3. Rodar a aplicação

```bash
# Linux / Mac
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

O Flyway cria as tabelas na primeira execução. A API sobe em `http://localhost:8080` (ou na porta definida em `server.port`).

---

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/lojas` | Cadastra uma loja |
| `GET` | `/api/lojas` | Lista as lojas |
| `POST` | `/api/colaboradores` | Cadastra um colaborador (cria a jornada se o papel for `COLABORADOR`) |
| `GET` | `/api/colaboradores` | Lista os colaboradores |
| `GET` | `/api/jornadas` | Lista as jornadas com o dia atual |
| `GET` | `/api/jornadas/colaborador/{id}` | Jornada de um colaborador |
| `POST` | `/api/webhook/whatsapp` | Recebe uma mensagem de colaborador e devolve a resposta |

### Exemplos

**Cadastrar loja**

```json
POST /api/lojas
{ "nome": "Loja Centro", "cidade": "Porto Alegre" }
```

**Cadastrar colaborador**

```json
POST /api/colaboradores
{
  "colaboradorName": "João Silva",
  "colaboradorTelefone": "51999990002",
  "papel": "COLABORADOR",
  "lojaId": 1,
  "gestorId": 1,
  "dataAdmissao": "2026-10-06"
}
```

**Simular uma mensagem (webhook)**

```json
POST /api/webhook/whatsapp
{ "telefone": "51999990002", "texto": "estou muito cansado hoje" }
```

---

## Estrutura do projeto

```
src/main/java/io/github/arthurmadalena/kallopolliapi
├── controllers/    endpoints REST
├── DTOs/           records de entrada e saída
├── entities/       entidades JPA e enums
├── repositories/   acesso ao banco (Spring Data)
├── service/        regras de negócio e fluxo de mensagens
├── ia/             contrato e stub da IA
├── whatsapp/       interface e stub de envio de mensagens
└── shared/         exceções e tratamento global de erros

src/main/resources
├── application.yml
└── db/migration/   migrations do Flyway (V1, V2, V3...)
```

---

## Banco de dados

| Tabela | Conteúdo |
|---|---|
| `loja` | Restaurantes |
| `colaborador` | Colaboradores, gestores e RH, com hierarquia |
| `jornada` | Acompanhamento de 90 dias de cada colaborador novo |
| `mensagem` | Histórico de conversas, com intenção, categoria e alvo |

Regra de ouro: **nunca edite uma migration já aplicada**; toda mudança vira uma nova versão (`V4__...`).

---

## Casos de uso previstos

- Colaborador tem uma dúvida: a IA responde com base no material de treinamento.
- Colaborador faz uma reclamação: o sistema coleta, categoriza e encaminha ao responsável.
- Relato sobre o próprio gestor: vai ao nível acima, **sem** que o gestor citado receba o aviso.
- Problema recorrente: o sistema identifica a repetição e alerta o responsável da área.
- Colaborador só quer conversar: a IA acolhe e identifica problemas relevantes.
- Sem resposta: depois de poucas tentativas, o sistema para de insistir.
- Observação do gestor: registrada à parte, nunca misturada com o relato do colaborador.

---

## Roadmap

- [x] Ambiente: Docker, PostgreSQL e Flyway
- [x] Loja e Colaborador
- [x] Jornada de 90 dias
- [x] Histórico de mensagens e webhook (com stubs)
- [ ] Cliente HTTP para a API Python (FastAPI)
- [ ] Integração com o App Fake do WhatsApp
- [ ] Relatos e roteamento (inclusive relato sobre o gestor)
- [ ] Confirmação do colaborador antes de notificar o líder
- [ ] Alertas ao líder, registro de atendimento e lembrete em 24h
- [ ] Cadência de mensagens ao longo dos 90 dias
- [ ] Pílulas de conteúdo de treinamento
- [ ] Painel mínimo do RH
- [ ] Segurança (autenticação e controle de acesso) e LGPD
- [ ] Documentação com Swagger e testes automatizados

---

## Privacidade (LGPD)

O sistema lida com relatos sensíveis de colaboradores. Antes de uso com pessoas reais, é obrigatório definir autenticação, controle de acesso por papel (o gestor nunca vê relatos sobre ele), auditoria de acessos e política de retenção de dados.


Projeto desenvolvido para a **Kallopolli**.
