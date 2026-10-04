package al3ncar.al3hg.addon.template;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.server.ServerLoadEvent;

import java.util.Objects;

/**
 * UNICO lugar do addon que conhece os eventos de jogo. Hoje usa eventos
 * PLACEHOLDER do Bukkit; o resto do addon depende apenas de {@link GameEventHandler}.
 *
 * <p>TODO(fase 1 - al3hg-api): trocar SOMENTE esta classe pelos eventos reais:
 * <pre>
 *   ServerLoadEvent  -> al3ncar.al3hg.api.event.HgGameStartEvent
 *   PlayerDeathEvent -> al3ncar.al3hg.api.event.HgPlayerEliminatedEvent
 *                       (usar event.player() em vez de event.getEntity())
 * </pre>
 * Contrato da API: eventos disparados sempre na thread principal. Considere
 * tambem checar {@code game.state()} (HgGame via ServicesManager) antes de repassar.
 */
public final class GameEventAdapter implements Listener {

    private final GameEventHandler handler;

    public GameEventAdapter(GameEventHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    /** TODO(fase 1): PLACEHOLDER de HgGameStartEvent. */
    @EventHandler
    public void onGameStart(ServerLoadEvent event) {
        handler.onGameStart();
    }

    /** TODO(fase 1): PLACEHOLDER de HgPlayerEliminatedEvent. */
    @EventHandler
    public void onPlayerEliminated(PlayerDeathEvent event) {
        handler.onPlayerEliminated(event.getEntity().getUniqueId(), event.getEntity().getName());
    }
}
