# Arquitetura de plugins — al3HG (Hunger Games, Paper 1.21.11)

Base analisada: https://github.com/dedectr/al3HG (branch main, Java 21, Maven, Paper API 1.21.11-R0.1-SNAPSHOT, AdvancedSlimePaper 4.2.0-SNAPSHOT, BungeeCord messaging).

## 1. Diagnóstico do código atual (lido no fonte)

O al3HG é um plugin único e pequeno (~20 classes). Estado da partida fica em uma instância estática (`Maneger.getPartida()`), e todos os listeners e o comando leem/escrevem nela diretamente. Hoje não existe forma de outro plugin reagir ao jogo sem tocar nas classes internas.

Problemas confirmados no código (precisam ser resolvidos ANTES de plugins novos dependerem disso):

| # | Onde | Problema |
|---|------|----------|
| 1 | `CancelarDano` | Lógica invertida: `if (statusPvp == OFF) return;` ou seja, com PVP desligado nada é cancelado, e com PVP ligado todo dano jogador x jogador é cancelado. |
| 2 | `JoinManeger` | A cada join chama `Barreiras.Diminuir("hgmapa", 100)` e `mobs.ControlerMax()`; adiciona qualquer jogador à partida, mesmo em andamento; força ADVENTURE para todos. |
| 3 | `Barreiras` | `Bukkit.getWorld(mapa)` sem null-check (NPE) e `setSize(tamanho)` instantâneo, sem tempo de transição. |
| 4 | `HgCore` | `start`/`stop`/`rest` chamam `Bukkit.getServer().shutdown()` imediatamente, antes do BungeeCord enviar os jogadores ao lobby. Usa `abs.MEIO` por uma variável `null` (funciona só porque enum é estático). Help e README divergem do código (`fs` x `start`). |
| 5 | `RemoverDaPartidaDead` | `setGameMode(SPECTATOR)` dentro de `PlayerDeathEvent` não persiste; todo respawn manda o jogador ao lobby; ao sobrar 1 vivo só muda o status, sem anunciar vencedor nem encerrar a partida. |
| 6 | `Partida` | O conjunto de jogadores nunca é limpo entre partidas; o estado nunca volta para `INCIOS`; não há tratamento de `PlayerQuitEvent`. |
| 7 | `al3hg.onEnable` | `RegeneretorWorld.limparMapa` apaga a pasta do mundo no disco a cada start; `onDisable` está vazio (tasks não são canceladas). |
| 8 | Docs/build | README cita jar 1.2.1 e `StatsPartida`; pom está em 1.3.0 e o enum se chama `StatusPartida`. `plugin.yml` declara "comandos" com espaço (`hgc fs`), o que o Bukkit ignora. Shade plugin sem uso. |

## 2. Decisões

- Manter Maven e Paper API 1.21.11 (alinhado com o servidor e o AdvancedSlimePaper). Não migrar para Gradle agora.
- **Decisão revisada (Java):** a decisão original era "manter Java 21". O AdvancedSlimePaper 4.2.0-SNAPSHOT é bytecode class 69 (Java 25), então o servidor provavelmente exige **Java 25** (a confirmar: subir o JAR do ASP 1.21.11 em Java 21 e em Java 25, item 1 de `docs/testes/roteiro-manual.md`). O plugin continua gerando bytecode Java 21, mas é compilado com **JDK 25**; mantém-se `source`/`target` 21: com `--release 21` o javac não lê o JAR do ASP (class 69), como explica o comentário do `pom.xml`. Até a confirmação, o servidor de teste deve rodar Java 25. Detalhes e risco em aberto em `docs/pesquisa/paper-asp-mockbukkit.md`.
- Manter `plugin.yml` neste momento. Migrar para `paper-plugin.yml` + Brigadier só junto com a fase 3 (a migração mexe em carregamento de dependências e comandos).
- Em vez de um plugin-biblioteca separado, o próprio **al3HG vira o plugin-base** e publica um módulo **`al3hg-api`** (interfaces + eventos). Os addons usam a API com escopo `provided` e declaram `depend: [Al3HG]`. Assim só existe uma cópia do código compartilhado no servidor, sem divergência de versão e sem relocate.
- Eventos próprios do jogo (Bukkit `Event`) são o canal principal entre o al3HG e os addons. Serviços (`ServicesManager`) cobrem consultas.

