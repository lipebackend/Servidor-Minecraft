package al3ncar.al3hg.events;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Regem implements Listener {
    @EventHandler
    public void UsarSopa(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.MUSHROOM_STEW)
            return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (player.getHealth() == player.getAttribute(Attribute.MAX_HEALTH).getValue())
            return;
        double vv = player.getHealth() + 3.0;
        player.setHealth(Math.min(vv, player.getAttribute(Attribute.MAX_HEALTH).getValue()));
        item.setAmount(item.getAmount() - 1);
        player.getInventory().addItem(new ItemStack(Material.BOWL));
        e.setCancelled(true);
    }
}
