package al3ncar.al3hg.arena;

/** Falha ao preparar a arena de uma partida. */
public class ArenaException extends Exception {

    public ArenaException(String message) {
        super(message);
    }

    public ArenaException(String message, Throwable cause) {
        super(message, cause);
    }
}
