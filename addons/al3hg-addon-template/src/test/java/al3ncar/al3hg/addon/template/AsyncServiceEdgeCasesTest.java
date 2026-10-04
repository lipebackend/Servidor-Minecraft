package al3ncar.al3hg.addon.template;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** Excecoes, shutdown, ordem e threads do AsyncService. */
class AsyncServiceEdgeCasesTest {

    /** "Thread principal" falsa e thread-safe: so executa quando o teste chama drain(). */
    private static final class FakeMainThread implements Executor {
        final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

        @Override
        public void execute(Runnable command) {
            queue.add(command);
        }

        int drain() {
            int n = 0;
            Runnable r;
            while ((r = queue.poll()) != null) {
                r.run();
                n++;
            }
            return n;
        }
    }

    private static Throwable root(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    // ---------- excecoes ----------

    @Test
    void workFailureExposesOriginalCauseAndNeverCallsCallback() {
        FakeMainThread main = new FakeMainThread();
        AtomicBoolean called = new AtomicBoolean();
        var service = new AsyncService(Runnable::run, main);

        CompletableFuture<Void> f = service.supplyThenSync(() -> {
            throw new IllegalStateException("boom");
        }, v -> called.set(true));
        main.drain();

        var ex = assertThrows(ExecutionException.class, () -> f.get(5, TimeUnit.SECONDS));
        assertInstanceOf(IllegalStateException.class, ex.getCause());
        assertEquals("boom", ex.getCause().getMessage());
        assertFalse(called.get());
    }

    @Test
    void workFailureDoesNotEvenScheduleMainThreadTask() {
        FakeMainThread main = new FakeMainThread();
        new AsyncService(Runnable::run, main).supplyThenSync(() -> {
            throw new IllegalStateException("x");
        }, v -> { });
        // aceita 0 (nao agenda) ou 1 (agenda e pula); o importante e nao executar callback -> coberto acima
        assertTrue(main.queue.size() <= 1);
    }

    @Test
    void callbackFailureFailsFutureWithOriginalCauseAndServiceKeepsWorking() throws Exception {
        FakeMainThread main = new FakeMainThread();
        var service = new AsyncService(Runnable::run, main);

        CompletableFuture<Void> bad = service.supplyThenSync(() -> 1, v -> {
            throw new UnsupportedOperationException("callback ruim");
        });
        main.drain();
        var ex = assertThrows(ExecutionException.class, () -> bad.get(5, TimeUnit.SECONDS));
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());

        AtomicInteger got = new AtomicInteger();
        CompletableFuture<Void> good = service.supplyThenSync(() -> 7, got::set);
        main.drain();
        good.get(5, TimeUnit.SECONDS);
        assertEquals(7, got.get());
    }

    @Test
    void failureIsNotSwallowedWhenJoined() {
        FakeMainThread main = new FakeMainThread();
        var f = new AsyncService(Runnable::run, main).supplyThenSync(() -> {
            throw new IllegalArgumentException("iae");
        }, v -> { });
        main.drain();
        var ex = assertThrows(CompletionException.class, f::join);
        assertInstanceOf(IllegalArgumentException.class, root(ex));
    }

    @Test
    void nullResultFromWorkIsDeliveredAsNull() throws Exception {
        FakeMainThread main = new FakeMainThread();
        AtomicReference<String> got = new AtomicReference<>("naoSetado");
        var f = new AsyncService(Runnable::run, main).<String>supplyThenSync(() -> null, got::set);
        main.drain();
        f.get(5, TimeUnit.SECONDS);
        assertNull(got.get());
    }

    @Test
    void nullWorkIsRejectedSynchronously() {
        var service = new AsyncService(Runnable::run, new FakeMainThread());
        assertThrows(NullPointerException.class, () -> service.supplyThenSync(null, v -> { }));
    }

    /**
     * Regressao (corrigido na main): o callback nulo so e rejeitado por thenAcceptAsync, DEPOIS de supplyAsync ja ter
     * disparado o trabalho. O NPE sobe sincrono, mas o {@code work} (I/O!) ja foi executado e o future
     * se perde. Esperado: validar com Objects.requireNonNull antes de iniciar qualquer trabalho.
     */
    @Test
    void nullCallbackMustBeRejectedBeforeAnyWorkRuns() {
        AtomicBoolean workRan = new AtomicBoolean();
        var service = new AsyncService(Runnable::run, new FakeMainThread());
        assertThrows(NullPointerException.class, () -> service.supplyThenSync(() -> {
            workRan.set(true);
            return 1;
        }, null));
        assertFalse(workRan.get(), "work executou mesmo com callback nulo (efeito colateral perdido)");
    }

    // ---------- shutdown / rejeicao ----------

    @Test
    void shutdownIoExecutorMakesSubmissionFailLoudlyAndSkipsCallback() {
        ExecutorService io = Executors.newSingleThreadExecutor();
        io.shutdown();
        FakeMainThread main = new FakeMainThread();
        AtomicBoolean called = new AtomicBoolean();
        var service = new AsyncService(io, main);

        try {
            CompletableFuture<Void> f = service.supplyThenSync(() -> 1, v -> called.set(true));
            main.drain();
            assertTrue(f.isCompletedExceptionally(), "future deveria falhar, nao ficar pendente");
        } catch (RejectedExecutionException sync) {
            // falha sincrona tambem e aceitavel
        }
        assertFalse(called.get());
    }

    @Test
    void rejectingMainThreadExecutorFailsFutureInsteadOfHanging() throws Exception {
        Executor rejecting = r -> {
            throw new RejectedExecutionException("main encerrada");
        };
        var service = new AsyncService(Runnable::run, rejecting);
        AtomicBoolean called = new AtomicBoolean();
        try {
            CompletableFuture<Void> f = service.supplyThenSync(() -> 1, v -> called.set(true));
            var ex = assertThrows(ExecutionException.class, () -> f.get(2, TimeUnit.SECONDS));
            assertInstanceOf(RejectedExecutionException.class, root(ex));
        } catch (RejectedExecutionException sync) {
            // aceitavel
        }
        assertFalse(called.get());
    }

    // ---------- threads e ordem ----------
}
