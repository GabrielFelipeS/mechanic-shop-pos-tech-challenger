# Infra Teste — EKS no AWS Academy

Stack Terraform que sobe o mesmo ambiente do `infra/kind`, mas em um cluster **EKS real**, respeitando as restricoes do **AWS Academy (Learner Lab / vocareum)**.

## Por que esta pasta existe

O `infra/aws` nao roda no AWS Academy porque o lab bloqueia as permissoes de IAM que aquele codigo precisa:

| Recurso em `infra/aws` | Permissao exigida | AWS Academy |
| --- | --- | --- |
| `aws_iam_role` (cluster, node, ebs-csi) | `iam:CreateRole` | bloqueado |
| `aws_iam_role_policy_attachment` | `iam:AttachRolePolicy` | bloqueado |
| `aws_iam_openid_connect_provider` | `iam:CreateOpenIDConnectProvider` | bloqueado |
| `aws_eks_access_entry` com o ARN do caller | ARN precisa ser de role, nao de sessao assumida | falha (`arn:aws:sts::...:assumed-role/voclabs/...`) |
| `aws_s3_bucket` para backend | varia por lab | evitado |

## O que muda aqui

- **Nenhuma role e criada.** Control plane e nodes usam a `LabRole`, que ja existe na conta e cuja trust policy aceita `eks.amazonaws.com` e `ec2.amazonaws.com`. O ARN e montado a partir do account id, sem sequer chamar a API de IAM.
- **Nenhum access entry.** `bootstrap_cluster_creator_admin_permissions = true` da acesso de admin ao principal que roda o `terraform apply`, que e exatamente o usuario do lab.
- **Sem OIDC/IRSA.** O addon `aws-ebs-csi-driver` e instalado sem `service_account_role_arn` e usa as credenciais da instance profile do node (`LabRole`), que ja tem EC2/EBS.
- **IMDS com hop limit 2.** Consequencia direta do item anterior e a pegadinha mais cara desta stack — veja a secao abaixo.
- **Sem LoadBalancer.** A exposicao e por `NodePort` nos IPs publicos dos nodes, igual ao modelo do Kind. A regra de entrada e criada no *cluster security group* que o EKS gera — que e o security group efetivamente usado por managed node groups sem launch template.
- **State local.** A sessao do lab e efemera; nao ha bucket S3 de backend.

## A pegadinha do IMDS (por que existe um launch template aqui)

Sem IRSA, o **IMDS e a unica fonte de credenciais AWS para os pods**. E o launch template que o EKS gera sozinho para um managed node group usa `http_put_response_hop_limit = 1`.

Hop limit 1 significa que o pacote sai da instancia mas nao sobrevive ao salto extra do namespace de rede do pod. O efeito e assimetrico e confunde bastante:

| Pod | `hostNetwork` | Com hop limit 1 |
| --- | --- | --- |
| `ebs-csi-node` (DaemonSet) | `true` | funciona |
| `ebs-csi-controller` (Deployment) | `false` | `ebs-plugin` em `CrashLoopBackOff` |

Como o addon so e marcado `ACTIVE` quando seus pods ficam saudaveis, o resultado e um `terraform apply` que morre assim:

```text
Error: waiting for EKS Add-On (eks-mechanic-shop:aws-ebs-csi-driver) create:
timeout while waiting for state to become 'ACTIVE' (last state: 'CREATING')
```

Por isso o [`launch-template.tf`](launch-template.tf) existe: ele so sobe o hop limit para 2. Nao define `image_id` nem `user_data`, entao o EKS continua injetando a AMI otimizada e o bootstrap normalmente. O disco do node migrou para o `block_device_mappings` do launch template porque `disk_size` no node group e mutuamente exclusivo com `launch_template`.

O launch template tambem **nao** declara `vpc_security_group_ids` de proposito: se declarasse, o EKS pararia de anexar o cluster security group aos nodes e a regra de NodePort do `network.tf` deixaria de valer.

## Infraestrutura provisionada

- VPC dedicada com 3 subnets publicas (uma por AZ), internet gateway e route table.
- Cluster EKS `eks-mechanic-shop` (Kubernetes 1.32) com endpoint publico.
- Managed node group com 2 nodes `t3.medium` em subnets publicas (IP publico automatico).
- Addon `aws-ebs-csi-driver` + `StorageClass` `ebs-sc` (gp3, `WaitForFirstConsumer`) para o volume do PostgreSQL.
- `metrics-server` em `kube-system` — o EKS nao instala por padrao e sem ele o HPA nao escala.
- Namespace `mechanic-shop` com API, PostgreSQL (`StatefulSet` + PVC EBS), Mailpit, `ConfigMap`, `Secrets` e `HorizontalPodAutoscaler`.
- Opcionalmente o `nri-bundle` do New Relic, quando `newrelic_license_key` e informada.

Portas expostas nos IPs publicos dos nodes:

| Servico | NodePort |
| --- | --- |
| API | `30080` |
| Mailpit UI | `30025` |
| Mailpit SMTP | `31025` |

## Pre-requisitos

- Terraform `>= 1.5`, `aws` CLI e `kubectl`.

> O `aws` CLI nao e opcional: o kubeconfig do EKS autentica chamando `aws eks get-token`. Sem ele o `kubectl` responde `Forbidden` / `couldn't get current server API group list`.
- Lab iniciado no AWS Academy e credenciais copiadas do botao **AWS Details -> AWS CLI** para `~/.aws/credentials`:

```ini
[default]
aws_access_key_id     = ...
aws_secret_access_key = ...
aws_session_token     = ...
```

> As credenciais do lab expiram junto com a sessao (cerca de 4h). Se um `apply` falhar com `ExpiredToken`, recopie o bloco e rode de novo.

