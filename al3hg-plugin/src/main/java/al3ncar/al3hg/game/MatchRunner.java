package al3ncar.al3hg.game;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import al3ncar.al3hg.api.GameState;

public class MatchRunner {
    public boolean Partidakk() {
        if (GameManager.current().getStatusP() == GameState.RUNNING) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.SURVIVAL);
            }
            return true;
        }
        return false;
    }
}
