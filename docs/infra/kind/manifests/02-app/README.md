# Documentacao da Pasta `infra/kind/manifests/02-app`

Esta camada contem workloads, services e componentes auxiliares da stack local. Ela e aplicada por ultimo, depois dos namespaces e das configuracoes.

## Bloco Metrics Server

### `05-metrics-server-serviceaccount.yaml`

Cria a `ServiceAccount` usada pelo `metrics-server` no namespace `kube-system`. Ela e a identidade do pod ao acessar a API do cluster.

### `06-metrics-server-rbac-reader.yaml`

Cria a `ClusterRole` `system:aggregated-metrics-reader`. Essa role permite leitura do grupo `metrics.k8s.io` e agrega permissao para perfis administrativos e de visualizacao.

### `07-metrics-server-rolebinding-auth-reader.yaml`

Cria o `RoleBinding` no `kube-system` que permite ao `metrics-server` ler o recurso `extension-apiserver-authentication-reader`.

### `08-metrics-server-clusterrolebinding-auth-delegator.yaml`

Cria o `ClusterRoleBinding` que liga o service account do `metrics-server` a role `system:auth-delegator`.

### `09-metrics-server-clusterrole.yaml`

Cria a `ClusterRole` `system:metrics-server`, com permissoes para:

- ler `nodes/metrics`;
- listar e observar `pods`;
- listar e observar `nodes`.

### `10-metrics-server-clusterrolebinding.yaml`

Cria o `ClusterRoleBinding` que associa a `ClusterRole` `system:metrics-server` ao service account `metrics-server`.

### `11-metrics-server-service.yaml`

Expõe o `metrics-server` via `Service` interno no `kube-system`, apontando para a porta HTTPS do pod.

### `12-metrics-server-deployment.yaml`

Sobe o `Deployment` do `metrics-server` com:

- imagem oficial;
- probes HTTPS;
- requests de CPU e memoria;
- hardening basico de security context;
- `--kubelet-insecure-tls` para suportar ambiente local;
- resolucao de metricas a cada 15 segundos.

Ele e a base para fazer `kubectl top` e para o HPA funcionar.

### `13-metrics-server-apiservice.yaml`

Registra o recurso `APIService` `v1beta1.metrics.k8s.io`, fazendo a API agregada apontar para o service `metrics-server`. Sem esse arquivo, o cluster nao exporia `pods.metrics.k8s.io`.

## Bloco PostgreSQL

### `20-postgres-service.yaml`

Cria um `Service` `ClusterIP` para o PostgreSQL. Ele oferece descoberta de servico interna em `postgres:5432` para a API.

### `21-postgres-statefulset.yaml`

Cria o `StatefulSet` do PostgreSQL com uma replica. O manifesto inclui:

- imagem `postgres:17`;
- `envFrom` vindo do `postgres-secret`;
- probes TCP;
- `volumeClaimTemplates` com `2Gi`.

O uso de `StatefulSet` faz sentido porque o banco precisa de identidade estavel e armazenamento persistente.

## Bloco Mailpit

### `30-mailpit-service.yaml`

Cria um `Service` `NodePort` para o Mailpit, expondo:

- porta web `8025`;
- porta SMTP `1025`.

As portas externas sao interpoladas a partir das variaveis do Terraform.

### `31-mailpit-deployment.yaml`

Cria o `Deployment` do Mailpit com uma replica e duas portas expostas, permitindo inspecionar emails e receber mensagens SMTP da aplicacao.

## Bloco API

### `40-api-service.yaml`

Cria o `Service` `ClusterIP` da API na porta `8080`. Nao ha `nodePort`: desde a entrada do Kong, o unico caminho de fora para dentro do cluster e o proxy do gateway, de modo que nenhuma requisicao consiga driblar as rotas protegidas por JWT. Para depurar direto na API use `kubectl -n mechanic-shop port-forward svc/mechanic-shop-backend 8081:8080`.

### `41-api-deployment.yaml`

Cria o `Deployment` principal da API. O manifesto:

- injeta configuracoes por `ConfigMap` e `Secret`;
- expõe a porta `8080`;
- define `startupProbe`, `readinessProbe` e `livenessProbe` TCP;
- usa `requests` e `limits` de CPU e memoria;
- permite variar imagem, replicas e limites por Terraform.

O `startupProbe` protege o bootstrap mais demorado do Spring Boot, evitando reinicio prematuro pelo kubelet. Os `requests` de CPU sao especialmente importantes para o HPA calcular utilizacao.

### `42-api-hpa.yaml`

Cria o `HorizontalPodAutoscaler` da API com:

- alvo no deployment `mechanic-shop-backend`;
- minimo e maximo de replicas parametrizados;
- escala por utilizacao media de CPU.

Esse manifesto depende funcionalmente do `metrics-server` e dos `requests` de CPU definidos em `41-api-deployment.yaml`. Na configuracao atual, a meta padrao foi ajustada para `80%`, reduzindo a agressividade do autoscaling em ambiente local.

## Bloco API Gateway

### `50-kong-service.yaml`

Cria o `Service` `NodePort` do proxy do Kong (`8000` -> `nodePort` configurado no Terraform, `30000` por padrao). E a porta de entrada da aplicacao: o `cluster.tf` mapeia `localhost:8080` do host para este `nodePort`.

### `51-kong-admin-service.yaml`

Cria o `Service` `ClusterIP` da Admin API (`8001`). Fica interno de proposito: em modo DB-less ela e somente leitura, mas ainda expoe a configuracao inteira, incluindo o segredo do plugin `jwt`. Acesso por `kubectl -n mechanic-shop port-forward svc/kong-admin 8001:8001`.

### `52-kong-deployment.yaml`

Cria o `Deployment` do Kong em modo DB-less (`KONG_DATABASE=off`), montando o `ConfigMap` `kong-declarative-config` em `/kong/declarative`. O manifesto:

- publica proxy (`8000`), Admin API (`8001`) e status (`8100`) em `0.0.0.0`;
- usa a porta de status para `startupProbe`, `readinessProbe` e `livenessProbe`, que assim nao competem com o trafego do proxy;
- carrega uma annotation `checksum/config` calculada pelo Terraform sobre o arquivo declarativo renderizado.

O checksum existe porque o Kong le o `kong.yml` apenas no boot: sem ele, uma alteracao de rota so passaria a valer no proximo restart acidental do pod.