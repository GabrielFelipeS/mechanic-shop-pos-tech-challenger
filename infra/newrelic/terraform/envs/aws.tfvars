# infra/aws -- EKS provisionado pelo stack "aws".
# Espelha: NEW_RELIC_APP_NAME de manifests/01-config/app-configmap.yaml e
#          aws_eks_cluster.cluster.name = "eks-${var.projectName}".

app_name      = "mechanic-shop (Production)"
cluster_name  = "eks-mechanic-shop-pos-tech-challenger"
namespace     = "mechanic-shop"
workload_name = "mechanic-shop-backend"
environment   = "production"

# health_check_url NAO fica aqui: nao e identidade, e endereco de runtime.
# Informe em terraform.tfvars ou via TF_VAR_health_check_url (o CI usa a
# variable HEALTH_CHECK_URL do repositorio). Um -var-file venceria a variavel
# de ambiente, entao deixar o valor aqui quebraria o monitor no CI.
