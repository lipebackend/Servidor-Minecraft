package al3ncar.al3hg.addon.stats;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Casos de borda do ranking: vazio, empates totais, extremos, uuid com bit alto, contrato do comparator. */
class RankingEdgeCasesTest {

    private static UUID uuid(int n) {
        return new UUID(0L, n);
    }

    private static PlayerStats s(int n, int kills, int wins, int deaths, int games) {
        return new PlayerStats(uuid(n), kills, wins, deaths, games);
    }

    private static List<UUID> ids(List<PlayerStats> list) {
        return list.stream().map(PlayerStats::uuid).toList();
    }

    @Test
    void emptyInputGivesEmptyResultForBothTypes() {
        for (RankingType t : RankingType.values()) {
            assertTrue(Ranking.top(List.of(), t, 10).isEmpty());
            assertTrue(Ranking.top(List.of(), t, 0).isEmpty());
        }
    }

    @Test
    void singlePlayerIsReturned() {
        var only = s(1, 0, 0, 0, 0);
        assertEquals(List.of(only), Ranking.top(List.of(only), RankingType.KILLS, 5));
    }

    @Test
    void playerWithNoGamesIsListedButRanksLast() {
        var zero = s(1, 0, 0, 0, 0);
        var oneKill = s(2, 1, 0, 0, 1);
        var oneWin = s(3, 0, 1, 0, 1);
        assertEquals(List.of(uuid(3), uuid(2), uuid(1)), ids(Ranking.top(List.of(zero, oneKill, oneWin), RankingType.WINS, 10)));
        assertEquals(List.of(uuid(2), uuid(3), uuid(1)), ids(Ranking.top(List.of(zero, oneKill, oneWin), RankingType.KILLS, 10)));
    }

    @Test
    void allZeroPlayersAreOrderedByUuidRegardlessOfInputOrder() {
        List<PlayerStats> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            list.add(s(i, 0, 0, 0, 0));
        }
        Collections.shuffle(list, new Random(7));
        List<UUID> expected = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            expected.add(uuid(i));
        }
        assertEquals(expected, ids(Ranking.top(list, RankingType.WINS, 10)));
        assertEquals(expected, ids(Ranking.top(list, RankingType.KILLS, 10)));
    }

    @Test
    void uuidTieBreakIsTextualNotSignedNumeric() {
        // UUID.compareTo (assinado) poria o uuid "ffff..." ANTES do "0000..."; o contrato diz texto minusculo.
        UUID high = new UUID(-1L, -1L);   // ffffffff-ffff-ffff-ffff-ffffffffffff
        UUID low = new UUID(0L, 1L);      // 00000000-0000-0000-0000-000000000001
        var a = new PlayerStats(high, 1, 1, 1, 1);
        var b = new PlayerStats(low, 1, 1, 1, 1);
        assertEquals(List.of(low, high), ids(Ranking.top(List.of(a, b), RankingType.WINS, 2)));
        assertEquals(List.of(low, high), ids(Ranking.top(List.of(b, a), RankingType.KILLS, 2)));
    }

    @Test
    void extremeValuesDoNotBreakDescendingOrder() {
        var max = s(1, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0);
        var zero = s(2, 0, 0, 0, 0);
        var mid = s(3, 5, 5, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(List.of(uuid(1), uuid(3), uuid(2)), ids(Ranking.top(List.of(zero, mid, max), RankingType.WINS, 3)));
        assertEquals(List.of(uuid(1), uuid(3), uuid(2)), ids(Ranking.top(List.of(zero, mid, max), RankingType.KILLS, 3)));
    }

    @Test
    void winsAndKillsRankingsCanDisagree() {
        var killer = s(1, 50, 0, 0, 10);
        var winner = s(2, 1, 3, 0, 10);
        assertEquals(uuid(2), Ranking.top(List.of(killer, winner), RankingType.WINS, 1).get(0).uuid());
        assertEquals(uuid(1), Ranking.top(List.of(killer, winner), RankingType.KILLS, 1).get(0).uuid());
    }

    @Test
    void sameUuidTwiceInInputIsNotDeduplicated() {
        var a = s(1, 1, 1, 0, 0);
        assertEquals(2, Ranking.top(List.of(a, a), RankingType.WINS, 10).size());
    }

    @Test
    void limitEqualToSizeAndMaxIntWork() {
        var list = List.of(s(1, 1, 1, 0, 0), s(2, 2, 2, 0, 0));
        assertEquals(2, Ranking.top(list, RankingType.WINS, 2).size());
        assertEquals(2, Ranking.top(list, RankingType.WINS, Integer.MAX_VALUE).size());
    }

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> Ranking.top(null, RankingType.WINS, 1));
        assertThrows(NullPointerException.class, () -> Ranking.top(List.of(s(1, 0, 0, 0, 0)), null, 1));
        assertThrows(NullPointerException.class, () -> Ranking.comparator(null));
    }

    @Test
    void worksWithUnorderedSetInput() {
        Set<PlayerStats> set = new java.util.HashSet<>(List.of(s(3, 0, 0, 0, 0), s(1, 0, 0, 0, 0), s(2, 0, 0, 0, 0)));
        assertEquals(List.of(uuid(1), uuid(2), uuid(3)), ids(Ranking.top(set, RankingType.WINS, 3)));
    }

    @Test
    void comparatorIsConsistentAntisymmetricAndTransitiveOnRandomData() {
        Random rnd = new Random(1234);
        List<PlayerStats> data = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            data.add(new PlayerStats(new UUID(rnd.nextLong(), rnd.nextLong()),
                    rnd.nextInt(3), rnd.nextInt(3), rnd.nextInt(3), rnd.nextInt(3)));
        }
        for (RankingType t : RankingType.values()) {
            var cmp = Ranking.comparator(t);
            for (PlayerStats a : data) {
                assertEquals(0, cmp.compare(a, a));
                for (PlayerStats b : data) {
                    assertEquals(Integer.signum(cmp.compare(a, b)), -Integer.signum(cmp.compare(b, a)));
                    if (!a.equals(b)) {
                        assertNotEquals(0, cmp.compare(a, b), "ordem total: so empata se for o mesmo jogador");
                    }
                    if (cmp.compare(a, b) < 0) {
                        for (PlayerStats c : data) {
                            if (cmp.compare(b, c) < 0) {
                                assertTrue(cmp.compare(a, c) < 0, "transitividade");
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void dataWithManyTiesIsStableAcrossShufflesForBothTypes() {
        Random rnd = new Random(99);
        List<PlayerStats> base = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            base.add(new PlayerStats(new UUID(rnd.nextLong(), rnd.nextLong()),
                    rnd.nextInt(2), rnd.nextInt(2), rnd.nextInt(2), rnd.nextInt(2)));
        }
        for (RankingType t : RankingType.values()) {
            List<PlayerStats> expected = Ranking.top(base, t, 100);
            for (int i = 0; i < 10; i++) {
                List<PlayerStats> copy = new ArrayList<>(base);
                Collections.shuffle(copy, rnd);
                assertEquals(expected, Ranking.top(copy, t, 100));
            }
        }
    }

    @Test
    void topNIsPrefixOfFullRanking() {
        List<PlayerStats> list = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            list.add(s(i, i % 4, i % 3, i % 2, i % 5));
        }
        List<PlayerStats> full = Ranking.top(list, RankingType.WINS, 20);
        assertEquals(full.subList(0, 7), Ranking.top(list, RankingType.WINS, 7));
    }
}
