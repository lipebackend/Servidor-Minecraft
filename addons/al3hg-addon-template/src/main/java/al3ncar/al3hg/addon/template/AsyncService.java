package al3ncar.al3hg.addon.template;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
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
        CompletableFuture<T> workFuture = new CompletableFuture<>();
        try {
            ioExecutor.execute(new PendingTask<>(work, workFuture));
        } catch (RuntimeException e) { // executor encerrado: RejectedExecutionException
            workFuture.completeExceptionally(e);
        }
        return workFuture.thenAcceptAsync(onMainThread, mainThreadExecutor);
    }

    /**
     * Completa com {@link RejectedExecutionException} os futures das tarefas que o
     * executor descartou (retorno de {@code ExecutorService.shutdownNow()}). Sem isso,
     * as tarefas que ficaram na fila deixariam seus futures pendentes para sempre.
     *
     * @return quantas tarefas deste servico foram rejeitadas (as demais sao ignoradas)
     */
    public static int rejectPending(Collection<Runnable> dropped) {
        Objects.requireNonNull(dropped, "dropped");
        int rejected = 0;
        for (Runnable task : dropped) {
            if (task instanceof PendingTask<?> pending) {
                pending.reject();
                rejected++;
            }
        }
        return rejected;
    }

    /** Tarefa que guarda o proprio future, para poder ser rejeitada se descartada da fila. */
    private static final class PendingTask<T> implements Runnable {
        private final Supplier<T> work;
        private final CompletableFuture<T> future;

        PendingTask(Supplier<T> work, CompletableFuture<T> future) {
            this.work = work;
            this.future = future;
        }

        @Override
        public void run() {
            try {
                future.complete(work.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }

        void reject() {
            future.completeExceptionally(new RejectedExecutionException("Executor encerrado antes de executar a tarefa"));
        }
    }

    /** Exemplo de "trabalho pesado" puro: calcula algo sem tocar no Bukkit. */
    public static int expensiveCalculation(String input) {
        return input.strip().length();
    }
}
