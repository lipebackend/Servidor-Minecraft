# Arquitetura al3HG — estrutura e API

## 3. Estrutura de repositório (Maven multi-módulo)

```
al3hg-parent/                  (pom pai: versões, Java 21, plugins Maven)
├── al3hg-api/                 (interfaces, enums, eventos; sem lógica)
├── al3hg-plugin/              (o al3HG atual, refatorado; implementa a API)
├── addons/
│   ├── al3hg-addon-template/  (modelo para copiar)
│   ├── al3hg-addon-stats/     (persistência de kills/wins, async)
│   └── ...                    (um módulo = um JAR = um addon)
└── docs/
```

Regras de dependência (obrigatórias):

- `addons/*` -> `al3hg-api` (provided). Nunca para `al3hg-plugin`.
- `al3hg-plugin` -> `al3hg-api` (compile, e é empacotado no JAR do al3HG).
- `al3hg-api` -> `paper-api` (provided) apenas.
- Nenhum addon acessa `Maneger`, `Partida` ou outras classes internas.

Pacotes: base `al3ncar.al3hg`; API em `al3ncar.al3hg.api`; addons em `al3ncar.al3hg.addon.<nome>`.
Nomes de classes, métodos e variáveis em inglês (ex.: `GameManager`, `Game`, `GameState`); textos mostrados ao jogador em português, centralizados em um único lugar (mensagens/prefixo). Classes antigas serão renomeadas na fase 1 (`Maneger`->`GameManager`, `Partida`->`Game`, `StatusPartida`->`GameState`, `Borreiras`->`BorderController`, `JoinManeger`->`PlayerConnectionListener`).

## 4. A API (`al3hg-api`)

```java
public enum GameState { WAITING, COUNTDOWN, GRACE, RUNNING, ENDING }

public interface HgGame {
    GameState state();
    Optional<String> matchId();          // vazio em WAITING sem arena preparada
    boolean isPvpEnabled();
    Set<UUID> alivePlayers();
    Set<UUID> spectators();
    boolean isAlive(UUID playerId);
    Optional<String> worldName();        // nome do mundo da arena, se houver
    Optional<World> world();             // mundo da arena, resolvido sob demanda (vazio se não carregado)
    Optional<UUID> winner();             // só em ENDING
}
// Acesso: Bukkit.getServicesManager().load(HgGame.class)
```

Eventos da fase 1 (todos em `al3ncar.al3hg.api.event`, estendem `HgEvent`, que estende `Event` e expõe `getMatchId()`). Todos carregam o `matchId` da partida e não são canceláveis:

| Evento | Quando |
|--------|--------|
| `HgStateChangeEvent(matchId, from, to)` | toda transição de estado, já com o novo estado aplicado |
| `HgGameStartEvent(matchId, Set<UUID> players)` | entra em GRACE |
| `HgPlayerEliminatedEvent(matchId, UUID player, @Nullable UUID killer, Reason reason, int remaining)` | jogador sai dos vivos; `Reason{DEATH, QUIT}`; `remaining` = vivos após a eliminação |
| `HgGameEndEvent(matchId, @Nullable UUID winner)` | ao definir vencedor (entra em ENDING), antes de enviar ao lobby; `winner` vazio se não houve vencedor |

### Fase 2 (ainda não implementados)

Planejados, mas ainda não existem em `al3hg-api/`:

| Evento | Quando | Cancelável |
|--------|--------|-----------|
| `HgPlayerJoinGameEvent` / `HgPlayerLeaveGameEvent` | entrada e saída (inclui quit) | join: sim |
| `HgPvpEnabledEvent` | fim do grace period | não |
| `HgBorderShrinkEvent(fromSize, toSize, seconds)` | cada fase da border | sim |

Contrato: eventos são disparados sempre na thread principal. Addons só leem estado pela API e reagem a eventos.

