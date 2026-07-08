
# Infra2

Stack Terraform 100% local para subir um cluster Kubernetes com Kind e aplicar todos os manifests da aplicacao via `terraform apply`, sem `kubectl apply`.

## O que esta pasta faz

- Cria um cluster local com `kind`.
- Exponibiliza a API em `http://localhost:8080`.
- Exponibiliza o Mailpit em `http://localhost:8025` e SMTP em `localhost:1025`.
- Aplica namespace, configuracoes, PostgreSQL, Mailpit, API e `metrics-server` com `kubectl_manifest`.

## Pre-requisitos

- Docker instalado e em execucao.
- `kind` instalado localmente.
- Terraform `>= 1.5`.

## Estrutura

```text
infra2/
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
cd infra2
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
