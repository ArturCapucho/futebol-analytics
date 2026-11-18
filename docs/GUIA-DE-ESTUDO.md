# Guia de estudo: futebol-analytics

> **Versão mais completa:** [guia-de-estudo.html](guia-de-estudo.html), que também está publicada
> como página para ler no celular. Ela tem a seção "Comece aqui", que explica de onde vêm os dados,
> o que acontece quando a aplicação liga e como o Spring funciona, com diagramas. Comece por ela.

Este guia explica o que foi construído na primeira parte do projeto, por que cada decisão foi
tomada e em que ordem vale a pena ler o código. A ideia é você ler com o projeto aberto ao lado.

## 1. O que a aplicação faz

Ela busca partidas de futebol em uma fonte, guarda em um banco e responde perguntas sobre elas
(classificação, resumo de gols e resultados) por uma API REST. Uma página HTML consome essa API e
mostra os números em tabela e gráficos.

O caminho de um dado, de ponta a ponta:

```
            COLETA                                       CONSULTA

 CSV ou API football-data                      navegador (dashboard)
          │                                             │  GET /classificacao
          ▼                                             ▼
   FonteDePartidas  (porta)                    CompeticaoController
          │                                             │
          ▼                                             ▼
   ColetarPartidas  (caso de uso)              ConsultarClassificacao (caso de uso)
          │                                             │
          ▼                                             ├──► RepositorioDePartidas ──► banco
   RepositorioDePartidas (porta)                        │
          │                                             └──► CalculadoraDeClassificacao
          ▼                                             │
        banco                                           ▼
                                               JSON (LinhaDaClassificacaoResponse)
```

Guarde esta imagem: quase todo o resto do guia é explicar por que existem tantas caixinhas em vez
de um controller que faz tudo.

## 2. Rode antes de ler

Ler código que você já viu funcionando é muito mais fácil.

1. Defina o Java 17 no terminal (uma vez): `setx JAVA_HOME "C:\Users\artur\.jdks\jdk-17.0.21+9"` e abra um terminal novo.
2. Na pasta do projeto: `.\mvnw.cmd spring-boot:run`
3. Abra http://localhost:8080 e veja o dashboard.
4. Abra http://localhost:8080/api/competicoes/DEMO/temporadas/2026/classificacao e veja o JSON cru.
5. Rode os testes: `.\mvnw.cmd test`

O `mvnw` é o Maven Wrapper: um script que baixa a versão certa do Maven sozinho. Por isso o
projeto não exige Maven instalado, só o JDK.

## 3. Ordem de leitura do código

Leia de dentro para fora: primeiro o que não depende de nada, por último o que depende de tudo.
Todos os caminhos abaixo ficam em `src/main/java/com/futebol/futebol_analytics/`.

### Passo 1: `dominio/` (os conceitos do futebol)

| Arquivo | O que observar |
|---|---|
| [Time.java](../src/main/java/com/futebol/futebol_analytics/dominio/Time.java) | o menor record do projeto; o construtor compacto valida e normaliza o nome |
| [Placar.java](../src/main/java/com/futebol/futebol_analytics/dominio/Placar.java) | a regra "quem ganhou" mora junto do dado, no método `resultado()` |
| [Competicao.java](../src/main/java/com/futebol/futebol_analytics/dominio/Competicao.java) | código + temporada; normaliza para maiúsculas |
| [Partida.java](../src/main/java/com/futebol/futebol_analytics/dominio/Partida.java) | as invariantes: encerrada exige placar, time não joga contra si mesmo |

Repare que nenhum arquivo aqui importa Spring, JPA ou qualquer biblioteca. É Java puro.

### Passo 2: `estatistica/` (os cálculos)

| Arquivo | O que observar |
|---|---|
| [LinhaDaClassificacao.java](../src/main/java/com/futebol/futebol_analytics/estatistica/LinhaDaClassificacao.java) | `comJogo(...)` devolve uma linha nova em vez de alterar a atual |
| [CalculadoraDeClassificacao.java](../src/main/java/com/futebol/futebol_analytics/estatistica/CalculadoraDeClassificacao.java) | o critério de desempate é um `Comparator` recebido no construtor |
| [CalculadoraDeResumo.java](../src/main/java/com/futebol/futebol_analytics/estatistica/CalculadoraDeResumo.java) | um laço simples; ignora partidas não encerradas |

