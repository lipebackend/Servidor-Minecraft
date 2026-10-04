package al3ncar.al3hg.api.event;

import al3ncar.al3hg.api.GameState;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/** Disparado em toda transição de estado, já com o novo estado aplicado. Não cancelável. */
public class HgStateChangeEvent extends HgEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GameState from;
    private final GameState to;

    public HgStateChangeEvent(String matchId, GameState from, GameState to) {
        super(matchId);
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
    }

    public GameState getFrom() {
        return from;
    }

    public GameState getTo() {
        return to;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
