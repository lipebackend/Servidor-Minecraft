package al3ncar.al3hg.game;

import al3ncar.al3hg.api.GameState;
import al3ncar.al3hg.api.HgGame;
import al3ncar.al3hg.api.event.HgGameEndEvent;
import al3ncar.al3hg.api.event.HgGameStartEvent;
import al3ncar.al3hg.api.event.HgPlayerEliminatedEvent;
import al3ncar.al3hg.api.event.HgStateChangeEvent;
import al3ncar.al3hg.arena.ArenaException;
import al3ncar.al3hg.arena.ArenaProvider;
import al3ncar.al3hg.config.HgSettings;
import al3ncar.al3hg.util.ArenaRules;
import al3ncar.al3hg.util.LobbyTransfer;
import al3ncar.al3hg.util.Messages;
import al3ncar.al3hg.util.WorldBorderController;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Controla o ciclo de vida da partida (antes {@code Maneger}) e implementa {@link HgGame}.
 * Única classe que muda o estado; valida a origem via {@link GameStateMachine} e dispara os eventos
 * da API. Tudo roda na thread principal. Todas as tasks pertencem à partida e são canceladas em
 * stop/reset/onDisable. Sem estado estático.
 */
public final class GameManager implements HgGame {

    public enum StartResult { OK, ALREADY_RUNNING, NOT_ENOUGH_PLAYERS, ARENA_UNAVAILABLE }

    private final Plugin plugin;
    private final ArenaProvider arena;
    private final LobbyTransfer lobby;
    private final Supplier<HgSettings> settings;
    private final GameStateMachine fsm = new GameStateMachine(this::onTransition);
    private final List<BukkitTask> tasks = new ArrayList<>();

    /** Estado da partida atual: só matchId + nome do mundo + UUIDs. {@code null} em WAITING. */
    private MatchState match;
    private boolean shutdownRequested;
    private long matchSequence;

    public GameManager(Plugin plugin, ArenaProvider arena, LobbyTransfer lobby, Supplier<HgSettings> settings) {
        this.plugin = plugin;
        this.arena = arena;
        this.lobby = lobby;
        this.settings = settings;
    }

    // ------------------------------------------------------------------ HgGame

    @Override
    public GameState state() {
        return fsm.state();
    }

    @Override
    public Optional<String> matchId() {
        return Optional.ofNullable(match).map(MatchState::matchId);
    }

    @Override
    public boolean isPvpEnabled() {
        return match != null && match.isPvpEnabled();
    }

    @Override
    public Set<UUID> alivePlayers() {
        return match == null ? Set.of() : match.alivePlayers();
    }

    @Override
    public Set<UUID> spectators() {
        return match == null ? Set.of() : match.spectators();
    }

    @Override
    public boolean isAlive(UUID playerId) {
        return match != null && match.isAlive(playerId);
    }

    @Override
    public Optional<String> worldName() {
        return Optional.ofNullable(match).map(MatchState::worldName);
    }

    @Override
    public Optional<World> world() {
        return worldName().map(Bukkit::getWorld);
    }

    @Override
    public Optional<UUID> winner() {
        return fsm.state() == GameState.ENDING && match != null ? match.winner() : Optional.empty();
    }

    // ------------------------------------------------------------------ comandos

    /** Prepara a arena e inicia a partida. {@code skipCountdown} = início rápido (vai direto para GRACE). */
    public StartResult start(boolean skipCountdown) {
        if (fsm.state() != GameState.WAITING) {
            return StartResult.ALREADY_RUNNING;
        }
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (!skipCountdown && players.size() < settings.get().minPlayers()) {
            return StartResult.NOT_ENOUGH_PLAYERS;
        }
        if (!arena.isReady()) {
            return StartResult.ARENA_UNAVAILABLE;
        }
        String matchId = nextMatchId();
        String worldName;
        try {
            worldName = arena.prepare(matchId);
        } catch (ArenaException e) {
            plugin.getLogger().severe("Não foi possível preparar a arena: " + e.getMessage()
                    + (e.getCause() != null ? " (" + e.getCause() + ")" : ""));
            return StartResult.ARENA_UNAVAILABLE;
        }
        match = new MatchState(matchId, worldName);
        players.forEach(p -> match.addParticipant(p.getUniqueId()));
        ArenaRules.apply(worldName, plugin.getLogger());
        players.forEach(p -> enterArena(p, GameMode.ADVENTURE));

        if (skipCountdown && !match.isOver()) {
            fsm.transition(GameState.GRACE);
            beginGrace();
        } else {
            // Início rápido sem jogadores suficientes também passa por COUNTDOWN para poder ir direto a ENDING.
            fsm.transition(GameState.COUNTDOWN);
            if (skipCountdown) {
                leaveCountdown();
            } else {
                startCountdown();
            }
        }
        return StartResult.OK;
    }