São funções puras: recebem uma coleção de partidas e devolvem um resultado, sem tocar em banco
nem em rede. Por isso os testes delas são tão curtos.

### Passo 3: `aplicacao/` (o que o sistema sabe fazer)

| Arquivo | O que observar |
|---|---|
| [porta/FonteDePartidas.java](../src/main/java/com/futebol/futebol_analytics/aplicacao/porta/FonteDePartidas.java) | interface de um método: "me dê as partidas desta competição" |
| [porta/RepositorioDePartidas.java](../src/main/java/com/futebol/futebol_analytics/aplicacao/porta/RepositorioDePartidas.java) | interface de dois métodos: salvar e buscar |
| [ColetarPartidas.java](../src/main/java/com/futebol/futebol_analytics/aplicacao/ColetarPartidas.java) | três linhas úteis: busca na fonte, salva no repositório, devolve a contagem |
| [ConsultarClassificacao.java](../src/main/java/com/futebol/futebol_analytics/aplicacao/ConsultarClassificacao.java) | busca no repositório e entrega à calculadora |

Este é o ponto central do projeto. `ColetarPartidas` não sabe se as partidas vêm de um CSV ou de
uma API, nem se vão para PostgreSQL ou para uma lista em memória. Ele só conhece as interfaces.

### Passo 4: `infraestrutura/` (o mundo real)

| Arquivo | O que observar |
|---|---|
| [coleta/FonteArquivoCsv.java](../src/main/java/com/futebol/futebol_analytics/infraestrutura/coleta/FonteArquivoCsv.java) | implementa `FonteDePartidas` lendo um arquivo; erros viram `FalhaNaColetaException` com o número da linha |
| [coleta/FonteFootballData.java](../src/main/java/com/futebol/futebol_analytics/infraestrutura/coleta/FonteFootballData.java) | implementa a mesma interface chamando uma API HTTP; os records internos espelham o JSON em inglês |
| [coleta/FonteDePartidasConfig.java](../src/main/java/com/futebol/futebol_analytics/infraestrutura/coleta/FonteDePartidasConfig.java) | escolhe qual das duas fontes existe, conforme `futebol.coleta.fonte` |
| [persistencia/PartidaEntity.java](../src/main/java/com/futebol/futebol_analytics/infraestrutura/persistencia/PartidaEntity.java) | a classe que o JPA mapeia para a tabela; converte de e para `Partida` |
| [persistencia/RepositorioDePartidasJpa.java](../src/main/java/com/futebol/futebol_analytics/infraestrutura/persistencia/RepositorioDePartidasJpa.java) | implementa `RepositorioDePartidas`; veja como `salvarTodas` atualiza em vez de duplicar |
| [config/CasosDeUsoConfig.java](../src/main/java/com/futebol/futebol_analytics/infraestrutura/config/CasosDeUsoConfig.java) | onde os casos de uso são criados e recebem os adaptadores |
| [config/ColetaProperties.java](../src/main/java/com/futebol/futebol_analytics/infraestrutura/config/ColetaProperties.java) | as chaves `futebol.coleta.*` do `application.properties` viram um record tipado |

### Passo 5: `api/` (a porta de entrada HTTP)

| Arquivo | O que observar |
|---|---|
| [CompeticaoController.java](../src/main/java/com/futebol/futebol_analytics/api/CompeticaoController.java) | cada método tem uma ou duas linhas: monta a `Competicao`, chama o caso de uso, converte a resposta |
| [LinhaDaClassificacaoResponse.java](../src/main/java/com/futebol/futebol_analytics/api/LinhaDaClassificacaoResponse.java) | o formato do JSON, separado do objeto de domínio |
| [TratadorDeErros.java](../src/main/java/com/futebol/futebol_analytics/api/TratadorDeErros.java) | transforma exceções em respostas HTTP 400 e 502 |

### Passo 6: recursos e testes

- [application.properties](../src/main/resources/application.properties): toda a configuração, comentada.
- [V1__criar_tabela_partida.sql](../src/main/resources/db/migration/V1__criar_tabela_partida.sql): a tabela.
- [index.html](../src/main/resources/static/index.html): o dashboard, um arquivo só.
- `src/test/java/...`: um teste por classe importante. Comece por
  [PartidaTest](../src/test/java/com/futebol/futebol_analytics/dominio/PartidaTest.java) e
  [CalculadoraDeClassificacaoTest](../src/test/java/com/futebol/futebol_analytics/estatistica/CalculadoraDeClassificacaoTest.java).

