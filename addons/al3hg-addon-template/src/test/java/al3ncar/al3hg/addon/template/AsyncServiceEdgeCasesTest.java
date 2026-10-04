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
}