    /**
     * Para a partida: define "sem vencedor", envia os jogadores ao lobby e SÓ ENTÃO descarta a arena.
     * Nunca chama {@code shutdown()} antes disso.
     *
     * @return {@code false} se não havia partida
     */
    public boolean stop() {
        if (match == null || fsm.state() == GameState.WAITING || fsm.state() == GameState.ENDING) {
            return false;
        }
        endMatch(null, 0);
        return true;
    }

    /** Igual a {@link #stop()}; se {@code ending.shutdown-on-rest}, desliga o servidor DEPOIS do envio ao lobby. */
    public boolean restart() {
        boolean stopped = stop();
        if (stopped) {
            shutdownRequested = settings.get().shutdownOnRest();
        }
        return stopped;
    }

    /** Chamado no onDisable: cancela tasks, descarta mundos carregados e zera o estado. */
    public void shutdown() {
        cancelTasks();
        arena.disposeAll();
        match = null;
        shutdownRequested = false;
        fsm.reset();
    }

    // ------------------------------------------------------------------ jogadores

    /** Entrada de jogador, conforme o estado (corrige o JoinManeger original). */
    public void handleJoin(Player p) {
        switch (fsm.state()) {
            case WAITING -> {
                p.setGameMode(GameMode.ADVENTURE);
                Messages.send(p, "Bem-vindo(a)! A partida começará em breve.");
            }
            case COUNTDOWN -> {
                match.addParticipant(p.getUniqueId());
                enterArena(p, GameMode.ADVENTURE);
            }
            case GRACE, RUNNING, ENDING -> {
                match.addSpectator(p.getUniqueId());
                enterArena(p, GameMode.SPECTATOR);
                Messages.send(p, "A partida já começou: você está como espectador.");
            }
        }
    }

    /** Saída de jogador: sai dos vivos, dispara o evento e checa o fim. */
    public void handleQuit(Player p) {
        if (match == null) {
            return;
        }
        UUID id = p.getUniqueId();
        switch (fsm.state()) {
            case COUNTDOWN -> {
                match.remove(id);
                if (match.aliveCount() == 0) {
                    stop();
                }
            }
            case GRACE, RUNNING -> eliminate(id, null, HgPlayerEliminatedEvent.Reason.QUIT);
            case ENDING -> match.remove(id);
            default -> {
            }
        }
    }

    /** Eliminação (morte ou saída): atualiza vivos/espectadores, dispara evento e checa vencedor. */
    public void eliminate(UUID id, UUID killer, HgPlayerEliminatedEvent.Reason reason) {
        if (match == null || !fsm.state().isInProgress()) {
            return;
        }
        boolean wasAlive = reason == HgPlayerEliminatedEvent.Reason.QUIT ? match.remove(id) : match.eliminate(id);
        if (!wasAlive) {
            return;
        }
        int remaining = match.aliveCount();
        callEvent(new HgPlayerEliminatedEvent(match.matchId(), id, killer, reason, remaining));
        Messages.broadcast("§eJogadores vivos: §f" + remaining);
        if (match.isOver()) {
            endMatch(match.soleSurvivor().orElse(null), settings.get().endingDelaySeconds());
        }
    }

    /** Local de respawn dos espectadores (arena), calculado sob demanda. */
    public Optional<Location> respawnLocation(Player p) {
        if (match == null || !match.isSpectator(p.getUniqueId())) {
            return Optional.empty();
        }
        return Optional.ofNullable(arenaSpawn());
    }

    public boolean isSpectator(UUID id) {
        return match != null && match.isSpectator(id);
    }