## 4. Os conceitos, um por um

### 4.1 Records e validação no construtor

Um `record` é uma classe imutável cujos campos são declarados no cabeçalho. O Java gera
construtor, `equals`, `hashCode` e `toString`.

O "construtor compacto" (sem parênteses) roda antes de os campos serem atribuídos:

```java
public record Placar(int golsMandante, int golsVisitante) {
    public Placar {
        if (golsMandante < 0 || golsVisitante < 0) {
            throw new IllegalArgumentException("gols não podem ser negativos");
        }
    }
}
```

A consequência prática: **não existe `Placar` inválido em lugar nenhum do sistema**. Quem recebe
um `Placar` não precisa checar de novo. O mesmo vale para `Partida`: se o objeto existe, as regras
valem. Esse é o motivo de validar no construtor e não em um método `validar()` que alguém pode
esquecer de chamar.

### 4.2 Imutabilidade

Nenhum objeto de domínio muda depois de criado. Veja `LinhaDaClassificacao.comJogo`: em vez de
somar nos campos, ele cria outra linha. Parece desperdício, mas elimina uma classe inteira de
bugs (dois lugares alterando o mesmo objeto) e torna o objeto seguro para uso em várias threads.

### 4.3 Arquitetura hexagonal (portas e adaptadores)

A regra é uma só: **as dependências apontam para dentro**.

```
api ─────────────┐
                 ▼
infraestrutura ─► aplicacao ─► estatistica ─► dominio
```

- **Porta**: uma interface definida por quem precisa dela. `aplicacao` precisa de partidas, então
  `aplicacao` declara `FonteDePartidas`.
- **Adaptador**: uma implementação da porta para uma tecnologia. `FonteArquivoCsv` e
  `FonteFootballData` são dois adaptadores da mesma porta.

O ganho concreto aparece nos testes: `ColetarPartidasTest` testa a coleta inteira usando uma
lambda como fonte e uma lista como repositório, sem banco, sem rede e sem Spring.

O custo também é real: há mais arquivos e mais conversões (`PartidaEntity` ↔ `Partida` ↔
`PartidaResponse`). Em um CRUD pequeno isso seria exagero. Aqui vale porque o projeto vai ganhar
novas fontes, IA e Kafka, e cada um entra como um adaptador novo sem mexer no miolo.

### 4.4 Injeção de dependência e raiz de composição

Nenhuma classe cria as próprias dependências com `new`. Todas recebem pelo construtor:

```java
public ColetarPartidas(FonteDePartidas fonte, RepositorioDePartidas repositorio) { ... }
```

Alguém precisa montar tudo. Esse lugar é `CasosDeUsoConfig`: cada método `@Bean` cria um caso de
uso e o Spring fornece os parâmetros. Repare que os casos de uso não têm `@Service` nem nenhuma
outra anotação: eles não sabem que o Spring existe. Só a classe de configuração sabe.

### 4.5 SOLID, com o exemplo de cada letra no código

| Letra | Princípio | Onde ver |
|---|---|---|
| S | Uma classe, um motivo para mudar | `CompeticaoController` só muda se o HTTP mudar; `CalculadoraDeClassificacao` só muda se a regra da tabela mudar |
| O | Aberto para extensão, fechado para alteração | fonte nova = classe nova que implementa `FonteDePartidas`; nenhum caso de uso é editado |
| L | Implementações são intercambiáveis | os testes trocam a fonte real por uma lambda e tudo continua funcionando |
| I | Interfaces pequenas | `FonteDePartidas` tem um método; quem só lê não é obrigado a implementar escrita |
| D | Depender de abstrações | `aplicacao` define a interface, `infraestrutura` implementa: a seta de dependência é invertida |

A letra D é a que sustenta a arquitetura. Sem ela, `ColetarPartidas` importaria
`RepositorioDePartidasJpa` e a aplicação dependeria do banco.

### 4.6 Três classes para "a mesma coisa"

Existe `Partida` (domínio), `PartidaEntity` (banco) e `PartidaResponse` (JSON). Não é repetição:
cada uma muda por um motivo diferente.

