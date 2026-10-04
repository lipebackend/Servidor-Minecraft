package al3ncar.al3hg.support;

import al3ncar.al3hg.arena.ArenaProvider;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** ArenaProvider falso: cria um mundo simples no ServerMock e registra as chamadas, em ordem. */
public final class FakeArenaProvider implements ArenaProvider {

    private final ServerMock server;
    /** Chamadas ao provider (e envios ao lobby, via TestPlayer), em ordem. */
    public final List<String> calls = new ArrayList<>();
    private String world;

    public FakeArenaProvider(ServerMock server) {
        this.server = server;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public String prepare(String matchId) {
        world = "hg_" + matchId;
        server.addSimpleWorld(world);
        calls.add("prepare");
        return world;
    }

    @Override
    public void dispose(String matchId) {
        calls.add("dispose");
    }

    @Override
    public void disposeAll() {
        calls.add("disposeAll");
    }

    @Override
    public Optional<String> worldName(String matchId) {
        return Optional.ofNullable(world);
    }
}
