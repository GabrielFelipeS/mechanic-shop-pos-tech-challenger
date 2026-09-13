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
- campos de SMTP;
- `JWT_SECRET`, o segredo HS256 dos tokens, que precisa ser identico ao `secret` do consumer `jwt` em `13-kong-config.yaml`.

Foi modelado com `stringData`, permitindo ao Kubernetes fazer a conversao para base64 internamente.

### `12-postgres-secret.yaml`

Cria o `Secret` `postgres-secret`, consumido pelo pod do PostgreSQL. Ele define:

- nome do banco;
- usuario;
- senha.

Esse segredo desacopla credenciais do manifesto do banco.

### `13-kong-config.yaml`

Cria o `ConfigMap` `kong-declarative-config` com o `kong.yml` que o Kong carrega no boot. E o mesmo conteudo do `kong/kong.yml` usado pelo `docker-compose`, com duas diferencas obrigatorias no cluster:

- o upstream aponta para o `Service` `mechanic-shop-backend` em vez do container `springapp`;
- o `secret` do consumer `jwt` vem de `var.jwt_secret`, o mesmo valor injetado como `JWT_SECRET` no `Secret` da aplicacao.

O Kong nao expande variaveis de ambiente dentro do arquivo declarativo, entao templar o arquivo pelo Terraform e a unica forma de manter os dois lados em sincronia. Um segredo divergente faz o gateway responder `401` a tokens que a API considera validos.