- Renomear uma coluna no banco altera só `PartidaEntity`.
- Mudar o formato do JSON altera só `PartidaResponse`, e quem consome a API não quebra quando o
  domínio muda por dentro.
- `Partida` fica livre de anotações do JPA e pode ser um record imutável (entidades JPA precisam
  de construtor vazio e campos mutáveis).

### 4.7 Flyway e `ddl-auto=validate`

O Hibernate consegue criar tabelas sozinho, mas isso não serve para produção: você não controla
o que ele faz nem tem histórico. Aqui a divisão é:

- **Flyway** cria e evolui o esquema, executando os arquivos de `db/migration` em ordem
  (`V1__...`, depois `V2__...`). Ele registra no banco o que já rodou.
- **Hibernate** só confere (`validate`) se as entidades batem com as tabelas, e a aplicação não
  sobe se não baterem.

Regra de ouro: migração já aplicada nunca é editada. Para mudar a tabela, cria-se `V2__...`.

### 4.8 Idempotência da coleta

Coletar duas vezes não pode duplicar partidas. A solução tem duas partes:

1. Toda partida tem `idExterno`, o identificador dela na fonte, com restrição `unique` no banco.
2. `RepositorioDePartidasJpa.salvarTodas` busca as que já existem por `idExterno`; as existentes
   são atualizadas (o placar pode ter saído), as novas são inseridas.

Isso permite repetir a coleta à vontade, por agenda ou por botão.

### 4.9 Configuração fora do código

Nada que varia entre ambientes está fixo no código. Três mecanismos aparecem:

- **`@ConfigurationProperties`**: `ColetaProperties` recebe as chaves `futebol.coleta.*` já
  convertidas e tipadas.
- **Variáveis de ambiente**: o Spring converte `SPRING_DATASOURCE_URL` em `spring.datasource.url`.
  É assim que o mesmo jar usa H2 na sua máquina e PostgreSQL no Docker.
- **`@ConditionalOnProperty`**: o bean só existe se a propriedade tiver certo valor. É como a
  fonte CSV e a fonte da API convivem sem `if` espalhado.

O token da API entra por `${FOOTBALL_DATA_TOKEN:}` e nunca é escrito em arquivo do repositório.

### 4.10 Tratamento de erros

O controller não tem `try/catch`. As exceções sobem e `TratadorDeErros`
(`@RestControllerAdvice`) decide a resposta:

- `IllegalArgumentException` (dado inválido vindo do cliente) → 400
- `FalhaNaColetaException` (a fonte externa falhou) → 502

O corpo segue o padrão `application/problem+json` (RFC 9457), que o Spring oferece com
`ProblemDetail`.

Repare também em `FonteFootballData`: ela captura a exceção da biblioteca HTTP e relança como
`FalhaNaColetaException`. Assim a camada de aplicação nunca vê um erro específico de tecnologia.

### 4.11 Os três níveis de teste

| Nível | Exemplo | Sobe o Spring? | Velocidade |
|---|---|---|---|
| Unitário puro | `PartidaTest`, `CalculadoraDeClassificacaoTest` | não | milissegundos |
| Controller isolado | `CompeticaoControllerTest` (MockMvc `standaloneSetup`) | não, só a camada web | rápido |
| Integração | `FutebolAnalyticsApplicationTests` (`@SpringBootTest`) | sim, tudo: Flyway, JPA, coleta | segundos |

A maior parte dos testes está no nível mais barato, e isso só é possível por causa da arquitetura.

Outra escolha: os testes usam **fakes** escritos à mão (`RepositorioEmMemoria`, em
[Partidas.java](../src/test/java/com/futebol/futebol_analytics/Partidas.java)) em vez de mocks do
Mockito. Um fake é uma implementação simples e real da interface; o teste verifica o resultado
("ficou uma partida no repositório") e não quais métodos foram chamados.

## 5. Parte 2: desempenho e previsão de resultados

A segunda etapa acrescentou três coisas: campanha em casa e fora, últimos resultados e um modelo
que prevê as partidas agendadas e é avaliado de forma honesta.

### 5.1 O que entrou e onde

