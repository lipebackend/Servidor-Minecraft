package al3ncar.al3hg.listener;

import al3ncar.al3hg.game.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Entrada e saída de jogadores (antes {@code JoinManeger}). Não mexe mais na border nem nas regras do
 * mundo a cada join, e trata o {@code PlayerQuitEvent}.
 */
public final class PlayerConnectionListener implements Listener {

    private final GameManager game;

    public PlayerConnectionListener(GameManager game) {
        this.game = game;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        game.handleJoin(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        game.handleQuit(e.getPlayer());
    }
}
