package al3ncar.al3hg.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Configuração imutável do plugin (lida de config.yml). Valores inválidos viram o padrão/mínimo.
 */
public record HgSettings(
        int minPlayers,
        int countdownSeconds,
        int graceSeconds,
        double borderInitialSize,
        double borderFinalSize,
        int borderShrinkSeconds,
        int endingDelaySeconds,
        int lobbyTransferWaitSeconds,
        String lobbyServer,
        boolean shutdownAfterEnd,
        boolean shutdownOnRest,
        String templateWorld,
        String slimeDirectory) {

    public static HgSettings defaults() {
        return new HgSettings(2, 10, 120, 300, 20, 300, 10, 5, "lobby", false, false, "hgmapa", "slime_worlds");
    }

    /** Lê a configuração; {@code null} usa só os padrões. */
    public static HgSettings from(ConfigurationSection c) {
        HgSettings d = defaults();
        if (c == null) {
            return d;
        }
        double initial = Math.max(1, c.getDouble("border.initial-size", d.borderInitialSize));
        double fin = Math.max(1, c.getDouble("border.final-size", d.borderFinalSize));
        return new HgSettings(
                Math.max(1, c.getInt("game.min-players", d.minPlayers)),
                Math.max(0, c.getInt("game.countdown-seconds", d.countdownSeconds)),
                Math.max(0, c.getInt("game.grace-seconds", d.graceSeconds)),
                initial,
                Math.min(fin, initial),
                Math.max(0, c.getInt("border.shrink-seconds", d.borderShrinkSeconds)),
                Math.max(0, c.getInt("ending.delay-seconds", d.endingDelaySeconds)),
                Math.max(0, c.getInt("ending.lobby-transfer-wait-seconds", d.lobbyTransferWaitSeconds)),
                nonBlank(c.getString("ending.lobby-server"), d.lobbyServer),
                c.getBoolean("ending.shutdown-after-end", d.shutdownAfterEnd),
                c.getBoolean("ending.shutdown-on-rest", d.shutdownOnRest),
                nonBlank(c.getString("arena.template-world"), d.templateWorld),
                nonBlank(c.getString("arena.slime-directory"), d.slimeDirectory));
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
