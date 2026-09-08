# GERADO por `terraform apply` em infra/aws-academy -- nao edite a mao.
#
# Consumido por infra/newrelic/terraform:
#   terraform apply -var-file=envs/aws-academy.tfvars

app_name      = "mechanic-shop (AWS Academy)"
cluster_name  = "eks-mechanic-shop"
namespace     = "mechanic-shop"
workload_name = "mechanic-shop-backend"
environment   = "lab"

health_check_url = "http://54.165.178.253:30080/actuator/health"
