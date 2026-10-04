package al3ncar.al3hg.addon.stats.repository;

import al3ncar.al3hg.addon.stats.PlayerStats;
import al3ncar.al3hg.addon.stats.RankingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/** Falhas de I/O, arquivo inexistente/corrompido, linhas invalidas e shutdown do SqliteStatsRepository. */
class SqliteStatsRepositoryFailureModesTest {

    @TempDir
    Path tempDir;

    private static <T> T await(CompletableFuture<T> f) throws Exception {
        return f.get(10, TimeUnit.SECONDS);
    }

    private static UUID uuid(int n) {
        return new UUID(0L, n);
    }

    private static Throwable root(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    // ---------- arquivo inexistente / invalido ----------

    @Test
    void missingFileInExistingDirectoryIsCreatedAndStartsEmpty() throws Exception {
        Path db = tempDir.resolve("novo.db");
        assertFalse(Files.exists(db));
        try (var repo = new SqliteStatsRepository(db)) {
            assertTrue(await(repo.load(uuid(1))).isEmpty());
            assertTrue(await(repo.top(RankingType.WINS, 5)).isEmpty());
        }
        assertTrue(Files.exists(db));
    }

    @Test
    void constructorDoesNotTouchDiskUntilFirstOperation() {
        Path db = tempDir.resolve("lazy.db");
        try (var repo = new SqliteStatsRepository(db)) {
            assertFalse(Files.exists(db));
        }
        assertFalse(Files.exists(db));
    }

    @Test
    void nullPathIsRejected() {
        assertThrows(NullPointerException.class, () -> new SqliteStatsRepository(null));
    }

    @Test
    void failureIsReportedOnEveryOperationAndRecoversOnceDirectoryAppears() throws Exception {
        Path dir = tempDir.resolve("later");
        try (var repo = new SqliteStatsRepository(dir.resolve("stats.db"))) {
            assertThrows(ExecutionException.class, () -> await(repo.load(uuid(1))));
            assertThrows(ExecutionException.class, () -> await(repo.save(PlayerStats.empty(uuid(1)))));
            assertThrows(ExecutionException.class, () -> await(repo.top(RankingType.KILLS, 3)));
            assertThrows(ExecutionException.class, () -> await(repo.update(uuid(1), s -> s)));

            Files.createDirectory(dir);   // o problema de I/O foi resolvido
            PlayerStats s = new PlayerStats(uuid(1), 1, 1, 1, 1);
            await(repo.save(s));
            assertEquals(s, await(repo.load(uuid(1))).orElseThrow());
        }
    }

    @Test
    void databasePathThatIsADirectoryFailsInFuture() throws Exception {
        Path asDir = Files.createDirectory(tempDir.resolve("pasta.db"));
        try (var repo = new SqliteStatsRepository(asDir)) {
            var ex = assertThrows(ExecutionException.class, () -> await(repo.load(uuid(1))));
            assertInstanceOf(SQLException.class, root(ex));
        }
    }
}
