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
