# ---------------------------------------------------------------------------
# Observabilidade do cluster EKS: nri-bundle
#
# Entrega o requisito "consumo de recursos do Kubernetes (CPU, memoria)" e
# "healthchecks e uptime" no nivel de cluster. Componentes instalados:
#   - newrelic-infrastructure : DaemonSet com metricas de node/pod/container
#   - kube-state-metrics      : estado dos objetos (Deployment, HPA, Pod, ...)
#   - nri-kube-events         : eventos do Kubernetes (OOMKilled, CrashLoop, ...)
#   - newrelic-logging        : Fluent Bit que envia o stdout dos pods (JSON
#                               estruturado) para o New Relic Logs
#
# Com license key vazia o recurso nao e criado, permitindo aplicar a infra sem
# depender do New Relic.
# ---------------------------------------------------------------------------

resource "helm_release" "newrelic_bundle" {
  count = var.newrelic_license_key == "" ? 0 : 1

  name       = "newrelic-bundle"
  repository = "https://helm-charts.newrelic.com"
  chart      = "nri-bundle"
  version    = var.newrelic_bundle_version != "" ? var.newrelic_bundle_version : null

  namespace        = "newrelic"
  create_namespace = true

  timeout = 900
  atomic  = true

  set_sensitive {
    name  = "global.licenseKey"
    value = var.newrelic_license_key
  }

  set {
    name  = "global.cluster"
    value = aws_eks_cluster.cluster.name
  }

  set {
    name  = "global.lowDataMode"
    value = tostring(var.newrelic_low_data_mode)
  }

  set {
    name  = "newrelic-infrastructure.enabled"
    value = "true"
  }

  set {
    name  = "newrelic-infrastructure.privileged"
    value = "true"
  }

  set {
    name  = "kube-state-metrics.enabled"
    value = "true"
  }

  set {
    name  = "nri-kube-events.enabled"
    value = "true"
  }

  set {
    name  = "newrelic-logging.enabled"
    value = "true"
  }

  # A instrumentacao da aplicacao vem do agente Java, nao de scrape Prometheus.
  set {
    name  = "newrelic-prometheus-agent.enabled"
    value = "false"
  }

  depends_on = [aws_eks_node_group.node_group]
}
