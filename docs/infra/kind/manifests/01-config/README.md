# Documentacao da Pasta `infra/kind/manifests/01-config`

Esta camada concentra configuracoes e segredos. Ela e aplicada depois dos namespaces e antes dos workloads.

## Arquivos

### `10-app-configmap.yaml`

Cria o `ConfigMap` `mechanic-shop-app-config` no namespace da aplicacao. Ele injeta:

- URL e usuario do PostgreSQL;
- configuracao do Mailpit;
- `APP_BASE_URL`;
- dados dos usuarios seed.

Os valores numericos e documentos foram colocados como string para evitar erros de parse no Kubernetes.

### `11-app-secret.yaml`

Cria o `Secret` `mechanic-shop-app-secret`. Ele guarda:

- senha do banco da aplicacao;
- senha padrao dos usuarios seed;
- campos de SMTP.

Foi modelado com `stringData`, permitindo ao Kubernetes fazer a conversao para base64 internamente.

### `12-postgres-secret.yaml`

Cria o `Secret` `postgres-secret`, consumido pelo pod do PostgreSQL. Ele define:

- nome do banco;
- usuario;
- senha.

Esse segredo desacopla credenciais do manifesto do banco.
