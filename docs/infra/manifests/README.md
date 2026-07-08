# Documentacao da Pasta `infra/manifests`

Esta pasta contem manifests Kubernetes da abordagem antiga, separados do Terraform local em `infra/kind`. Eles representam uma versao mais direta da aplicacao no cluster.

## Arquivos

### `app-deployment.yaml`

Declara o `Deployment` da API `mechanic-shop-backend` usando a imagem `kaizenn/mechanic-shop-backend:latest`. O manifesto:

- identifica os pods pela label `app: mechanic-shop-backend`;
- expõe a porta `80`;
- define probes HTTP em `/health`;
- tenta declarar `resources.requests.cpu`.

Existe um detalhe importante neste arquivo: o bloco `resources` ficou fora do container, entao a estrutura nao esta ideal para um manifesto moderno. Na stack `infra/kind`, essa definicao foi reescrita corretamente.

### `app-service.yaml`

Cria um `Service` do tipo `NodePort` para a API. Ele encaminha:

- porta `80` do service;
- para `targetPort: 80` no container;
- usando `nodePort: 31100`.

Serve para expor a aplicacao para fora do cluster sem ingress controller.

### `app-hpa.yaml`

Cria um `HorizontalPodAutoscaler` para o deployment `mechanic-shop-backend`. A politica:

- minimo de 1 replica;
- maximo de 10 replicas;
- escala por utilizacao media de CPU com meta de `30%`.

Para funcionar, esse arquivo depende de duas condicoes:

- o deployment precisa ter `resources.requests.cpu`;
- o cluster precisa expor a Metrics API, normalmente via `metrics-server`.
