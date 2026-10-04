package al3ncar.al3hg.addon.template;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/** Apos shutdownNow + rejectPending, futures de tarefas descartadas completam com RejectedExecutionException. */
class AsyncServiceShutdownNowTest {

    @Test
    void shutdownNowWithQueuedTasksCompletesDiscardedFuturesExceptionally() throws Exception {
        ExecutorService io = Executors.newSingleThreadExecutor();
        var service = new AsyncService(io, Runnable::run);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch never = new CountDownLatch(1);
        service.supplyThenSync(() -> {
            started.countDown();
            try {
                never.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return 0;
        }, v -> { });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        List<CompletableFuture<Void>> queued = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            queued.add(service.supplyThenSync(() -> 1, v -> { }));
        }

        assertEquals(3, AsyncService.rejectPending(io.shutdownNow()));

        for (CompletableFuture<Void> f : queued) {
            ExecutionException ex = assertThrows(ExecutionException.class, () -> f.get(1, TimeUnit.SECONDS));
            assertInstanceOf(RejectedExecutionException.class, ex.getCause());
        }
    }
}
