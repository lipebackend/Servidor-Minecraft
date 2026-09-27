package al3ncar.al3hg.events;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import al3ncar.al3hg.partida.Maneger;
import al3ncar.al3hg.partida.StatsPartida;

public class JoinManeger implements Listener {
    @EventHandler
    public void JoinTituleServer(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.setGameMode(GameMode.ADVENTURE);
        Maneger.getPartida().adicionarJogadores(p);
        p.sendMessage("Iniciando a partida em alguns segundos");
        if (Maneger.getPartida().getJogadoresVivos() >= 2) {
            Maneger.getPartida().setStatusP(StatsPartida.MEIO);
        }
    }
}
