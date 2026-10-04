package al3ncar.al3hg.addon.template;

import java.util.UUID;

/**
 * Abstracao dos eventos do jogo que o addon consome, SEM tipos do Bukkit.
 * Quem traduz eventos Bukkit para estes callbacks e a {@link GameEventAdapter}
 * (o unico ponto a alterar quando a al3hg-api existir).
 *
 * <p>Os callbacks sempre chegam na thread principal.
 */
public interface GameEventHandler {

    /** A partida comecou (futuro HgGameStartEvent). */
    void onGameStart();

    /** Um jogador foi eliminado (futuro HgPlayerEliminatedEvent). */
    void onPlayerEliminated(UUID playerId, String playerName);
}