## Estrutura

```text
teste/
├── data.tf          # caller identity, token do EKS, IPs publicos dos nodes
├── eks.tf           # cluster, node group e addon EBS CSI (tudo com LabRole)
├── launch-template.tf # IMDS hop limit 2 — sem isso o addon EBS CSI trava
├── locals.tf        # ARN da LabRole, nome do cluster, URLs publicas
├── manifests.tf     # aplica os manifests em ordem
├── network.tf       # VPC, subnets, IGW, route table e regra de NodePort
├── newrelic.tf      # nri-bundle (opcional)
├── outputs.tf
├── providers.tf
├── variables.tf
└── manifests/
    ├── 00-namespaces/
    ├── 01-config/
    ├── 02-storage/
    └── 03-app/
```

## Como usar

```bash
cd infra/teste
cp terraform.tfvars.example terraform.tfvars   # opcional
terraform init
terraform apply
```

### A imagem precisa ter o codigo de observabilidade

Os probes destes manifests chamam `/actuator/health/readiness`. Esse endpoint so responde 200 nas versoes que trazem o `probes.enabled: true` no `application.yaml` **e** o `permitAll` de `/actuator/health/**` no `SecurityConfigurations`. Uma imagem anterior a isso responde **403**, o startup probe falha 30 vezes e o pod entra em CrashLoop.

Por isso o default de `app_image` e uma tag fixa, e nao `latest`. Para publicar uma nova:

```bash
docker build -t kaizenn/mechanic-shop-backend:obs-$(git rev-parse --short HEAD) .
docker push kaizenn/mechanic-shop-backend:obs-$(git rev-parse --short HEAD)
terraform apply -var="app_image=kaizenn/mechanic-shop-backend:obs-$(git rev-parse --short HEAD)"
```

O apply leva de 15 a 20 minutos (a criacao do cluster e do node group domina o tempo). Ao final:

```bash
terraform output api_url
terraform output swagger_url
terraform output mailpit_ui_url

aws eks update-kubeconfig --region us-east-1 --name eks-mechanic-shop
kubectl get pods -n mechanic-shop
kubectl get hpa -n mechanic-shop
kubectl top pods -n mechanic-shop
```

Para destruir antes de o lab expirar (evita consumo de credito):

```bash
terraform destroy
```

## Variaveis mais usadas

| Variavel | Default | Para que serve |
| --- | --- | --- |
| `lab_role_name` | `LabRole` | Role pre-existente do lab. Alguns labs usam outro nome. |
| `app_image` | `kaizenn/mechanic-shop-backend:latest` | Imagem da API, de um registry publico. |
| `capacity_type` | `ON_DEMAND` | `SPOT` reduz o gasto de credito do lab. |
| `nodeport_allowed_cidr` | `0.0.0.0/0` | Restrinja ao seu IP publico. |
| `api_server_allowed_cidr` | `0.0.0.0/0` | Restrinja o endpoint do API server. |
| `newrelic_license_key` | `""` | Vazio nao instala o `nri-bundle`. |
| `app_base_url` | `""` | Vazio usa `http://<ip-publico-do-node>:30080`. |
| `imds_hop_limit` | `2` | Nao baixe para 1: quebra o `ebs-csi-controller`. |
| `use_exec_auth` | `true` | `false` usa token estatico de 15min e quebra applies longos. |

## Se algo falhar

- **`AccessDenied` em `iam:*`** — o nome da role do lab nao e `LabRole`. Confira em `aws iam list-roles --query 'Roles[].RoleName'` e ajuste `lab_role_name`.
- **Node group em `CREATE_FAILED` com `NodeCreationFailure`** — normalmente o node nao alcancou o API server. Verifique se as subnets estao com `map_public_ip_on_launch` e a route table com rota para o IGW (ja e o caso aqui).
- **API nao responde no IP publico** — cheque `kubectl get svc -n mechanic-shop` (o NodePort deve ser 30080) e `curl http://<ip-do-node>:30080/actuator/health`. Se o pod estiver `Running` e o curl der timeout, o problema e a regra de seguranca: `aws ec2 describe-security-groups --group-ids $(aws eks describe-cluster --name eks-mechanic-shop --query 'cluster.resourcesVpcConfig.clusterSecurityGroupId' --output text)`.
- **Addon EBS CSI preso em `CREATING` / PVC em `Pending`** — quase sempre e o hop limit do IMDS. Confirme com `kubectl get pods -n kube-system -l app=ebs-csi-controller`: se o container `ebs-plugin` estiver reiniciando enquanto o `ebs-csi-node` esta saudavel, e exatamente esse caso. Verifique o valor atual com `aws ec2 describe-instances --instance-ids <id-do-node> --query 'Reservations[].Instances[].MetadataOptions.HttpPutResponseHopLimit'`; tem que ser `2`.
- **`ExpiredToken`** — recopie as credenciais do lab.
- **`Unauthorized` no meio do apply, em algum `kubectl_manifest`** — nao e permissao, e o token de 15 minutos do `data.aws_eks_cluster_auth` expirando durante um apply longo. Os providers ja usam `exec` (`aws eks get-token`) por padrao para evitar isso; se voce tiver setado `use_exec_auth = false`, volte para `true`.
- **Pod da API `Running` mas nunca `Ready`, reiniciando, com `Startup probe failed: HTTP probe failed with statuscode: 403`** — a imagem publicada esta velha. Os probes chamam `/actuator/health/readiness`, que so existe e so e liberado pelo Spring Security nas versoes com a mudanca de observabilidade. Rebuild e push da imagem antes do apply (a pipeline publica `kaizenn/mechanic-shop-backend:latest` no push), ou aponte `app_image` para uma tag que ja tenha o codigo novo.
