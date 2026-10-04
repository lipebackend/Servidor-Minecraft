package al3ncar.al3hg.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;

import java.util.logging.Logger;

/** Controla a border do mundo da arena (com checagem de mundo nulo e tempo de transição). */
public final class WorldBorderController {

    private WorldBorderController() {
    }

    /**
     * Define o tamanho da border do mundo.
     *
     * @param seconds duração da transição; {@code <= 0} aplica imediatamente
     * @return {@code false} se o mundo não está carregado (nada é feito, sem exceção)
     */
    public static boolean shrink(String worldName, double size, long seconds, Logger log) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            log.warning("Border não aplicada: mundo '" + worldName + "' não está carregado");
            return false;
        }
        WorldBorder border = world.getWorldBorder();
        if (seconds <= 0) {
            border.setSize(size);
        } else {
            border.setSize(size, seconds);
        }
        return true;
    }
}
