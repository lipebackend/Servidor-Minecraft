package al3ncar.al3hg.api;

import org.bukkit.World;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Visão somente-leitura da partida atual do al3HG.
 *
 * <p>Obtenha-a com {@code Bukkit.getServicesManager().load(HgGame.class)}.
 * Todos os métodos devem ser chamados na thread principal do servidor.
 * Os conjuntos retornados são cópias imutáveis (snapshots).</p>
 */
public interface HgGame {

    /** Estado atual da máquina de estados. */
    GameState state();

    /** Identificador da partida atual, ou vazio se estiver em {@link GameState#WAITING} sem arena preparada. */
    Optional<String> matchId();

    /** {@code true} se o PVP jogador x jogador está liberado. */
    boolean isPvpEnabled();

    /** Jogadores vivos na partida (snapshot imutável). */
    Set<UUID> alivePlayers();

    /** Espectadores (eliminados ou que entraram depois do início) (snapshot imutável). */
    Set<UUID> spectators();

    /** @return {@code true} se o jogador está vivo na partida. */
    boolean isAlive(UUID playerId);

    /** Nome do mundo da arena atual, se houver. */
    Optional<String> worldName();

    /** Mundo da arena atual, resolvido sob demanda (pode ser vazio se não carregado). */
    Optional<World> world();

    /** Vencedor; presente somente em {@link GameState#ENDING} e se houve um. */
    Optional<UUID> winner();
}
