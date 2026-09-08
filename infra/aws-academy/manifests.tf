resource "kubectl_manifest" "namespaces" {
  for_each = local.namespace_files

  yaml_body = templatefile("${path.module}/manifests/00-namespaces/${each.value}", {
    namespace = var.namespace
  })

  depends_on = [aws_eks_node_group.node_group]
}

resource "kubectl_manifest" "config" {
  for_each = local.config_files

  yaml_body = templatefile("${path.module}/manifests/01-config/${each.value}", {
    namespace             = var.namespace
    postgres_database     = var.postgres_database
    postgres_username     = var.postgres_username
    postgres_password     = var.postgres_password
    seed_default_password = var.seed_default_password
    app_base_url          = var.app_base_url != "" ? var.app_base_url : local.api_url
    environment           = var.environment
    newrelic_app_name     = var.newrelic_app_name
    newrelic_license_key  = var.newrelic_license_key
  })

  depends_on = [kubectl_manifest.namespaces]
}

resource "kubectl_manifest" "storage" {
  for_each = local.storage_files

  yaml_body = templatefile("${path.module}/manifests/02-storage/${each.value}", {
    storage_class = var.storage_class
    volume_type   = var.ebs_volume_type
  })

  depends_on = [aws_eks_addon.ebs_csi]
}

resource "kubectl_manifest" "app" {
  for_each = local.app_files

  yaml_body = templatefile("${path.module}/manifests/03-app/${each.value}", {
    namespace                   = var.namespace
    app_image                   = var.app_image
    app_replicas                = var.app_replicas
    app_cpu_request             = var.app_cpu_request
    app_cpu_limit               = var.app_cpu_limit
    app_memory_request          = var.app_memory_request
    app_memory_limit            = var.app_memory_limit
    app_node_port               = var.app_node_port
    mailpit_ui_node_port        = var.mailpit_ui_node_port
    mailpit_smtp_node_port      = var.mailpit_smtp_node_port
    hpa_min_replicas            = var.hpa_min_replicas
    hpa_max_replicas            = var.hpa_max_replicas
    hpa_cpu_average_utilization = var.hpa_cpu_average_utilization
    cluster_name                = local.cluster_name
    storage_class               = var.storage_class
    postgres_storage_size       = var.postgres_storage_size
  })

  depends_on = [kubectl_manifest.config, kubectl_manifest.storage]
}
