package al3ncar.al3hg.addon.stats;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Ranking puro e deterministico.
 *
 * <p>Ordem (todas as chaves, em sequencia):
 * <ul>
 *   <li>WINS:  wins desc, kills desc, deaths asc, gamesPlayed asc, uuid asc</li>
 *   <li>KILLS: kills desc, wins desc, deaths asc, gamesPlayed asc, uuid asc</li>
 * </ul>
 * O uuid e comparado como texto minusculo, o que coincide com o ORDER BY do SQLite.
 */
public final class Ranking {

    private static final Comparator<PlayerStats> TAIL = Comparator
            .comparingInt(PlayerStats::deaths)
            .thenComparingInt(PlayerStats::gamesPlayed)
            .thenComparing(s -> s.uuid().toString());

    private static final Comparator<PlayerStats> BY_WINS = Comparator
            .comparingInt(PlayerStats::wins).reversed()
            .thenComparing(Comparator.comparingInt(PlayerStats::kills).reversed())
            .thenComparing(TAIL);

    private static final Comparator<PlayerStats> BY_KILLS = Comparator
            .comparingInt(PlayerStats::kills).reversed()
            .thenComparing(Comparator.comparingInt(PlayerStats::wins).reversed())
            .thenComparing(TAIL);

    private Ranking() {
    }

    public static Comparator<PlayerStats> comparator(RankingType type) {
        return switch (type) {
            case WINS -> BY_WINS;
            case KILLS -> BY_KILLS;
        };
    }

    /** Os {@code limit} melhores, ja ordenados. Lista imutavel. */
    public static List<PlayerStats> top(Collection<PlayerStats> all, RankingType type, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit nao pode ser negativo");
        }
        return all.stream().sorted(comparator(type)).limit(limit).toList();
    }
}
