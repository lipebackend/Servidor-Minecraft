package al3ncar.al3hg.utils;

import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;


public class Barreiras {
    public static void Diminuir(String mapa,double tamanho) {
        WorldBorder bord = Bukkit.getWorld(mapa).getWorldBorder();
        bord.setSize(tamanho);
    }
}
