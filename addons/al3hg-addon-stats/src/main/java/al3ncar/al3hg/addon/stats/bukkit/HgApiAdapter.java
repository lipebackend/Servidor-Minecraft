package al3ncar.al3hg.addon.stats.bukkit;

import al3ncar.al3hg.api.event.HgGameEndEvent;
import al3ncar.al3hg.api.event.HgGameStartEvent;
import al3ncar.al3hg.api.event.HgPlayerEliminatedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Objects;

/**
 * UNICA classe do addon que conhece a al3hg-api: traduz os eventos para {@link GameEventsHandler}.
 * Os eventos da API sao disparados na thread principal e nao sao canceláveis.
 */
public final class HgApiAdapter implements Listener {

    private final GameEventsHandler handler;

    public HgApiAdapter(GameEventsHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStart(HgGameStartEvent event) {
        handler.onGameStart(event.getMatchId(), event.getPlayers());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerEliminated(HgPlayerEliminatedEvent event) {
        handler.onPlayerEliminated(event.getMatchId(), event.getPlayer(),
                event.getKiller().orElse(null), event.getReason() == HgPlayerEliminatedEvent.Reason.DEATH);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameEnd(HgGameEndEvent event) {
        handler.onGameEnd(event.getMatchId(), event.getWinner().orElse(null));
    }
}
