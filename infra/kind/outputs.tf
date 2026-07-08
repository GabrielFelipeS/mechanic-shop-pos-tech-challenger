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
