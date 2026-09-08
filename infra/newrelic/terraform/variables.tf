variable "newrelic_account_id" {
  description = "ID da conta New Relic."
  type        = number
}

variable "newrelic_api_key" {
  description = "User API key do New Relic (NRAK-...). Em CI vem do secret NEW_RELIC_API_KEY."
  type        = string
  sensitive   = true
}

variable "newrelic_region" {
  description = "Regiao da conta New Relic: US ou EU."
  type        = string
  default     = "US"

  validation {
    condition     = contains(["US", "EU"], var.newrelic_region)
    error_message = "newrelic_region deve ser US ou EU."
  }
}

variable "app_name" {
  description = "Nome da aplicacao no APM (deve casar com NEW_RELIC_APP_NAME do ambiente)."
  type        = string
  default     = "mechanic-shop (Production)"
}

variable "cluster_name" {
  description = "Nome do cluster Kubernetes reportado pelo nri-bundle (global.cluster)."
  type        = string
  default     = "eks-mechanic-shop"
}

variable "environment" {
  description = "Ambiente coberto por este stack (usado nos nomes dos recursos)."
  type        = string
  default     = "production"
}

variable "health_check_url" {
  description = "URL publica do healthcheck monitorada pelo Synthetics."
  type        = string
}

variable "synthetics_locations" {
  description = "Localidades publicas do monitor Synthetics."
  type        = list(string)
  default     = ["AWS_US_EAST_1", "AWS_SA_EAST_1"]
}

variable "alert_email" {
  description = "E-mail que recebe as notificacoes dos alertas."
  type        = string
}

# ---------------------------------------------------------------------------
# Limiares dos alertas
# ---------------------------------------------------------------------------

variable "api_latency_threshold_seconds" {
  description = "Latencia media (p95) das APIs a partir da qual o alerta dispara."
  type        = number
  default     = 1.5
}

variable "error_rate_threshold_percent" {
  description = "Percentual de transacoes com erro a partir do qual o alerta dispara."
  type        = number
  default     = 5
}

variable "container_memory_threshold_percent" {
  description = "Uso de memoria do container em relacao ao limite a partir do qual alerta."
  type        = number
  default     = 85
}

variable "container_cpu_threshold_percent" {
  description = "Uso de CPU do container em relacao ao limite a partir do qual alerta."
  type        = number
  default     = 85
}
