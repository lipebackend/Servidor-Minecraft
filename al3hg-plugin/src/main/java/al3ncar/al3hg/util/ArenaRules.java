package al3ncar.al3hg.util;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;

import java.util.logging.Logger;

/** Regras do mundo da arena (antes {@code MobsControillers}); aplicadas uma vez ao criar a arena. */
public final class ArenaRules {

    private ArenaRules() {
    }

    @SuppressWarnings("deprecation")
    public static boolean apply(String worldName, Logger log) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            log.warning("Regras não aplicadas: mundo '" + worldName + "' não está carregado");
            return false;
        }
        world.setTime(12000);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        return true;
    }
}
