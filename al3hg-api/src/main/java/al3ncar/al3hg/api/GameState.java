package al3ncar.al3hg.api;

/**
 * Estados do ciclo de vida de uma partida do al3HG.
 *
 * <pre>
 * WAITING -> COUNTDOWN -> GRACE -> RUNNING -> ENDING -> WAITING
 * </pre>
 */
public enum GameState {
    /** Aguardando jogadores / início. Nenhuma partida em curso. */
    WAITING,
    /** Contagem regressiva antes do início. */
    COUNTDOWN,
    /** Partida iniciada, PVP ainda desligado (período de graça). */
    GRACE,
    /** Partida em andamento com PVP ligado. */
    RUNNING,
    /** Vencedor definido; aguardando o envio dos jogadores ao lobby. */
    ENDING;

    /** @return {@code true} se há uma partida em curso (GRACE ou RUNNING). */
    public boolean isInProgress() {
        return this == GRACE || this == RUNNING;
    }
}
