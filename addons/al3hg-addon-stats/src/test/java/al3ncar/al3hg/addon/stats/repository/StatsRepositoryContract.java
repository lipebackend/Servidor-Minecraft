package al3ncar.al3hg.addon.stats.repository;

import al3ncar.al3hg.addon.stats.PlayerStats;
import al3ncar.al3hg.addon.stats.RankingType;
import al3ncar.al3hg.addon.stats.StatsRules;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Comportamento comum a todas as implementacoes de {@link StatsRepository}. */
abstract class StatsRepositoryContract {

    protected abstract StatsRepository repository();

    protected static <T> T await(CompletableFuture<T> future) throws Exception {
        return future.get(10, TimeUnit.SECONDS);
    }

    private static UUID uuid(int n) {
        return new UUID(0L, n);
    }

    @Test
    void loadOfUnknownIsEmpty() throws Exception {
        assertTrue(await(repository().load(uuid(1))).isEmpty());
    }

    @Test
    void saveThenLoadRoundTrips() throws Exception {
        PlayerStats s = new PlayerStats(uuid(1), 3, 2, 1, 4);
        await(repository().save(s));
        assertEquals(s, await(repository().load(uuid(1))).orElseThrow());
    }

    @Test
    void saveTwiceOverwrites() throws Exception {
        await(repository().save(new PlayerStats(uuid(1), 1, 1, 1, 1)));
        PlayerStats second = new PlayerStats(uuid(1), 9, 8, 7, 6);
        await(repository().save(second));
        assertEquals(second, await(repository().load(uuid(1))).orElseThrow());
        assertEquals(1, await(repository().top(RankingType.WINS, 10)).size());
    }

    @Test
    void topRespectsOrderTieBreakAndLimit() throws Exception {
        await(repository().save(new PlayerStats(uuid(1), 5, 2, 0, 0)));
        await(repository().save(new PlayerStats(uuid(2), 9, 2, 0, 0)));  // desempata por kills
        await(repository().save(new PlayerStats(uuid(3), 1, 7, 0, 0)));
        await(repository().save(new PlayerStats(uuid(4), 9, 2, 0, 0)));  // empate total com 2 -> uuid

        List<PlayerStats> wins = await(repository().top(RankingType.WINS, 10));
        assertEquals(List.of(uuid(3), uuid(2), uuid(4), uuid(1)), wins.stream().map(PlayerStats::uuid).toList());

        List<PlayerStats> kills = await(repository().top(RankingType.KILLS, 2));
        assertEquals(List.of(uuid(2), uuid(4)), kills.stream().map(PlayerStats::uuid).toList());
    }

    @Test
    void topWithNegativeLimitFails() {
        var ex = assertThrows(ExecutionException.class, () -> await(repository().top(RankingType.WINS, -1)));
        assertInstanceOf(IllegalArgumentException.class, rootOf(ex));
    }

    @Test
    void updateCreatesAppliesAndPersists() throws Exception {
        StatsRules rules = StatsRules.defaults();
        PlayerStats created = await(repository().update(uuid(5), rules::recordKill));
        assertEquals(new PlayerStats(uuid(5), 1, 0, 0, 0), created);
        await(repository().update(uuid(5), rules::recordWin));
        assertEquals(new PlayerStats(uuid(5), 1, 1, 0, 0), await(repository().load(uuid(5))).orElseThrow());
    }

    @Test
    void concurrentUpdatesDoNotLoseWrites() throws Exception {
        StatsRules rules = StatsRules.defaults();
        int n = 200;
        CompletableFuture<?>[] all = new CompletableFuture<?>[n];
        for (int i = 0; i < n; i++) {
            all[i] = CompletableFuture.runAsync(() -> repository().update(uuid(6), rules::recordKill).join());
        }
        await(CompletableFuture.allOf(all));
        assertEquals(n, await(repository().load(uuid(6))).orElseThrow().kills());
    }

    protected static Throwable rootOf(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }
}
