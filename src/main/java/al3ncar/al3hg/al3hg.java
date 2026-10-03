package al3ncar.al3hg;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


import al3ncar.al3hg.mapa.RegeneretorWorld;
import al3ncar.al3hg.command.HgCore;
import al3ncar.al3hg.craft.Refil;
import al3ncar.al3hg.events.JoinManeger;
import al3ncar.al3hg.events.Regem;
import al3ncar.al3hg.events.RemoverDaPartidaDead;
import al3ncar.al3hg.partida.Maneger;

public final class al3hg extends JavaPlugin implements Listener {
    // ---------- Privetes -------------------- //
    private static al3hg ints;
    private final HgCore hgcore = new HgCore();
    private final Regem rg = new Regem();
    private final JoinManeger jn = new JoinManeger();
    private final RemoverDaPartidaDead rdead = new RemoverDaPartidaDead();
    // ---------- Plugin -------------------- //

    @Override
    public void onEnable() {
        ints = this;
        RegeneretorWorld.limparMapa(this,"hgmapa");
        getCommand("hgc").setExecutor(hgcore);
        Bukkit.getPluginManager().registerEvents(rg, ints);
        Bukkit.getPluginManager().registerEvents(jn, ints);
        Bukkit.getPluginManager().registerEvents(rdead, ints);
        Maneger.init();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Refil.RegisterRecipeMethods();
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