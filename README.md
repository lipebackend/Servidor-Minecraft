# al3HG

Plugin de **Hunger Games** para Minecraft **1.21.11** (Paper + AdvancedSlimePaper).

## Visão geral

Jogadores aguardam no lobby; ao `/hgc start` o plugin cria uma **arena nova** a partir de um mapa-modelo
`.slime` (AdvancedSlimePaper), faz a contagem, libera o PVP depois do período de graça, encolhe a border,
anuncia o vencedor, envia todos ao lobby via BungeeCord **e só então descarta a arena**.

## Módulos (Maven multi-módulo, Java 21 de bytecode)

| Módulo | Descrição |
|--------|-----------|
| `al3hg-api` | Interfaces e eventos públicos (`HgGame`, `GameState`, `Hg*Event`). Sem lógica. Addons usam com escopo `provided`. |
| `al3hg-plugin` | O plugin al3HG (artefato `al3hg`). Implementa a API e empacota o `al3hg-api` no JAR. |
| `addons` | Agregador dos addons (cada um = um módulo = um JAR): `al3hg-addon-stats` (estatísticas, `/hgstats`, `/hgtop`) e `al3hg-addon-template` (modelo para novos addons). |

Coordenadas: `al3ncar.al3hg:al3hg-api:2.0.0-SNAPSHOT`.

### Para addons

```xml
<dependency>
  <groupId>al3ncar.al3hg</groupId>
  <artifactId>al3hg-api</artifactId>
  <version>2.0.0-SNAPSHOT</version>
  <scope>provided</scope>
</dependency>
```

No `plugin.yml` do addon: `depend: [Al3HG]`. Consulta: `Bukkit.getServicesManager().load(HgGame.class)`.
Eventos (todos síncronos, thread principal): `HgStateChangeEvent`, `HgGameStartEvent`,
`HgPlayerEliminatedEvent`, `HgGameEndEvent`.

Os poms dos addons herdam de `addons/pom.xml` (que herda de `al3hg-parent`): `paper-api` e `al3hg-api`
ficam `provided`; bibliotecas de runtime (ex.: `sqlite-jdbc`) vão em `libraries:` no `plugin.yml`, sem shade.
Para criar um addon copie `addons/al3hg-addon-template` (ver o README dele).

## Máquina de estados

```
WAITING -> COUNTDOWN -> GRACE -> RUNNING -> ENDING -> WAITING
  (WAITING -> GRACE no início rápido /hgc fs; COUNTDOWN/GRACE/RUNNING -> ENDING ao terminar ou parar)
```

## Arena (AdvancedSlimePaper)

- Mapa-modelo: `<raiz do servidor>/slime_worlds/hgmapa.slime` (configurável em `config.yml`), lido uma vez.
- Cada partida: `template.clone("hg-<matchId>", loader)` + `loadWorld` na thread principal.
- Fim: jogadores são enviados ao lobby, aguarda-se `ending.lobby-transfer-wait-seconds`, quem sobrar vai para um
  mundo de fallback e a arena é descartada com `Bukkit.unloadWorld(nome, false)`.
- `onDisable` descarta qualquer mundo carregado do mesmo jeito.

## Comandos (`hg.admin`)

| Comando | Descrição |
|---------|-----------|
| `/hgc start` | Prepara a arena e inicia com contagem (exige `game.min-players`) |
| `/hgc fs` | Início rápido, sem contagem e sem mínimo de jogadores |
| `/hgc stop` | Para a partida: envia ao lobby e descarta a arena (sem `shutdown()` imediato) |
| `/hgc rest` | Como `stop`; com `ending.shutdown-on-rest: true` desliga o servidor **depois** do envio ao lobby |
| `/hgc reload` | Recarrega `config.yml` e o mapa-modelo |
| `/hgc help` | Ajuda |

## Build

Requer **JDK 25+** para compilar (o `com.infernalsuite.asp:api:4.2.0-SNAPSHOT` é compilado para Java 25),
mas o bytecode gerado é Java 21 (`-source/-target 21`).

```bash
mvn clean package        # na raiz
# JAR: al3hg-plugin/target/al3hg-2.0.0-SNAPSHOT.jar
```

Repositórios usados: `https://repo.papermc.io/repository/maven-public/` e
`https://repo.infernalsuite.com/repository/maven-snapshots/`.

## Documentação adicional

- [task.md](task.md) — lista de tarefas do projeto.

## Autor

Dedectr / al3ncar
