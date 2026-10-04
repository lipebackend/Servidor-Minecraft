package al3ncar.al3hg.api.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Disparado quando o vencedor é definido (entrada em ENDING), ANTES de os jogadores
 * serem enviados ao lobby. Não cancelável.
 */
public class HgGameEndEvent extends HgEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID winner;

    public HgGameEndEvent(String matchId, @Nullable UUID winner) {
        super(matchId);
        this.winner = winner;
    }

    /** Vencedor; vazio se a partida terminou sem vencedor (todos saíram/morreram ou parada manual). */
    public Optional<UUID> getWinner() {
        return Optional.ofNullable(winner);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
