# Controle de Saldo de Contratos — API

API REST para gestão de contratos com controle de saldo. Cada contrato funciona como um orçamento pré-aprovado, válido por 1 ano. Cada lançamento consome parte desse orçamento, e o estorno devolve o valor ao saldo.

## Tecnologias

- Java 21
- Spring Boot 4.1.2 (Spring Web MVC, Spring Data JPA, Validation)
- PostgreSQL 16
- Flyway para migrations
- Lombok
- JUnit 5, Mockito e AssertJ para testes
- Docker Compose para o banco

## Pré-requisitos

- Java 21
- Docker e Docker Compose
- Maven (o projeto inclui o wrapper `./mvnw`)

## Como executar

### 1. Subir o banco

Crie o arquivo `.env` a partir do exemplo. O Docker Compose lê esse arquivo para configurar o PostgreSQL:

```bash
cp .env.example .env
docker compose up -d db
```

Aguarde o serviço `db` aparecer como `healthy`:

```bash
docker compose ps
```

### 2. Subir a API

Confira que o `.env` tem as mesmas credenciais configuradas em `src/main/resources/application.yaml`:

```
POSTGRES_DB=controle_saldo
POSTGRES_USER=controle
POSTGRES_PASSWORD=controle
```

Depois, execute a classe `ControleSaldoApiApplication` pela IDE, ou pelo terminal:

```bash
./mvnw spring-boot:run
```

A API fica disponível em `http://localhost:8080`. Na inicialização, o Flyway aplica as migrations `V1` e `V2` no banco `controle_saldo`.

## Testes

```bash
./mvnw verify
```

Os testes unitários (`*Test`) rodam sem banco. Os testes de controller e de contexto usam o banco `controle_saldo_test`, no mesmo PostgreSQL, com o perfil `test`. Antes de rodar, crie o banco:

```bash
docker exec -it controle-saldo-db psql -U controle -d controle_saldo -c "CREATE DATABASE controle_saldo_test;"
```

Os testes de integração (`*IT`) ficam desativados por padrão. Para executá-los, use:

```bash
./mvnw verify -DskipITs=false
```

## Endpoints

### Contratos

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/contratos` | Cadastra um contrato |
| `GET` | `/api/contratos` | Lista os contratos |
| `GET` | `/api/contratos/{id}` | Busca um contrato por id |

Corpo do `POST`:

```json
{
  "nome": "Contrato Exemplo",
  "valorContratado": 100000.00,
  "dataInicio": "2026-01-01"
}
```

A `dataFim` é calculada pelo sistema como `dataInicio + 1 ano`, e não é aceita na requisição.

### Lançamentos

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/lancamento/registrar` | Registra um lançamento vinculado a um contrato |
| `GET` | `/lancamento/listar/{contratoId}` | Lista os lançamentos de um contrato, ordenados por data |
| `PUT` | `/lancamento/editar/{id}` | Edita a data e o valor de um lançamento |
| `POST` | `/lancamento/estorno/{id}` | Estorna um lançamento |

Corpo do registro:

```json
{
  "contratoId": "uuid-do-contrato",
  "data": "2026-03-01",
  "valor": 15000.00
}
```

### Saldos

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/saldo` | Visão consolidada de todos os contratos |
| `GET` | `/saldo/contrato/{contratoId}` | Saldo de um contrato |

## Regras de negócio

- **Saldo disponível** = valor contratado − soma dos lançamentos com status `ATIVO`.
- **Registro:** o valor deve ser maior que zero e não pode ultrapassar o saldo disponível. Valor igual ao saldo é aceito.
- **Edição:** o saldo considera o valor antigo do próprio lançamento, que é devolvido antes da comparação. Lançamentos estornados não podem ser editados.
- **Estorno:** é lógico. O registro permanece no banco com status `ESTORNADO`, e a data e o motivo são gravados. Estornar um lançamento já estornado é recusado.
- **Vigência:** a data do lançamento não é validada contra a vigência do contrato. Esta é uma premissa do projeto, pois o enunciado não exige essa validação.

## Alertas de saldo

Os alertas são calculados por contrato, com base no percentual consumido do valor contratado e no percentual da vigência já decorrida:

| Alerta | Condição |
|---|---|
| `PROXIMO_DO_LIMITE` | Consumo igual ou acima de 80% |
| `RISCO_SUBUTILIZACAO` | Consumo abaixo de 20%, com 50% ou mais da vigência decorrida |
| `NENHUM` | Demais casos |

Os percentuais são constantes em `SaldoService`, para ajuste sem mudança na lógica. A data de referência vem de um `Clock` injetado, o que torna os testes determinísticos.

## Tratamento de erros

| Situação | Status |
|---|---|
| Contrato ou lançamento inexistente | `404` |
| Validação de entrada (ex.: valor zero) | `400` |
| Saldo insuficiente | `422` |
| Estorno duplicado ou edição de lançamento estornado | `409` |

## Decisões de projeto

- **Migrations versionadas** com Flyway, e não geração automática pelo Hibernate. Os `CHECK` do banco são uma segunda camada de proteção, além das validações do service.
- **Concorrência:** o registro e a edição travam a linha do contrato com `PESSIMISTIC_WRITE`, para duas requisições simultâneas não ultrapassarem o saldo.
- **Dinheiro** é `BigDecimal` no código e `NUMERIC(15, 2)` no banco.
- **Identificadores** são `UUID`, gerados pela aplicação.
- **Saldo derivado:** o saldo não é armazenado. É calculado a partir dos lançamentos ativos, para não haver divergência entre o valor guardado e os registros.

## Estrutura do projeto

```
src/main/java/com/example/controle_saldo_api/
├── contrato/        (entity, repository, service, controller, dto)
├── lancamento/      (entity, repository, service, controller, dto)
├── saldo/           (service, controller, dto)
└── shared/          (exceções de negócio e handler global)
```

## Fora do escopo

Conforme o enunciado, não há login nem controle de acesso. Em produção, a API precisaria de autenticação, o que fica como evolução.
