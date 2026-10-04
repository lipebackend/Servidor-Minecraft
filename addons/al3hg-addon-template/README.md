# al3hg-addon-template

Modelo para criar novos addons do al3HG. Cada addon = um módulo = um JAR.

## Como copiar

1. Copie a pasta `addons/al3hg-addon-template` para `addons/al3hg-addon-<nome>`.
2. No `pom.xml` do novo módulo, troque o `artifactId` e o `<name>`.
3. Adicione `<module>addons/al3hg-addon-<nome></module>` no `pom.xml` pai.
4. Renomeie o pacote `al3ncar.al3hg.addon.template` para `al3ncar.al3hg.addon.<nome>` (pastas e `package`).
5. Renomeie `TemplateAddon` e atualize `main:` e `name:` no `src/main/resources/plugin.yml`.
   Mantenha `depend: [Al3HG]`.
6. Troque os textos de `Messages` (sempre em português; classes e métodos em inglês).
7. Rode `mvn clean verify` na raiz.

## O que vem pronto

- `TemplateAddon`: `JavaPlugin` sem estado estático; cria um executor próprio e o fecha no `onDisable`.
- `GameEventAdapter`: ÚNICA classe que conhece os eventos de jogo (hoje placeholders do Bukkit). Traduz para `GameEventHandler`.
- `GameEventHandler`: interface sem tipos do Bukkit com os callbacks do jogo (`onGameStart`, `onPlayerEliminated`).
- `ExampleListener`: implementa `GameEventHandler`; é onde fica a lógica do addon (ver TODO abaixo).
- `AsyncService`: roda trabalho em `CompletableFuture` no executor próprio e volta à thread principal
  pelo scheduler (`TemplateAddon#mainThreadExecutor`). Testável sem Bukkit (`AsyncServiceTest`).

## Regras

- API do Bukkit/Paper só na thread principal; I/O só no executor do addon.
- Sem `static` mutável; dependências pelo construtor.
- Regras puras (sem Bukkit) em classes próprias, testadas com JUnit 5.

## Pendente até a `al3hg-api` existir (fase 1)

- Adicionar a dependência `al3hg-api` (scope `provided`) no `pom.xml` (há um bloco comentado).
- Em `GameEventAdapter` (só nele), trocar os eventos placeholder pelos reais:
  `ServerLoadEvent` → `HgGameStartEvent` e `PlayerDeathEvent` → `HgPlayerEliminatedEvent`.
  `ExampleListener` e o resto do addon não mudam.
- Ler o estado da partida via `HgGame` (`Bukkit.getServicesManager().load(HgGame.class)`).

## Bibliotecas de runtime: `libraries:` no plugin.yml (sem shade/relocate)

Se o addon precisar de uma biblioteca Maven (ex.: JDBC), **não** use shade/relocate: declare-a em
`libraries:` no `plugin.yml` (o Paper baixa do Maven Central ao carregar) e deixe a dependência no pom
como `provided`. O `plugin.yml` deste template traz um exemplo comentado
(`org.xerial:sqlite-jdbc:3.47.1.0`, usado pelo `al3hg-addon-stats`). Mantenha a versão igual à property
`sqlite-jdbc.version` do pom pai. O template em si não precisa de nenhuma biblioteca.
