package al3ncar.al3hg.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import al3ncar.al3hg.al3hg;

public class Refil {
    public static void RegisterRecipeMethods() {
        // Namespaces //
        NamespacedKey SopaCocoa = new NamespacedKey(al3hg.getInts(), "sopa_de_cocoa");
        NamespacedKey SopaCacto = new NamespacedKey(al3hg.getInts(), "sopa_de_cacto");
        // retorno //
        ItemStack sopaFeita = new ItemStack(Material.MUSHROOM_STEW);
        // coisas que eu registro o craft //

        ShapelessRecipe sopaCocoa = new ShapelessRecipe(SopaCocoa, sopaFeita);
        sopaCocoa.addIngredient(Material.COCOA_BEANS);
        sopaCocoa.addIngredient(Material.BOWL);

        ShapelessRecipe sopaCacto = new ShapelessRecipe(SopaCacto, sopaFeita);
        sopaCacto.addIngredient(Material.CACTUS);
        sopaCacto.addIngredient(Material.BOWL);

        // Registro da receipe //
        Bukkit.getServer().addRecipe(sopaCocoa);
        Bukkit.getServer().addRecipe(sopaCacto);
    }
}