# ---------------------------------------------------------------------------
# Projeto e AWS Academy
# ---------------------------------------------------------------------------

variable "project_name" {
  description = "Nome base usado nos recursos criados."
  type        = string
  default     = "mechanic-shop"
}

variable "region" {
  description = "Regiao AWS. O AWS Academy Learner Lab so libera us-east-1 (e as vezes us-west-2)."
  type        = string
  default     = "us-east-1"
}

variable "lab_role_name" {
  description = <<-EOT
    Role ja existente na conta do AWS Academy, usada tanto pelo control plane quanto pelos nodes.
    No Learner Lab o nome e "LabRole". Em outros labs pode ser "voclabs" ou similar.
  EOT
  type        = string
  default     = "LabRole"
}

variable "cluster_version" {
  description = "Versao do Kubernetes do EKS."
  type        = string
  default     = "1.32"
}

variable "tags" {
  description = "Tags aplicadas por padrao a todos os recursos AWS."
  type        = map(string)
  default = {
    Name        = "mechanic-shop-pos-tech-challenger"
    School      = "FIAP"
    Turma       = "15SOAT"
    Environment = "Lab"
  }
}

# ---------------------------------------------------------------------------
# Rede
# ---------------------------------------------------------------------------

variable "vpc_cidr" {
  description = "CIDR da VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "subnet_count" {
  description = "Quantidade de subnets publicas. O EKS exige no minimo 2 AZs."
  type        = number
  default     = 3
}

variable "nodeport_allowed_cidr" {
  description = "CIDR com acesso a faixa de NodePort dos nodes. Restrinja ao seu IP se possivel."
  type        = string
  default     = "0.0.0.0/0"
}

variable "api_server_allowed_cidr" {
  description = "CIDR com acesso ao endpoint publico do API server do EKS."
  type        = string
  default     = "0.0.0.0/0"
}

# ---------------------------------------------------------------------------
# Node group
# ---------------------------------------------------------------------------

variable "instance_type" {
  description = "Tipo de instancia dos nodes. O AWS Academy limita ate t3.medium / t3a.medium."
  type        = string
  default     = "t3.medium"
}

variable "capacity_type" {
  description = "ON_DEMAND ou SPOT. SPOT gasta menos credito do lab."
  type        = string
  default     = "ON_DEMAND"
}

variable "node_disk_size" {
  description = "Tamanho em GB do disco de cada node."
  type        = number
  default     = 30
}

variable "node_desired_size" {
  description = "Quantidade desejada de nodes."
  type        = number
  default     = 2
}

variable "node_min_size" {
  description = "Quantidade minima de nodes."
  type        = number
  default     = 2
}

variable "node_max_size" {
  description = "Quantidade maxima de nodes."
  type        = number
  default     = 3
}

# ---------------------------------------------------------------------------
# Storage
# ---------------------------------------------------------------------------

variable "storage_class" {
  description = "StorageClass usada pelo PVC do PostgreSQL."
  type        = string
  default     = "ebs-sc"
}

variable "ebs_volume_type" {
  description = "Tipo do volume EBS provisionado pela StorageClass."
  type        = string
  default     = "gp3"
}

variable "postgres_storage_size" {
  description = "Tamanho do volume do PostgreSQL."
  type        = string
  default     = "2Gi"
}

# ---------------------------------------------------------------------------
# Aplicacao
# ---------------------------------------------------------------------------

variable "namespace" {
  description = "Namespace Kubernetes da aplicacao."
  type        = string
  default     = "mechanic-shop"
}

variable "app_image" {
  description = <<-EOT
    Imagem Docker da API. Precisa estar em um registry publico ou no ECR da conta do lab.

    Fixada numa tag de commit de proposito. `latest` ja apontou para uma imagem meses mais
    antiga que a anterior, derrubando o deploy com 403 no startup probe (ver a secao de
    problemas comuns no README). Com a tag de commit, `kubectl get po -o jsonpath='{..image}'`
    diz exatamente o que esta rodando.

    Nao aponte para imagens anteriores a mudanca de observabilidade: os probes chamam
    /actuator/health/readiness, que so e liberado pelo Spring Security a partir dela.
  EOT
  type        = string
  default     = "kaizenn/mechanic-shop-backend:obs-b9c00c1"
}

variable "app_replicas" {
  description = "Quantidade de replicas da API."
  type        = number
  default     = 1
}

variable "app_cpu_request" {
  description = "CPU request da API, usada pelo HPA como base de utilizacao."
  type        = string
  default     = "300m"
}

variable "app_cpu_limit" {
  description = "CPU limit da API."
  type        = string
  default     = "1000m"
}

variable "app_memory_request" {
  description = "Memoria request da API."
  type        = string
  default     = "256Mi"
}

variable "app_memory_limit" {
  description = "Memoria limit da API."
  type        = string
  default     = "512Mi"
}

variable "app_base_url" {
  description = <<-EOT
    URL publica da API usada nos links enviados por e-mail. Vazio faz o Terraform montar
    http://<ip-publico-do-node>:<kong_node_port> depois que o node group sobe.
  EOT
  type        = string
  default     = ""
}

variable "hpa_min_replicas" {
  description = "Numero minimo de replicas no HorizontalPodAutoscaler."
  type        = number
  default     = 1
}

