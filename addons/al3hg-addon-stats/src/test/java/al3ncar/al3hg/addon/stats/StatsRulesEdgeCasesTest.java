package al3ncar.al3hg.addon.stats;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Casos de borda das regras de pontuacao (zero, extremos, overflow, imutabilidade). */
class StatsRulesEdgeCasesTest {

    private final UUID id = new UUID(0L, 1L);

    @Test
    void scoreOfEmptyPlayerIsZero() {
        assertEquals(0L, StatsRules.defaults().score(PlayerStats.empty(id)));
    }

    @Test
    void zeroWeightsAreAllowedAndScoreIsAlwaysZero() {
        StatsRules free = new StatsRules(0, 0);
        assertEquals(0L, free.score(new PlayerStats(id, 100, 100, 0, 0)));
        assertEquals(0, free.killPoints());
        assertEquals(0, free.winPoints());
    }

    @Test
    void eachWeightCanBeZeroIndependently() {
        PlayerStats s = new PlayerStats(id, 3, 2, 0, 0);
        assertEquals(10L, new StatsRules(0, 5).score(s));
        assertEquals(3L, new StatsRules(1, 0).score(s));
    }

    @Test
    void negativeWinPointsAloneAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new StatsRules(1, -1));
        assertThrows(IllegalArgumentException.class, () -> new StatsRules(Integer.MIN_VALUE, 0));
    }

    @Test
    void scoreDoesNotOverflowWithExtremeValues() {
        int max = Integer.MAX_VALUE;
        PlayerStats s = new PlayerStats(id, max, max, 0, 0);
        long expected = 2L * max * max; // ~9.2e18, ainda cabe em long
        assertEquals(expected, new StatsRules(max, max).score(s));
        assertTrue(expected > 0);
    }

    @Test
    void scoreIgnoresDeathsAndGames() {
        StatsRules r = StatsRules.defaults();
        assertEquals(r.score(new PlayerStats(id, 2, 1, 0, 0)), r.score(new PlayerStats(id, 2, 1, 50, 99)));
    }

    @Test
    void everyCounterOverflowIsNotSilent() {
        StatsRules r = StatsRules.defaults();
        int max = Integer.MAX_VALUE;
        assertThrows(ArithmeticException.class, () -> r.recordDeath(new PlayerStats(id, 0, 0, max, 0)));
        assertThrows(ArithmeticException.class, () -> r.recordWin(new PlayerStats(id, 0, max, 0, 0)));
        assertThrows(ArithmeticException.class, () -> r.recordGameEnd(new PlayerStats(id, 0, 0, 0, max)));
    }

    @Test
    void recordsKeepUuidAndOtherCounters() {
        StatsRules r = StatsRules.defaults();
        PlayerStats base = new PlayerStats(id, 1, 2, 3, 4);
        assertEquals(new PlayerStats(id, 2, 2, 3, 4), r.recordKill(base));
        assertEquals(new PlayerStats(id, 1, 3, 3, 4), r.recordWin(base));
        assertEquals(new PlayerStats(id, 1, 2, 4, 4), r.recordDeath(base));
        assertEquals(new PlayerStats(id, 1, 2, 3, 5), r.recordGameEnd(base));
    }

    @Test
    void recordsRejectNullStats() {
        StatsRules r = StatsRules.defaults();
        assertThrows(NullPointerException.class, () -> r.recordKill(null));
        assertThrows(NullPointerException.class, () -> r.score(null));
    }

    @Test
    void allWithMethodsRejectNegativeAndPreserveUuid() {
        PlayerStats s = PlayerStats.empty(id);
        assertThrows(IllegalArgumentException.class, () -> s.withKills(-1));
        assertThrows(IllegalArgumentException.class, () -> s.withDeaths(-1));
        assertThrows(IllegalArgumentException.class, () -> s.withGamesPlayed(-1));
        assertThrows(IllegalArgumentException.class, () -> new PlayerStats(id, 0, 0, Integer.MIN_VALUE, 0));
        assertThrows(IllegalArgumentException.class, () -> new PlayerStats(id, 0, 0, 0, -1));
        assertEquals(id, s.withKills(5).uuid());
    }

    @Test
    void recordEqualityIsValueBased() {
        assertEquals(new PlayerStats(id, 1, 2, 3, 4), new PlayerStats(new UUID(0L, 1L), 1, 2, 3, 4));
        assertEquals(new PlayerStats(id, 1, 2, 3, 4).hashCode(), new PlayerStats(id, 1, 2, 3, 4).hashCode());
    }

    @Test
    void manyRecordsAccumulateExactly() {
        StatsRules r = StatsRules.defaults();
        PlayerStats s = PlayerStats.empty(id);
        for (int i = 0; i < 10_000; i++) {
            s = r.recordKill(s);
        }
        assertEquals(10_000, s.kills());
        assertEquals(10_000L, r.score(s));
    }
}