    /** Aplica SPECTATOR um tick depois (setar dentro do death/respawn event não persiste). */
    public void applySpectatorNextTick(Player p) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (p.isOnline() && isSpectator(p.getUniqueId())) {
                p.setGameMode(GameMode.SPECTATOR);
            }
        });
    }

    // ------------------------------------------------------------------ transições internas

    private void onTransition(GameState from, GameState to) {
        if (match == null) {
            return;
        }
        match.setPvpEnabled(to == GameState.RUNNING);
        callEvent(new HgStateChangeEvent(match.matchId(), from, to));
    }

    private void startCountdown() {
        int[] remaining = {settings.get().countdownSeconds()};
        if (remaining[0] <= 0) {
            leaveCountdown();
            return;
        }
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                if (fsm.state() != GameState.COUNTDOWN) {
                    cancel();
                    return;
                }
                int left = remaining[0]--;
                if (left <= 0) {
                    cancel();
                    leaveCountdown();
                } else if (left <= 5 || left % 10 == 0) {
                    Messages.broadcast("§eA partida começa em §f" + left + "§e segundo(s)...");
                }
            }
        }.runTaskTimer(plugin, 0L, 20L));
    }

    /**
     * Saída do COUNTDOWN: se não sobrou jogador suficiente ({@code isOver()}), vai direto a ENDING
     * (sem HgStateChangeEvent para GRACE nem HgGameStartEvent); senão segue o fluxo normal para GRACE.
     */
    private void leaveCountdown() {
        if (match.isOver()) {
            endMatch(match.soleSurvivor().orElse(null), settings.get().endingDelaySeconds());
            return;
        }
        fsm.transition(GameState.GRACE);
        beginGrace();
    }

    /** GRACE: SURVIVAL para os vivos, border inicial, HgGameStartEvent e agenda o fim da graça. */
    private void beginGrace() {
        HgSettings s = settings.get();
        for (UUID id : match.alivePlayers()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                p.setGameMode(GameMode.SURVIVAL);
            }
        }
        WorldBorderController.shrink(match.worldName(), s.borderInitialSize(), 0, plugin.getLogger());
        callEvent(new HgGameStartEvent(match.matchId(), match.alivePlayers()));
        Messages.broadcast("§aQue comecem os jogos! PVP liberado em §f" + s.graceSeconds() + "§as.");
        schedule(this::beginRunning, s.graceSeconds() * 20L);
    }

    private void beginRunning() {
        if (fsm.state() != GameState.GRACE) {
            return;
        }
        HgSettings s = settings.get();
        fsm.transition(GameState.RUNNING);
        Messages.broadcast("§cPVP LIBERADO!");
        WorldBorderController.shrink(match.worldName(), s.borderFinalSize(), s.borderShrinkSeconds(), plugin.getLogger());
    }

    /** ENDING: cancela tasks, dispara HgGameEndEvent; depois envia ao lobby e só então descarta a arena. */
    private void endMatch(UUID winner, int delaySeconds) {
        if (match == null || fsm.state() == GameState.WAITING || fsm.state() == GameState.ENDING) {
            return;
        }
        cancelTasks();
        match.setWinner(winner);
        fsm.transition(GameState.ENDING);
        callEvent(new HgGameEndEvent(match.matchId(), winner));
        if (winner != null) {
            Messages.broadcast("§6Vencedor: §f" + nameOf(winner) + "§6!");
        } else {
            Messages.broadcast("§6A partida terminou sem vencedor.");
        }
        long delay = Math.max(0, delaySeconds) * 20L;
        long wait = Math.max(0, settings.get().lobbyTransferWaitSeconds()) * 20L;
        schedule(this::sendArenaPlayersToLobby, delay);
        schedule(this::finishMatch, delay + wait + 1L);
    }

    private void sendArenaPlayersToLobby() {
        if (match == null) {
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().getName().equals(match.worldName())) {
                lobby.sendToLobby(p);
            }
        }
    }

    /** Descarta a arena (jogadores já enviados ao lobby), volta a WAITING e limpa o estado. */
    private void finishMatch() {
        if (match == null) {
            return;
        }
        arena.dispose(match.matchId());
        fsm.transition(GameState.WAITING);
        match = null;
        cancelTasks();
        // Quem continua online (ex.: evacuado pelo dispose) volta ao modo de espera, não fica em SPECTATOR/SURVIVAL.
        Bukkit.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.ADVENTURE));
        if (shutdownRequested || settings.get().shutdownAfterEnd()) {
            shutdownRequested = false;
            plugin.getLogger().info("Arena descartada; desligando o servidor conforme a configuração.");
            Bukkit.shutdown();
        }
    }

    // ------------------------------------------------------------------ utilidades

    private void enterArena(Player p, GameMode mode) {
        if (mode != GameMode.SPECTATOR) {
            resetPlayer(p);
        }
        Location spawn = arenaSpawn();
        if (spawn != null) {
            p.teleportAsync(spawn);
        }
        p.setGameMode(mode);
    }

    /** Limpa vida, fome e inventário de quem vai jogar (não deixa itens/estado de antes da partida). */
    private static void resetPlayer(Player p) {
        AttributeInstance maxHealth = p.getAttribute(Attribute.MAX_HEALTH);
        p.setHealth(maxHealth == null ? 20.0 : maxHealth.getValue());
        p.setFoodLevel(20);
        p.setSaturation(20.0f);
        p.getInventory().clear();
    }

    /** Resolvido sob demanda a partir do nome do mundo (nunca armazenado). */
    private Location arenaSpawn() {
        World w = match == null ? null : Bukkit.getWorld(match.worldName());
        return w == null ? null : w.getSpawnLocation();
    }

    private String nextMatchId() {
        return Long.toString(System.currentTimeMillis(), 36) + (matchSequence++);
    }

    private static String nameOf(UUID id) {
        String name = Bukkit.getOfflinePlayer(id).getName();
        return name != null ? name : id.toString();
    }

    private void schedule(Runnable action, long delayTicks) {
        tasks.add(Bukkit.getScheduler().runTaskLater(plugin, action, delayTicks));
    }

    private void cancelTasks() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }

    private static void callEvent(Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }
}
