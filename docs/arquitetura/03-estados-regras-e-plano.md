# Arquitetura al3HG — estados, regras e plano

## 5. Máquina de estados da partida

```
WAITING -> COUNTDOWN -> GRACE -> RUNNING -> ENDING -> WAITING
```

- Uma única classe (`GameManager`) controla transições, valida o estado de origem e dispara `HgStateChangeEvent`.
- Todas as tasks (contagem, grace, border, fim) pertencem à partida e são canceladas em `stop`, em `reset` e no `onDisable`.
- ENDING: anuncia vencedor, espera X segundos (configurável), envia ao lobby via BungeeCord e só então reinicia/desliga. Nunca `shutdown()` imediato.
- Reset limpa jogadores, border, mobs, regras de mundo e volta para WAITING.

## 6. Regras de projeto para quem programa

1. API do Bukkit/Paper só na thread principal. I/O (arquivo, SQLite/MySQL) em `CompletableFuture` com executor próprio; voltar para a thread principal com `Bukkit.getScheduler().runTask(...)` antes de tocar em jogador, mundo ou inventário.
2. Sem estado estático global (nada de `static` + `getInts()`); injetar dependências pelo construtor.
3. Configuração em `config.yml` (tempos, border, PVP, loot) e mensagens em arquivo próprio; nenhum número mágico no código.
4. Cada listener faz uma coisa e checa `game.state()` antes de agir.
5. `core` de regras (cálculo de border, fases de loot, pontuação) em classes puras sem Bukkit, para testar com JUnit 5.
6. Commits pequenos, um PR por tarefa, build `mvn clean verify` verde antes de abrir PR.

## 7. Divisão de trabalho sugerida

**Fase 1 — Estabilizar o al3HG (Programador Java).** Criar o pom pai e os módulos `al3hg-api` e `al3hg-plugin`; mover o código atual; renomear classes; corrigir os itens 1 a 8 da seção 1; implementar a máquina de estados e publicar `HgGame` + eventos; `onDisable` limpo; `config.yml`.

**Fase 2 — Addons em paralelo (Programador Java 2), depois que a API da fase 1 estiver mergeada.** Ordem sugerida:
1. `al3hg-addon-template` (classe principal, listener de exemplo, serviço assíncrono com `CompletableFuture`, `plugin.yml` com `depend: [Al3HG]`).
2. `al3hg-addon-stats` (kills/wins em SQLite, ranking, async).
3. `al3hg-addon-feedback` (contagem com title/actionbar, kill feed, scoreboard).
4. `al3hg-addon-loot` (fases early/mid/late, kits).
5. `al3hg-addon-maps` (rotação de mapas com AdvancedSlimePaper).

Enquanto a API não existe, o Programador Java 2 pode escrever as regras puras e testes do módulo de stats (sem depender do Bukkit).

**Pesquisador.** Validar na documentação atual do Paper 1.21.11 e do AdvancedSlimePaper 4.2.0: eventos usados (respawn/spectator, border com tempo), carregamento/descarte de mundos `.slime` em runtime (substitui o `limparMapa` por apagar pasta), e se vale migrar para `paper-plugin.yml`/Brigadier na fase 3.

**Fase 3 — Opcional.** Migrar comandos para Brigadier com tab completion, `/hgc start`, `/hgc setspawn`, e `paper-plugin.yml`.

## 8. Critérios de pronto (fase 1)

- `mvn clean verify` passa na raiz.
- Um addon de exemplo carrega no servidor, recebe `HgGameStartEvent` e `HgPlayerEliminatedEvent` e lê `HgGame` sem acessar classes internas.
- Uma partida completa (entrar, contagem, grace, PVP, border, vencedor, envio ao lobby, reinício) roda sem exceções no log e sem `shutdown()` imediato.
- Jogador que sai no meio da partida é removido dos vivos e dispara o evento de saída.
