package al3ncar.al3hg.api.event;

import org.bukkit.event.Event;

import java.util.Objects;

/**
 * Base dos eventos do al3HG. Todos são síncronos e disparados na thread principal.
 */
public abstract class HgEvent extends Event {

    private final String matchId;

    protected HgEvent(String matchId) {
        this.matchId = Objects.requireNonNull(matchId, "matchId");
    }

    /** Identificador da partida que originou o evento. */
    public String getMatchId() {
        return matchId;
    }
}
