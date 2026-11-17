# futebol-analytics

Coleta partidas de futebol, guarda em banco e expõe estatísticas por API REST e por um dashboard.
Projeto de estudo: Spring Boot 4, JPA, Flyway, Docker e Kubernetes.

## Requisitos

- JDK 17 ou superior (o Maven vem pelo wrapper `mvnw`, não precisa instalar)
- Docker, só para os passos de contêiner e Kubernetes

## Rodar

```
.\mvnw.cmd spring-boot:run
```

Sem configurar nada, a aplicação sobe com banco H2 em memória e carrega um campeonato
fictício (`DEMO`, temporada 2026) de `src/main/resources/dados/partidas-demo.csv`.

- Dashboard: http://localhost:8080
- Testes: `.\mvnw.cmd test`

## API

Base: `/api/competicoes/{codigo}/temporadas/{temporada}`

| Método e caminho | O que faz |
|---|---|
| `GET /classificacao` | tabela ordenada, com pontos, saldo e aproveitamento |
| `GET /resumo` | partidas encerradas, gols, média e distribuição de resultados |
| `GET /partidas` | partidas em ordem cronológica |
| `POST /coletas` | busca as partidas na fonte e grava (pode repetir: não duplica) |

Erros saem como `application/problem+json`: 400 para parâmetro inválido, 502 quando a fonte falha.

## Dados reais (football-data.org)

Crie um token gratuito em https://www.football-data.org e suba com:

```
$env:FOOTBALL_DATA_TOKEN = "seu-token"
$env:FUTEBOL_COLETA_FONTE = "football-data"
$env:FUTEBOL_COLETA_AO_INICIAR = "false"
.\mvnw.cmd spring-boot:run
```

Depois dispare a coleta, por exemplo do Brasileirão 2026: `POST /api/competicoes/BSA/temporadas/2026/coletas`
(ou use "Coletar agora" no dashboard). O token nunca vai para o repositório: só variável de ambiente.

## Arquitetura

Hexagonal (portas e adaptadores). As dependências apontam sempre para dentro:

```
api ─────────────┐
                 ▼
infraestrutura ─► aplicacao ─► estatistica ─► dominio
```

| Pacote | Responsabilidade | Conhece o Spring? |
|---|---|---|
| `dominio` | `Partida`, `Placar`, `Time`, `Competicao`: records imutáveis, validados no construtor | não |
| `estatistica` | cálculos puros sobre partidas (classificação, resumo) | não |
| `aplicacao` | casos de uso e as portas `FonteDePartidas` e `RepositorioDePartidas` | não |
| `infraestrutura` | adaptadores das portas (JPA, CSV, football-data), configuração, agendamento | sim |
| `api` | controllers e DTOs de resposta | sim |

Onde cada princípio SOLID aparece:

- **S**: cada caso de uso faz uma coisa; controller só traduz HTTP; entidade JPA separada do domínio.
- **O**: fonte de dados nova = novo adaptador de `FonteDePartidas` + um bean em `FonteDePartidasConfig`.
  Critério de desempate novo = outro `Comparator` passado à `CalculadoraDeClassificacao`.
- **L**: qualquer `FonteDePartidas` é intercambiável (os testes usam lambdas no lugar das reais).
- **I**: portas pequenas, com um método (fonte) ou dois (repositório).
- **D**: `aplicacao` define as interfaces; `infraestrutura` implementa. `CasosDeUsoConfig` faz a ligação.

## Docker

```
docker compose up --build
```

Sobe a aplicação com PostgreSQL. O esquema é criado pelo Flyway (`src/main/resources/db/migration`).

## Kubernetes (minikube ou kind)

```
docker build -t futebol-analytics:0.0.1 .
minikube image load futebol-analytics:0.0.1
kubectl apply -k k8s/
kubectl -n futebol port-forward svc/futebol-analytics 8080:80
```

`k8s/` tem Namespace, PostgreSQL (StatefulSet + Secret) e a aplicação (ConfigMap, Deployment com
2 réplicas, probes de startup/liveness/readiness e Service).

## Próximos passos

1. Mais estatísticas: desempenho como mandante/visitante, sequência de resultados, evolução por rodada.
2. IA: porta `AnalistaDePartidas` em `aplicacao`, com adaptador para um LLM que comenta a rodada
   a partir da classificação e do resumo; depois, um modelo de previsão de resultado.
3. Coleta em Kubernetes como `CronJob` (ou ShedLock), para poder agendar com várias réplicas.
4. Testcontainers para testar o repositório contra PostgreSQL de verdade.
5. Kafka: publicar um evento por partida coletada e calcular estatísticas a partir do fluxo.
6. CI (GitHub Actions): testes, build da imagem e deploy.
