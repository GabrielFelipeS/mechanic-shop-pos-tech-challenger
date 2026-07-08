resource "kind_cluster" "this" {
  name           = var.cluster_name
  wait_for_ready = true

  kind_config {
    kind        = "Cluster"
    api_version = "kind.x-k8s.io/v1alpha4"

    node {
      role = "control-plane"

      extra_port_mappings {
        container_port = var.app_node_port
        host_port      = var.app_host_port
        protocol       = "TCP"
      }

      extra_port_mappings {
        container_port = var.mailpit_ui_node_port
        host_port      = var.mailpit_ui_host_port
        protocol       = "TCP"
      }

      extra_port_mappings {
        container_port = var.mailpit_smtp_node_port
        host_port      = var.mailpit_smtp_host_port
        protocol       = "TCP"
      }
    }

    node {
      role = "worker"
    }
  }
}

resource "terraform_data" "load_app_image" {
  count = var.load_local_image ? 1 : 0

  input = var.app_image

  provisioner "local-exec" {
    command = "kind load docker-image ${self.input} --name ${var.cluster_name}"
  }

  depends_on = [kind_cluster.this]
}
