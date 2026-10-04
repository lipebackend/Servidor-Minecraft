package al3ncar.al3hg.listener;

import al3ncar.al3hg.api.HgGame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Cancela dano jogador x jogador enquanto o PVP estiver DESLIGADO (corrigido: a lógica
 * original estava invertida — com PVP desligado não cancelava nada).
 */
public final class PvpDamageListener implements Listener {

    private final HgGame game;

    public PvpDamageListener(HgGame game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (game.isPvpEnabled() || !(e.getEntity() instanceof Player)) {
            return;
        }
        boolean playerAttacker = e.getDamager() instanceof Player
                || (e.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player);
        if (playerAttacker) {
            e.setCancelled(true);
        }
    }
}
