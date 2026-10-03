package al3ncar.al3hg.events;

import al3ncar.al3hg.enums.StatusPvp;
import al3ncar.al3hg.utils.Barreiras;
import al3ncar.al3hg.utils.MobsControillers;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import al3ncar.al3hg.utils.PrefixoC;
import al3ncar.al3hg.partida.Maneger;


public class JoinManeger implements Listener {
    private String ps = PrefixoC.PREFIXO;
    private final MobsControillers mobs = new MobsControillers();
    @EventHandler
    public void JoinTituleServer(PlayerJoinEvent e) {
        mobs.ControlerMax();
        Player p = e.getPlayer();
        p.setGameMode(GameMode.ADVENTURE);
        Maneger.getPartida().adicionarJogadores(p);
        Barreiras.Diminuir("hgmapa", 100);
        p.sendMessage(ps + "Bem vindo(a) a partida já ira começar");  
    }
}
