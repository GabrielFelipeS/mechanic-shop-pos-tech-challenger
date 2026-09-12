
# Infra Kind

Stack Terraform 100% local para subir um cluster Kubernetes com Kind e aplicar todos os manifests da aplicacao via `terraform apply`, sem `kubectl apply`.

## O que esta pasta faz

- Cria um cluster local com `kind`.
- Exponibiliza a API em `http://localhost:8080`, atras do API Gateway (Kong).
- Exponibiliza o Mailpit em `http://localhost:8025` e SMTP em `localhost:1025`.
- Aplica namespace, configuracoes, PostgreSQL, Mailpit, API, Kong e `metrics-server` com `kubectl_manifest`.

## Infraestrutura provisionada

- Cluster Kubernetes local com `kind`, composto por 1 node `control-plane` e 1 node `worker`.
- Namespace dedicado `mechanic-shop` para os recursos da aplicacao.
- API `mechanic-shop-backend` em `Deployment`, com `Service` `ClusterIP` na porta `8080` e imagem configuravel via Terraform. Nao ha NodePort na API: o unico caminho de fora para dentro e o Kong.
- API Gateway `kong` (imagem `kong:3.7`, modo DB-less) em `Deployment`, com `Service` `NodePort` para o proxy (`8000`) e `ClusterIP` para a Admin API (`8001`). A configuracao declarativa vem do `ConfigMap` `kong-declarative-config`, gerado a partir de `manifests/01-config/13-kong-config.yaml`.
- `HorizontalPodAutoscaler` da API com minimo de 1 replica, maximo de 10 e alvo medio de 80% de CPU.
- Banco PostgreSQL interno ao cluster com `Service` `ClusterIP`, `StatefulSet` de 1 replica e volume persistente de `2Gi`.
- Mailpit em `Deployment`, exposto por `Service` `NodePort` para UI HTTP (`8025`) e SMTP (`1025`).
- `ConfigMap` e `Secrets` para configuracao da aplicacao, credenciais do PostgreSQL e senha padrao dos usuarios seed.
- `metrics-server` instalado em `kube-system`, com RBAC, `Service`, `Deployment` e `APIService` para suportar HPA e consultas de metricas.
- Mapeamento de portas do host para o cluster:
  - API (via Kong): `localhost:8080` -> `NodePort 30000`
  - Mailpit UI: `localhost:8025` -> `NodePort 30025`
  - Mailpit SMTP: `localhost:1025` -> `NodePort 31025`

## Pre-requisitos

- Docker instalado e em execucao.
- `kind` instalado localmente.
- Terraform `>= 1.5`.

## Estrutura

```text
kind/
├── cluster.tf
├── locals.tf
├── manifests.tf
├── outputs.tf
├── providers.tf
├── variables.tf
└── manifests/
    ├── 00-namespaces/
    ├── 01-config/
    └── 02-app/
```

## Como usar

```bash
cd infra/kind
terraform init
terraform apply
```

Se quiser usar uma imagem local da API, construa a imagem antes e informe a variavel:

```bash
docker build -t mechanic-shop-api:local ..
terraform apply -var='app_image=mechanic-shop-api:local' -var='load_local_image=true'
```

Nesse modo, o proprio Terraform executa o `kind load docker-image` depois da criacao do cluster e antes dos manifests da aplicacao.

## Observacoes

- O padrao de aplicacao dos manifests segue a separacao `00-namespaces`, `01-config` e `02-app`.
- O PostgreSQL foi mantido interno ao cluster para o ambiente local de desenvolvimento.
- A API usa a porta `8080` porque esse e o default real da aplicacao Spring Boot neste repositorio.
- O `metrics-server` e aplicado no namespace `kube-system` para habilitar HPA e `kubectl top`.

## API Gateway (Kong)

O Kong roda em modo **DB-less**: le `manifests/01-config/13-kong-config.yaml` no boot e nao aceita
escrita pela Admin API. As mesmas rotas do `kong/kong.yml` usado pelo `docker-compose` valem aqui,
com duas diferencas:

- o upstream aponta para o `Service` `mechanic-shop-backend` em vez do container `springapp`;
- o `secret` do consumer `jwt` vem de `var.jwt_secret`, que o Terraform tambem injeta como
  `JWT_SECRET` no `Secret` da aplicacao. **Os dois lados precisam ser o mesmo valor** — o Kong nao
  expande variaveis de ambiente dentro do arquivo declarativo, e um segredo divergente faz o
  gateway rejeitar com `401` tokens que a API considera validos.

O rollout do Kong e disparado por um `checksum/config` na annotation do pod (`local.kong_config_checksum`),
porque o arquivo declarativo so e lido na inicializacao.

Rotas com verificacao de JWT no gateway (somando-se ao `@PreAuthorize` do Spring, nao substituindo):
`GET/POST` de `service-orders` por id, `service-orders/search`, `vehicles` por id e `vehicles/search`.
Login, o link publico de aprovacao de orcamento, Swagger e Actuator passam sem checagem no gateway.

Para inspecionar a configuracao carregada:

```bash
kubectl -n mechanic-shop port-forward svc/kong-admin 8001:8001
curl -s localhost:8001/routes | jq '.data[].name'
```

Para falar direto com a API, sem passar pelo gateway (debug):

```bash
kubectl -n mechanic-shop port-forward svc/mechanic-shop-backend 8081:8080
```
