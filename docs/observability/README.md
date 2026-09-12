# Observabilidade

Stack de observabilidade da Mechanic Shop, baseada em **New Relic**, cobrindo os
requisitos de *Monitoramento e Observabilidade* da Fase 3 do Tech Challenge.

## Visão geral

```
┌──────────────────────────────────────────────────────────────────────────┐
│                            Cluster Kubernetes                            │
│                                                                          │
│  ┌────────────────────────────────┐   ┌──────────────────────────────┐   │
│  │  Pod mechanic-shop-backend     │   │  Namespace newrelic          │   │
│  │                                │   │  (Helm: nri-bundle)          │   │
│  │  JVM                           │   │                              │   │
│  │   └─ New Relic Java agent      │   │  newrelic-infrastructure     │   │
│  │      (-javaagent)              │   │   → CPU / memória de node,   │   │
│  │       • latência (Transaction) │   │     pod e container          │   │
│  │       • traces distribuídos    │   │  kube-state-metrics          │   │
│  │       • erros                  │   │   → réplicas, HPA, status    │   │
│  │       • custom events da OS    │   │  nri-kube-events             │   │
│  │                                │   │   → OOMKilled, CrashLoop     │   │
│  │  stdout: JSON (ECS)            │──▶│  newrelic-logging (Fluent    │   │
│  │   correlationId, trace.id      │   │   Bit) → New Relic Logs      │   │
│  │                                │   │                              │   │
│  │  /actuator/health/liveness     │◀──┤  kubelet probes              │   │
│  │  /actuator/health/readiness    │   │                              │   │
│  └────────────────────────────────┘   └──────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
              ┌──────────────────────────────────────────┐
              │             New Relic One                │
              │  APM · Logs · Kubernetes · Synthetics    │
              │  Dashboard + Alert Policy + Workflow     │
              │  (provisionados por Terraform)           │
              └──────────────────────────────────────────┘
                                   ▲
              Synthetics (externo) ─┘  GET /actuator/health a cada 1 min
```

## Como cada requisito é atendido

| Requisito | Onde |
| --- | --- |
| Latência das APIs | Agente Java (`Transaction`) → página *APIs* do dashboard + alerta de p95 |
| Consumo de recursos do Kubernetes (CPU, memória) | `nri-bundle` (`K8sContainerSample`, `K8sPodSample`) → página *Kubernetes* + alertas de CPU/memória |
| Healthchecks e uptime | Probes do Actuator (`/actuator/health/liveness` e `/readiness`) + monitor Synthetics externo |
| Alertas para falhas no processamento de OS | Evento `MechanicShopServiceOrderFailure` → condição NRQL na política de alertas |
| Logs estruturados (JSON) com correlação | Profile `prod` (`logging.structured.format.console: ecs`) + `CorrelationIdFilter` |
| Volume diário de ordens de serviço | Evento `MechanicShopServiceOrderOpened` |
| Tempo médio de execução por status | Atributo `phaseDurationSeconds` de `MechanicShopServiceOrderStatusChanged` |
| Erros e falhas nas integrações | Evento `MechanicShopIntegrationFailure` |

## Logs estruturados

O formato JSON está **restrito ao profile `prod`** — localmente e em testes os logs
continuam em texto legível.

- Ativação: `SPRING_PROFILES_ACTIVE=prod` (já definido nos ConfigMaps de
  `infra/kind` e `infra/aws`).
- Configuração: [`src/main/resources/application-prod.yaml`](../../src/main/resources/application-prod.yaml),
  formato **ECS** (`logging.structured.format.console: ecs`).
- Para ver o formato de produção localmente:
  `SPRING_PROFILES_ACTIVE=prod docker compose up springapp`.

### Correlação entre requisições

[`CorrelationIdFilter`](../../src/main/java/org/project/mechanic_shop/shared/config/observability/CorrelationIdFilter.java)
roda com a maior precedência possível na cadeia de filtros e:

1. aproveita o `X-Correlation-Id` (ou `X-Request-Id`) enviado pelo API Gateway /
   Lambda authorizer, ou gera um UUID quando não há;
2. publica o valor no `MDC` — logo ele sai como campo de primeira classe em cada
   linha JSON;
3. adiciona o valor como atributo customizado na transação do APM, permitindo
   cruzar log ↔ APM ↔ trace;
4. devolve o valor no header `X-Correlation-Id` da resposta.

O agente Java injeta `trace.id` e `span.id` automaticamente nos logs encaminhados,
então em New Relic Logs dá para pular direto de uma linha de log para o trace
distribuído correspondente.

Exemplo de consulta:

```sql
SELECT timestamp, level, correlationId, trace.id, message
FROM Log WHERE correlationId = '<id>' ORDER BY timestamp
```

## Eventos customizados de negócio

Emitidos por
[`ServiceOrderObservabilityListener`](../../src/main/java/org/project/mechanic_shop/shared/config/observability/ServiceOrderObservabilityListener.java)
e [`ObservabilityReporter`](../../src/main/java/org/project/mechanic_shop/shared/config/observability/ObservabilityReporter.java).
Os nomes ficam centralizados em `ObservabilityEvents` — renomear algo lá exige
atualizar o NRQL em `infra/newrelic/terraform`.

