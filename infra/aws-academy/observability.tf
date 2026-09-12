# ---------------------------------------------------------------------------
# Ligacao com a stack infra/newrelic/terraform
#
# O nri-bundle (newrelic.tf) e o agente Java (NEW_RELIC_APP_NAME no configmap)
# reportam este cluster com nomes proprios. A stack de dashboards/alertas filtra
# as NRQL exatamente por esses nomes -- se divergirem, os dados chegam na conta
# mas os paineis ficam vazios.
#
# Em vez de manter os nomes duplicados a mao, o apply desta infra escreve o
# arquivo de identidade que a stack consome:
#
#   cd ../newrelic/terraform
#   terraform apply -var-file=envs/aws-academy.tfvars
#
# Assim o cluster e a observabilidade sempre falam do mesmo appName/clusterName.
# ---------------------------------------------------------------------------

locals {
  observability_health_check_url = local.api_url == "" ? "" : "${local.api_url}${var.health_check_path}"

  observability_tfvars = <<-EOT
    # GERADO por `terraform apply` em infra/aws-academy -- nao edite a mao.
    #
    # Consumido por infra/newrelic/terraform:
    #   terraform apply -var-file=envs/aws-academy.tfvars

    app_name      = "${var.newrelic_app_name}"
    cluster_name  = "${local.cluster_name}"
    namespace     = "${var.namespace}"
    workload_name = "mechanic-shop-backend"
    environment   = "${var.environment}"

    health_check_url = "${local.observability_health_check_url}"
  EOT
}

resource "local_file" "observability_tfvars" {
  count = var.write_observability_tfvars ? 1 : 0

  filename        = "${path.module}/../newrelic/terraform/envs/aws-academy.tfvars"
  content         = local.observability_tfvars
  file_permission = "0644"

  # Os manifests definem o NEW_RELIC_APP_NAME e o helm_release define o
  # global.cluster: so faz sentido publicar a identidade depois deles.
  depends_on = [
    kubectl_manifest.config,
    helm_release.newrelic_bundle,
  ]
}
