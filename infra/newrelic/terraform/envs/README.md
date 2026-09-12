# Identidade por ambiente

Cada arquivo aqui descreve **um** ambiente monitorado por esta stack. Os valores
de `app_name` / `cluster_name` / `workload_name` sao o contrato com as infras:
sao exatamente os nomes que a aplicacao (agente Java, via `NEW_RELIC_APP_NAME`)
e o `nri-bundle` (via `global.cluster`) reportam. Se divergirem, os paineis de
APM e de Kubernetes ficam vazios mesmo com dados chegando na conta.

## aws-academy: gerado pela propria infra

`infra/aws-academy/observability.tf` escreve `aws-academy.tfvars` a cada
`terraform apply` (recurso `local_file`, desligavel com
`write_observability_tfvars = false`), com o `appName`, o `clusterName` e a URL
de healthcheck reais daquele cluster. Nao edite esse arquivo a mao -- o proximo
apply o sobrescreve. O fluxo e:

```bash
cd infra/aws-academy && terraform apply          # provisiona e publica a identidade
cd ../newrelic/terraform
terraform workspace select -or-create aws-academy
terraform apply -var-file=envs/aws-academy.tfvars
```

## kind / aws

Estes dois nao geram o arquivo automaticamente; os valores estao versionados
aqui e cada infra expoe o bloco equivalente para conferencia:

```bash
cd ../../kind && terraform output -raw observability_tfvars
cd ../aws     && terraform output -raw observability_tfvars
```

As credenciais (`newrelic_account_id`, `newrelic_api_key`, `alert_email`,
`health_check_url`) continuam em `terraform.tfvars`, que nao e comitado.

Use um workspace por ambiente: os recursos (dashboard, politica, monitor) tem
nome derivado de `environment`, mas cada ambiente precisa do seu proprio state.
