package al3ncar.al3hg.addon.template;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executor;

/**
 * Classe principal do addon. Sem estado estatico: tudo e criado em
 * {@link #onEnable()} e injetado pelo construtor.
 */
public final class TemplateAddon extends JavaPlugin {

    private ExecutorService ioExecutor;

    @Override
    public void onEnable() {
        // Executor proprio para I/O (nunca use o ForkJoinPool comum do servidor).
        ioExecutor = Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "al3hg-template-io");
            thread.setDaemon(true);
            return thread;
        });

        AsyncService asyncService = new AsyncService(ioExecutor, mainThreadExecutor());
        getServer().getPluginManager().registerEvents(new ExampleListener(asyncService, getLogger()), this);
    }

    @Override
    public void onDisable() {
        if (ioExecutor != null) {
            ioExecutor.shutdown();
            try {
                if (!ioExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    ioExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                ioExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        // As tarefas do scheduler deste plugin sao canceladas pelo proprio Bukkit ao desabilitar.
    }

    /** Executor que roda na thread principal via scheduler. */
    public Executor mainThreadExecutor() {
        return task -> {
            if (!isEnabled()) {
                return; // plugin desabilitando: o scheduler recusaria a tarefa
            }
            getServer().getScheduler().runTask(this, task);
        };
    }
}
