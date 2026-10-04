package al3ncar.al3hg.util;

import al3ncar.al3hg.Al3HgPlugin;
import al3ncar.al3hg.enums.PvpStatus;
import al3ncar.al3hg.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PvpTimer {
    public void mexernopvp(PvpStatus pvp, Plugin plugin){
        switch (pvp){
            case ON: {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    GameManager.current().setPvpStatus(PvpStatus.ON);
                    Bukkit.broadcastMessage(Messages.PREFIXO + "§cPVP LIBERADO!");
                }, 20L * 120);
            }
            case OFF: {
                System.out.println("....");
            }
        }
    }
}
