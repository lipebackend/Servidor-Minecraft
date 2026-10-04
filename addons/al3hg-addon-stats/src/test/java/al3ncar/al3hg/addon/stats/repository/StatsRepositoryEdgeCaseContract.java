package al3ncar.al3hg.addon.stats.repository;

import al3ncar.al3hg.addon.stats.PlayerStats;
import al3ncar.al3hg.addon.stats.Ranking;
import al3ncar.al3hg.addon.stats.RankingType;
import al3ncar.al3hg.addon.stats.StatsRules;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/** Casos de borda comuns a qualquer {@link StatsRepository} (rode contra InMemory e SQLite). */
abstract class StatsRepositoryEdgeCaseContract {

    protected abstract StatsRepository repository();

    private static <T> T await(CompletableFuture<T> f) throws Exception {
        return f.get(10, TimeUnit.SECONDS);
    }

    private static UUID uuid(int n) {
        return new UUID(0L, n);
    }

    private static Throwable root(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    /** Aceita falha sincrona OU no future (as implementacoes divergem hoje; ver relatorio). */
    private static void assertRejectsNull(Supplier<CompletableFuture<?>> call) {
        try {
            CompletableFuture<?> f = call.get();
            ExecutionException ex = assertThrows(ExecutionException.class, () -> f.get(10, TimeUnit.SECONDS));
            assertInstanceOf(NullPointerException.class, root(ex));
        } catch (NullPointerException expected) {
            // falha sincrona: ok
        }
    }

    // ---------- valores de borda ----------

    @Test
    void zeroStatsRoundTripAndAppearInTop() throws Exception {
        PlayerStats zero = PlayerStats.empty(uuid(1));
        await(repository().save(zero));
        assertEquals(zero, await(repository().load(uuid(1))).orElseThrow());
        assertEquals(List.of(zero), await(repository().top(RankingType.WINS, 5)));
    }

    @Test
    void maxIntValuesRoundTrip() throws Exception {
        int max = Integer.MAX_VALUE;
        PlayerStats big = new PlayerStats(uuid(2), max, max, max, max);
        await(repository().save(big));
        assertEquals(big, await(repository().load(uuid(2))).orElseThrow());
    }

    @Test
    void uuidWithHighBitsRoundTrips() throws Exception {
        UUID high = new UUID(-1L, Long.MIN_VALUE);
        PlayerStats s = new PlayerStats(high, 1, 2, 3, 4);
        await(repository().save(s));
        assertEquals(s, await(repository().load(high)).orElseThrow());
    }

    @Test
    void topOnEmptyRepositoryIsEmptyAndLimitZeroIsEmpty() throws Exception {
        for (RankingType t : RankingType.values()) {
            assertTrue(await(repository().top(t, 10)).isEmpty());
        }
        await(repository().save(PlayerStats.empty(uuid(1))));
        assertTrue(await(repository().top(RankingType.WINS, 0)).isEmpty());
    }

    @Test
    void topWithHugeLimitReturnsEverything() throws Exception {
        for (int i = 1; i <= 3; i++) {
            await(repository().save(PlayerStats.empty(uuid(i))));
        }
        assertEquals(3, await(repository().top(RankingType.KILLS, Integer.MAX_VALUE)).size());
    }

    @Test
    void topResultIsImmutable() throws Exception {
        await(repository().save(PlayerStats.empty(uuid(1))));
        List<PlayerStats> top = await(repository().top(RankingType.WINS, 5));
        assertThrows(UnsupportedOperationException.class, () -> top.add(PlayerStats.empty(uuid(9))));
    }

    @Test
    void topMatchesPureRankingOnTieHeavyRandomData() throws Exception {
        Random rnd = new Random(2024);
        List<PlayerStats> all = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            PlayerStats s = new PlayerStats(new UUID(rnd.nextLong(), rnd.nextLong()),
                    rnd.nextInt(3), rnd.nextInt(3), rnd.nextInt(3), rnd.nextInt(3));
            all.add(s);
            await(repository().save(s));
        }
        for (RankingType t : RankingType.values()) {
            for (int limit : new int[]{1, 7, 80, 500}) {
                assertEquals(Ranking.top(all, t, limit), await(repository().top(t, limit)),
                        "SQL/Memoria divergem de Ranking para " + t + " limit=" + limit);
            }
        }
    }

    // ---------- argumentos invalidos ----------

    @Test
    void nullArgumentsAreRejected() {
        assertRejectsNull(() -> repository().load(null));
        assertRejectsNull(() -> repository().save(null));
        assertRejectsNull(() -> repository().top(null, 1));
        assertRejectsNull(() -> repository().update(null, s -> s));
        assertRejectsNull(() -> repository().update(uuid(1), null));
    }

    // ---------- update: falhas, atomicidade, ordem ----------

    @Test
    void failingChangeFailsFutureKeepsStateAndDoesNotPoisonRepository() throws Exception {
        PlayerStats before = new PlayerStats(uuid(1), 4, 3, 2, 1);
        await(repository().save(before));

        var ex = assertThrows(ExecutionException.class, () -> await(repository().update(uuid(1), s -> {
            throw new IllegalStateException("boom");
        })));
        assertInstanceOf(IllegalStateException.class, root(ex));

        assertEquals(before, await(repository().load(uuid(1))).orElseThrow());
        assertEquals(5, await(repository().update(uuid(1), StatsRules.defaults()::recordKill)).kills());
    }

    @Test
    void failingChangeOnUnknownPlayerDoesNotCreateRow() throws Exception {
        assertThrows(ExecutionException.class, () -> await(repository().update(uuid(7), s -> {
            throw new IllegalStateException("boom");
        })));
        assertTrue(await(repository().load(uuid(7))).isEmpty());
    }

    @Test
    void changeProducingInvalidStatsFailsAndKeepsState() throws Exception {
        PlayerStats before = new PlayerStats(uuid(1), 0, 0, 0, 0);
        await(repository().save(before));
        var ex = assertThrows(ExecutionException.class,
                () -> await(repository().update(uuid(1), s -> s.withKills(-1))));
        assertInstanceOf(IllegalArgumentException.class, root(ex));
        assertEquals(before, await(repository().load(uuid(1))).orElseThrow());
    }

    @Test
    void changeReceivesEmptyStatsForUnknownPlayer() throws Exception {
        PlayerStats seen = await(repository().update(uuid(3), s -> {
            assertEquals(PlayerStats.empty(uuid(3)), s);
            return s;
        }));
        assertEquals(PlayerStats.empty(uuid(3)), seen);
        assertTrue(await(repository().load(uuid(3))).isPresent());
    }

    /** Regressao (corrigido na main): contrato diz "devolve o valor gravado"; retornar null nao deveria ser aceito/apagar o jogador. */
    @Test
    void changeReturningNullMustFailAndKeepState() throws Exception {
        PlayerStats before = new PlayerStats(uuid(1), 2, 2, 2, 2);
        await(repository().save(before));
        CompletableFuture<PlayerStats> f = repository().update(uuid(1), s -> null);
        assertThrows(ExecutionException.class, () -> f.get(10, TimeUnit.SECONDS),
                "update com change==null deveria falhar");
        assertEquals(before, await(repository().load(uuid(1))).orElseThrow());
    }
}
