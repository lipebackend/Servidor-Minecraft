package al3ncar.al3hg;

import al3ncar.al3hg.api.HgGame;
import al3ncar.al3hg.arena.ArenaProvider;
import al3ncar.al3hg.arena.AspArenaProvider;
import al3ncar.al3hg.command.HgCommand;
import al3ncar.al3hg.config.HgSettings;
import al3ncar.al3hg.craft.SoupRecipes;
import al3ncar.al3hg.game.GameManager;
import al3ncar.al3hg.listener.PlayerConnectionListener;
import al3ncar.al3hg.listener.PlayerDeathListener;
import al3ncar.al3hg.listener.PvpDamageListener;
import al3ncar.al3hg.listener.SoupHealListener;
import al3ncar.al3hg.util.LobbyTransfer;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/** Classe principal (antes {@code al3hg}). Sem estado estático: tudo é injetado pelo construtor. */
public final class Al3HgPlugin extends JavaPlugin {

    private volatile HgSettings settings = HgSettings.defaults();
    private ArenaProvider arena;
    private GameManager game;
    private SoupRecipes recipes;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = HgSettings.from(getConfig());

        AdvancedSlimePaperAPI asp;
        try {
            asp = AdvancedSlimePaperAPI.instance();
        } catch (LinkageError | RuntimeException e) {
            getLogger().severe("AdvancedSlimePaper não encontrado: o al3HG exige o servidor AdvancedSlimePaper 4.2.0. " + e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        arena = new AspArenaProvider(asp, this::settings, getLogger());
        arena.reloadTemplate();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        game = new GameManager(this, arena, new LobbyTransfer(this, this::settings), this::settings);
        getServer().getServicesManager().register(HgGame.class, game, this, ServicePriority.Normal);

        getServer().getPluginManager().registerEvents(new SoupHealListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(game), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(game), this);
        getServer().getPluginManager().registerEvents(new PvpDamageListener(game), this);

        PluginCommand hgc = getCommand("hgc");
        if (hgc != null) {
            HgCommand executor = new HgCommand(this, game);
            hgc.setExecutor(executor);
            hgc.setTabCompleter(executor);
        }

        recipes = new SoupRecipes(this);
        recipes.register();
    }

    @Override
    public void onDisable() {
        if (recipes != null) {
            recipes.unregister();
        }
        if (game != null) {
            // cancela tasks e descarta mundos carregados com Bukkit.unloadWorld(nome, false)
            game.shutdown();
            getServer().getServicesManager().unregister(HgGame.class, game);
        }
    }

    public HgSettings settings() {
        return settings;
    }

    /** /hgc reload: relê config.yml e o mapa-modelo. */
    public void reloadSettings() {
        reloadConfig();
        settings = HgSettings.from(getConfig());
        if (arena != null) {
            arena.reloadTemplate();
        }
    }
}
