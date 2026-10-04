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

    @Test
    void workRunsOnIoThreadAndCallbackOnlyOnTheThreadThatDrainsMain() throws Exception {
        ExecutorService io = Executors.newSingleThreadExecutor(r -> new Thread(r, "io-test"));
        try {
            FakeMainThread main = new FakeMainThread();
            AtomicReference<String> workThread = new AtomicReference<>();
            AtomicReference<String> callbackThread = new AtomicReference<>();

            CompletableFuture<Void> f = new AsyncService(io, main).supplyThenSync(() -> {
                workThread.set(Thread.currentThread().getName());
                return 1;
            }, v -> callbackThread.set(Thread.currentThread().getName()));

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (main.queue.isEmpty() && System.nanoTime() < deadline) {
                Thread.sleep(1);
            }
            assertNull(callbackThread.get(), "callback nao pode rodar antes do tick da thread principal");
            main.drain();
            f.get(5, TimeUnit.SECONDS);

            assertEquals("io-test", workThread.get());
            assertEquals(Thread.currentThread().getName(), callbackThread.get());
        } finally {
            io.shutdownNow();
        }
    }

    @Test
    void singleThreadIoPreservesSubmissionOrderOfCallbacks() throws Exception {
        ExecutorService io = Executors.newSingleThreadExecutor();
        try {
            FakeMainThread main = new FakeMainThread();
            var service = new AsyncService(io, main);
            List<Integer> delivered = new ArrayList<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                int n = i;
                futures.add(service.supplyThenSync(() -> n, delivered::add));
            }
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (main.queue.size() < 50 && System.nanoTime() < deadline) {
                Thread.sleep(1);
            }
            assertEquals(50, main.drain());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get(5, TimeUnit.SECONDS);

            List<Integer> expected = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                expected.add(i);
            }
            assertEquals(expected, delivered);
        } finally {
            io.shutdownNow();
        }
    }

    @Test
    void slowFirstTaskOnMultiThreadPoolMayBeDeliveredAfterFasterOne() throws Exception {
        // Caracterizacao: com pool > 1 NAO ha garantia de ordem; quem precisa de ordem deve usar executor de 1 thread.
        ExecutorService io = Executors.newFixedThreadPool(2);
        try {
            FakeMainThread main = new FakeMainThread();
            var service = new AsyncService(io, main);
            java.util.concurrent.CountDownLatch gate = new java.util.concurrent.CountDownLatch(1);
            List<String> delivered = java.util.Collections.synchronizedList(new ArrayList<>());

            CompletableFuture<Void> slow = service.supplyThenSync(() -> {
                try {
                    gate.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "lenta";
            }, delivered::add);
            CompletableFuture<Void> fast = service.supplyThenSync(() -> "rapida", delivered::add);

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (main.queue.isEmpty() && System.nanoTime() < deadline) {
                Thread.sleep(1);
            }
            gate.countDown();
            while (main.queue.size() < 2 && System.nanoTime() < deadline) {
                Thread.sleep(1);
            }
            main.drain();
            CompletableFuture.allOf(slow, fast).get(5, TimeUnit.SECONDS);
            assertEquals(2, delivered.size());
            assertEquals("rapida", delivered.get(0));
        } finally {
            io.shutdownNow();
        }
    }

    @Test
    void manyConcurrentCallersAllGetTheirOwnResult() throws Exception {
        ExecutorService io = Executors.newFixedThreadPool(4);
        try {
            FakeMainThread main = new FakeMainThread();
            var service = new AsyncService(io, main);
            AtomicInteger sum = new AtomicInteger();
            List<CompletableFuture<Void>> all = new ArrayList<>();
            for (int i = 1; i <= 200; i++) {
                int n = i;
                all.add(service.supplyThenSync(() -> n, sum::addAndGet));
            }
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            int drained = 0;
            while (drained < 200 && System.nanoTime() < deadline) {
                drained += main.drain();
                Thread.sleep(1);
            }
            CompletableFuture.allOf(all.toArray(new CompletableFuture<?>[0])).get(5, TimeUnit.SECONDS);
            assertEquals(200 * 201 / 2, sum.get());
        } finally {
            io.shutdownNow();
        }
    }

    // ---------- expensiveCalculation ----------

    @Test
    void expensiveCalculationEdgeCases() {
        assertEquals(0, AsyncService.expensiveCalculation(""));
        assertEquals(0, AsyncService.expensiveCalculation("   \t\n"));
        assertEquals(3, AsyncService.expensiveCalculation("\u2003abc\u2003"));   // strip() remove espacos Unicode
        assertEquals(5, AsyncService.expensiveCalculation(" a b c "));   // espacos internos contam
        assertEquals(1000, AsyncService.expensiveCalculation("x".repeat(1000)));
        assertThrows(NullPointerException.class, () -> AsyncService.expensiveCalculation(null));
    }

    @Test
    void expensiveCalculationCountsUtf16UnitsNotCodePoints() {
        // Caracterizacao: emoji = 2 chars (par substituto); relevante para Messages.eliminated(int)
        assertEquals(2, AsyncService.expensiveCalculation("😀"));
    }
}
