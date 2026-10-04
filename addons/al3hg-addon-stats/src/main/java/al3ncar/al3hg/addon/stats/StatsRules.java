package al3ncar.al3hg.addon.stats;

/**
 * Regras puras de atualizacao e pontuacao. Todas as operacoes devolvem uma
 * nova instancia de {@link PlayerStats}.
 *
 * <p>Os pesos da pontuacao sao injetados pelo construtor (sem numeros magicos
 * nem estado estatico); use {@link #defaults()} para os valores padrao.
 */
public final class StatsRules {

    public static final int DEFAULT_KILL_POINTS = 1;
    public static final int DEFAULT_WIN_POINTS = 5;

    private final int killPoints;
    private final int winPoints;

    public StatsRules(int killPoints, int winPoints) {
        if (killPoints < 0 || winPoints < 0) {
            throw new IllegalArgumentException("Pontos nao podem ser negativos");
        }
        this.killPoints = killPoints;
        this.winPoints = winPoints;
    }

    public static StatsRules defaults() {
        return new StatsRules(DEFAULT_KILL_POINTS, DEFAULT_WIN_POINTS);
    }

    public PlayerStats recordKill(PlayerStats stats) {
        return stats.withKills(Math.addExact(stats.kills(), 1));
    }

    public PlayerStats recordDeath(PlayerStats stats) {
        return stats.withDeaths(Math.addExact(stats.deaths(), 1));
    }

    public PlayerStats recordWin(PlayerStats stats) {
        return stats.withWins(Math.addExact(stats.wins(), 1));
    }

    /** Fim de partida: conta uma partida jogada (vitoria/morte sao registradas a parte). */
    public PlayerStats recordGameEnd(PlayerStats stats) {
        return stats.withGamesPlayed(Math.addExact(stats.gamesPlayed(), 1));
    }

    /** Pontuacao total = kills * killPoints + wins * winPoints. */
    public long score(PlayerStats stats) {
        return (long) stats.kills() * killPoints + (long) stats.wins() * winPoints;
    }

    public int killPoints() {
        return killPoints;
    }

    public int winPoints() {
        return winPoints;
    }
}
