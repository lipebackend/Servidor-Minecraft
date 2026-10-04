package al3ncar.al3hg.support;

import al3ncar.al3hg.config.HgSettings;
import al3ncar.al3hg.game.GameManager;
import al3ncar.al3hg.util.LobbyTransfer;
import org.bukkit.plugin.Plugin;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

/** Monta servidor falso + GameManager real com arena falsa e tempos curtos (em segundos). */
public final class GameFixture implements AutoCloseable {

    public final ServerMock server = MockBukkit.mock();
    public final FakeArenaProvider arena = new FakeArenaProvider(server);
    public final GameManager game;
    public final Plugin plugin = MockBukkit.createMockPlugin();

    public GameFixture() {
        // minPlayers=2, contagem 3s, graça 5s, border 300->20 em 30s, fim 2s, espera do lobby 1s
        HgSettings s = new HgSettings(2, 3, 5, 300, 20, 30, 2, 1, "lobby", false, false, "mapa", "slime");
        game = new GameManager(plugin, arena, new LobbyTransfer(plugin, () -> s), () -> s);
    }

    /** Conecta um jogador falso (com teleportAsync funcional). */
    public PlayerMock addPlayer(String name) {
        PlayerMock p = new TestPlayer(server, name);
        server.addPlayer(p);
        return p;
    }

    /** Avança o relógio do servidor falso em segundos (20 ticks cada). */
    public void passSeconds(int seconds) {
        server.getScheduler().performTicks(seconds * 20L);
    }

    @Override
    public void close() {
        MockBukkit.unmock();
    }
}