variable "hpa_max_replicas" {
  description = "Numero maximo de replicas no HorizontalPodAutoscaler."
  type        = number
  default     = 10
}

variable "hpa_cpu_average_utilization" {
  description = "Meta media de utilizacao de CPU do HPA."
  type        = number
  default     = 80
}

variable "mailpit_ui_node_port" {
  description = "NodePort da UI do Mailpit."
  type        = number
  default     = 30025
}

variable "mailpit_smtp_node_port" {
  description = "NodePort do SMTP do Mailpit."
  type        = number
  default     = 31025
}

variable "postgres_database" {
  description = "Nome do banco PostgreSQL da aplicacao."
  type        = string
  default     = "mechanic-shop-db"
}

variable "postgres_username" {
  description = "Usuario do PostgreSQL."
  type        = string
  default     = "postgres"
}

variable "postgres_password" {
  description = "Senha do PostgreSQL."
  type        = string
  default     = "postgres"
  sensitive   = true
}

variable "seed_default_password" {
  description = "Senha padrao dos usuarios seed."
  type        = string
  default     = "123456"
  sensitive   = true
}

# ---------------------------------------------------------------------------
# Observabilidade
# ---------------------------------------------------------------------------

variable "environment" {
  description = "Nome do ambiente reportado na telemetria."
  type        = string
  default     = "lab"
}

variable "newrelic_app_name" {
  description = "Nome da aplicacao no APM do New Relic."
  type        = string
  default     = "mechanic-shop (AWS Academy)"
}

variable "newrelic_license_key" {
  description = <<-EOT
    Ingest license key do New Relic. Vazio provisiona o cluster sem observabilidade
    (o agente Java sobe como no-op e o nri-bundle nao e instalado).
  EOT
  type        = string
  default     = "a0b842d78ab3ce3779e758233aca648cd8e3NRAL"
  sensitive   = true
}

variable "newrelic_bundle_version" {
  description = "Versao do chart Helm nri-bundle. Vazio usa a versao mais recente."
  type        = string
  default     = ""
}

variable "newrelic_low_data_mode" {
  description = "Reduz o volume de dados enviados pelo nri-bundle."
  type        = bool
  default     = true
}

variable "imds_hop_limit" {
  description = <<-EOT
    Limite de hops do IMDS nos nodes. Precisa ser 2 para que pods sem hostNetwork
    (como o ebs-csi-controller) consigam pegar as credenciais da role do node.
    Como este cluster nao tem IRSA, baixar para 1 quebra o driver EBS CSI.
  EOT
  type        = number
  default     = 2
}

variable "use_exec_auth" {
  description = <<-EOT
    Autentica no cluster chamando "aws eks get-token" a cada requisicao, em vez de usar
    um token estatico de 15 minutos. Exige o aws CLI instalado. Deixe true: com token
    estatico um apply do zero expira no meio e falha com "Unauthorized".
  EOT
  type        = bool
  default     = true
}

variable "health_check_path" {
  description = "Path do healthcheck da API, usado no monitor Synthetics da stack de observabilidade."
  type        = string
  default     = "/actuator/health"
}

variable "write_observability_tfvars" {
  description = <<-EOT
    Escreve infra/newrelic/terraform/envs/aws-academy.tfvars com o appName,
    clusterName e health check URL reais deste cluster, para que a stack de
    dashboards/alertas consulte exatamente o que esta infra reporta.
  EOT
  type        = bool
  default     = true
}

# ---------------------------------------------------------------------------
# API Gateway (Kong)
# ---------------------------------------------------------------------------

variable "kong_image" {
  description = "Imagem do Kong usada como API Gateway."
  type        = string
  default     = "kong:3.7"
}

variable "kong_replicas" {
  description = "Quantidade de replicas do Kong."
  type        = number
  default     = 2
}

variable "kong_node_port" {
  description = <<-EOT
    NodePort do proxy do Kong. E a porta publica da aplicacao nos IPs dos nodes
    (a regra de ingress do network.tf ja libera a faixa 30000-32767).
  EOT
  type        = number
  default     = 30000
}

variable "kong_cpu_request" {
  description = "CPU request do Kong."
  type        = string
  default     = "100m"
}

variable "kong_cpu_limit" {
  description = "CPU limit do Kong."
  type        = string
  default     = "500m"
}

variable "kong_memory_request" {
  description = "Memoria request do Kong."
  type        = string
  default     = "256Mi"
}

variable "kong_memory_limit" {
  description = "Memoria limit do Kong."
  type        = string
  default     = "512Mi"
}

variable "jwt_secret" {
  description = <<-EOT
    Segredo HS256 dos tokens da aplicacao. Injetado como JWT_SECRET no Secret da API
    e como `secret` do consumer jwt na configuracao declarativa do Kong — os dois
    precisam ser identicos para o gateway validar os tokens emitidos pela API.
  EOT
  type        = string
  default     = "local-dev-jwt-secret-change-me"
  sensitive   = true
}

variable "cpf_login_url" {
  description = <<-EOT
    Invoke URL da funcao Lambda de login por CPF (API Gateway HTTP API), roteada
    pelo Kong em /functions/cpf-login.
  EOT
  type        = string
  default     = "https://tpi1pjo0hh.execute-api.us-east-1.amazonaws.com/cpf-login"
}
