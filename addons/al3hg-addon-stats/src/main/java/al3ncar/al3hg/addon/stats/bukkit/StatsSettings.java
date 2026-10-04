package al3ncar.al3hg.addon.stats.bukkit;

import al3ncar.al3hg.addon.stats.StatsRules;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

/**
 * Valores lidos do {@code config.yml}, já validados. Valores inválidos voltam ao padrão
 * (com aviso no log) em vez de impedir o addon de ligar.
 */
public record StatsSettings(String databaseFile, int killPoints, int winPoints, int topSize,
                            boolean quitCountsAsDeath) {

    public static final String DEFAULT_DATABASE_FILE = "stats.db";
    public static final int DEFAULT_TOP_SIZE = 10;
    public static final int MAX_TOP_SIZE = 50;

    public static StatsSettings from(ConfigurationSection config, Logger logger) {
        String file = config.getString("database.file", DEFAULT_DATABASE_FILE);
        if (file == null || file.isBlank()) {
            logger.warning("config.yml: database.file vazio; usando " + DEFAULT_DATABASE_FILE);
            file = DEFAULT_DATABASE_FILE;
        }
        return new StatsSettings(
                file.strip(),
                nonNegative(config, "scoring.kill-points", StatsRules.DEFAULT_KILL_POINTS, logger),
                nonNegative(config, "scoring.win-points", StatsRules.DEFAULT_WIN_POINTS, logger),
                topSize(config, logger),
                config.getBoolean("count-quit-as-death", true));
    }

    public StatsRules rules() {
        return new StatsRules(killPoints, winPoints);
    }

    private static int nonNegative(ConfigurationSection config, String path, int fallback, Logger logger) {
        int value = config.getInt(path, fallback);
        if (value < 0) {
            logger.warning("config.yml: " + path + " não pode ser negativo; usando " + fallback);
            return fallback;
        }
        return value;
    }

    private static int topSize(ConfigurationSection config, Logger logger) {
        int size = config.getInt("top.size", DEFAULT_TOP_SIZE);
        if (size < 1 || size > MAX_TOP_SIZE) {
            logger.warning("config.yml: top.size deve ficar entre 1 e " + MAX_TOP_SIZE + "; usando " + DEFAULT_TOP_SIZE);
            return DEFAULT_TOP_SIZE;
        }
        return size;
    }
}
