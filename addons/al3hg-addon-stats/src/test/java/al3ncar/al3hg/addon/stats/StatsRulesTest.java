package al3ncar.al3hg.addon.stats;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StatsRulesTest {

    private final StatsRules rules = StatsRules.defaults();
    private final UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void emptyStartsAtZero() {
        assertEquals(new PlayerStats(id, 0, 0, 0, 0), PlayerStats.empty(id));
    }

    @Test
    void recordKillIncrementsOnlyKillsAndDoesNotMutateOriginal() {
        PlayerStats before = PlayerStats.empty(id);
        PlayerStats after = rules.recordKill(before);
        assertEquals(new PlayerStats(id, 1, 0, 0, 0), after);
        assertEquals(0, before.kills());
    }

    @Test
    void recordDeathWinAndGameEnd() {
        PlayerStats s = PlayerStats.empty(id);
        s = rules.recordDeath(s);
        s = rules.recordWin(s);
        s = rules.recordGameEnd(s);
        assertEquals(new PlayerStats(id, 0, 1, 1, 1), s);
    }

    @Test
    void fullMatchFlow() {
        PlayerStats s = PlayerStats.empty(id);
        s = rules.recordKill(rules.recordKill(rules.recordKill(s)));
        s = rules.recordWin(s);
        s = rules.recordGameEnd(s);
        assertEquals(new PlayerStats(id, 3, 1, 0, 1), s);
    }

    @Test
    void scoreUsesInjectedWeights() {
        PlayerStats s = new PlayerStats(id, 4, 2, 9, 9);
        assertEquals(4 * 1 + 2 * 5, rules.score(s));
        assertEquals(4 * 2 + 2 * 10, new StatsRules(2, 10).score(s));
    }

    @Test
    void negativeValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerStats(id, -1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> PlayerStats.empty(id).withWins(-1));
        assertThrows(IllegalArgumentException.class, () -> new StatsRules(-1, 1));
        assertThrows(NullPointerException.class, () -> PlayerStats.empty(null));
    }

    @Test
    void overflowIsNotSilent() {
        PlayerStats max = new PlayerStats(id, Integer.MAX_VALUE, 0, 0, 0);
        assertThrows(ArithmeticException.class, () -> rules.recordKill(max));
    }
}
