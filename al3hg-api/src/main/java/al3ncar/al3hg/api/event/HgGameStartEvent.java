package al3ncar.al3hg.api.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

/** Disparado quando a partida começa (entrada em GRACE). Não cancelável. */
public class HgGameStartEvent extends HgEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Set<UUID> players;

    public HgGameStartEvent(String matchId, Set<UUID> players) {
        super(matchId);
        this.players = Set.copyOf(players);
    }

    /** Jogadores que iniciaram a partida (imutável). */
    public Set<UUID> getPlayers() {
        return players;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
