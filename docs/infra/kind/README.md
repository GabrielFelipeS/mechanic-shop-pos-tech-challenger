# Documentacao da Pasta `infra/kind`

Esta pasta contem a stack Terraform local baseada em Kind. Ela foi criada para permitir um ambiente Kubernetes totalmente local, incluindo cluster, aplicacao, PostgreSQL, Mailpit, `metrics-server` e HPA, tudo aplicado por `terraform apply`.

## Arquivos da Raiz

### `README.md`

Documento operacional da propria stack local. Explica pre-requisitos, uso com `terraform init` e `terraform apply`, suporte a imagem local da API e a funcao do `metrics-server`.

### `providers.tf`

Declara e configura os providers usados pela stack local:

- `tehcyx/kind` para criar o cluster;
- `gavinbunney/kubectl` para aplicar manifests.

O provider `kubectl` usa diretamente os certificados e endpoint expostos pelo recurso `kind_cluster.this`, sem depender de kubeconfig externo.

### `variables.tf`

Centraliza toda a parametrizacao da stack local. As variaveis cobrem:

- nome do cluster e namespace;
- imagem e replicas da API;
- requests e limits de CPU e memoria;
- parametros do HPA;
- portas expostas para API e Mailpit;
- configuracao do PostgreSQL;
- senha padrao dos usuarios seed;
- opcao `load_local_image` para carregar imagem Docker local no Kind.

Os defaults atuais da API foram ajustados para um perfil menos agressivo de autoscaling local:

- `app_cpu_request = 300m`
- `app_cpu_limit = 1000m`
- `app_memory_request = 384Mi`
- `app_memory_limit = 768Mi`
- `hpa_cpu_average_utilization = 80`

### `locals.tf`

Usa `fileset` para descobrir dinamicamente os manifests em tres camadas:

- `00-namespaces`
- `01-config`
- `02-app`

Isso evita listar arquivos manualmente no Terraform toda vez que um manifesto novo e adicionado.

### `cluster.tf`

Contem dois blocos principais:

- `kind_cluster.this`: cria o cluster local com um `control-plane` e um `worker`;
- `terraform_data.load_app_image`: opcionalmente executa `kind load docker-image` se `load_local_image=true`.

O cluster mapeia portas do host para `NodePort`s internos do Kong (`localhost:8080`, a entrada da aplicacao) e do Mailpit. A API nao tem `NodePort`: seu `Service` e `ClusterIP` e o acesso externo passa obrigatoriamente pelo gateway.

### `manifests.tf`

Aplica os manifests Kubernetes com `kubectl_manifest`, em tres etapas:

1. namespaces;
2. configuracoes;
3. aplicacao e componentes auxiliares.

Os manifests sao renderizados com `templatefile`, o que permite interpolar variaveis do Terraform diretamente dentro dos YAMLs.

### `outputs.tf`

Exporta:

- nome do cluster;
- kubeconfig gerado;
- URL local da API;
- URL local da interface web do Mailpit.

Esses outputs ajudam a operar e depurar o cluster local.

### `mechanic-shop-local-config`

Arquivo kubeconfig materializado para o cluster local. Ele contem:

- endpoint do cluster;
- CA;
- certificado de cliente;
- chave do cliente;
- context atual.

Na pratica, e um snapshot local de acesso ao cluster Kind.

### `.terraform.lock.hcl`

Lockfile dos providers da stack local. Garante reproducibilidade das versoes de `kind` e `kubectl`.

### `.terraform.tfstate.lock.info`

Arquivo temporario de lock do state usado pelo Terraform para evitar operacoes concorrentes no mesmo diretório de trabalho.

### `terraform.tfstate`

State local da stack Kind, com o inventario do cluster, manifests e outputs.

### `terraform.tfstate.backup`

Backup do state local anterior.

## Subpastas

- [`.terraform/README.md`](/home/kaizen/projetos/fiap/mechanic-shop-pos-tech-challenger/docs/infra/kind/.terraform/README.md)
- [`manifests/00-namespaces/README.md`](/home/kaizen/projetos/fiap/mechanic-shop-pos-tech-challenger/docs/infra/kind/manifests/00-namespaces/README.md)
- [`manifests/01-config/README.md`](/home/kaizen/projetos/fiap/mechanic-shop-pos-tech-challenger/docs/infra/kind/manifests/01-config/README.md)
- [`manifests/02-app/README.md`](/home/kaizen/projetos/fiap/mechanic-shop-pos-tech-challenger/docs/infra/kind/manifests/02-app/README.md)
