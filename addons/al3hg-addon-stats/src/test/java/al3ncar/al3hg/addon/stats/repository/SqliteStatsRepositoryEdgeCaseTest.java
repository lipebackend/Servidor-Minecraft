package al3ncar.al3hg.addon.stats.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

/** Roda o contrato de bordas contra o SQLite real (arquivo temporario). */
class SqliteStatsRepositoryEdgeCaseTest extends StatsRepositoryEdgeCaseContract {

    @TempDir
    Path tempDir;

    private SqliteStatsRepository repository;

    @BeforeEach
    void open() {
        repository = new SqliteStatsRepository(tempDir.resolve("edge.db"));
    }

    @AfterEach
    void close() {
        repository.close();
    }

    @Override
    protected StatsRepository repository() {
        return repository;
    }
}
