package al3ncar.al3hg.addon.stats.repository;

import al3ncar.al3hg.addon.stats.PlayerStats;
import al3ncar.al3hg.addon.stats.RankingType;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

/**
 * Repositorio SQLite. Um executor proprio de thread unica serializa todo o
 * acesso, e a unica {@link Connection} so e usada dentro dessa thread (aberta
 * de forma preguicosa na primeira operacao, junto com a criacao da tabela).
 */
public final class SqliteStatsRepository implements StatsRepository {

    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS player_stats (
                uuid         TEXT PRIMARY KEY,
                kills        INTEGER NOT NULL DEFAULT 0,
                wins         INTEGER NOT NULL DEFAULT 0,
                deaths       INTEGER NOT NULL DEFAULT 0,
                games_played INTEGER NOT NULL DEFAULT 0
            )""";

    private static final String SELECT_ONE =
            "SELECT uuid, kills, wins, deaths, games_played FROM player_stats WHERE uuid = ?";

    private static final String UPSERT = """
            INSERT INTO player_stats (uuid, kills, wins, deaths, games_played)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                kills = excluded.kills,
                wins = excluded.wins,
                deaths = excluded.deaths,
                games_played = excluded.games_played""";

    // Mesma ordem de Ranking (uuid como texto, BINARY collation).
    private static final String TOP_WINS = """
            SELECT uuid, kills, wins, deaths, games_played FROM player_stats
            ORDER BY wins DESC, kills DESC, deaths ASC, games_played ASC, uuid ASC LIMIT ?""";

    private static final String TOP_KILLS = """
            SELECT uuid, kills, wins, deaths, games_played FROM player_stats
            ORDER BY kills DESC, wins DESC, deaths ASC, games_played ASC, uuid ASC LIMIT ?""";

    private final String jdbcUrl;
    private final ExecutorService executor;
    /** Acessada apenas pela thread do {@link #executor}. */
    private Connection connection;

    public SqliteStatsRepository(Path databaseFile) {
        Objects.requireNonNull(databaseFile, "databaseFile");
        this.jdbcUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "al3hg-stats-sqlite");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public CompletableFuture<Optional<PlayerStats>> load(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return run(() -> select(uuid));
    }

    @Override
    public CompletableFuture<Void> save(PlayerStats stats) {
        Objects.requireNonNull(stats, "stats");
        return run(() -> {
            upsert(stats);
            return null;
        });
    }

    @Override
    public CompletableFuture<List<PlayerStats>> top(RankingType type, int limit) {
        Objects.requireNonNull(type, "type");
        return run(() -> {
            if (limit < 0) {
                throw new IllegalArgumentException("limit nao pode ser negativo");
            }
            return selectTop(type, limit);
        });
    }

    @Override
    public CompletableFuture<PlayerStats> update(UUID uuid, UnaryOperator<PlayerStats> change) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(change, "change");
        return run(() -> {
            PlayerStats current = select(uuid).orElseGet(() -> PlayerStats.empty(uuid));
            PlayerStats updated = StatsUpdates.requireSameUuid(uuid, change.apply(current));
            upsert(updated);
            return updated;
        });
    }

    /** Conclui as tarefas pendentes, fecha a conexao e encerra a thread. */
    @Override
    public void close() {
        try {
            executor.submit(this::closeConnection).get(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // executor ja encerrado ou fechamento lento: seguimos para o shutdown
        } finally {
            executor.shutdown();
        }
    }

    // ---- tudo abaixo roda somente na thread do executor ----

    @FunctionalInterface
    private interface SqlTask<T> {
        T run() throws SQLException;
    }

    private <T> CompletableFuture<T> run(SqlTask<T> task) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return task.run();
                } catch (SQLException e) {
                    throw new CompletionException(e);
                }
            }, executor);
        } catch (RejectedExecutionException e) { // repositorio ja fechado
            return CompletableFuture.failedFuture(e);
        }
    }

    private Connection connection() throws SQLException {
        if (connection == null) {
            Connection opened = DriverManager.getConnection(jdbcUrl);
            try (Statement st = opened.createStatement()) {
                st.execute(CREATE_TABLE);
            } catch (SQLException e) {
                opened.close();
                throw e;
            }
            connection = opened;
        }
        return connection;
    }

    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // nada a fazer
            }
            connection = null;
        }
    }

    private Optional<PlayerStats> select(UUID uuid) throws SQLException {
        try (PreparedStatement ps = connection().prepareStatement(SELECT_ONE)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    private void upsert(PlayerStats s) throws SQLException {
        try (PreparedStatement ps = connection().prepareStatement(UPSERT)) {
            ps.setString(1, s.uuid().toString());
            ps.setInt(2, s.kills());
            ps.setInt(3, s.wins());
            ps.setInt(4, s.deaths());
            ps.setInt(5, s.gamesPlayed());
            ps.executeUpdate();
        }
    }

    private List<PlayerStats> selectTop(RankingType type, int limit) throws SQLException {
        String sql = type == RankingType.WINS ? TOP_WINS : TOP_KILLS;
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<PlayerStats> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return List.copyOf(result);
            }
        }
    }

    private static PlayerStats map(ResultSet rs) throws SQLException {
        return new PlayerStats(
                UUID.fromString(rs.getString("uuid")),
                rs.getInt("kills"),
                rs.getInt("wins"),
                rs.getInt("deaths"),
                rs.getInt("games_played"));
    }
}
