package al3ncar.al3hg.events;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
 
import al3ncar.al3hg.utils.PrefixoC;
import al3ncar.al3hg.partida.Maneger;

public class JoinManeger implements Listener {
    private String ps = PrefixoC.PREFIXO;
    @EventHandler
    public void JoinTituleServer(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.setGameMode(GameMode.ADVENTURE);
        Maneger.getPartida().adicionarJogadores(p);
        p.sendMessage(ps + "Iniciando a partida em alguns segundos");
    }
}
