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
    boolean isPvpEnabled();
    Set<UUID> alivePlayers();
    Set<UUID> spectators();
    boolean isAlive(UUID id);
    World world();                       // mundo da arena
    Optional<UUID> winner();             // só em ENDING
}
// Acesso: Bukkit.getServicesManager().load(HgGame.class)
```

Eventos (todos em `al3ncar.al3hg.api.event`, estendem `Event`):

| Evento | Quando | Cancelável |
|--------|--------|-----------|
| `HgStateChangeEvent(from, to)` | toda transição de estado | não |
| `HgGameStartEvent` | entra em GRACE/RUNNING | não |
| `HgPlayerJoinGameEvent` / `HgPlayerLeaveGameEvent` | entrada e saída (inclui quit) | join: sim |
| `HgPlayerEliminatedEvent(player, killer?)` | jogador sai dos vivos | não |
| `HgPvpEnabledEvent` | fim do grace period | não |
| `HgBorderShrinkEvent(fromSize, toSize, seconds)` | cada fase da border | sim |
| `HgGameEndEvent(winner?)` | ao definir vencedor, antes de enviar ao lobby | não |

Contrato: eventos são disparados sempre na thread principal. Addons só leem estado pela API e reagem a eventos.

