package al3ncar.al3hg.support;

import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** PlayerMock cujo teleportAsync funciona (no MockBukkit 4.116.3 ele lança UnimplementedOperationException). */
public final class TestPlayer extends PlayerMock {

    private final List<String> log;

    public TestPlayer(ServerMock server, String name, List<String> log) {
        super(server, name);
        this.log = log;
    }

    /** Registra "lobby:nome" em vez de exigir canal BungeeCord registrado. */
    @Override
    public void sendPluginMessage(org.bukkit.plugin.Plugin source, String channel, byte[] message) {
        log.add("lobby:" + getName());
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Location location, PlayerTeleportEvent.TeleportCause cause,
                                                    TeleportFlag... flags) {
        return CompletableFuture.completedFuture(teleport(location));
    }
}
