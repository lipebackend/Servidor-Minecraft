package al3ncar.al3hg.listener;

import al3ncar.al3hg.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;


public class KillNotifier implements Listener {
    @EventHandler 
    public void Notifcar(PlayerDeathEvent e){
        Player p = e.getPlayer();
        for(Player h : Bukkit.getOnlinePlayers()){
            String msg = String.format("O Player: %s matou o player %s",p,h);
            p.setKiller(h);
            Bukkit.broadcastMessage(msg);
        }

    }
}
