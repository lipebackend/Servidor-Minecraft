# al3HG

Plugin de **Hunger Games** para Minecraft **1.21.11** (Paper/AdvancedSlimePaper).

## Visão Geral

**al3HG** é um plugin que adiciona um modo Hunger Games completo ao servidor: jogadores entram no lobby, são teleportados para uma arena (carregada via schematics/slimes com AdvancedSlimePaper), lutam até sobrar um vencedor, e o plugin cuida de contagem, border, regens, remoção de mortos e envio entre servidores via BungeeCord.

## Tecnologias

| Tecnologia | Descrição |
|------------|-----------|
| **Java 21** | JDK do projeto |
| **Maven** | Build e dependências |
| **Paper API 1.21.11** | API principal do plugin |
| **AdvancedSlimePaper 4.2.0** | Carregamento de mundos/schematics (`.slime`) |
| **BungeeCord Plugin Messaging** | Envio de jogadores entre servidores |

## Funcionalidades

- **Modo Hunger Games completo** — lobby, contagem regressiva, grace period e fim de partida
- **Gerenciamento de partida** — lista de jogadores vivos, status da partida (`INCIOS`, em andamento, etc.)
- **Sopa de cura** — clique direito com sopa de cogumelo cura o jogador (`Regem`)
- **Receitas customizadas** — itens especiais via `Refil.RegisterRecipeMethods()`
- **Border do mundo** — área encolhendo durante a partida (`Borreiras`)
- **Remoção de mortos** — elimina jogadores mortos da contagem (`RemoverDaPartidaDead`)
- **BungeeCord** — envia jogadores para outro servidor ao final (`EnviarServer`)

## Arquitetura

```
src/main/java/al3ncar/al3hg/
├── al3hg.java                 # Classe principal (JavaPlugin)
├── command/
│   └── HgCore.java            # Comando /hgc (fs, stop, rest, help, reload)
├── craft/
│   └── Refil.java             # Registro de receitas customizadas
├── events/
│   ├── JoinManeger.java       # Gerenciamento de entrada no jogo
│   ├── Regem.java             # Evento de cura com sopa
│   └── RemoverDaPartidaDead.java
├── partida/
│   ├── Maneger.java           # Instância única da partida
│   ├── Partida.java           # Estado e jogadores da partida
│   ├── PartidaRolando.java    # Lógica da partida em andamento
│   └── StatsPartida.java      # Enum de estados
└── utils/
    ├── Borreiras.java         # Border encolhendo
    ├── EnviarServer.java      # Mensagens BungeeCord
    └── PrefixoC.java          # Prefixo das mensagens
```

## Comandos

| Comando | Permissão | Descrição |
|---------|-----------|-----------|
| `/hgc` | `hg.admin` | Comando principal (mostra ajuda) |
| `/hgc fs` | `hg.admin` | Força o início rápido da partida |
| `/hgc stop` | `hg.admin` | Para a partida e volta todos ao lobby |
| `/hgc rest` | `hg.admin` | Reinicia a partida |
| `/hgc reload` | `hg.admin` | Recarrega a configuração |
| `/hgc help` | `hg.admin` | Ajuda dos comandos |

## Permissões

- `hg.admin` — acesso total aos comandos do plugin

## Build

```bash
mvn clean package
```

O JAR será gerado em `al3hg-plugin/target/al3hg-2.0.0-SNAPSHOT.jar`.

## Tarefas

Veja [task.md](task.md) para a lista completa de tarefas do projeto.

## Autor

Dedectr / al3ncar
