package al3ncar.al3hg;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import al3ncar.al3hg.command.HgCore;
import al3ncar.al3hg.command.HgManegar;
import al3ncar.al3hg.events.Refil;
import al3ncar.al3hg.events.Regem;

public final class al3hg extends JavaPlugin implements Listener {
    // ---------- Privetes -------------------- //
    private static al3hg ints;
    private final HgCore hgcore = new HgCore();
    private final HgManegar hgmanegar = new HgManegar();
    private final Regem rg = new Regem();
    // ---------- Plugin -------------------- //

    @Override
    public void onEnable() {
        ints = this;
        getCommand("hgc").setExecutor(hgcore);
        getCommand("hgm").setExecutor(hgmanegar);
        Refil.RegisterRecipeMethods();
        Bukkit.getPluginManager().registerEvents(rg, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // ---------- Metodos -------------------- //
    public static al3hg getInts() {
        return ints;
    }
}