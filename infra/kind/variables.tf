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
  description = "Imagem Docker da API."
  type        = string
  default     = "kaizenn/mechanic-shop-backend:latest"
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

variable "app_host_port" {
  description = "Porta local exposta para a API."
  type        = number
  default     = 8080
}

variable "app_node_port" {
  description = "NodePort da API dentro do cluster."
  type        = number
  default     = 30080
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
