package al3ncar.sopaRegemClick;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import al3ncar.sopaRegemClick.command.SopaReload;
import al3ncar.sopaRegemClick.events.Regem;

public final class SopaRegemClick extends JavaPlugin implements Listener {
    private final SopaReload sop = new SopaReload();
    private final Regem rg = new Regem();

    @Override
    public void onEnable() {
        getCommand("sopa").setExecutor(sop);
        Bukkit.getPluginManager().registerEvents(rg, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}