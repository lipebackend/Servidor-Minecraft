package al3ncar.al3hg.game;

import al3ncar.al3hg.api.GameState;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Máquina de estados da partida. Pura (sem Bukkit): valida transições e notifica o ouvinte
 * depois de aplicar o novo estado.
 *
 * <pre>
 * WAITING -> COUNTDOWN -> GRACE -> RUNNING -> ENDING -> WAITING
 *  (WAITING -> GRACE: início rápido, sem contagem)
 *  (COUNTDOWN/GRACE/RUNNING -> ENDING: vencedor ou parada manual)
 * </pre>
 */
public final class GameStateMachine {

    /** Chamado após cada transição (já com o novo estado aplicado). */
    @FunctionalInterface
    public interface TransitionListener {
        void onTransition(GameState from, GameState to);
    }

    private static final Map<GameState, Set<GameState>> ALLOWED = new EnumMap<>(GameState.class);

    static {
        ALLOWED.put(GameState.WAITING, EnumSet.of(GameState.COUNTDOWN, GameState.GRACE));
        ALLOWED.put(GameState.COUNTDOWN, EnumSet.of(GameState.GRACE, GameState.ENDING));
        ALLOWED.put(GameState.GRACE, EnumSet.of(GameState.RUNNING, GameState.ENDING));
        ALLOWED.put(GameState.RUNNING, EnumSet.of(GameState.ENDING));
        ALLOWED.put(GameState.ENDING, EnumSet.of(GameState.WAITING));
    }

    private final TransitionListener listener;
    private GameState state = GameState.WAITING;

    public GameStateMachine(TransitionListener listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    public GameState state() {
        return state;
    }

    public boolean canTransition(GameState to) {
        return to != null && ALLOWED.get(state).contains(to);
    }

    /** @throws IllegalStateException se a transição não for permitida a partir do estado atual */
    public void transition(GameState to) {
        if (!canTransition(to)) {
            throw new IllegalStateException("Transição inválida: " + state + " -> " + to);
        }
        GameState from = state;
        state = to;
        listener.onTransition(from, to);
    }

    /** @return {@code true} se a transição foi aplicada */
    public boolean tryTransition(GameState to) {
        if (!canTransition(to)) {
            return false;
        }
        transition(to);
        return true;
    }

    /** Volta para WAITING sem notificar (uso: desligamento do plugin). */
    public void reset() {
        state = GameState.WAITING;
    }
}
