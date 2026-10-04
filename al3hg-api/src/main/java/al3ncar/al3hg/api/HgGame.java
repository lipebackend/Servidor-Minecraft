package al3ncar.al3hg.api;

import org.bukkit.World;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Visão somente-leitura da partida atual. Obtenha com
 * {@code Bukkit.getServicesManager().load(HgGame.class)} e use só na thread principal.
 * Os conjuntos retornados são cópias imutáveis.
 */
public interface HgGame {

    GameState state();

    /** Id da partida atual (vazio em WAITING). */
    Optional<String> matchId();

    boolean isPvpEnabled();

    Set<UUID> alivePlayers();

    Set<UUID> spectators();

    boolean isAlive(UUID playerId);

    /** Nome do mundo da arena atual. */
    Optional<String> worldName();

    /** Mundo da arena, resolvido sob demanda (vazio se não carregado). */
    Optional<World> world();

    /** Vencedor; presente só em {@link GameState#ENDING}. */
    Optional<UUID> winner();
}
