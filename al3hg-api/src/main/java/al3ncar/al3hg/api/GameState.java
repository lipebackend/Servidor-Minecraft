package al3ncar.al3hg.api;

/** Estados da partida: WAITING -> COUNTDOWN -> GRACE -> RUNNING -> ENDING -> WAITING. */
public enum GameState {
    /** Sem partida em curso. */
    WAITING,
    /** Contagem regressiva antes do início. */
    COUNTDOWN,
    /** Partida iniciada, PVP ainda desligado. */
    GRACE,
    /** Partida em andamento com PVP ligado. */
    RUNNING,
    /** Vencedor definido; aguardando o envio ao lobby. */
    ENDING;

    /** {@code true} em GRACE e RUNNING. */
    public boolean isInProgress() {
        return this == GRACE || this == RUNNING;
    }
}
