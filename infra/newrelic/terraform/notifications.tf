# ---------------------------------------------------------------------------
# Notificacoes
#
# Liga a politica de alertas a um destino de e-mail. Sem o workflow abaixo os
# alertas abrem incidentes no New Relic mas nao avisam ninguem.
# ---------------------------------------------------------------------------

resource "newrelic_notification_destination" "email" {
  name = "mechanic-shop email (${var.environment})"
  type = "EMAIL"

  property {
    key   = "email"
    value = var.alert_email
  }
}

resource "newrelic_notification_channel" "email" {
  name           = "mechanic-shop alerts (${var.environment})"
  type           = "EMAIL"
  destination_id = newrelic_notification_destination.email.id
  product        = "IINT"

  property {
    key   = "subject"
    value = "[mechanic-shop/${var.environment}] {{ issueTitle }}"
  }
}

resource "newrelic_workflow" "mechanic_shop" {
  name                  = "mechanic-shop / ${var.environment}"
  muting_rules_handling = "NOTIFY_ALL_ISSUES"

  issues_filter {
    name = "mechanic-shop policy"
    type = "FILTER"

    predicate {
      attribute = "labels.policyIds"
      operator  = "EXACTLY_MATCHES"
      values    = [newrelic_alert_policy.mechanic_shop.id]
    }
  }

  destination {
    channel_id = newrelic_notification_channel.email.id
  }
}
