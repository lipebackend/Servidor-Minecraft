package al3ncar.al3hg.partida;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PartidaRolando {
    public boolean Partidakk() {
        if (Maneger.getPartida().getStatusP() == StatsPartida.MEIO) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.SURVIVAL);
            }
            return true;
        }
        return true;
    }
}
