package al3ncar.al3hg.utils;

import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;

import al3ncar.al3hg.partida.Maneger;
import al3ncar.al3hg.partida.StatsPartida;

public class Borreiras {
    public static void Diminuir(String mapa) {
        WorldBorder bord = Bukkit.getWorld(mapa).getWorldBorder();
        switch (Maneger.getPartida().getStatusP()) {
            case StatsPartida.INCIOS:
                bord.setSize(500);
                break;
            case StatsPartida.MEIO:
                bord.setSize(300);
                break;
            case StatsPartida.FINAL:
                bord.setSize(10);
                break;
            default:
                bord.setSize(100);
                break;
        }
    }
}