| Pacote | Arquivo | Papel |
|---|---|---|
| `estatistica` | [CalculadoraDeDesempenho.java](../src/main/java/com/futebol/futebol_analytics/estatistica/CalculadoraDeDesempenho.java) | campanha em casa, fora e os últimos 5 jogos de cada time |
| `previsao` | [Previsao.java](../src/main/java/com/futebol/futebol_analytics/previsao/Previsao.java) | as chances de cada resultado, gols esperados e placar mais provável |
| `previsao` | [TreinadorDeModelo.java](../src/main/java/com/futebol/futebol_analytics/previsao/TreinadorDeModelo.java) e [ModeloDePrevisao.java](../src/main/java/com/futebol/futebol_analytics/previsao/ModeloDePrevisao.java) | as duas interfaces: aprender com o histórico e prever |
| `previsao` | [TreinadorPoisson.java](../src/main/java/com/futebol/futebol_analytics/previsao/TreinadorPoisson.java) e [ModeloPoisson.java](../src/main/java/com/futebol/futebol_analytics/previsao/ModeloPoisson.java) | a implementação estatística |
| `previsao` | [AvaliadorDeModelo.java](../src/main/java/com/futebol/futebol_analytics/previsao/AvaliadorDeModelo.java) | mede quanto o modelo acerta (backtest) |
| `aplicacao` | `ConsultarDesempenho`, `PreverPartidas`, `AvaliarModelo` | os três casos de uso novos |
| `api` | [PrevisaoController.java](../src/main/java/com/futebol/futebol_analytics/api/PrevisaoController.java) | `GET /previsoes` e `GET /previsoes/avaliacao` |

O pacote `previsao`, assim como `estatistica`, é Java puro: nenhuma linha de Spring. A arquitetura
da parte 1 recebeu tudo isso sem que nenhuma classe existente precisasse ser reescrita; só a
`CasosDeUsoConfig` e o controller ganharam linhas novas.

### 5.2 A ideia do modelo de Poisson, sem fórmula assustadora

Gols são eventos raros e mais ou menos independentes ao longo de 90 minutos. Eventos assim seguem
bem a **distribuição de Poisson**: dada uma média esperada λ (lambda), ela diz a chance de sair
0, 1, 2, 3... gols. Com λ = 1,5, por exemplo, sair 1 gol é o mais provável (33%), seguido de 2 gols (25%)
e 0 gols (22%).

O modelo só precisa, então, de um λ para cada time em cada jogo:

```
λ do mandante  = média de gols dos mandantes na liga × ataque em casa do mandante × defesa fora do visitante
λ do visitante = média de gols dos visitantes na liga × ataque fora do visitante × defesa em casa do mandante
```

"Ataque" e "defesa" são números relativos à média: ataque 1,3 significa marcar 30% acima da média;
defesa 0,8 significa sofrer 20% menos que a média. Mandante e visitante têm forças separadas porque
jogar em casa faz diferença no futebol (o gráfico "casa e fora" do dashboard mostra isso por time).

Com os dois λ, o código calcula a chance de cada placar de 0 x 0 a 10 x 10 multiplicando as
probabilidades dos dois lados, e soma: todos os placares em que o mandante fez mais gols viram a
chance de vitória do mandante, e assim por diante. Está tudo em `ModeloPoisson.prever`, em
umas 30 linhas.

Uma curiosidade que o próprio dashboard mostra: em "Porto Real x Serrano", o placar mais provável é
1 x 1, mas o resultado mais provável é vitória do mandante. Não é contradição: 1 x 1 é o placar
**individual** mais provável, mas a soma de 1 x 0, 2 x 0, 2 x 1, 3 x 1... supera a soma dos empates.

### 5.3 Regularização: não acreditar demais em poucos jogos

Se um time jogou uma vez em casa e fez 6 x 0, a conta crua diz que o ataque dele é quatro vezes a
média. Ninguém acredita nisso depois de um jogo só. A solução foi dar a cada time **dois jogos
imaginários exatamente na média** da liga:

```
taxa = (gols reais + 2 × média) / (jogos reais + 2)
```

Com 1 jogo, os imaginários pesam muito e puxam o número para a média. Com 30 jogos, quase não
pesam. Esse "puxar para a média quando há pouco dado" é a ideia de **regularização**, presente em
praticamente todo modelo de machine learning, das regressões às redes neurais.

O teste `TreinadorPoissonTest.regularizacaoSegura` tem uma história: na primeira versão ele falhou.
O modelo estava certo e o teste estava errado. Ele comparava o ataque do time contra um adversário
real, cuja defesa também era puxada para a média, e os dois efeitos se misturavam. A correção foi
usar um adversário sem jogos (força neutra). Lição: quando um teste falha, primeiro descubra quem
está errado, o código ou o teste.

