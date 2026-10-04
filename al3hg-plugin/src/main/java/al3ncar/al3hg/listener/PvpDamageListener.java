package al3ncar.al3hg.listener;

import al3ncar.al3hg.enums.PvpStatus;
import al3ncar.al3hg.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpDamageListener implements Listener {
    @EventHandler
    public void onDano(EntityDamageByEntityEvent e) {
        if (GameManager.current().getStatusPvp() == PvpStatus.OFF) return;
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
        // cobrir flechas/projéteis:
        if (e.getDamager() instanceof Projectile p
                && p.getShooter() instanceof Player
                && e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }
}
