package al3ncar.al3hg;

import al3ncar.al3hg.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import al3ncar.al3hg.arena.ArenaCleaner;
import al3ncar.al3hg.command.HgCommand;
import al3ncar.al3hg.craft.SoupRecipes;
import al3ncar.al3hg.game.GameManager;

public final class Al3HgPlugin extends JavaPlugin implements Listener {
    // ---------- Privetes -------------------- //
    private static Al3HgPlugin ints;
    private final HgCommand hgcore = new HgCommand();
    private final SoupHealListener rg = new SoupHealListener();
    private final KillNotifier killnotificador = new KillNotifier();
    private final PvpDamageListener cldano = new PvpDamageListener();
    private final PlayerConnectionListener jn = new PlayerConnectionListener();
    private final PlayerDeathListener rdead = new PlayerDeathListener();
    // ---------- Plugin -------------------- //

    @Override
    public void onEnable() {
        ints = this;
        ArenaCleaner.limparMapa(this,"hgmapa");
        getCommand("hgc").setExecutor(hgcore);
        Bukkit.getPluginManager().registerEvents(rg, ints);
        Bukkit.getPluginManager().registerEvents(jn, ints);
        Bukkit.getPluginManager().registerEvents(rdead, ints);
        Bukkit.getPluginManager().registerEvents(killnotificador, ints);
        Bukkit.getPluginManager().registerEvents(cldano, ints);
        GameManager.init();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        SoupRecipes.RegisterRecipeMethods();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // ---------- Metodos -------------------- //
    public static Al3HgPlugin getInts() {
        return ints;
    }
}