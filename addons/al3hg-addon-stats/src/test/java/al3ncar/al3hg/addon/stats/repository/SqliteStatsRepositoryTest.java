package al3ncar.al3hg.addon.stats.repository;

import al3ncar.al3hg.addon.stats.PlayerStats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class SqliteStatsRepositoryTest extends StatsRepositoryContract {

    @TempDir
    Path tempDir;

    private SqliteStatsRepository repository;

    @BeforeEach
    void open() {
        repository = new SqliteStatsRepository(tempDir.resolve("stats.db"));
    }

    @AfterEach
    void close() {
        repository.close();
    }

    @Override
    protected StatsRepository repository() {
        return repository;
    }

    @Test
    void createsDatabaseFileAndTableOnFirstUse() throws Exception {
        Path file = tempDir.resolve("stats.db");
        assertFalse(Files.exists(file) && Files.size(file) > 0);
        await(repository.load(UUID.randomUUID()));
        assertTrue(Files.size(file) > 0);
    }

    @Test
    void dataSurvivesReopeningTheFile() throws Exception {
        UUID id = UUID.randomUUID();
        PlayerStats s = new PlayerStats(id, 4, 3, 2, 1);
        await(repository.save(s));
        repository.close();

        try (SqliteStatsRepository reopened = new SqliteStatsRepository(tempDir.resolve("stats.db"))) {
            assertEquals(s, await(reopened.load(id)).orElseThrow());
        }
        // @AfterEach fecha o repositorio original de novo: close() e idempotente
    }

    @Test
    void operationsAfterCloseFailInsteadOfHanging() {
        repository.close();
        assertThrows(Exception.class, () -> await(repository.load(UUID.randomUUID())));
    }

    @Test
    void ioHappensOffTheCallingThread() throws Exception {
        Thread caller = Thread.currentThread();
        Thread[] seen = new Thread[1];
        await(repository.update(UUID.randomUUID(), s -> {
            seen[0] = Thread.currentThread();
            return s;
        }));
        assertNotSame(caller, seen[0]);
        assertEquals("al3hg-stats-sqlite", seen[0].getName());
    }

    @Test
    void unwritablePathReportsFailureInTheFuture() {
        try (SqliteStatsRepository broken = new SqliteStatsRepository(tempDir.resolve("no/such/dir/stats.db"))) {
            assertThrows(ExecutionException.class, () -> await(broken.load(UUID.randomUUID())));
        }
    }
}
