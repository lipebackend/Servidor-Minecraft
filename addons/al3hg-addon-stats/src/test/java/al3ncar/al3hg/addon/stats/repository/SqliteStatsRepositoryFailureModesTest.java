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

    @Test
    void corruptFileFailsWithSqlExceptionAndIsNotOverwritten() throws Exception {
        Path db = tempDir.resolve("lixo.db");
        byte[] garbage = "isto nao e um banco sqlite, e apenas texto qualquer com mais de cem bytes de tamanho para parecer um arquivo real............"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(db, garbage);
        try (var repo = new SqliteStatsRepository(db)) {
            var ex = assertThrows(ExecutionException.class, () -> await(repo.save(PlayerStats.empty(uuid(1)))));
            assertInstanceOf(SQLException.class, root(ex));
        }
        assertArrayEquals(garbage, Files.readAllBytes(db), "arquivo corrompido nao deve ser sobrescrito");
    }

    @Test
    void emptyExistingFileIsAcceptedAsNewDatabase() throws Exception {
        Path db = Files.createFile(tempDir.resolve("vazio.db"));
        try (var repo = new SqliteStatsRepository(db)) {
            PlayerStats s = new PlayerStats(uuid(1), 1, 0, 0, 0);
            await(repo.save(s));
            assertEquals(s, await(repo.load(uuid(1))).orElseThrow());
        }
    }

    @Test
    void pathWithSpacesAndNonAsciiWorks() throws Exception {
        Path dir = Files.createDirectory(tempDir.resolve("pasta com espaço e acentuação"));
        try (var repo = new SqliteStatsRepository(dir.resolve("estatísticas.db"))) {
            PlayerStats s = new PlayerStats(uuid(1), 1, 2, 3, 4);
            await(repo.save(s));
            assertEquals(s, await(repo.load(uuid(1))).orElseThrow());
        }
    }

    @Test
    void relativePathIsResolvedAgainstWorkingDirectoryAtConstruction() {
        // nao deve lancar no construtor; o caminho absoluto e fixado na criacao
        assertDoesNotThrow(() -> new SqliteStatsRepository(Path.of("relativo-que-nao-sera-aberto.db")).close());
        assertFalse(Files.exists(Path.of("relativo-que-nao-sera-aberto.db")));
    }

    // ---------- dados invalidos ja gravados (banco editado a mao / versao antiga) ----------

    private void insertRaw(Path db, String uuidText, int kills, int wins, int deaths, int games) throws SQLException {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db.toAbsolutePath());
             Statement st = c.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS player_stats (uuid TEXT PRIMARY KEY, kills INTEGER NOT NULL DEFAULT 0,
                    wins INTEGER NOT NULL DEFAULT 0, deaths INTEGER NOT NULL DEFAULT 0, games_played INTEGER NOT NULL DEFAULT 0)""");
            st.execute("INSERT INTO player_stats VALUES ('" + uuidText + "', " + kills + ", " + wins + ", " + deaths + ", " + games + ")");
        }
    }

    /**
     * Caracterizacao (RISCO): uma unica linha invalida (contador negativo) derruba o top inteiro,
     * pois map() lanca IllegalArgumentException e nada isola a linha ruim.
     */
    @Test
    void oneCorruptRowCurrentlyBreaksTheWholeTopRanking() throws Exception {
        Path db = tempDir.resolve("ruim.db");
        insertRaw(db, uuid(1).toString(), 5, 5, 0, 1);
        insertRaw(db, uuid(2).toString(), -3, 0, 0, 0);
        try (var repo = new SqliteStatsRepository(db)) {
            assertEquals(5, await(repo.load(uuid(1))).orElseThrow().kills());   // linha boa continua legivel
            var ex = assertThrows(ExecutionException.class, () -> await(repo.top(RankingType.WINS, 10)));
            assertInstanceOf(IllegalArgumentException.class, root(ex));
            var ex2 = assertThrows(ExecutionException.class, () -> await(repo.load(uuid(2))));
            assertInstanceOf(IllegalArgumentException.class, root(ex2));
        }
    }

    @Test
    void rowWithMalformedUuidFailsLoadOfThatRowOnlyViaTop() throws Exception {
        Path db = tempDir.resolve("uuid-ruim.db");
        insertRaw(db, "nao-e-uuid", 1, 1, 1, 1);
        try (var repo = new SqliteStatsRepository(db)) {
            var ex = assertThrows(ExecutionException.class, () -> await(repo.top(RankingType.KILLS, 10)));
            assertInstanceOf(IllegalArgumentException.class, root(ex));
            assertTrue(await(repo.load(uuid(1))).isEmpty());   // outras consultas seguem funcionando
        }
    }

    @Test
    void uppercaseUuidTextInDatabaseIsNotMatchedByLookup() throws Exception {
        // O repositorio sempre grava UUID.toString() (minusculo); texto maiusculo editado a mao nao e achado.
        Path db = tempDir.resolve("maiusculo.db");
        insertRaw(db, uuid(0xABC).toString().toUpperCase(), 1, 1, 1, 1);
        try (var repo = new SqliteStatsRepository(db)) {
            assertTrue(await(repo.load(uuid(0xABC))).isEmpty());
        }
    }

    // ---------- shutdown ----------
}
