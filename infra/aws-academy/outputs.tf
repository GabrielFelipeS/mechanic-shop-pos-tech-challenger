output "cluster_name" {
  description = "Nome do cluster EKS."
  value       = aws_eks_cluster.cluster.name
}

output "cluster_endpoint" {
  description = "Endpoint do API server do EKS."
  value       = aws_eks_cluster.cluster.endpoint
}

output "kubeconfig_command" {
  description = "Comando para configurar o kubectl."
  value       = "aws eks update-kubeconfig --region ${var.region} --name ${aws_eks_cluster.cluster.name}"
}

output "node_public_ips" {
  description = "IPs publicos dos nodes. Qualquer um deles atende os NodePorts."
  value       = data.aws_instances.nodes.public_ips
}

output "api_url" {
  description = "URL publica da API."
  value       = local.api_url
}

output "mailpit_ui_url" {
  description = "URL publica da UI do Mailpit."
  value       = local.mailpit_ui_url
}

output "swagger_url" {
  description = "URL do Swagger UI."
  value       = local.api_url == "" ? "" : "${local.api_url}/swagger-ui/index.html"
}

# ---------------------------------------------------------------------------
# Contrato com infra/newrelic/terraform
#
# Gerado automaticamente em observability.tf (local_file). Estes outputs
# existem para inspecao e para uso em pipelines.
# ---------------------------------------------------------------------------

output "newrelic_app_name" {
  description = "NEW_RELIC_APP_NAME reportado pela aplicacao (= var.app_name da stack)."
  value       = var.newrelic_app_name
}

output "newrelic_cluster_name" {
  description = "global.cluster reportado pelo nri-bundle (= var.cluster_name da stack)."
  value       = local.cluster_name
}

output "observability_tfvars" {
  description = <<-EOT
    Bloco tfvars consumido por infra/newrelic/terraform. Gravado
    automaticamente em ../newrelic/terraform/envs/aws-academy.tfvars quando
    var.write_observability_tfvars = true.
  EOT
  value       = local.observability_tfvars
}

output "health_check_url" {
  description = "URL do healthcheck monitorada pelo Synthetics da stack de observabilidade."
  value       = local.observability_health_check_url
}
