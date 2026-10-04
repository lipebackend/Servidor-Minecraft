package al3ncar.al3hg.addon.stats;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RankingTest {

    private static UUID uuid(int n) {
        return new UUID(0L, n);
    }

    private static PlayerStats s(int n, int kills, int wins, int deaths, int games) {
        return new PlayerStats(uuid(n), kills, wins, deaths, games);
    }

    @Test
    void topWinsOrdersByWinsDescending() {
        var list = List.of(s(1, 0, 1, 0, 0), s(2, 0, 5, 0, 0), s(3, 0, 3, 0, 0));
        assertEquals(List.of(uuid(2), uuid(3), uuid(1)), ids(Ranking.top(list, RankingType.WINS, 10)));
    }

    @Test
    void topKillsOrdersByKillsDescending() {
        var list = List.of(s(1, 7, 0, 0, 0), s(2, 2, 9, 0, 0), s(3, 5, 0, 0, 0));
        assertEquals(List.of(uuid(1), uuid(3), uuid(2)), ids(Ranking.top(list, RankingType.KILLS, 10)));
    }

    @Test
    void limitTruncatesAndZeroIsEmpty() {
        var list = List.of(s(1, 1, 1, 0, 0), s(2, 2, 2, 0, 0), s(3, 3, 3, 0, 0));
        assertEquals(2, Ranking.top(list, RankingType.WINS, 2).size());
        assertTrue(Ranking.top(list, RankingType.WINS, 0).isEmpty());
        assertEquals(3, Ranking.top(list, RankingType.WINS, 100).size());
        assertThrows(IllegalArgumentException.class, () -> Ranking.top(list, RankingType.WINS, -1));
    }

    @Test
    void tieOnWinsBrokenByKills() {
        var list = List.of(s(1, 3, 2, 0, 0), s(2, 8, 2, 0, 0));
        assertEquals(List.of(uuid(2), uuid(1)), ids(Ranking.top(list, RankingType.WINS, 2)));
    }

    @Test
    void tieOnKillsBrokenByWins() {
        var list = List.of(s(1, 4, 1, 0, 0), s(2, 4, 3, 0, 0));
        assertEquals(List.of(uuid(2), uuid(1)), ids(Ranking.top(list, RankingType.KILLS, 2)));
    }

    @Test
    void thenFewerDeathsThenFewerGamesThenUuid() {
        var fewerDeaths = s(9, 1, 1, 1, 5);
        var moreDeaths = s(1, 1, 1, 4, 1);
        assertEquals(List.of(uuid(9), uuid(1)),
                ids(Ranking.top(List.of(moreDeaths, fewerDeaths), RankingType.WINS, 2)));

        var fewerGames = s(9, 1, 1, 1, 2);
        var moreGames = s(1, 1, 1, 1, 7);
        assertEquals(List.of(uuid(9), uuid(1)),
                ids(Ranking.top(List.of(moreGames, fewerGames), RankingType.KILLS, 2)));

        var a = s(1, 1, 1, 1, 1);
        var b = s(2, 1, 1, 1, 1);
        assertEquals(List.of(uuid(1), uuid(2)), ids(Ranking.top(List.of(b, a), RankingType.WINS, 2)));
    }

    @Test
    void resultIsIndependentOfInputOrder() {
        List<PlayerStats> base = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            base.add(s(i, i % 4, i % 3, i % 5, i % 2));
        }
        List<UUID> expected = ids(Ranking.top(base, RankingType.WINS, 30));
        Random rnd = new Random(42);
        for (int i = 0; i < 20; i++) {
            List<PlayerStats> shuffled = new ArrayList<>(base);
            Collections.shuffle(shuffled, rnd);
            assertEquals(expected, ids(Ranking.top(shuffled, RankingType.WINS, 30)));
        }
    }

    @Test
    void resultIsImmutableAndInputUntouched() {
        List<PlayerStats> input = new ArrayList<>(List.of(s(2, 0, 0, 0, 0), s(1, 0, 1, 0, 0)));
        List<PlayerStats> result = Ranking.top(input, RankingType.WINS, 2);
        assertThrows(UnsupportedOperationException.class, () -> result.add(s(3, 0, 0, 0, 0)));
        assertEquals(uuid(2), input.get(0).uuid());
    }

    private static List<UUID> ids(List<PlayerStats> list) {
        return list.stream().map(PlayerStats::uuid).toList();
    }
}
