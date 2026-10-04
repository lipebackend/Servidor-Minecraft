package al3ncar.al3hg.api.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Disparado quando um jogador deixa o conjunto de vivos (morte ou saída do servidor).
 * Já disparado com o jogador removido dos vivos. Não cancelável.
 */
public class HgPlayerEliminatedEvent extends HgEvent {

    /** Motivo da eliminação. */
    public enum Reason { DEATH, QUIT }

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID player;
    private final UUID killer;
    private final Reason reason;
    private final int remaining;

    public HgPlayerEliminatedEvent(String matchId, UUID player, @Nullable UUID killer, Reason reason, int remaining) {
        super(matchId);
        this.player = Objects.requireNonNull(player, "player");
        this.killer = killer;
        this.reason = Objects.requireNonNull(reason, "reason");
        this.remaining = remaining;
    }

    public UUID getPlayer() {
        return player;
    }

    /** Autor da morte, se houver (sempre vazio para {@link Reason#QUIT}). */
    public Optional<UUID> getKiller() {
        return Optional.ofNullable(killer);
    }

    public Reason getReason() {
        return reason;
    }

    /** Quantos jogadores continuam vivos depois desta eliminação. */
    public int getRemaining() {
        return remaining;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
