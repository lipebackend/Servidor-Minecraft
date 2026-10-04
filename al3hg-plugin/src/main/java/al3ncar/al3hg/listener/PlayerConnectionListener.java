package al3ncar.al3hg.listener;

import al3ncar.al3hg.enums.PvpStatus;
import al3ncar.al3hg.util.WorldBorderController;
import al3ncar.al3hg.util.ArenaRules;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import al3ncar.al3hg.util.Messages;
import al3ncar.al3hg.game.GameManager;


public class PlayerConnectionListener implements Listener {
    private String ps = Messages.PREFIXO;
    private final ArenaRules mobs = new ArenaRules();
    @EventHandler
    public void JoinTituleServer(PlayerJoinEvent e) {
        mobs.ControlerMax();
        Player p = e.getPlayer();
        p.setGameMode(GameMode.ADVENTURE);
        GameManager.current().adicionarJogadores(p);
        WorldBorderController.shrink("hgmapa", 100);
        p.sendMessage(ps + "Bem vindo(a) a partida já ira começar");  
    }
}
