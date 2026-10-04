package al3ncar.al3hg.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import al3ncar.al3hg.api.GameState;
import al3ncar.al3hg.game.GameManager;
import al3ncar.al3hg.util.LobbyTransfer;

public class PlayerDeathListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        GameManager.current().removerJogadores(p);
        p.setGameMode(GameMode.SPECTATOR);
        atualizarContador(p);
    }

    @SuppressWarnings("deprecation")
    public void atualizarContador(Player p) {
        int vivos = GameManager.current().getJogadoresVivos();
        if(GameManager.current().getJogadoresVivos() == 1){ 
            GameManager.current().setStatusP(GameState.ENDING); 
         }
        Bukkit.broadcastMessage("§eJogadores vivos: §f" + vivos);
    }

    @EventHandler
    public void onRespwam(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        LobbyTransfer.SendServer(p, "lobby");
    }
}
