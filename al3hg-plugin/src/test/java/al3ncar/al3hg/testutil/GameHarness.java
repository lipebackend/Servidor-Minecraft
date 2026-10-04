package al3ncar.al3hg.testutil;

import al3ncar.al3hg.arena.ArenaProvider;
import al3ncar.al3hg.config.HgSettings;
import al3ncar.al3hg.game.GameManager;
import al3ncar.al3hg.util.LobbyTransfer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static al3ncar.al3hg.testutil.Stubs.PASS;
import static al3ncar.al3hg.testutil.Stubs.stub;

/** Monta um GameManager real sobre o {@link FakeServer}, com arena falsa que registra "dispose". */
public final class GameHarness {

    public final FakeServer server = FakeServer.I;
    public final List<Player> players = new ArrayList<>();
    public final GameManager game;

    public GameHarness(HgSettings settings, String... names) {
        FakeServer.install();
        for (String n : names) {
            players.add(server.player(n));
        }
        ArenaProvider arena = stub(ArenaProvider.class, (m, a) -> switch (m) {
            case "isReady" -> true;
            case "prepare" -> FakeServer.ARENA;
            case "dispose" -> server.log.add("dispose");
            default -> PASS;
        });
        game = new GameManager(server.plugin, arena, new LobbyTransfer(server.plugin, () -> settings), () -> settings);
    }

    /** minPlayers=1, contagem de 3s, shutdown-after-end ligado (para provar a ordem lobby -> dispose -> shutdown). */
    public static HgSettings settings(boolean shutdownAfterEnd) {
        return new HgSettings(1, 3, 120, 300, 20, 300, 10, 5, "lobby", shutdownAfterEnd, false, "hgmapa", "slime_worlds");
    }
}
