package al3ncar.al3hg.addon.stats.repository;

import al3ncar.al3hg.addon.stats.PlayerStats;
import al3ncar.al3hg.addon.stats.Ranking;
import al3ncar.al3hg.addon.stats.RankingType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

/** Implementacao em memoria, para testes. Os futures ja voltam completos. */
public final class InMemoryStatsRepository implements StatsRepository {

    private final Map<UUID, PlayerStats> data = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Optional<PlayerStats>> load(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return CompletableFuture.completedFuture(Optional.ofNullable(data.get(uuid)));
    }

    @Override
    public CompletableFuture<Void> save(PlayerStats stats) {
        Objects.requireNonNull(stats, "stats");
        data.put(stats.uuid(), stats);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<PlayerStats>> top(RankingType type, int limit) {
        try {
            return CompletableFuture.completedFuture(Ranking.top(data.values(), type, limit));
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<PlayerStats> update(UUID uuid, UnaryOperator<PlayerStats> change) {
        try {
            return CompletableFuture.completedFuture(data.compute(uuid, (id, current) ->
                    change.apply(current != null ? current : PlayerStats.empty(id))));
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public void close() {
        // nada a liberar
    }
}
