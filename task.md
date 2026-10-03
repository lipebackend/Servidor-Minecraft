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

## 🔒 Proteções da partida (PVP + Mobs)

### Mobs — desativados "para sempre" (durante a partida)

- [ ] Desativar spawn natural de mobs no mundo da partida:
  ```java
  world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
  world.setGameRule(GameRule.DO_WEATHER_CYCLE, false); // opcional
  world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false); // opcional
  ```
- [ ] Remover mobs já existentes ao iniciar:
  ```java
  world.getEntities().stream()
       .filter(e -> e instanceof Monster)
       .forEach(org.bukkit.entity.Entity::remove);
  ```
- [ ] (Extra segurança) Cancelar spawn via evento para não depender só da gamerule:
  ```java
  @EventHandler
  public void onSpawn(CreatureSpawnEvent e) {
      if (e.getEntityType() != EntityType.PLAYER) e.setCancelled(true);
  }
  ```

### PVP — desativado até 2 minutos após o start

- [ ] Adicionar flag na `Partida`:
  ```java
  private boolean pvpLiberado = false;
  public boolean isPvpLiberado() { return pvpLiberado; }
  public void setPvpLiberado(boolean v) { this.pvpLiberado = v; }
  ```
- [ ] Liberar PVP 2 minutos depois do início (20 ticks × 120s):
  ```java
  Bukkit.getScheduler().runTaskLater(plugin, () -> {
      partida.setPvpLiberado(true);
      Bukkit.broadcastMessage(PrefixoC.PREFIXO + "§cPVP LIBERADO!");
  }, 20L * 120);
  ```
- [ ] Cancelar dano jogador → jogador enquanto grace period ativo:
  ```java
  @EventHandler
  public void onDano(EntityDamageByEntityEvent e) {
      if (partida.isPvpLiberado()) return;
      if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
          e.setCancelled(true);
      }
      // cobrir flechas/projéteis:
      if (e.getDamager() instanceof Projectile p
          && p.getShooter() instanceof Player
          && e.getEntity() instanceof Player) {
          e.setCancelled(true);
      }
  }
  ```
- [ ] Anotar: ao parar/reiniciar a partida, resetar `pvpLiberado = false` e restaurar `DO_MOB_SPAWNING`

---

## 🐛 Erros de lógica / arquitetura (corrigidos por você)

### HgCore.java
- [ ] `/hgc fs`: o loop `for (int a = 100; a > 0; a--)` envia as 100 mensagens **instantaneamente** (sem delay). Use `Bukkit.getScheduler().runTaskTimer` para uma contagem de verdade.
- [ ] `/hgc stop` e `/hgc rest`: mesmo problema do loop sem delay — e o `Bukkit.getServer().shutdown()` roda **na hora**, antes do jogador ser enviado ao lobby via BungeeCord. Use delay (ex.: `runTaskLater`) antes do shutdown.
- [ ] Campo `private static StatsPartida a;` nunca é inicializado e ainda conflita com a variável local `a` do loop; `a.MEIO` funciona por acaso (enum é estática), mas é confuso. Remova o campo e use `StatsPartida.MEIO` direto.
- [ ] `/hgc help` não lista `stop` nem `rest`.

### PartidaRolando.java
- [ ] `Partidakk()` sempre retorna `true` (o segundo `return true;` deveria ser `return false;`), então o chamador não consegue saber se a partida realmente iniciou.

### Partida.java / Maneger.java
- [ ] `jogadores` nunca é limpo entre partidas — jogadores de uma partida antiga continuam contados como vivos. Limpe o set ao iniciar/parar.
- [ ] O status (`estadoP`) nunca volta para `INCIOS` ao reiniciar.
- [ ] Jogadores que **desconectam** (`PlayerQuitEvent`) não são removidos do set, então `getJogadoresVivos()` fica inflado.

### JoinManeger.java
- [ ] `Borreiras.Diminuir("hgmapa")` é chamado a **cada join**, resetando a border para todos. Chame uma vez só no início da partida.
- [ ] Sem checagem de `Bukkit.getWorld("hgmapa") == null` → NPE se o mundo não existir.
- [ ] Todo player que entra é adicionado à partida, mesmo com partida em andamento (`MEIO`) — deve entrar como espectador/lobby.
- [ ] Define `ADVENTURE` para todos no join, o que sobrescreve o `SURVIVAL` de quem já estava jogando.

### RemoverDaPartidaDead.java
- [ ] `p.setGameMode(GameMode.SPECTATOR)` dentro de `PlayerDeathEvent` geralmente não persiste (o respawn reseta). Coloque o modo espectador no `PlayerRespawnEvent`.
- [ ] `onRespwam` envia **todo** jogador que respawna para o lobby — inclusive se a partida ainda não acabou. Deve respawnar como espectador até o fim.
- [ ] Quando faltam só 1 vivo, faz `shutdown()` imediato — deveria anunciar vencedor, esperar alguns segundos e só então enviar ao lobby/desligar.
- [ ] `atualizarContador`: não precisa dos dois métodos; e o broadcast de "vivos" deveria acontecer antes do check de fim.

### Regem.java (sopa)
- [ ] Sem cooldown — clique spam cura até a vida máxima instantaneamente. Adicione delay (ex.: 1s por uso, via `player.setCooldown` ou timestamp).
- [ ] `setSaturation(20.0f)` e `setFoodLevel(20)` a cada clique é forte demais; revise os valores.
- [ ] Não verifica se o jogador está em partida/mundo certo — qualquer player com sopa se cura em qualquer mundo.

### Borreiras.java
- [ ] `Diminuir` chamado no join (errado — ver JoinManeger) e nunca chamado em `MEIO`/`FINAL` → border nunca encolhe de verdade.
- [ ] `bord.setSize(...)` sem tempo de transição — encolhe instantaneamente. Use `setSize(tamanho, tempoSegundos)`.
- [ ] Sem null-check no mundo.

### Gerais
- [ ] Em nenhum lugar há listener de `PlayerQuitEvent` para remover o jogador da partida.
- [ ] Sem cancelamento de drops/quebra de blocos/comandos de mob no modo — qualquer mob que apareça pode interferir (ver seção Mobs).
- [ ] `Maneger.getPartida()` pode dar NPE se chamado antes de `init()`.
- [ ] Prefixo/mensagens com erros de português ("permição", "Reniciando", "INCIOS" → "INICIO").


