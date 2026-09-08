variable "projectName" {
  default = "mechanic-shop-pos-tech-challenger"
}

variable "region_default" {
  default = "us-east-1"
}

variable "cidr_vpc" {
  default = "10.0.0.0/16"
}

variable "tags" {
  default = {
    Name        = "mechanic-shop-pos-tech-challenger",
    School      = "FIAP",
    Turma       = "15SOAT",
    Environment = "Production",
    Year        = "2026"
  }
}

variable "instance_type" {
  default = "t3.medium"
}



# ---------------------------------------------------------------------------
# Observabilidade
# ---------------------------------------------------------------------------

variable "newrelic_license_key" {
  description = <<-EOT
    Ingest license key do New Relic. Deixe vazio para provisionar o cluster sem o nri-bundle.
    Em CI vem do secret NEW_RELIC_LICENSE_KEY (TF_VAR_newrelic_license_key).
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
  description = "Reduz o volume de dados enviados pelo nri-bundle."
  type        = bool
  default     = false
}

# ---------------------------------------------------------------------------
# Identidade da telemetria
#
# Precisa casar com o NEW_RELIC_APP_NAME do
# manifests/01-config/app-configmap.yaml e com o var.app_name da stack
# infra/newrelic/terraform.
# ---------------------------------------------------------------------------

variable "newrelic_app_name" {
  description = "Nome da aplicacao no APM do New Relic."
  type        = string
  default     = "mechanic-shop (Production)"
}

variable "namespace" {
  description = "Namespace Kubernetes da aplicacao."
  type        = string
  default     = "mechanic-shop"
}

variable "environment" {
  description = "Nome do ambiente reportado na telemetria."
  type        = string
  default     = "production"
}

variable "health_check_path" {
  description = "Path do healthcheck exposto pela API."
  type        = string
  default     = "/actuator/health"
}
