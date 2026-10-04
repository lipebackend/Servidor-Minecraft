package al3ncar.al3hg.support;

import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.concurrent.CompletableFuture;

/** PlayerMock cujo teleportAsync funciona (no MockBukkit 4.116.3 ele lança UnimplementedOperationException). */
public final class TestPlayer extends PlayerMock {

    public TestPlayer(ServerMock server, String name) {
        super(server, name);
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Location location, PlayerTeleportEvent.TeleportCause cause,
                                                    TeleportFlag... flags) {
        return CompletableFuture.completedFuture(teleport(location));
    }
}
