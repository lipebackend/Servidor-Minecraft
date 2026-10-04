package al3ncar.al3hg.listener;

import al3ncar.al3hg.api.event.HgPlayerEliminatedEvent;
import al3ncar.al3hg.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Morte e respawn (antes {@code RemoverDaPartidaDead}). Quem morre sai dos vivos e vira espectador de forma
 * estável: respawna na arena e o modo SPECTATOR é aplicado um tick depois. Ninguém é mais enviado ao lobby no respawn.
 */
public final class PlayerDeathListener implements Listener {

    private final GameManager game;

    public PlayerDeathListener(GameManager game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();
        if (!game.isAlive(dead.getUniqueId())) {
            return;
        }
        Player killer = dead.getKiller();
        game.eliminate(dead.getUniqueId(), killer == null ? null : killer.getUniqueId(),
                HgPlayerEliminatedEvent.Reason.DEATH);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        game.respawnLocation(p).ifPresent(e::setRespawnLocation);
        if (game.isSpectator(p.getUniqueId())) {
            game.applySpectatorNextTick(p);
        }
    }
}