| Evento | Quando | Atributos principais |
| --- | --- | --- |
| `MechanicShopServiceOrderOpened` | OS criada (após commit) | `serviceOrderId`, `status`, `correlationId` |
| `MechanicShopServiceOrderStatusChanged` | Cada transição de status | `oldStatus`, `newStatus`, `phase`, `phaseDurationSeconds`, `openForSeconds` |
| `MechanicShopServiceOrderFailure` | Falha no processamento assíncrono de uma OS | `operation`, `errorClass`, `errorMessage` |
| `MechanicShopIntegrationFailure` | Falha em dependência externa (SMTP, …) | `integration`, `operation`, `errorClass` |

### Como o "tempo médio por status" é medido

As durações vêm de timestamps que o domínio **já persiste**, então não há tabela
extra nem estado em memória — os números sobrevivem a restart de pod e a
scale-out:

| `phase` | Intervalo medido | Registrado na transição para |
| --- | --- | --- |
| `DIAGNOSIS` | `createdAt` → `approvalDate` | `IN_PROGRESS` |
| `EXECUTION` | `approvalDate` → `actualCompletionDate` | `COMPLETED` |
| `FINALIZATION` | `actualCompletionDate` → agora | `DELIVERED` |

Transições fora desses três pontos continuam sendo registradas, apenas sem
`phaseDurationSeconds`.

> Telemetria nunca interrompe o fluxo de negócio: todos os métodos de reporte
> engolem suas próprias exceções, e sem o `-javaagent` as chamadas da API do New
> Relic são no-op.

## Healthchecks

| Endpoint | Grupo | Usado por |
| --- | --- | --- |
| `/actuator/health` | completo | Synthetics (uptime externo) |
| `/actuator/health/liveness` | `livenessState` | `livenessProbe` do Kubernetes |
| `/actuator/health/readiness` | `readinessState`, `db` | `readinessProbe` e `startupProbe` |

As probes usam `httpGet` no Actuator (antes eram `tcpSocket`, que só verificava se
a porta estava aberta). O `readiness` inclui o indicador `db`, então um pod com
banco indisponível sai do balanceamento em vez de responder 500.

## Provisionamento

### 1. Agente na aplicação

O agente Java é **baixado no build** ([`Dockerfile`](../../Dockerfile), estágio
`newrelic-agent`, versão fixada em `NEW_RELIC_AGENT_VERSION`) — nenhum binário
vai para o git. A configuração vem de
[`infra/newrelic/newrelic.yml`](../newrelic/newrelic.yml), e o que varia por
ambiente vem de variáveis de ambiente.

### 2. Telemetria do cluster (`nri-bundle`)

```bash
cd infra/kind    # ou infra/aws
terraform init
terraform apply -var="newrelic_license_key=$NEW_RELIC_LICENSE_KEY"
```

Com `newrelic_license_key` vazio o `helm_release` não é criado — dá para subir o
cluster local sem depender do New Relic.

### 3. Dashboards, alertas e Synthetics

```bash
cd infra/newrelic/terraform
cp terraform.tfvars.example terraform.tfvars   # preencha
terraform init
terraform apply
terraform output dashboard_permalink
```

No CI isso roda no job `deploy-observability` de
[`.github/workflows/deploy.yml`](../../.github/workflows/deploy.yml), que é
ignorado silenciosamente quando os segredos não estão configurados.

## Segredos e variáveis

| Nome | Tipo | Onde é usado |
| --- | --- | --- |
| `NEW_RELIC_LICENSE_KEY` | GitHub *secret* | Secret da aplicação + `nri-bundle` |
| `NEW_RELIC_API_KEY` | GitHub *secret* | Provider Terraform (User API key, `NRAK-…`) |
| `NEW_RELIC_ACCOUNT_ID` | GitHub *secret* | Provider Terraform |
| `HEALTH_CHECK_URL` | GitHub *variable* | URL monitorada pelo Synthetics |
| `ALERT_EMAIL` | GitHub *variable* | Destino das notificações de alerta |

Localmente, `NEW_RELIC_LICENSE_KEY` vem do `.env` (não versionado).

> ⚠️ A license key que estava embutida em `newrelic.yml` e `newrelic-infra.yml`
> **precisa ser rotacionada**: ela permanece no histórico do git. Gere uma nova em
> *New Relic One → API keys → INGEST - LICENSE* e revogue a antiga.

## Variáveis de ambiente do agente

| Variável | Valor | Efeito |
| --- | --- | --- |
| `NEW_RELIC_LICENSE_KEY` | *(secret)* | Autenticação — sem ela o agente é no-op |
| `NEW_RELIC_APP_NAME` | `mechanic-shop (Production)` | Separa homologação de produção no APM |
| `NEW_RELIC_LABELS` | `env:…;service:mechanic-shop` | Facetagem por ambiente |
| `NEW_RELIC_DISTRIBUTED_TRACING_ENABLED` | `true` | Traces ponta a ponta |
| `NEW_RELIC_APPLICATION_LOGGING_FORWARDING_ENABLED` | `true` | Encaminha logs com `trace.id` |
| `NEW_RELIC_LOG_FILE_NAME` | `STDOUT` | Log do agente vai para o stdout do container |
| `NEW_RELIC_METADATA_KUBERNETES_*` | Downward API | Liga APM ↔ pod / deployment / cluster |
| `SPRING_PROFILES_ACTIVE` | `prod` | Liga os logs JSON |
| `APP_ENVIRONMENT` | `production` | Campo `service.environment` no log ECS |
