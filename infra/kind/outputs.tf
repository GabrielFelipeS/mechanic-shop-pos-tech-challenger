output "cluster_name" {
  description = "Nome do cluster local criado."
  value       = kind_cluster.this.name
}

output "kubeconfig" {
  description = "Kubeconfig gerado pelo provider Kind."
  value       = kind_cluster.this.kubeconfig
  sensitive   = true
}

output "api_url" {
  description = "URL local da API."
  value       = "http://localhost:${var.app_host_port}"
}

output "mailpit_ui_url" {
  description = "URL local da UI do Mailpit."
  value       = "http://localhost:${var.mailpit_ui_host_port}"
}

# ---------------------------------------------------------------------------
# Contrato com infra/newrelic/terraform
#
# A stack de observabilidade filtra as NRQL por appName / clusterName. Estes
# outputs sao a fonte de verdade desses nomes: rode
#
#   terraform output -raw observability_tfvars > ../newrelic/terraform/envs/kind.tfvars
#
# e aplique a stack com -var-file=envs/kind.tfvars.
# ---------------------------------------------------------------------------

output "newrelic_app_name" {
  description = "NEW_RELIC_APP_NAME reportado pela aplicacao (= var.app_name da stack)."
  value       = var.newrelic_app_name
}

output "newrelic_cluster_name" {
  description = "global.cluster reportado pelo nri-bundle (= var.cluster_name da stack)."
  value       = kind_cluster.this.name
}

output "observability_tfvars" {
  description = "Bloco tfvars pronto para infra/newrelic/terraform."
  value       = <<-EOT
    app_name      = "${var.newrelic_app_name}"
    cluster_name  = "${kind_cluster.this.name}"
    namespace     = "${var.namespace}"
    workload_name = "mechanic-shop-backend"
    environment   = "${var.environment}"

    health_check_url = "http://localhost:${var.app_host_port}/actuator/health"
  EOT
}
