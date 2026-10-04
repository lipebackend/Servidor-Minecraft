package al3ncar.al3hg.craft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;

import java.util.List;

/** Receitas das sopas (antes {@code Refil}); agora sem singleton estático. */
public final class SoupRecipes {

    private final List<NamespacedKey> keys;

    public SoupRecipes(Plugin plugin) {
        this.keys = List.of(new NamespacedKey(plugin, "sopa_de_cocoa"), new NamespacedKey(plugin, "sopa_de_cacto"));
    }

    public void register() {
        ItemStack soup = new ItemStack(Material.MUSHROOM_STEW);

        ShapelessRecipe cocoa = new ShapelessRecipe(keys.get(0), soup);
        cocoa.addIngredient(Material.COCOA_BEANS);
        cocoa.addIngredient(Material.BOWL);

        ShapelessRecipe cactus = new ShapelessRecipe(keys.get(1), soup);
        cactus.addIngredient(Material.CACTUS);
        cactus.addIngredient(Material.BOWL);

        Bukkit.addRecipe(cocoa);
        Bukkit.addRecipe(cactus);
    }

    /** Remove as receitas (onDisable), para não duplicar em /reload. */
    public void unregister() {
        keys.forEach(Bukkit::removeRecipe);
    }
}
