package al3ncar.al3hg.game;

import al3ncar.al3hg.api.GameState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static al3ncar.al3hg.api.GameState.*;
import static org.junit.jupiter.api.Assertions.*;

/** Fatia 3: ouvinte de transição, tryTransition, reset e GameState.isInProgress(). */
class GameStateMachineNotificationTest {

    private final List<String> seen = new ArrayList<>();
    private final List<GameState> stateSeenByListener = new ArrayList<>();
    private final GameStateMachine[] holder = new GameStateMachine[1];
    private final GameStateMachine fsm = new GameStateMachine((from, to) -> {
        seen.add(from + ">" + to);
        stateSeenByListener.add(holder[0].state());
    });

    GameStateMachineNotificationTest() {
        holder[0] = fsm;
    }

    @Test
    @DisplayName("ouvinte recebe (from, to) em cada transição, na ordem")
    void listenerReceivesFromAndTo() {
        fsm.transition(COUNTDOWN);
        fsm.transition(GRACE);
        assertEquals(List.of("WAITING>COUNTDOWN", "COUNTDOWN>GRACE"), seen);
    }

    @Test
    @DisplayName("ouvinte já enxerga o NOVO estado aplicado")
    void listenerSeesNewState() {
        fsm.transition(GRACE);
        assertEquals(List.of(GRACE), stateSeenByListener);
    }

    @Test
    @DisplayName("transição inválida não notifica o ouvinte")
    void invalidTransitionDoesNotNotify() {
        assertFalse(fsm.tryTransition(RUNNING));
        assertThrows(IllegalStateException.class, () -> fsm.transition(ENDING));
        assertTrue(seen.isEmpty());
    }

    @Test
    @DisplayName("tryTransition devolve true quando aplica e false quando recusa")
    void tryTransitionResult() {
        assertTrue(fsm.tryTransition(COUNTDOWN));
        assertFalse(fsm.tryTransition(COUNTDOWN));
        assertEquals(COUNTDOWN, fsm.state());
        assertEquals(1, seen.size());
    }

    @Test
    @DisplayName("reset volta a WAITING sem notificar (uso no onDisable)")
    void resetIsSilent() {
        fsm.transition(GRACE);
        seen.clear();
        fsm.reset();
        assertEquals(WAITING, fsm.state());
        assertTrue(seen.isEmpty());
    }

    @Test
    @DisplayName("ouvinte null é recusado na construção")
    void nullListenerRejected() {
        assertThrows(NullPointerException.class, () -> new GameStateMachine(null));
    }

    @Test
    @DisplayName("GameState.isInProgress(): só GRACE e RUNNING")
    void inProgressOnlyGraceAndRunning() {
        for (GameState s : GameState.values()) {
            assertEquals(s == GRACE || s == RUNNING, s.isInProgress(), s.name());
        }
    }
}
