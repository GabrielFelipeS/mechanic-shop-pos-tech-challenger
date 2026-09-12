variable "cluster_name" {
  description = "Nome do cluster Kind local."
  type        = string
  default     = "mechanic-shop-local"
}

variable "namespace" {
  description = "Namespace Kubernetes da aplicacao."
  type        = string
  default     = "mechanic-shop"
}

variable "app_image" {
  description = <<-EOT
    Imagem Docker da API, fixada numa tag de commit em vez de `latest`. Tag mutavel
    esconde qual codigo esta rodando: o cluster ja subiu, sem ninguem perceber, uma
    imagem com o segredo do JWT hardcoded e outra anterior aos probes de readiness.

    Para iterar com uma build local: -var='app_image=mechanic-shop-api:local' -var='load_local_image=true'.
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

variable "load_local_image" {
  description = "Carrega a imagem da API no cluster Kind usando `kind load docker-image`."
  type        = bool
  default     = false
}

variable "mailpit_ui_host_port" {
  description = "Porta local exposta para a UI do Mailpit."
  type        = number
  default     = 8025
}

variable "mailpit_ui_node_port" {
  description = "NodePort da UI do Mailpit."
  type        = number
  default     = 30025
}

variable "mailpit_smtp_host_port" {
  description = "Porta local exposta para o SMTP do Mailpit."
  type        = number
  default     = 1025
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
# Observability
# ---------------------------------------------------------------------------

variable "environment" {
  description = "Nome do ambiente reportado na telemetria (local, homolog, prod)."
  type        = string
  default     = "local"
}

variable "newrelic_app_name" {
  description = "Nome da aplicacao no APM do New Relic. Deve ser distinto por ambiente."
  type        = string
  default     = "mechanic-shop (Local)"
}

variable "newrelic_license_key" {
  description = <<-EOT
    Ingest license key do New Relic. Deixe vazio para provisionar o cluster sem observabilidade
    (o agente Java sobe como no-op e o nri-bundle nao e instalado).
  EOT
  type        = string
  default     = ""
  sensitive   = true
}

variable "newrelic_bundle_version" {
  description = "Versao do chart Helm nri-bundle. Vazio usa a versao mais recente."
  type        = string
  default     = ""
}

variable "newrelic_low_data_mode" {
  description = "Reduz o volume de dados enviados pelo nri-bundle (recomendado fora de producao)."
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
  default     = 1
}

variable "kong_node_port" {
  description = "NodePort do proxy do Kong dentro do cluster."
  type        = number
  default     = 30000
}

variable "kong_host_port" {
  description = "Porta local exposta para o proxy do Kong. E a porta de entrada da aplicacao."
  type        = number
  default     = 8080
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
