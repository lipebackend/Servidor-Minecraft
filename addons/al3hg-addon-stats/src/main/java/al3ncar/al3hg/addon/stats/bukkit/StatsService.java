package al3ncar.al3hg.addon.stats.bukkit;

import al3ncar.al3hg.addon.stats.PlayerStats;
import al3ncar.al3hg.addon.stats.RankingType;
import al3ncar.al3hg.addon.stats.StatsRules;
import al3ncar.al3hg.addon.stats.repository.StatsRepository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aplica as {@link StatsRules} via {@link StatsRepository}. Sem Bukkit: o I/O roda no executor
 * proprio do repositorio e os futures completam la; quem usar o resultado com a API do Bukkit
 * deve voltar a thread principal antes (ver os comandos).
 */
public final class StatsService {

    private final StatsRepository repository;
    private final StatsRules rules;
    private final Logger logger;

    public StatsService(StatsRepository repository, StatsRules rules, Logger logger) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.rules = Objects.requireNonNull(rules, "rules");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public void recordKill(UUID player) {
        update(player, rules::recordKill, "registrar abate");
    }

    public void recordDeath(UUID player) {
        update(player, rules::recordDeath, "registrar morte");
    }

    public void recordWin(UUID player) {
        update(player, rules::recordWin, "registrar vitória");
    }

    public void recordGamePlayed(Collection<UUID> players) {
        for (UUID player : players) {
            update(player, rules::recordGameEnd, "registrar partida");
        }
    }

    public CompletableFuture<Optional<PlayerStats>> find(UUID player) {
        return repository.load(player);
    }

    public CompletableFuture<List<PlayerStats>> top(RankingType type, int limit) {
        return repository.top(type, limit);
    }

    public long score(PlayerStats stats) {
        return rules.score(stats);
    }

    private void update(UUID player, UnaryOperator<PlayerStats> change, String action) {
        repository.update(player, change).exceptionally(error -> {
            logFailure("Falha ao " + action + " de " + player, error);
            return null;
        });
    }

    /** Registra uma falha de I/O; encerramento do repositorio (RejectedExecutionException) nao e erro. */
    public void logFailure(String message, Throwable error) {
        Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
        if (!(cause instanceof RejectedExecutionException)) {
            logger.log(Level.WARNING, message, cause);
        }
    }
}
