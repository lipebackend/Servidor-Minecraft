package al3ncar.al3hg.addon.template;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AsyncServiceTest {

    /** "Thread principal" falsa: so executa quando o teste manda. */
    private static final class FakeMainThread implements Executor {
        final Queue<Runnable> queue = new ArrayDeque<>();

        @Override
        public void execute(Runnable command) {
            queue.add(command);
        }

        void drain() {
            Runnable r;
            while ((r = queue.poll()) != null) {
                r.run();
            }
        }
    }

    @Test
    void resultIsDeliveredOnlyThroughMainThreadExecutor() throws Exception {
        FakeMainThread main = new FakeMainThread();
        AtomicReference<Integer> received = new AtomicReference<>();
        AtomicReference<Thread> workThread = new AtomicReference<>();

        AsyncService service = new AsyncService(Runnable::run, main);
        // ioExecutor sincrono: o trabalho roda, mas a entrega fica pendente ate o "tick"
        CompletableFuture<Void> future = service.supplyThenSync(() -> {
            workThread.set(Thread.currentThread());
            return AsyncService.expensiveCalculation("  abc ");
        }, received::set);

        assertNull(received.get());
        assertFalse(future.isDone());
        main.drain();
        assertEquals(3, received.get());
        assertTrue(future.isDone());
    }

    @Test
    void workFailureSkipsMainThreadCallbackAndFailsFuture() {
        FakeMainThread main = new FakeMainThread();
        AtomicReference<Object> received = new AtomicReference<>();
        AsyncService service = new AsyncService(Runnable::run, main);

        CompletableFuture<Void> future = service.supplyThenSync(() -> {
            throw new IllegalStateException("boom");
        }, received::set);

        main.drain();
        assertNull(received.get());
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    void nullExecutorsAreRejected() {
        assertThrows(NullPointerException.class, () -> new AsyncService(null, Runnable::run));
        assertThrows(NullPointerException.class, () -> new AsyncService(Runnable::run, null));
    }
}
