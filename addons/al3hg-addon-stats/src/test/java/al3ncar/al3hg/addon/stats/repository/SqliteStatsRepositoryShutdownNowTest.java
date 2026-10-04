package al3ncar.al3hg.addon.stats.repository;

import al3ncar.al3hg.addon.stats.PlayerStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** closeNow(): futures descartados da fila completam com RejectedExecutionException (nao ficam pendentes). */
class SqliteStatsRepositoryShutdownNowTest {

    @TempDir
    Path tempDir;

    @Test
    void closeNowCompletesDiscardedFuturesWithRejectedExecution() throws Exception {
        var repo = new SqliteStatsRepository(tempDir.resolve("s.db"));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch never = new CountDownLatch(1);
        UUID id = new UUID(0L, 1);
        repo.update(id, s -> {
            started.countDown();
            try {
                never.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return s;
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        List<CompletableFuture<Void>> queued = new ArrayList<>();
        for (int i = 2; i <= 4; i++) {
            queued.add(repo.save(PlayerStats.empty(new UUID(0L, i))));
        }

        repo.closeNow();

        for (CompletableFuture<Void> f : queued) {
            ExecutionException ex = assertThrows(ExecutionException.class, () -> f.get(1, TimeUnit.SECONDS));
            assertInstanceOf(RejectedExecutionException.class, ex.getCause());
        }
        assertThrows(Exception.class, () -> repo.save(PlayerStats.empty(id)).get(1, TimeUnit.SECONDS));
    }
}
