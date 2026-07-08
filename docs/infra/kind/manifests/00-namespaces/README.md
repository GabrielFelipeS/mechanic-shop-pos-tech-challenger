# Documentacao da Pasta `infra/kind/manifests/00-namespaces`

Esta camada e aplicada primeiro e existe para criar namespaces base antes de qualquer configuracao ou workload.

## Arquivos

### `00-mechanic-shop.yaml`

Cria o namespace principal da aplicacao com nome vindo da variavel `${namespace}`. Todo o restante da stack de app local, como API, PostgreSQL e Mailpit, depende da existencia desse namespace.
