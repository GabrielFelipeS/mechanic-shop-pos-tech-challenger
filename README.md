# Mechanic Shop API - Gestão de Oficina Mecânica

API para gestão de ordens de serviço, controle de estoque de peças e automação de orçamentos de uma oficina mecânica. 

Este projeto foi desenvolvido com foco em regras de negócio, utilizando **Gerenciamento de Estados** para o ciclo de vida das Ordens de Serviço e processamento assíncrono para envio de notificações por e-mail.

---

## 💻 Tecnologias Utilizadas

A aplicação foi construída utilizando as seguintes tecnologias e ferramentas:

* **[Java 21](https://dev.java/)**: Linguagem principal utilizada no desenvolvimento do backend.
* **[Spring Boot](https://spring.io/projects/spring-boot)**: Framework principal para criação da API REST, facilitando a configuração e o deploy.
* **[Spring Security](https://spring.io/projects/spring-security) & [JWT (Auth0)](https://jwt.io/)**: Implementação de segurança, autenticação e controle de acesso baseado em roles (RBAC).
* **[PostgreSQL](https://www.postgresql.org/)**: Banco de dados relacional escolhido para persistência segura das informações.
* **[Spring Data JPA / Hibernate](https://spring.io/projects/spring-data-jpa)**: ORM utilizado para o mapeamento objeto-relacional e comunicação com o banco de dados.
* **[MapStruct](https://mapstruct.org/) & [Lombok](https://projectlombok.org/)**: Bibliotecas essenciais para redução de código boilerplate e mapeamento eficiente entre Entidades e DTOs.
* **[Swagger / OpenAPI (SpringDoc)](https://swagger.io/)**: Geração automática da documentação interativa da API.
* **[Docker](https://www.docker.com/) & Docker Compose**: Containerização da aplicação e do banco de dados, garantindo paridade entre os ambientes de desenvolvimento e produção.
* **[Maven](https://maven.apache.org/)**: Gerenciador de dependências e automação do build da aplicação.

## Pré-requisitos para Avaliação

Para rodar e testar este projeto localmente, você precisará de:
* **[Docker](https://www.docker.com/) e Docker Compose** instalados na máquina.
* **Gerador de Documentos:** O sistema possui validação real. Ao criar novos usuários ou clientes, utilize um gerador válido de CPF/CNPJ (recomendamos o site [4Devs](https://www.4devs.com.br/gerador_de_cpf)).
* **Servidor de E-mails Local (Mailpit):** Não é necessário criar contas em serviços externos para testar o disparo de e-mails assíncronos. O projeto utiliza o Mailpit embarcado no Docker para interceptar e exibir todos os e-mails localmente, garantindo testes rápidos e sem bloqueios de rede.

---

## Como Executar o Projeto Localmente

1. **Clone o repositório no GitHub**
2. **Rode o comando do Docker:**
   docker compose up --build
   
3. **Usuários Padrão (Seed):** O sistema criará automaticamente usuários base com roles específicas para acesso aos endpoints. 
*(A senha para todos os usuários abaixo é `123456`)*

| Perfil (Role) | E-mail de Login | Nível de Acesso |
| :--- | :--- | :--- |
| **ADMIN** | `admin@shop.com` | Acesso total a todas as rotas do sistema. |
| **RECEPTIONIST** | `receptionist@shop.com` | Gerencia clientes, veículos, cria e entrega Ordens de Serviço. |
| **MECHANIC** | `mechanic@shop.com` | Atualiza ordens de Serviço, finaliza serviços e busca catálogo, visualiza ordens de serviço. |
| **WAREHOUSE_CLERK** | `warehouse@shop.com` | Gerencia (cria/edita) o estoque de peças. |
| **BUYER** | `buyer@shop.com` | Usuário de compras do sistema. |
| **CUSTOMER** | `emailquearecepcaoselecionar@shop.com` | Visualiza Ordens de Serviço, aprova/reprova orçamento, vê registro do veículo. *(**Essa Role não é criada como um usuário padrão a recepção é quem precisa criar o usuário com essa role específica**)|

---

## 🔐 Permissões e Endpoints (Matriz de Acesso)

Abaixo está o mapeamento de quais perfis podem acessar cada rota da API. Lembre-se de realizar o login (`/api/auth/login`) com o usuário adequado para obter o token JWT.
**Nota:** O perfil `ADMIN` possui permissão global em todas as rotas restritas.

### 1. Autenticação (Livre)
* `POST /api/auth/login`: Realiza o login e retorna o Token JWT.

### 2. Gestão de Usuários (`/api/users`)
* `POST /create`: `RECEPTIONIST`
* `PUT /{id}`: `RECEPTIONIST`
* `GET /{id}` e `/search`: `RECEPTIONIST`
* `GET /available-roles`: `RECEPTIONIST`

### 3. Gestão de Veículos (`/api/vehicles`)
* `POST /create`: `RECEPTIONIST`
* `PUT /{id}`: `RECEPTIONIST`
* `GET /{id}` e `/search`: `RECEPTIONIST`, `MECHANIC`,`CUSTOMER`

### 4. Catálogo de Serviços (`/api/mechanic-services`)
* `POST /create`: `ADMIN`, `MECHANIC`
* `PUT /{id}`: `ADMIN`, `MECHANIC`
* `GET /{id}` e `/search`: `RECEPTIONIST`, `MECHANIC`

### 5. Controle de Estoque (`/api/stock-items`)
* `POST /create`: `WAREHOUSE_CLERK`, `MECHANIC`
* `PUT /{id}`: `WAREHOUSE_CLERK`, `MECHANIC`
* `GET /{id}` e `/search`: `WAREHOUSE_CLERK`, `RECEPTIONIST`, `MECHANIC`

### 6. Ordem de Serviço (Máquina de Estados) (`/api/v1/service-orders`)
O ciclo de vida da OS é protegido rigorosamente por perfil funcional:
* **Criar OS** (`POST /create`): `RECEPTIONIST`
* **Orçar/Diagnóstico** (`PUT /{id}/quote`): `MECHANIC`
* **Solicitar Aprovação** (`POST /{id}/request-approval`): `MECHANIC`
* **Aprovar Orçamento** (`POST /{id}/budget-response`): `CUSTOMER` (Cliente)
* **Finalizar Serviço** (`POST /{id}/finish`): `MECHANIC`
* **Entregar Veículo** (`POST /{id}/deliver`): `RECEPTIONIST`
* **Visualizar OS** (`GET /{id}` e `/search`): `RECEPTIONIST`, `MECHANIC`, `SALESPERSON`, `CUSTOMER`

---

## Documentação Interativa (Swagger)

A API possui documentação viva detalhando todos os Schemas, DTOs e Endpoints. Após subir os containers via Docker, acesse diretamente no navegador:

**[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

## Arquitetura

O C4 Model da aplicacao esta documentado em [docs/architecture/c4-model.md](docs/architecture/c4-model.md).

## ✉️ Caixa de E-mails de Teste (Mailpit)

Todos os e-mails disparados de forma assíncrona pela aplicação (notificações de Ordem de Serviço, aprovações de orçamento, alertas de estoque zerado) são interceptados e podem ser visualizados em tempo real através da interface web do Mailpit.

Após subir os containers, acesse a caixa de entrada em:
**[http://localhost:8025](http://localhost:8025)**


## 📦 Referência de Payloads (JSON) para Testes

Abaixo estão exemplos de payloads validados para criação e edição de dados no sistema. 

> **Atenção:** Nos campos marcados com `UUID_...`, você deve substituir pelo ID real gerado pelo banco de dados nas etapas anteriores. Para o campo `document` de Usuários, utilize um gerador de CPF/CNPJ válido.

### 1. Usuários (Clientes, Mecânicos, etc.)
**Permissão:** `ADMIN`, `RECEPTIONIST`
* **Criar:** `POST /api/users/create`
* **Atualizar:** `PUT /api/users/{id}`

```json
{
  "document": "266.441.520-40", 
  "name": "João da Silva",
  "email": "joao.silva@email.com",
  "role": "CUSTOMER", 
  "active": true,
  "password": "senhaSegura123",
  "phone": "11988887777"
}
```
*(Nota: Valores aceitos para `role`: `ADMIN`, `RECEPTIONIST`, `MECHANIC`, `WAREHOUSE_CLERK`, `SALESPERSON`, `BUYER`, `CUSTOMER`)*

### 2. Veículos
**Permissão:** `ADMIN`, `RECEPTIONIST`
* **Criar:** `POST /api/vehicles/create`
* **Atualizar:** `PUT /api/vehicles/{id}`

```json
{
  "licensePlate": "ABC1D23",
  "brand": "Toyota",
  "model": "Corolla XEI",
  "year": 2022,
  "color": "Prata",
  "ownerId": "UUID_DO_CLIENTE"
}
```
*(Nota: A placa deve seguir o padrão Mercosul ou o padrão antigo, ex: `ABC1D23`)*

### 3. Catálogo de Serviços (Mão de Obra)
**Permissão:** `ADMIN` (Somente o administrador pode cadastrar/alterar serviços base)
* **Criar:** `POST /api/mechanic-services/create`
* **Atualizar:** `PUT /api/mechanic-services/{id}`

```json
{
  "name": "Alinhamento e Balanceamento",
  "description": "Alinhamento 3D e balanceamento das 4 rodas.",
  "estimatedTimeMinutes": 60,
  "price": 120.00
}
```

### 4. Itens de Estoque (Peças e Insumos)
**Permissão:** `ADMIN`, `WAREHOUSE_CLERK`
* **Criar:** `POST /api/stock-items/create`
* **Atualizar:** `PUT /api/stock-items/{id}`

```json
{
  "code": "WHL-001",
  "name": "Roda Aro 14 Aço",
  "type": "PART",
  "description": "Roda de aço aro 14 polegadas, compatível com veículos de pequeno porte (hatch e sedan compacto). Produto resistente, ideal para uso urbano.",
  "quantity": 50,
  "costPrice": 120.00,
  "salePrice": 199.90
}
```
*(Nota: Valores aceitos para `type`: `PART` ou `CONSUMABLE`)*

### 5. Ordens de Serviço (Máquina de Estados)

**A. Abertura de OS (`RECEIVED`)**
* **Endpoint:** `POST /api/v1/service-orders/create`
* **Permissão:** `ADMIN`, `RECEPTIONIST`
```json
{
  "vehicleExternalId": "UUID_DO_VEICULO",
  "customerComplaint": "Volante puxando para a direita em alta velocidade.",
  "odometerReading": 32000,
  "mechanicExternalId": "UUID_DO_MECANICO"
}
```

**B. Inclusão de Orçamento (`DIAGNOSIS`)**
* **Endpoint:** `PUT /api/v1/service-orders/{id}/quote`
* **Permissão:** `ADMIN`, `MECHANIC`
```json
{
  "mechanicDiagnosis": "Necessário alinhamento e troca de dois pneus dianteiros.",
  "parts": [
    { 
      "partExternalId": "UUID_DO_PNEU_NO_ESTOQUE", 
      "quantity": 2 
    }
  ],
  "labors": [
    { 
      "mechanicServiceExternalId": "UUID_DO_SERVICO_DE_ALINHAMENTO", 
      "quantity": 1 
    }
  ]
}
```

**C. Resposta do Cliente (`IN_PROGRESS` ou `CANCELED`)**
* **Endpoint:** `POST /api/v1/service-orders/{id}/budget-response`
* **Permissão:** `ADMIN`, `CUSTOMER`
```json
{
  "approved": true
}
```
