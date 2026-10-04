package al3ncar.al3hg.addon.template;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Servico assincrono de exemplo. NAO usa Bukkit diretamente: recebe dois
 * executores por construtor (sem estado estatico), o que o torna testavel.
 *
 * <ul>
 *   <li>{@code ioExecutor}: executor PROPRIO do addon para I/O (arquivo, banco, HTTP);</li>
 *   <li>{@code mainThreadExecutor}: devolve a execucao para a thread principal
 *       (ver {@link TemplateAddon#mainThreadExecutor()}).</li>
 * </ul>
 *
 * Regra: dentro de {@code work} NUNCA toque na API do Bukkit; faca isso apenas em {@code onMainThread}.
 */
public final class AsyncService {

    private final Executor ioExecutor;
    private final Executor mainThreadExecutor;

    public AsyncService(Executor ioExecutor, Executor mainThreadExecutor) {
        this.ioExecutor = Objects.requireNonNull(ioExecutor, "ioExecutor");
        this.mainThreadExecutor = Objects.requireNonNull(mainThreadExecutor, "mainThreadExecutor");
    }

    /**
     * Executa {@code work} fora da thread principal e entrega o resultado em
     * {@code onMainThread}, ja de volta a thread principal.
     */
    public <T> CompletableFuture<Void> supplyThenSync(Supplier<T> work, Consumer<T> onMainThread) {
        Objects.requireNonNull(work, "work");
        Objects.requireNonNull(onMainThread, "onMainThread");
        return CompletableFuture
                .supplyAsync(work, ioExecutor)
                .thenAcceptAsync(onMainThread, mainThreadExecutor);
    }

    /** Exemplo de "trabalho pesado" puro: calcula algo sem tocar no Bukkit. */
    public static int expensiveCalculation(String input) {
        return input.strip().length();
    }
}
