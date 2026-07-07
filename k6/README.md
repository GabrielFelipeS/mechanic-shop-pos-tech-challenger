# k6 load tests

Suite `k6` para validar escalabilidade da API com foco em uso real da aplicacao:

- fluxo de leitura geral do sistema (`search` em usuarios, veiculos, catalogo, estoque e ordens de servico)
- ciclo completo de ordem de servico (`create -> quote -> request-approval -> budget-response -> finish -> deliver`)
- cenarios de `baseline`, `stress` e `spike`

## Estrutura

- `script.js`: entrypoint com `setup()` compartilhado e export dos cenarios
- `src/config.js`: configuracao, thresholds e cenarios
- `src/auth.js`: autenticacao e descoberta do mecanico seed
- `src/data-factory.js`: geracao de payloads unicos e validos
- `src/http.js`: wrapper HTTP com validacao padrao
- `src/workflows.js`: jornadas de negocio reutilizaveis

## Pre-requisitos

1. Subir a API e o banco.
2. Garantir que os usuarios seed existam e consigam autenticar.
3. Instalar `k6`.

## Variaveis suportadas

Principais:

```bash
BASE_URL=http://localhost:8080
SEED_PASSWORD=123456
ADMIN_EMAIL=admin@shop.com
RECEPTIONIST_EMAIL=receptionist@shop.com
MECHANIC_EMAIL=mechanic@shop.com
WAREHOUSE_EMAIL=warehouse@shop.com
SCENARIO=all
```

Controle fino dos cenarios:

```bash
BASELINE_TARGET_VUS=10
STRESS_TARGET_1=20
STRESS_TARGET_2=40
STRESS_TARGET_3=60
SPIKE_TARGET=80
```

## Execucao

Rodar tudo:

```bash
k6 run k6/script.js
```

Rodar apenas stress:

```bash
SCENARIO=stress k6 run k6/script.js
```

Rodar apenas spike:

```bash
SCENARIO=spike SPIKE_TARGET=120 k6 run k6/script.js
```

## Observacoes

- O teste cria clientes, veiculos, servicos, itens de estoque e ordens de servico de forma dinamica para evitar colisao entre VUs.
- O fluxo de aprovacao do orcamento autentica com o cliente criado na propria iteracao.
- Como o teste grava bastante dado, o ideal e executar em ambiente isolado ou com limpeza entre rodadas maiores.
