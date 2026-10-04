package al3ncar.al3hg.arena;

import java.util.Optional;

/**
 * Fornece e descarta o mundo (arena) de cada partida. Todas as chamadas ocorrem na
 * thread principal do servidor. O estado da partida guarda só o {@code matchId} e o
 * NOME do mundo devolvido por {@link #prepare(String)} — nunca World/Chunk/Location.
 */
public interface ArenaProvider {

    /** {@code true} se há um mapa-modelo carregado e {@link #prepare(String)} pode ser chamado. */
    boolean isReady();

    /**
     * Cria e carrega uma arena nova para a partida.
     *
     * @return o nome do mundo carregado
     * @throws ArenaException se o mapa não estiver disponível ou o carregamento falhar
     */
    String prepare(String matchId) throws ArenaException;

    /**
     * Descarta a arena da partida. Os jogadores já devem ter sido enviados ao lobby; quem
     * ainda estiver no mundo é movido para um mundo de fallback ANTES do unload (que nunca salva).
     * Idempotente.
     */
    void dispose(String matchId);

    /** Descarta todas as arenas carregadas (usado no {@code onDisable}). */
    void disposeAll();

    /** Nome do mundo da partida, se estiver carregada. */
    Optional<String> worldName(String matchId);

    /** Relê o mapa-modelo (ex.: após {@code /hgc reload}). Por padrão não faz nada. */
    default void reloadTemplate() {
    }
}
