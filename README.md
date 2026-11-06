# Kallopolli API --- Parceiro dos 90 Dias

API central do projeto **Parceiro dos 90 Dias**, responsável por
gerenciar colaboradores, acompanhar a jornada de integração, processar
mensagens, registrar relatos, organizar alertas, conteúdos de
treinamento, cursos, mentorias e pontuação.

A aplicação foi desenvolvida em **Java 17 com Spring Boot** e utiliza
**PostgreSQL** como banco de dados. O banco pode ser executado
localmente com Docker ou hospedado no Supabase.

> **Papel desta API:** o Java concentra as regras de negócio e a
> persistência. O serviço Python/FastAPI pode realizar a classificação
> das mensagens com IA e enviar o resultado para o endpoint de
> integração do Java.

------------------------------------------------------------------------

## Índice

-   [Arquitetura](#arquitetura)
-   [Funcionalidades](#funcionalidades)
-   [Tecnologias](#tecnologias)
-   [Estrutura do projeto](#estrutura-do-projeto)
-   [Configuração do ambiente](#configuração-do-ambiente)
-   [Executar localmente](#executar-localmente)
-   [Integração com Python/FastAPI](#integração-com-pythonfastapi)
-   [Endpoints disponíveis](#endpoints-disponíveis)
-   [Fluxo de processamento de
    mensagens](#fluxo-de-processamento-de-mensagens)
-   [Banco de dados e migrations](#banco-de-dados-e-migrations)
-   [Agendamentos automáticos](#agendamentos-automáticos)
-   [Limitações e pontos de atenção](#limitações-e-pontos-de-atenção)
-   [Segurança](#segurança)

------------------------------------------------------------------------

## Arquitetura

``` text
Colaborador / WhatsApp / sistema de entrada
                    |
                    v
       Serviço Python / FastAPI (IA)
       - recebe ou analisa a mensagem
       - classifica intenção e categoria
       - sugere uma resposta
                    |
                    | POST /api/mensagens/processadas
                    | Header: X-API-Key
                    v
          API Java / Spring Boot
          - valida a chave de integração
          - identifica o colaborador pelo telefone
          - aplica regras de negócio
          - registra mensagens e eventos
          - consulta conteúdos e jornadas
          - cria relatos e alertas quando necessário
                    |
                    v
             PostgreSQL / Supabase
```

O serviço Python não precisa acessar diretamente o banco de dados. A
comunicação prevista é feita pela API Java. Assim, a aplicação Java
permanece responsável pelas regras de negócio e pela gravação dos dados.

------------------------------------------------------------------------

## Funcionalidades

### 1. Lojas e colaboradores

-   Cadastro e listagem de lojas.
-   Cadastro e listagem de colaboradores.
-   Associação do colaborador a uma loja.
-   Associação hierárquica de um colaborador a um gestor.
-   Registro de papel, função, data de admissão e disponibilidade para
    atuar como mentor.
-   Normalização do telefone para comparação e localização do
    colaborador.
-   Verificação de telefone duplicado no cadastro.
-   Criação automática da jornada de 90 dias para colaboradores com
    papel `COLABORADOR`.

### 2. Jornada de 90 dias

-   Criação da jornada para o colaborador elegível.
-   Consulta de todas as jornadas.
-   Consulta da jornada de um colaborador.
-   Cálculo do dia atual a partir da data de admissão.
-   Registro de interação/resposta do colaborador.
-   Processamento de cadência de acompanhamento por tarefa agendada.

A cadência e as mensagens previstas são controladas pela lógica de
`JornadaService`. O envio efetivo depende do componente de envio
configurado, que atualmente é um simulador (stub).

### 3. Processamento de mensagens e integração com IA

A API possui dois caminhos:

-   `POST /api/webhook/whatsapp`: caminho de simulação local. Recebe
    telefone e texto e usa o `ServicoIAStub`, que classifica por regras
    simples de palavras-chave.
-   `POST /api/mensagens/processadas`: caminho de integração. Recebe do
    Python a mensagem já classificada e aplica o fluxo de negócio do
    Java.

O fluxo principal:

1.  Localiza o colaborador pelo telefone.
2.  Verifica se o colaborador está ativo.
3.  Registra a interação na jornada.
4.  Salva a mensagem recebida no histórico.
5.  Registra pontuação de interação quando as regras permitem.
6.  Aplica a regra correspondente à intenção.
7.  Salva a resposta gerada no histórico.
8.  Aciona o componente de envio de mensagem.
9.  Devolve um JSON com a resposta e os dados de classificação.

### 4. Dúvidas e conteúdos de treinamento

-   Cadastro de conteúdos com título, categoria e texto.
-   Listagem de conteúdos ativos.
-   Busca por categoria.
-   Desativação de conteúdo.
-   Quando a intenção recebida é `DUVIDA`, o Java busca conteúdos da
    categoria informada.
-   Se não encontrar conteúdo naquela categoria, tenta usar os conteúdos
    ativos.
-   A resposta inclui até três conteúdos encontrados.

### 5. Relatos e alertas

-   Quando a intenção é `RECLAMACAO`/`RECLAMAÇÃO`, o Java cria um relato
    com status `AGUARDANDO_CONFIRMACAO`.
-   O colaborador precisa confirmar com `SIM` ou cancelar com `NÃO`.
-   Depois da confirmação, o relato é encaminhado ao responsável
    definido pela hierarquia ou a um administrador ativo da loja,
    conforme as regras do serviço.
-   O sistema evita encaminhar ao próprio gestor citado no relato.
-   É criado um alerta para o responsável.
-   O alerta possui prazo de lembrete de 24 horas.
-   O responsável pode consultar alertas pendentes e registrar o
    atendimento.
-   O painel do RH disponibiliza contagens de relatos e alertas por
    status.

### 6. Cursos, mentorias e gamificação

-   Cadastro e listagem de cursos.
-   Registro de conclusão de curso.
-   Lançamento de pontos associados à conclusão.
-   Sugestão de mentores disponíveis.
-   Criação e listagem de mentorias.
-   Atualização do status de uma mentoria.
-   Registro de pontos por interação com IA, respeitando as regras
    implementadas.
-   Consulta de pontuação individual.
-   Ranking por semana, mês ou total, com filtro opcional por loja.

------------------------------------------------------------------------

## Tecnologias

-   Java 17
-   Spring Boot 4.1.1
-   Spring Web MVC
-   Spring Data JPA / Hibernate
-   Bean Validation
-   PostgreSQL
-   Supabase (PostgreSQL hospedado, quando configurado)
-   Flyway (versionamento e execução de migrations)
-   Maven
-   Docker Compose (opção de banco local)

------------------------------------------------------------------------

## Estrutura do projeto

``` text
src/main/java/io/github/arthurmadalena/kallopolliapi/
├── controllers/    # Endpoints REST
├── DTOs/           # Objetos de entrada e saída da API
├── entities/       # Entidades JPA e enums
├── repositories/   # Acesso ao banco via Spring Data
├── service/        # Regras de negócio e fluxos
├── ia/             # Interface e implementação simulada da IA
├── whatsapp/       # Interface e implementação simulada de envio
└── shared/         # Exceções e tratamento compartilhado

src/main/resources/
├── application.yml
├── application.properties
└── db/migration/   # Scripts SQL versionados do Flyway
```

------------------------------------------------------------------------

## Configuração do ambiente

### Pré-requisitos

-   JDK 17.
-   Maven (ou utilizar o Maven Wrapper incluído no projeto).
-   Acesso a um PostgreSQL: Supabase ou banco local via Docker.
-   IntelliJ IDEA ou outra IDE Java (opcional).

### Variáveis de ambiente

Configure as variáveis no ambiente em que a aplicação Java será
executada:

  -----------------------------------------------------------------------
  Variável                            Finalidade
  ----------------------------------- -----------------------------------
  `PORT`                              Porta HTTP da aplicação. Padrão:
                                      `8080`.

  `SPRING_DATASOURCE_URL`             URL JDBC do PostgreSQL.

  `SPRING_DATASOURCE_USERNAME`        Usuário do banco.

  `SPRING_DATASOURCE_PASSWORD`        Senha do banco.

  `INTEGRACAO_API_KEY`                Chave compartilhada entre Python e
                                      Java.

  `APP_SCHEDULER_ENABLED`             Está declarada na configuração
                                      atual, mas ainda não controla os
                                      métodos agendados no código.
  -----------------------------------------------------------------------

Exemplo de URL JDBC para Supabase Session Pooler (confirme host, porta e
usuário na área **Connect** do seu projeto Supabase):

``` text
jdbc:postgresql://SEU_HOST_POOLER:5432/postgres?sslmode=require
```

O usuário do pooler pode ter formato semelhante a
`postgres.<project-ref>`, conforme os dados apresentados pelo Supabase.

### Exemplo de `application.yml`

Use placeholders para que credenciais não fiquem gravadas no
repositório:

``` yaml
server:
  port: ${PORT:8080}

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  integracao:
    api-key: ${INTEGRACAO_API_KEY}

  scheduler:
    enabled: ${APP_SCHEDULER_ENABLED:false}
```

**Importante:** o `application.yml` presente no ZIP usado para esta
documentação contém uma credencial de banco escrita diretamente no
arquivo. Antes de compartilhar ou publicar o projeto, substitua essa
configuração pelo exemplo acima, configure as variáveis de ambiente e
troque a senha que foi exposta. Não versione senhas, tokens ou chaves
reais.

O Spring Boot não carrega automaticamente um arquivo `.env` comum sem
configuração adicional. As variáveis precisam estar disponíveis no
sistema, na configuração de execução da IDE ou no ambiente de
hospedagem.

------------------------------------------------------------------------

## Executar localmente

### Opção A --- Usar Supabase

1.  Configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e
    `SPRING_DATASOURCE_PASSWORD`.
2.  Configure `INTEGRACAO_API_KEY`.
3.  Confirme que a senha e os dados do pooler estão corretos no painel
    do Supabase.
4.  Execute a aplicação.

O Flyway está habilitado e executa as migrations encontradas em
`src/main/resources/db/migration` durante a inicialização. O Hibernate
está configurado com `ddl-auto: validate`, portanto valida o mapeamento
das entidades em vez de criar ou alterar tabelas automaticamente.

### Opção B --- Usar PostgreSQL local com Docker

O arquivo `docker-compose.yml` configura PostgreSQL 16 com:

-   Banco: `parceiro90`
-   Usuário: `parceiro`
-   Senha local de desenvolvimento: `parceiro`
-   Porta do computador: `5433` (mapeada para a porta `5432` do
    container)

Inicie o banco na raiz do projeto:

``` bash
docker compose up -d
```

Configure as variáveis para esse banco local:

``` text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/parceiro90
SPRING_DATASOURCE_USERNAME=parceiro
SPRING_DATASOURCE_PASSWORD=parceiro
INTEGRACAO_API_KEY=uma-chave-local-compartilhada-com-o-python
```

Essas credenciais são apenas para desenvolvimento local. Não as utilize
em produção.

### Iniciar a API

Windows:

``` cmd
mvnw.cmd spring-boot:run
```

Linux/macOS:

``` bash
./mvnw spring-boot:run
```

Ou execute a classe principal `KallopolliapiApplication` pela IDE.

Por padrão, a API fica disponível em:

``` text
http://localhost:8080
```

Se a porta 8080 estiver ocupada, encerre a aplicação/processo que a
utiliza ou defina outra porta pela variável `PORT`.

------------------------------------------------------------------------

## Integração com Python/FastAPI

### Endpoint de integração

``` http
POST /api/mensagens/processadas
```

Endereço local completo:

``` text
http://localhost:8080/api/mensagens/processadas
```

Em ambiente hospedado, substitua `localhost:8080` pela URL pública da
API Java.

### Cabeçalhos

``` http
Content-Type: application/json
X-API-Key: SUA_CHAVE_DE_INTEGRACAO
```

O valor de `X-API-Key` deve ser igual ao valor de `INTEGRACAO_API_KEY`
configurado na aplicação Java.

### Corpo esperado

O DTO `MensagemProcessadaRequest` define os seguintes campos:

  ----------------------------------------------------------------------------
  Campo                Tipo                       Obrigatório Descrição
  -------------------- ---------------- --------------------- ----------------
  `telefone`           string                             Sim Telefone
                                                              cadastrado do
                                                              colaborador.

  `texto`              string                             Sim Texto original
                                                              da mensagem
                                                              recebida.

  `intencao`           string                             Sim Intenção
                                                              classificada
                                                              pela IA.

  `categoria`          string ou `null`                   Não Categoria
                                                              identificada.

  `alvo`               string ou `null`                   Não Alvo relacionado
                                                              à mensagem,
                                                              quando
                                                              aplicável.

  `respostaSugerida`   string ou `null`                   Não Sugestão de
                                                              resposta
                                                              produzida pela
                                                              IA.
  ----------------------------------------------------------------------------

Exemplo:

``` json
{
  "telefone": "5551999999999",
  "texto": "Estou com dúvidas sobre meu treinamento inicial.",
  "intencao": "DUVIDA",
  "categoria": "TREINAMENTO",
  "alvo": null,
  "respostaSugerida": "Vou te ajudar com as informações sobre o treinamento!"
}
```

Os campos `telefone`, `texto` e `intencao` não podem ser vazios. O
telefone é normalizado no Java para comparação, removendo caracteres que
não sejam números. Ele precisa corresponder a um colaborador cadastrado
e ativo no banco.

### Exemplo em Python usando `requests`

``` python
import os
import requests

API_JAVA_URL = os.getenv(
    "API_JAVA_URL",
    "http://localhost:8080"
)

API_KEY = os.environ["INTEGRACAO_API_KEY"]

payload = {
    "telefone": "5551999999999",
    "texto": "Estou com dúvidas sobre meu treinamento inicial.",
    "intencao": "DUVIDA",
    "categoria": "TREINAMENTO",
    "alvo": None,
    "respostaSugerida": "Vou te ajudar com as informações sobre o treinamento!"
}

response = requests.post(
    f"{API_JAVA_URL}/api/mensagens/processadas",
    json=payload,
    headers={
        "X-API-Key": API_KEY
    },
    timeout=20
)

response.raise_for_status()
print(response.json())
```

No ambiente Python, configure `API_JAVA_URL` com o endereço base da API
Java e `INTEGRACAO_API_KEY` com a mesma chave configurada no Java.

### Exemplo de resposta

A API retorna um objeto `WebhookResponse`, com os dados da resposta e da
classificação processada. Exemplo ilustrativo:

``` json
{
  "resposta": "Encontrei estas informações nos materiais de treinamento...",
  "intencao": "DUVIDA",
  "categoria": "TREINAMENTO",
  "alvo": null
}
```

### Possíveis resultados HTTP

-   `200 OK`: mensagem processada.
-   `400 Bad Request`: corpo inválido, campos obrigatórios ausentes ou
    regra de negócio não atendida.
-   `401 Unauthorized`: `X-API-Key` ausente ou inválida.
-   `404 Not Found`: colaborador não encontrado, entre outros recursos
    não encontrados.

> **Atenção:** o Python e a API Java executados em computadores
> diferentes não podem usar `localhost` um do outro. Para comunicação
> entre máquinas, a API Java precisa estar hospedada ou exposta em um
> endereço de rede acessível. Não exponha o banco diretamente ao serviço
> Python; mantenha o acesso ao banco no Java.

------------------------------------------------------------------------

## Endpoints disponíveis

As rotas abaixo foram identificadas nos controllers do projeto.

  -------------------------------------------------------------------------------------------------
  Método                  Rota                                            Descrição
  ----------------------- ----------------------------------------------- -------------------------
  `POST`                  `/api/lojas`                                    Cadastra uma loja.

  `GET`                   `/api/lojas`                                    Lista lojas.

  `POST`                  `/api/colaboradores`                            Cadastra colaborador.

  `GET`                   `/api/colaboradores`                            Lista colaboradores.

  `GET`                   `/api/colaboradores/{id}/mensagens`             Consulta histórico de
                                                                          mensagens do colaborador.

  `GET`                   `/api/jornadas`                                 Lista jornadas.

  `GET`                   `/api/jornadas/colaborador/{colaboradorId}`     Consulta jornada de um
                                                                          colaborador.

  `POST`                  `/api/webhook/whatsapp`                         Simula entrada de
                                                                          mensagem e classificação
                                                                          local pelo stub de IA.

  `POST`                  `/api/mensagens/processadas`                    Recebe mensagem
                                                                          classificada pelo Python;
                                                                          exige `X-API-Key`.

  `POST`                  `/api/conteudos`                                Cadastra conteúdo de
                                                                          treinamento.

  `GET`                   `/api/conteudos`                                Lista conteúdos ativos.

  `GET`                   `/api/conteudos/categoria/{categoria}`          Busca conteúdos ativos
                                                                          por categoria.

  `PATCH`                 `/api/conteudos/{id}/desativar`                 Desativa um conteúdo.

  `POST`                  `/api/cursos`                                   Cadastra curso.

  `GET`                   `/api/cursos`                                   Lista cursos.

  `POST`                  `/api/cursos/{id}/conclusao`                    Registra conclusão de
                                                                          curso para colaborador.

  `GET`                   `/api/ranking`                                  Consulta ranking; aceita
                                                                          `periodo` e `lojaId` como
                                                                          parâmetros.

  `GET`                   `/api/colaboradores/{id}/pontos`                Consulta pontos e posição
                                                                          do colaborador.

  `GET`                   `/api/rh/mentorias/sugestoes/{colaboradorId}`   Sugere mentores para o
                                                                          colaborador.

  `POST`                  `/api/rh/mentorias`                             Cria mentoria.

  `PATCH`                 `/api/rh/mentorias/{id}/status`                 Atualiza status da
                                                                          mentoria.

  `GET`                   `/api/rh/mentorias`                             Lista mentorias.

  `GET`                   `/api/rh/dashboard`                             Retorna indicadores de
                                                                          relatos e alertas para o
                                                                          painel do RH.

  `GET`                   `/api/lideranca/alertas`                        Lista alertas pendentes
                                                                          do responsável; exige
                                                                          `X-Lideranca-Telefone`.

  `POST`                  `/api/lideranca/alertas/{id}/atender`           Registra atendimento;
                                                                          exige
                                                                          `X-Lideranca-Telefone`.
  -------------------------------------------------------------------------------------------------

### Parâmetros do ranking

Exemplo:

``` http
GET /api/ranking?periodo=SEMANA&lojaId=1
```

Valores de período disponíveis no código:

-   `SEMANA`
-   `MES`
-   `TOTAL`

Se `periodo` não for informado, o padrão é `SEMANA`. `lojaId` é
opcional.

### Exemplos de corpos adicionais

Criar loja:

``` json
{
  "lojaName": "Loja Centro",
  "lojaCity": "Porto Alegre"
}
```

Cadastrar colaborador:

``` json
{
  "colaboradorName": "João Silva",
  "colaboradorTelefone": "5551999999999",
  "papel": "COLABORADOR",
  "lojaId": 1,
  "gestorId": 2,
  "dataAdmissao": "2026-10-06",
  "funcao": "Atendente",
  "mentorDisponivel": false
}
```

Cadastrar conteúdo:

``` json
{
  "titulo": "Primeiros passos",
  "categoria": "TREINAMENTO",
  "texto": "Orientações iniciais para o colaborador."
}
```

Cadastrar curso:

``` json
{
  "titulo": "Integração institucional",
  "pontos": 10
}
```

Registrar conclusão de curso:

``` json
{
  "colaboradorId": 1
}
```

Criar mentoria:

``` json
{
  "mentorId": 2,
  "mentoradoId": 1
}
```

Atender alerta:

``` json
{
  "retorno": "Conversei com o colaborador e registrei as orientações."
}
```

------------------------------------------------------------------------

## Fluxo de processamento de mensagens

### Intenção `DUVIDA`

O Java procura conteúdos ativos pela categoria recebida. Se não houver
resultados específicos, tenta listar conteúdos ativos em geral. A
resposta utiliza no máximo três conteúdos. Se não houver materiais
cadastrados, retorna uma mensagem informando que não encontrou material
específico.

### Intenção `RECLAMACAO` ou `RECLAMAÇÃO`

O Java abre um relato pendente e solicita confirmação do colaborador.
Enquanto houver relato aguardando confirmação:

-   `SIM` confirma o relato e inicia o encaminhamento.
-   `NÃO` cancela o relato.
-   Outra mensagem mantém o pedido de confirmação.

Após a confirmação, o sistema seleciona o responsável de acordo com a
hierarquia e as regras de segurança do `RelatoService`, registra o
alerta e define prazo de lembrete de 24 horas.

### Outras intenções

Para intenções diferentes das regras especiais acima, o Java utiliza
`respostaSugerida` se ela estiver preenchida. Caso contrário, utiliza
uma resposta padrão.

### Pontuação

O serviço de pontuação registra pontos de interação de acordo com as
regras do projeto. A pontuação por resposta de IA possui limite de uma
concessão por colaborador por dia e exige texto com pelo menos cinco
caracteres. A conclusão de curso também gera pontuação conforme a
configuração do curso.

------------------------------------------------------------------------

## Banco de dados e migrations

O projeto utiliza Flyway para versionar a estrutura do banco. Os scripts
existentes no repositório são:

  -------------------------------------------------------------------------
  Migration                             Responsabilidade
  ------------------------------------- -----------------------------------
  `V1__criando_tabela_base.sql`         Estrutura inicial de lojas e
                                        colaboradores.

  `V2__criar_tabela_jornada.sql`        Tabela de jornada.

  `V3__criar_tabela_mensagem.sql`       Histórico de mensagens.

  `V4__mentoria_e_gameficacao.sql`      Mentorias, cursos, conclusões e
                                        eventos de pontuação; também
                                        adiciona campos ao colaborador.

  `V5__relatos_alertas_conteudos.sql`   Relatos, alertas, conteúdos e
                                        índices.

  `V6__controle_cadencia_jornada.sql`   Campos adicionais para controle da
                                        cadência da jornada.
  -------------------------------------------------------------------------

A configuração atual utiliza:

``` yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

Isso significa que o Flyway executa as migrations e o Hibernate valida
se o mapeamento das entidades é compatível com o banco.

**Regra importante:** não altere migrations que já foram aplicadas em um
banco compartilhado. Para mudanças futuras, crie uma nova migration com
o próximo número disponível.

------------------------------------------------------------------------

## Agendamentos automáticos

A aplicação habilita o agendamento com `@EnableScheduling`.

O `LembreteScheduler` possui duas tarefas:

-   Verificação de alertas pendentes cujo prazo de lembrete venceu, a
    cada 60 segundos.
-   Processamento da cadência das jornadas, a cada 60 minutos.

A configuração `APP_SCHEDULER_ENABLED` aparece no `application.yml`, mas
**na versão atual do código ela não está conectada a uma condição que
habilite/desabilite o `LembreteScheduler`**. Portanto, definir essa
variável como `false` não desliga as tarefas agendadas. Se houver mais
de uma instância da API em produção, é necessário tratar a execução
duplicada antes de escalar.

------------------------------------------------------------------------

## Limitações e pontos de atenção

O código deste repositório contém componentes simulados e integrações
que ainda precisam ser conectadas:

1.  **IA:** `ServicoIAStub` classifica mensagens localmente por
    palavras-chave. O endpoint `/api/mensagens/processadas` já recebe a
    classificação externa do Python, mas o Java não faz uma chamada HTTP
    de volta ao Python.
2.  **WhatsApp:** `EnviadorMensagemStub` apenas registra no log uma
    mensagem com a marca `[WHATSAPP FAKE]`. Não envia mensagens reais
    pelo WhatsApp.
3.  **Autenticação e autorização:** a rota de integração de mensagens
    valida `X-API-Key`. As demais rotas listadas não apresentam
    autenticação/autorização geral configurada nos controllers
    analisados. Não exponha a API publicamente sem implementar a
    proteção adequada.
4.  **Agendamentos:** a variável `APP_SCHEDULER_ENABLED` está declarada,
    mas não controla o agendador no código atual.
5.  **Documentação interativa:** não foi identificada configuração de
    Swagger/OpenAPI no `pom.xml`.
6.  **Testes:** execute os testes Maven disponíveis antes de publicar
    alterações; esta documentação não representa uma certificação de que
    todos os cenários foram testados.

------------------------------------------------------------------------

## Segurança

-   Nunca envie `application.yml` com senha real do banco para GitHub,
    ZIP público ou grupo de mensagens.
-   Utilize variáveis de ambiente no computador de cada desenvolvedor e
    no serviço de hospedagem.
-   A chave `INTEGRACAO_API_KEY` deve ser secreta e igual nos ambientes
    Java e Python que se comunicam.
-   Não compartilhe a senha do Supabase com a equipe Python: o Python
    deve chamar o endpoint Java.
-   Como a senha do banco apareceu no arquivo de configuração
    compartilhado, **troque-a no Supabase antes de compartilhar ou
    publicar o repositório**.
-   Em produção, configure HTTPS e implemente autenticação/autorização
    para os endpoints administrativos, de RH, liderança e cadastros.
-   Não registre senhas, tokens ou dados pessoais desnecessários nos
    logs.

------------------------------------------------------------------------

## Resumo da integração Python → Java

Para o serviço Python enviar uma mensagem classificada:

-   Método: `POST`
-   Rota: `/api/mensagens/processadas`
-   Header: `Content-Type: application/json`
-   Header de autenticação: `X-API-Key`
-   Campos obrigatórios: `telefone`, `texto`, `intencao`
-   Campos opcionais: `categoria`, `alvo`, `respostaSugerida`

Localmente:

``` text
http://localhost:8080/api/mensagens/processadas
```

Em produção, use a URL pública da API Java, mantendo a mesma rota.

------------------------------------------------------------------------

**Projeto:** Kallopolli --- Parceiro dos 90 Dias\
**Aplicação:** API REST Java / Spring Boot\
**Banco:** PostgreSQL / Supabase\
**Integração de classificação:** Python / FastAPI
