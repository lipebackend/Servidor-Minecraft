package al3ncar.al3hg.addon.stats;

import java.util.Objects;
import java.util.UUID;

/**
 * Estatisticas imutaveis de um jogador. Sem dependencia de Bukkit.
 * Para alterar use {@link StatsRules}; os metodos "with" aqui apenas copiam.
 */
public record PlayerStats(UUID uuid, int kills, int wins, int deaths, int gamesPlayed) {

    public PlayerStats {
        Objects.requireNonNull(uuid, "uuid");
        if (kills < 0 || wins < 0 || deaths < 0 || gamesPlayed < 0) {
            throw new IllegalArgumentException("Contadores nao podem ser negativos");
        }
    }

    public static PlayerStats empty(UUID uuid) {
        return new PlayerStats(uuid, 0, 0, 0, 0);
    }

    public PlayerStats withKills(int kills) {
        return new PlayerStats(uuid, kills, wins, deaths, gamesPlayed);
    }

    public PlayerStats withWins(int wins) {
        return new PlayerStats(uuid, kills, wins, deaths, gamesPlayed);
    }

    public PlayerStats withDeaths(int deaths) {
        return new PlayerStats(uuid, kills, wins, deaths, gamesPlayed);
    }

    public PlayerStats withGamesPlayed(int gamesPlayed) {
        return new PlayerStats(uuid, kills, wins, deaths, gamesPlayed);
    }
}
