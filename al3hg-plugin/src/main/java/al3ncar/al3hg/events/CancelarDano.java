package al3ncar.al3hg.events;

import al3ncar.al3hg.enums.StatusPvp;
import al3ncar.al3hg.partida.Maneger;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CancelarDano implements Listener {
    @EventHandler
    public void onDano(EntityDamageByEntityEvent e) {
        if (Maneger.getPartida().getStatusPvp() == StatusPvp.OFF) return;
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
