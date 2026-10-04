package al3ncar.al3hg.utils;

import al3ncar.al3hg.al3hg;
import al3ncar.al3hg.enums.StatusPvp;
import al3ncar.al3hg.partida.Maneger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class AutolizarPvp {
    public void mexernopvp(StatusPvp pvp, Plugin plugin){
        switch (pvp){
            case ON: {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Maneger.getPartida().setPvpStatus(StatusPvp.ON);
                    Bukkit.broadcastMessage(PrefixoC.PREFIXO + "§cPVP LIBERADO!");
                }, 20L * 120);
            }
            case OFF: {
                System.out.println("....");
            }
        }
    }
}
