package al3ncar.al3hg.addon.template;

import al3ncar.al3hg.api.event.HgGameStartEvent;
import al3ncar.al3hg.api.event.HgPlayerEliminatedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;
import java.util.UUID;

/**
 * UNICO lugar do addon que conhece os eventos da al3hg-api. O resto do addon depende
 * apenas de {@link GameEventHandler}. Os eventos sao disparados na thread principal.
 * Considere tambem checar {@code game.state()} (HgGame via ServicesManager) antes de repassar.
 */
public final class GameEventAdapter implements Listener {

    private final GameEventHandler handler;

    public GameEventAdapter(GameEventHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @EventHandler
    public void onGameStart(HgGameStartEvent event) {
        handler.onGameStart();
    }

    @EventHandler
    public void onPlayerEliminated(HgPlayerEliminatedEvent event) {
        UUID playerId = event.getPlayer();
        String name = Bukkit.getOfflinePlayer(playerId).getName(); // so le o cache local, sem rede
        handler.onPlayerEliminated(playerId, name != null ? name : playerId.toString());
    }
}
