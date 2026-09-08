output "vpc_cidr" {
  value = aws_vpc.vpc_fiap.cidr_block
}

output "vpc_id" {
  value = aws_vpc.vpc_fiap.id
}

output "subnet_cidr" {
  value = aws_subnet.subnet_public[*].cidr_block
}

output "subnet_id" {
  value = aws_subnet.subnet_public[*].id
}
# ---------------------------------------------------------------------------
# Contrato com infra/newrelic/terraform
#
#   terraform output -raw observability_tfvars > ../newrelic/terraform/envs/aws.tfvars
# ---------------------------------------------------------------------------

output "newrelic_app_name" {
  description = "NEW_RELIC_APP_NAME reportado pela aplicacao (= var.app_name da stack)."
  value       = var.newrelic_app_name
}

output "newrelic_cluster_name" {
  description = "global.cluster reportado pelo nri-bundle (= var.cluster_name da stack)."
  value       = aws_eks_cluster.cluster.name
}

output "observability_tfvars" {
  description = "Bloco tfvars pronto para infra/newrelic/terraform."
  value       = <<-EOT
    app_name      = "${var.newrelic_app_name}"
    cluster_name  = "${aws_eks_cluster.cluster.name}"
    namespace     = "${var.namespace}"
    workload_name = "mechanic-shop-backend"
    environment   = "${var.environment}"

    # Ajuste para o endereco publico do Ingress/LoadBalancer do ambiente.
    health_check_url = "http://<endereco-publico>${var.health_check_path}"
  EOT
}
