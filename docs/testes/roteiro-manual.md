# Roteiro de teste manual — al3HG fase 1

Cobre o que os testes JUnit (MockBukkit + arena falsa) **não** conseguem verificar: AdvancedSlimePaper real,
BungeeCord/lobby real, border animada, comportamento de gamemode/respawn no cliente e o log do servidor.
Referência: critérios de pronto da seção 8 de `docs/arquitetura/03-estados-regras-e-plano.md`.

## Pré-requisitos

- Servidor **AdvancedSlimePaper 4.2.0 (Paper 1.21.11)** com **Java 25** (ver `README.md`); BungeeCord/Velocity com um servidor `lobby`.
- `al3hg-2.0.0-SNAPSHOT.jar` em `plugins/`, mapa-modelo em `slime_worlds/hgmapa.slime`.
- Pelo menos **3 contas** (A, B, C) e um operador. `config.yml` de teste sugerido: `countdown-seconds: 10`, `grace-seconds: 30`,
  `border.shrink-seconds: 60`, `ending.delay-seconds: 5`.
- Deixe `logs/latest.log` aberto (`tail -f`) durante todo o roteiro.

## Roteiro

Marque ✅/❌ e anote o horário e o trecho do log em caso de falha.

| # | Passo | Resultado esperado | Critério |
|---|-------|--------------------|----------|
| 1 | Subir o servidor com o plugin | Sem exceções; `HgGame` registrado; sem aviso de ASP ausente | §8-1 |
| 2 | A, B, C entram (estado WAITING) | Modo ADVENTURE e mensagem de boas-vindas; border do mundo principal **não** muda a cada join (bug #9) | #9 |
| 3 | `/hgc start` com só 1 jogador online | "Jogadores insuficientes"; continua WAITING | — |
| 4 | `/hgc start` com 3 jogadores | Contagem 10s com avisos; todos teleportados à arena, vida/fome cheias e inventário limpo | — |
| 5 | Durante a contagem, B sai e volta | Ao voltar entra na arena (COUNTDOWN) sem duplicar na lista de vivos | — |
| 6 | Fim da contagem | Todos em SURVIVAL, border em 300; mensagem "PVP liberado em N s" | — |
| 7 | Na graça, A tenta bater em B | Dano **cancelado** (bug #8: lógica invertida) | #8 |
| 8 | Fim da graça | "PVP LIBERADO!"; A consegue bater em B; border começa a encolher (final 20 em 60 s) | #8 |
| 9 | Um jogador novo (D) entra com a partida em andamento | Vai para a arena como SPECTATOR; border continua encolhendo (não reseta) | #9 |
| 10 | C morre (PvP ou queda) e clica "Respawn" | Não vai ao lobby; respawna na arena e fica SPECTATOR de forma estável (bug #12); jogadores vivos −1 no chat | #12 |
| 11 | Reiniciar o cenário; B fecha o jogo no meio da partida | B sai dos vivos, o contador cai, evento de saída disparado (sem exceção) | §8-4, #13 |
| 12 | Sobra um vivo | Anuncia o vencedor; nos 5 s de ENDING o vencedor **não toma dano** (invulnerável); depois todos vão ao lobby | §8-3 |
| 13 | Observar o log durante o passo 12 | Ordem: vencedor → envio ao lobby → arena descartada → WAITING. Servidor **não** desliga (a menos que `shutdown-after-end: true`) | #11 |
| 14 | Após voltar ao WAITING, jogadores retornam ao servidor | Estão em ADVENTURE, partida nova pode ser iniciada sem reiniciar o servidor | — |
| 15 | `/hgc fs` com ninguém online (pelo console) | Passa por COUNTDOWN e vai direto a ENDING, sem "Que comecem os jogos" e sem HgGameStartEvent | — |
| 16 | `/hgc stop` no meio da partida | Todos enviados ao lobby **antes** de a arena sumir; sem `shutdown()` imediato | #11 |
| 17 | `/hgc rest` com `shutdown-on-rest: true` | Servidor só desliga depois do envio ao lobby e do descarte da arena | #11 |
| 18 | `/hgc reload` | Config relida sem exceção | — |
| 19 | Remover/renomear o `.slime` e usar `/hgc start` | Mensagem "arena indisponível", log com a causa, estado continua WAITING | — |
| 20 | Sopa de cogumelo (MUSHROOM_STEW) em WAITING e em GRACE/RUNNING | Só cura em GRACE/RUNNING | — |
| 21 | `/stop` do servidor no meio de uma partida | `onDisable` limpo: tasks canceladas, mundos da arena descartados, sem exceção | §8-3 |

## Addon de exemplo (critério §8-2)

1. Colocar um addon com `depend: [Al3HG]` em `plugins/`.
2. Verificar que ele recebe `HgGameStartEvent` e `HgPlayerEliminatedEvent` e lê `HgGame` (via `ServicesManager`) **sem** importar classes de `al3ncar.al3hg.game`.

## O que já é coberto por testes automáticos (`mvn clean verify`)

Transições da máquina de estados, contagem de vivos, quit/morte, vencedor, sequência de eventos, ordem do ENDING
(lobby → dispose), cancelamento de dano (PVP e ENDING), border no join e mundo nulo. Veja `al3hg-plugin/src/test`.
