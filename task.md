# 📋 Tasks — al3HG (Hunger Games 1.21.11)

> Plugin de Hunger Games para Paper 1.21.11 com AdvancedSlimePaper.

## ✅ Legenda

- [ ] Pendente
- [x] Concluído
- 🔴 Alta prioridade · 🟡 Média · 🟢 Baixa

---

## 🔴 Prioridade Alta

- [x] **Core estável** — revisar `al3hg.java`: garantir `onDisable` limpo (parar tasks, salvar dados)
- [x] **Ciclo de vida da partida** — validar transições de `StatsPartida` em `Partida`/`Maneger`/`PartidaRolando`
- [x] **Remoção de dead** — revisar `RemoverDaPartidaDead` (espectador, drops, kill credit)
- [ ] **Grace period** — não permitir dano nos primeiros X segundos
- [ ] **Border** — revisar `Borreiras`: dano fora da área, encolhimento progressivo
- [x] **Build final** — garantir `mvn clean package` sem erros no Paper 1.21.11

## 🟡 Prioridade Média

### Partida
- [ ] Contagem regressiva com title/actionbar
- [ ] Fases de loot (early/mid/late)
- [ ] Sistema de vitória e premiação
- [ ] Kill feed no chat

### Comandos
- [ ] Finalizar `/hgc` (help, fs, stop, rest, reload)
- [ ] Adicionar `/hgc start`, `/hgc setspawn`
- [ ] Tab completers

### Itens / Receitas
- [ ] Revisar `Refil.RegisterRecipeMethods()`
- [ ] Kits/armas customizadas por cargo

### Mundo / Mapas
- [ ] Integração com AdvancedSlimePaper (`.slime`)
- [ ] Rotação de mapas
- [ ] Spawnpoints balanceados

### Stats
- [ ] Persistência de stats (kills, wins) em JSON/SQLite
- [ ] Placar/ranking

## 🟢 Prioridade Baixa

- [ ] Anti-cheat básico (fly, speed, reach)
- [ ] `config.yml` com valores ajustáveis (tempo, border, loot)
- [ ] Mensagens/prefixo centralizados (`PrefixoC`)
- [ ] Integração BungeeCord completa (`EnviarServer` + fila/lobby)
- [ ] Atualizar README com prints e instruções de instalação
- [ ] Testes em servidor real 1.21.11

---

## 📁 Arquivos-chave do projeto

| Arquivo | Responsabilidade |
|---------|------------------|
| `al3hg.java` | Classe principal |
| `command/HgCore.java` | Comandos `/hgc` |
| `partida/Maneger.java` | Instância da partida |
| `partida/Partida.java` | Jogadores e status |
| `partida/PartidaRolando.java` | Lógica da partida |
| `partida/StatsPartida.java` | Estados (enum) |
| `events/Regem.java` | Cura com sopa |
| `events/JoinManeger.java` | Entrada de jogadores |
| `events/RemoverDaPartidaDead.java` | Morte/espectador |
| `craft/Refil.java` | Receitas customizadas |
| `utils/Borreiras.java` | Border do mundo |
| `utils/EnviarServer.java` | BungeeCord |
| `utils/PrefixoC.java` | Prefixo de mensagens |