### 5.4 Treinar e prever são coisas separadas

`TreinadorDeModelo.treinar(historico)` devolve um `ModeloDePrevisao`, que por sua vez sabe
`prever(partida)`. Por que não um método só?

- A avaliação precisa treinar **muitas vezes**, cada vez com um pedaço diferente do passado.
- Um modelo treinado poderia ser guardado e reutilizado, sem treinar a cada requisição.
- Um modelo novo (regressão logística, rede neural, chamada a um LLM) entra como outra implementação
  de `TreinadorDeModelo`. Trocar o modelo do sistema inteiro é mudar uma linha em
  `CasosDeUsoConfig.treinadorDeModelo()`. É o princípio aberto/fechado (letra O) de novo.

### 5.5 Avaliação honesta: o backtest

Dizer que um modelo "prevê" não vale nada sem medir. O `AvaliadorDeModelo` faz assim:

1. Ordena as partidas encerradas por data.
2. Para cada uma, treina o modelo **só com as partidas anteriores** a ela.
3. Prevê aquela partida e compara com o que aconteceu.

O passo 2 é o mais importante. Treinar com a própria partida que se quer prever é **vazamento de
dados** (data leakage): o modelo "já sabe a resposta", parece ótimo no teste e falha no mundo real.
O teste `AvaliadorDeModeloTest.naoVazaODaFuturo` usa um treinador espião para garantir que cada
treino recebeu só o passado.

São três números:

| Métrica | O que mede | Como ler |
|---|---|---|
| Taxa de acerto | quantas vezes o resultado mais provável aconteceu | sozinha engana, por isso vem a próxima |
| Linha de base | acerto de quem sempre aposta no mandante | o modelo precisa ganhar disto para valer alguma coisa |
| Brier score | o erro das **probabilidades**, não só do palpite | 0 é perfeito; chutar 1/3 para tudo dá 0,667 |

O Brier importa porque premia ser "certo na medida certa": dizer 90% e errar é muito pior que
dizer 45% e errar.

### 5.6 O resultado que parece ruim e é a melhor lição

No campeonato de demonstração, o modelo acertou **29%**, contra **46%** de "sempre o mandante", e o
Brier deu **0,761**, pior que chutar. O motivo: os placares do CSV de demonstração foram gerados
por uma fórmula aritmética, então não existe "time forte" para aprender; é ruído. O modelo tenta
achar padrão onde não há, e a avaliação denuncia.

Se a avaliação não existisse, o dashboard mostraria previsões com cara de precisas e todas
inúteis. É por isso que nenhum modelo vai para produção sem avaliação contra uma linha de base.
O próximo passo natural é rodar com dados reais do football-data.org e comparar.

### 5.7 Exercícios da parte 2

1. Mude os jogos fictícios de 2 para 0 e para 10 em `CasosDeUsoConfig` e veja o que acontece com a
   avaliação e com as previsões. Explique por quê.
2. Escreva um `TreinadorDeModelo` "ingênuo" que ignora os times e sempre dá 45% / 27% / 28%. Ele
   ganha do Poisson no campeonato de demonstração? Isso diz o quê sobre os dados?
3. Acrescente ao `AvaliacaoDoModelo` o Brier da linha de base, para comparar os dois de igual para igual.
4. Faça o modelo dar mais peso aos jogos recentes (dica: multiplique cada jogo por um peso que
   diminui com a idade).

## 6. Docker e Kubernetes

**Atenção: esta parte foi escrita mas nunca executada**, porque a máquina não tinha Docker. Trate
como material a validar; é provável que algum ajuste apareça na primeira execução.

### Dockerfile

Tem dois estágios. O primeiro usa uma imagem com JDK para compilar; o segundo copia só o jar para
uma imagem com JRE. A imagem final fica menor e sem ferramentas de build. O `pom.xml` é copiado
antes do código para o Docker reaproveitar a camada de dependências enquanto o pom não mudar. O
contêiner roda com um usuário sem privilégios.

### docker-compose.yml

Sobe dois contêineres: PostgreSQL e a aplicação. O `depends_on` com `service_healthy` faz a
aplicação esperar o banco aceitar conexões.

