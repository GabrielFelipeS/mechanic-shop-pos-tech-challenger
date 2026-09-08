# infra/kind -- cluster Kind local.
# Espelha: var.newrelic_app_name (default "mechanic-shop (Local)") e
#          kind_cluster.this.name (var.cluster_name, default "mechanic-shop-local").

app_name      = "mechanic-shop (Local)"
cluster_name  = "mechanic-shop-local"
namespace     = "mechanic-shop"
workload_name = "mechanic-shop-backend"
environment   = "local"

# health_check_url NAO fica aqui: nao e identidade, e endereco de runtime.
# Informe em terraform.tfvars ou via TF_VAR_health_check_url (o CI usa a
# variable HEALTH_CHECK_URL do repositorio). Um -var-file venceria a variavel
# de ambiente, entao deixar o valor aqui quebraria o monitor no CI.
