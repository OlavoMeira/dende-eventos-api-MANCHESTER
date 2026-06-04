# 🎉 Dendê Eventos API — Time Manchester

API REST para gerenciamento de eventos, construída com **Spring Boot 3.2** e documentada com **Swagger (OpenAPI 3)**.

---

## 🚀 Como executar

### Pré-requisitos
- Java 17+
- Maven 3.8+

### Rodando localmente

```bash
git clone https://github.com/seu-usuario/dende-eventos-spring-api-manchester.git
cd dende-eventos-spring-api-manchester
mvn spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

### Swagger UI

Acesse a documentação interativa em:  
📄 `http://localhost:8080/swagger-ui.html`

### H2 Console (banco em memória)

Acesse o banco de dados em:  
🗄️ `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:mem:dende_eventos_db`

---

## 📋 Endpoints

### Eventos — `/api/v1/eventos`

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/eventos` | Listar todos (filtro por status opcional) |
| GET | `/api/v1/eventos/com-vagas` | Listar eventos com vagas |
| GET | `/api/v1/eventos/{id}` | Buscar por ID |
| POST | `/api/v1/eventos` | Criar novo evento |
| PUT | `/api/v1/eventos/{id}` | Atualizar evento |
| PATCH | `/api/v1/eventos/{id}/cancelar` | Cancelar evento |
| DELETE | `/api/v1/eventos/{id}` | Excluir evento |

### Participantes — `/api/v1`

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/eventos/{eventoId}/participantes` | Listar participantes do evento |
| GET | `/api/v1/participantes/{id}` | Buscar participante por ID |
| POST | `/api/v1/eventos/{eventoId}/participantes` | Inscrever participante |
| DELETE | `/api/v1/participantes/{id}` | Cancelar inscrição |

---

## 🏗️ Arquitetura

```
src/main/java/com/dende/eventos/
├── controller/         # Controllers REST + anotações Swagger
├── service/            # Regras de negócio
├── repository/         # Interfaces JPA
├── model/              # Entidades JPA (Evento, Participante, StatusEvento)
├── dto/                # Data Transfer Objects (Request / Response)
├── exception/          # Exceções customizadas + GlobalExceptionHandler
└── config/             # OpenAPI / Swagger config
```

---

## 🔧 Tecnologias

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| Java | 17 | Linguagem |
| Spring Boot | 3.2.5 | Framework principal |
| Spring Data JPA | 3.2.5 | Persistência |
| Hibernate | 6.x | ORM |
| H2 | latest | Banco em memória (dev) |
| PostgreSQL | latest | Banco de dados (produção) |
| Springdoc OpenAPI | 2.5.0 | Swagger UI |
| Lombok | latest | Redução de boilerplate |
| Bean Validation | 3.x | Validação de dados |

---

## 📌 Regras de Negócio

- Um evento não pode ter data de fim anterior à data de início
- Não é possível inscrever participantes em eventos **CANCELADOS**, **ENCERRADOS** ou **ESGOTADOS**
- CPF e e-mail são únicos por evento (sem duplicatas)
- Eventos com participantes não podem ser excluídos diretamente (devem ser cancelados primeiro)
- Quando o último participante cancela inscrição em evento **ESGOTADO**, o status volta para **ATIVO**

---

## 👥 Time Manchester

Projeto desenvolvido como entrega final do curso **Dendê Eventos**.