### Manifests em `k8s/`

| Recurso | Para que serve |
|---|---|
| Namespace | separa os recursos do projeto dos demais no cluster |
| Secret | usuário e senha do banco |
| ConfigMap | configuração não sensível, injetada como variáveis de ambiente |
| StatefulSet (Postgres) | pod com identidade e disco estáveis, adequado a banco |
| Deployment (app) | mantém 2 réplicas da aplicação e faz atualização gradual |
| Service | endereço estável na frente dos pods |

As três probes do Deployment usam endpoints do Actuator:

- **startup**: dá tempo para a aplicação subir antes de as outras começarem a valer;
- **liveness**: se falhar, o Kubernetes reinicia o contêiner;
- **readiness**: se falhar, o pod sai do balanceamento mas não é reiniciado.

## 7. Decisões que têm um custo

Nenhuma destas é "a certa"; são escolhas que você deve saber defender ou trocar.

- **Time identificado pelo nome.** Simples, mas quebra se a fonte mudar a grafia. O passo seguinte
  seria guardar o id do time na fonte.
- **`Partida.placar()` pode devolver `null`** quando a partida não terminou. O record garante
  quando é nulo, mas quem usa precisa checar `encerrada()` antes.
- **Status gravado como texto** em `PartidaEntity`, convertido à mão. Evita diferenças de
  mapeamento de enum entre H2 e PostgreSQL.
- **H2 no desenvolvimento, PostgreSQL em produção.** Prático, mas os testes não exercitam o banco
  real. Testcontainers resolve isso.
- **Coleta automática desligada no Kubernetes.** Com 2 réplicas, as duas coletariam ao mesmo
  tempo. O correto é um CronJob ou um lock distribuído.
- **Lombok removido.** Com records ele não tinha uso.

## 8. Exercícios da parte 1

Fazer estes exercícios ensina mais do que reler o código.

1. **Nova estatística no resumo.** Adicione "maior goleada" ao `ResumoDaCompeticao`. Comece pelo
   teste em `CalculadoraDeResumoTest`. Você vai tocar em três arquivos; descubra quais.
2. **Novo critério de desempate.** Crie um `Comparator` com confronto direto ou menos derrotas e
   teste passando-o à `CalculadoraDeClassificacao`. Note que a calculadora não é alterada.
3. **Novo endpoint.** `GET .../times/{nome}/partidas` com as partidas de um time. Decida: o filtro
   fica no controller, no caso de uso ou no repositório? Por quê?
4. **Desempenho mandante/visitante.** Uma tabela só com jogos em casa e outra só fora.
5. **Nova migração.** Adicione a coluna `estadio` com um `V2__...sql`, e leve o campo até o JSON.
6. **Nova fonte.** Implemente `FonteDePartidas` lendo um JSON local. Registre em
   `FonteDePartidasConfig`. Confirme que nenhum caso de uso mudou.
7. **Testcontainers.** Escreva um teste de `RepositorioDePartidasJpa` contra PostgreSQL real.
8. **Docker.** Instale o Docker Desktop, rode `docker compose up --build` e corrija o que aparecer.

## 9. Vocabulário rápido

| Termo | Significado aqui |
|---|---|
| Porta | interface que a aplicação define para falar com o mundo externo |
| Adaptador | implementação de uma porta para uma tecnologia específica |
| Caso de uso | uma ação que o sistema oferece, como "coletar partidas" |
| DTO | objeto que só carrega dados entre camadas, como `PartidaResponse` |
| Bean | objeto criado e gerenciado pelo Spring |
| Idempotente | repetir a operação dá o mesmo resultado de fazê-la uma vez |
| Migração | script versionado que altera o esquema do banco |
| Invariante | regra que é sempre verdadeira para um objeto válido |
| Distribuição de Poisson | fórmula que dá a chance de 0, 1, 2... eventos raros, dada a média esperada |
| Lambda (λ) | a média de gols esperada de um time em uma partida |
| Regularização | puxar estimativas para a média quando há poucos dados |
| Backtest | avaliar um modelo prevendo o passado, treinando só com o que veio antes |
| Vazamento de dados | treinar com informação que não estaria disponível na hora de prever |
| Linha de base | a regra mais simples possível, que o modelo precisa superar |
| Brier score | erro médio das probabilidades previstas; 0 é perfeito |
