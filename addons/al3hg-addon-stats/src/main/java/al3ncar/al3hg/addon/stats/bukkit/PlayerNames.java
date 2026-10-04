package al3ncar.al3hg.addon.stats.bukkit;

import org.bukkit.OfflinePlayer;

/** Nome para exibir de um jogador, mesmo offline e sem nome no cache. */
final class PlayerNames {

    private PlayerNames() {
    }

    static String of(OfflinePlayer player) {
        String name = player.getName();
        return name != null ? name : player.getUniqueId().toString();
    }
}
