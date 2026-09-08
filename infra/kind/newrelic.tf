# ---------------------------------------------------------------------------
# Observabilidade do cluster: nri-bundle
#
# Entrega o requisito "consumo de recursos do Kubernetes (CPU, memoria)" e o
# "healthchecks e uptime" no nivel de cluster. O bundle instala:
#   - newrelic-infrastructure : DaemonSet com metricas de node/pod/container
#   - kube-state-metrics      : estado dos objetos (Deployment, HPA, Pod, ...)
#   - nri-kube-events         : eventos do Kubernetes (OOMKilled, CrashLoop, ...)
#   - newrelic-logging        : DaemonSet Fluent Bit que envia o stdout dos pods
#                               (JSON estruturado) para o New Relic Logs
#
# Se nenhuma license key for informada o recurso simplesmente nao e criado,
# permitindo subir o cluster local sem depender do New Relic.
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
    value = var.cluster_name
  }

  set {
    name  = "global.lowDataMode"
    value = tostring(var.newrelic_low_data_mode)
  }

  # Metricas de infraestrutura do Kubernetes (CPU / memoria de node, pod e container).
  set {
    name  = "newrelic-infrastructure.enabled"
    value = "true"
  }

  set {
    name  = "newrelic-infrastructure.privileged"
    value = "true"
  }

  # Estado dos objetos do cluster, incluindo replicas do HPA.
  set {
    name  = "kube-state-metrics.enabled"
    value = "true"
  }

  # Eventos do cluster: reinicios, falhas de probe, OOMKilled.
  set {
    name  = "nri-kube-events.enabled"
    value = "true"
  }

  # Coleta o stdout dos pods. Como a aplicacao roda com o profile "prod", o que chega
  # aqui ja e JSON estruturado com correlationId, trace.id e span.id.
  set {
    name  = "newrelic-logging.enabled"
    value = "true"
  }

  # Nao usado neste projeto: a instrumentacao vem do agente Java, nao de scrape Prometheus.
  set {
    name  = "newrelic-prometheus-agent.enabled"
    value = "false"
  }

  depends_on = [kind_cluster.this]
}
