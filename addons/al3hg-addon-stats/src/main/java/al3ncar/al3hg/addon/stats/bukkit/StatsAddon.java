package al3ncar.al3hg.addon.stats.bukkit;

import al3ncar.al3hg.addon.stats.repository.SqliteStatsRepository;
import al3ncar.al3hg.addon.stats.repository.StatsRepository;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * Addon de estatisticas do al3HG. Sem estado estatico: tudo e criado no {@link #onEnable()}
 * e injetado pelo construtor. O I/O roda na thread propria do {@link SqliteStatsRepository};
 * a API do Bukkit so e usada na thread principal.
 */
public final class StatsAddon extends JavaPlugin {

    private StatsRepository repository;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        StatsSettings settings = StatsSettings.from(getConfig(), getLogger());

        try {
            // O driver vem de "libraries:" (classloader do plugin): registra-o explicitamente no DriverManager.
            Class.forName("org.sqlite.JDBC");
            Path database = getDataFolder().toPath().resolve(settings.databaseFile());
            Path parent = database.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            repository = new SqliteStatsRepository(database);
        } catch (ClassNotFoundException | IOException e) {
            getLogger().severe("Não foi possível iniciar o banco de estatísticas: " + e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        StatsService service = new StatsService(repository, settings.rules(), getLogger());
        getServer().getPluginManager().registerEvents(
                new HgApiAdapter(new StatsListener(service, settings.quitCountsAsDeath())), this);

        Executor mainThread = mainThreadExecutor();
        register("hgstats", new HgStatsCommand(service, mainThread));
        register("hgtop", new HgTopCommand(service, mainThread, settings.topSize()));
    }

    @Override
    public void onDisable() {
        if (repository != null) {
            repository.close(); // drena as gravacoes pendentes (shutdown), nao as descarta
            repository = null;
        }
    }

    private <T extends CommandExecutor & TabCompleter> void register(
            String name, T handler) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Comando /" + name + " ausente do plugin.yml");
            return;
        }
        command.setExecutor(handler);
        command.setTabCompleter(handler);
    }

    /** Executor que roda na thread principal via scheduler. */
    private Executor mainThreadExecutor() {
        return task -> {
            if (!isEnabled()) {
                // desabilitando: o scheduler recusaria a tarefa; falha o future em vez de deixa-lo pendente
                throw new RejectedExecutionException("Addon desabilitado");
            }
            getServer().getScheduler().runTask(this, task);
        };
    }
}
