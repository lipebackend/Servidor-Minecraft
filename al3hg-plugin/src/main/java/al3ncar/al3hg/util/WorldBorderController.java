package al3ncar.al3hg.util;

import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;


public class WorldBorderController {
    public static void shrink(String mapa,double tamanho) {
        WorldBorder bord = Bukkit.getWorld(mapa).getWorldBorder();
        bord.setSize(tamanho);
    }
}
