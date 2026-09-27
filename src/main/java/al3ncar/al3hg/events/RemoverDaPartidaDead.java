package al3ncar.al3hg.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import al3ncar.al3hg.partida.Maneger;
import al3ncar.al3hg.utils.EnviarServer;

public class RemoverDaPartidaDead implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        Maneger.getPartida().removerJogadores(p);
        atualizarContador();
    }

    @SuppressWarnings("deprecation")
    public void atualizarContador() {
        int vivos = Maneger.getPartida().getJogadoresVivos();

        Bukkit.broadcastMessage("§eJogadores vivos: §f" + vivos);
    }

    @EventHandler
    public void onRespwam(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        EnviarServer.SendServer(p, "lobby");
    }
}
