# Documentacao da Pasta `infra`

Esta pasta descreve a infraestrutura original em AWS/EKS e tambem aponta os artefatos gerados localmente pelo Terraform. A documentacao abaixo esta separada por arquivo, mantendo a estrutura da pasta `infra`.

## Visao Geral

- `infra/`: stack Terraform original para AWS.
- `infra/manifests/`: manifests Kubernetes soltos usados pela abordagem antiga.
- `infra/kind/`: stack Terraform local baseada em Kind.
- `infra/.terraform/`, `infra/terraform.tfstate` e similares: artefatos gerados pelo Terraform.

## Arquivos da Raiz

### `access-entry.tf`

Define quem pode acessar o cluster EKS depois da criacao. O recurso `aws_eks_access_entry` registra a role `LabRole` como principal com grupos Kubernetes `group-15soat` e `group-profs`. Em seguida, `aws_eks_access_policy_association` vincula essa mesma role a politica `AmazonEKSClusterAdminPolicy`, dando permissao administrativa no cluster inteiro.

### `backend.tf`

Configura o backend remoto do Terraform em um bucket S3 chamado `mechanic-shop-pos-tech-challenger`, com state salvo na chave `backend/terraform.tfstate` na regiao `us-east-1`. A intencao aqui e compartilhar e persistir o state fora da maquina local.

### `bucket.tf`

Contem uma tentativa comentada de criar o bucket S3 do backend via Terraform. Como o proprio backend depende da existencia previa do bucket, esse arquivo ficou desativado e funciona mais como lembrete do bootstrap inicial do ambiente.

### `data.tf`

Declara data sources usados para consultar recursos criados no mesmo plano:

- `aws_eks_cluster.cluster`: le endpoint e CA do cluster EKS.
- `aws_eks_cluster_auth.auth`: gera o token de autenticacao para os providers Kubernetes.
- `aws_caller_identity.current`: descobre o `account_id` atual da conta AWS.

Esses dados alimentam principalmente `providers.tf` e `locals.tf`.

### `eks-cluster.tf`

Cria o cluster EKS principal com nome `eks-${var.projectName}`. O cluster:

- usa autenticacao `API`;
- reutiliza a role `LabRole` como `role_arn`;
- fixa a versao do Kubernetes em `1.32`;
- recebe tres subnets publicas e um security group.

Em resumo, este e o arquivo central da stack AWS.

### `eks-node.tf`

Cria o node group gerenciado do EKS. Ele:

- aponta para o cluster criado em `eks-cluster.tf`;
- usa a mesma role `LabRole`;
- distribui nodes nas subnets publicas;
- define disco de `50 GB`;
- usa `var.instance_type`;
- configura escalabilidade minima de 2, desejada de 2 e maxima de 3 nodes.

Este arquivo cuida da capacidade computacional do cluster.

### `iam-role.tf`

Guarda uma versao comentada da criacao de roles e attachments IAM para o cluster e para os nodes. Nada daqui esta ativo hoje. O arquivo serve como historico de uma alternativa em que as roles seriam provisionadas pelo proprio Terraform, em vez de reaproveitar roles do laboratorio.

### `internet-g.tf`

Cria o Internet Gateway da VPC. Ele e necessario para que as subnets publicas tenham saida para a internet e para que recursos publicos do EKS possam ser acessados externamente.

### `locals.tf`

Monta valores locais baseados na conta AWS atual:

- `lab_role_arn`: role `LabRole`;
- `principalArn`: role `voclabs`.

O valor realmente usado na stack e `lab_role_arn`, consumido pelo cluster, node group e controle de acesso.

### `output.tf`

Exporta informacoes basicas de rede:

- CIDR da VPC;
- ID da VPC;
- CIDRs das subnets;
- IDs das subnets.

Esses outputs ajudam em depuracao e integracoes manuais.

### `providers.tf`

Configura os providers da stack AWS:

- declara `gavinbunney/kubectl`;
- declara `hashicorp/kubernetes`;
- configura o provider `aws` com `var.region_default`;
- conecta `kubectl` e `kubernetes` ao endpoint do EKS usando os data sources de `data.tf`.

Esse arquivo fecha a ponte entre a criacao do cluster e a aplicacao de objetos Kubernetes via Terraform.

### `route-t.tf`

Define a route table publica da VPC. Ela contem:

- rota local para o CIDR da propria VPC;
- rota default `0.0.0.0/0` apontando para o Internet Gateway;
- associacoes explicitas com cada uma das tres subnets publicas.

Sem este arquivo, as subnets nao teriam roteamento publico consistente.

### `sg.tf`

Cria o security group principal da stack. Ele:

- libera entrada HTTP na porta `80` para `0.0.0.0/0`;
- libera todo trafego de saida.

E um security group simples, voltado para expor services publicamente.

### `subnet.tf`

Cria tres subnets publicas por meio de `count`. Cada subnet:

- usa a VPC principal;
- recebe CIDR derivado com `cidrsubnet`;
- ganha IP publico no launch;
- e associada a uma AZ diferente (`us-east-1a`, `us-east-1b`, `us-east-1c`);
- herda `var.tags`.

Esse arquivo distribui a rede do cluster entre zonas de disponibilidade.

### `vars.tf`

Centraliza as variaveis da stack AWS:

- nome do projeto;
- regiao default;
- CIDR da VPC;
- tags padrao;
- tipo de instancia do node group.

Ele define a parametrizacao basica usada pelos demais arquivos.

### `vpc.tf`

Cria a VPC principal com suporte a DNS e hostnames habilitado. Como EKS depende fortemente de resolucao DNS e descoberta de endpoints, essas flags sao importantes para o funcionamento do cluster.

### `.terraform.lock.hcl`

Arquivo gerado pelo Terraform para travar versoes exatas de providers usados na stack AWS. Ele nao descreve infraestrutura por si so; serve para reprodutibilidade e consistencia de ambiente entre execucoes.

### `terraform.tfstate`

State local gerado pelo Terraform para a stack AWS. Ele registra o inventario real dos recursos conhecidos, os IDs retornados pela AWS e a relacao entre configuracao e recursos provisionados.

### `terraform.tfstate.backup`

Backup automatico do state local anterior. E usado pelo Terraform como mecanismo basico de seguranca durante atualizacoes do state.

## Artefatos Gerados

Os itens abaixo sao documentados por pasta, porque sao artefatos de runtime e nao codigo de infraestrutura:

- [`.terraform/README.md`](/home/kaizen/projetos/fiap/mechanic-shop-pos-tech-challenger/docs/infra/.terraform/README.md)
- [`manifests/README.md`](/home/kaizen/projetos/fiap/mechanic-shop-pos-tech-challenger/docs/infra/manifests/README.md)
- [`kind/README.md`](/home/kaizen/projetos/fiap/mechanic-shop-pos-tech-challenger/docs/infra/kind/README.md)
