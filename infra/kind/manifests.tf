resource "kubectl_manifest" "namespaces" {
  for_each = local.namespace_files
  yaml_body = templatefile("${path.module}/manifests/00-namespaces/${each.value}", {
    namespace = var.namespace
  })

  depends_on = [kind_cluster.this]
}

resource "kubectl_manifest" "config" {
  for_each = local.config_files
  yaml_body = templatefile("${path.module}/manifests/01-config/${each.value}", {
    namespace             = var.namespace
    app_image             = var.app_image
    app_replicas          = var.app_replicas
    postgres_database     = var.postgres_database
    postgres_username     = var.postgres_username
    postgres_password     = var.postgres_password
    seed_default_password = var.seed_default_password
    environment           = var.environment
    newrelic_app_name     = var.newrelic_app_name
    newrelic_license_key  = var.newrelic_license_key
    jwt_secret            = var.jwt_secret
    cpf_login_url         = var.cpf_login_url
  })

  depends_on = [kubectl_manifest.namespaces]
}

resource "kubectl_manifest" "app" {
  for_each = local.app_files
  yaml_body = templatefile("${path.module}/manifests/02-app/${each.value}", {
    namespace                   = var.namespace
    app_image                   = var.app_image
    app_replicas                = var.app_replicas
    app_cpu_request             = var.app_cpu_request
    app_cpu_limit               = var.app_cpu_limit
    app_memory_request          = var.app_memory_request
    app_memory_limit            = var.app_memory_limit
    mailpit_ui_node_port        = var.mailpit_ui_node_port
    mailpit_smtp_node_port      = var.mailpit_smtp_node_port
    hpa_min_replicas            = var.hpa_min_replicas
    hpa_max_replicas            = var.hpa_max_replicas
    hpa_cpu_average_utilization = var.hpa_cpu_average_utilization
    cluster_name                = var.cluster_name
    kong_image                  = var.kong_image
    kong_replicas               = var.kong_replicas
    kong_node_port              = var.kong_node_port
    kong_cpu_request            = var.kong_cpu_request
    kong_cpu_limit              = var.kong_cpu_limit
    kong_memory_request         = var.kong_memory_request
    kong_memory_limit           = var.kong_memory_limit
    kong_config_checksum        = local.kong_config_checksum
  })

  depends_on = [kubectl_manifest.config, terraform_data.load_app_image]
}
