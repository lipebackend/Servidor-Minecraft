package al3ncar.al3hg.addon.stats.bukkit;

import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Eventos do jogo que o addon consome, SEM tipos da al3hg-api. Quem traduz os eventos
 * da API para estes callbacks e o {@link HgApiAdapter} (unica classe que importa a API).
 * Os callbacks chegam sempre na thread principal.
 */
public interface GameEventsHandler {

    /** A partida comecou com estes jogadores (HgGameStartEvent). */
    void onGameStart(String matchId, Set<UUID> players);

    /**
     * Um jogador deixou o conjunto de vivos (HgPlayerEliminatedEvent).
     *
     * @param killer  autor da morte, ou null
     * @param byDeath true se o motivo foi morte; false se o jogador saiu do servidor
     */
    void onPlayerEliminated(String matchId, UUID player, @Nullable UUID killer, boolean byDeath);

    /** A partida terminou (HgGameEndEvent); winner e null se nao houve vencedor. */
    void onGameEnd(String matchId, @Nullable UUID winner);
}
