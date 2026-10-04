package al3ncar.al3hg.game;

import al3ncar.al3hg.api.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static al3ncar.al3hg.api.GameState.*;
import static org.junit.jupiter.api.Assertions.*;

/** Fatia 1: transições VÁLIDAS da máquina de estados (lógica pura, sem Bukkit). */
class GameStateMachineValidTransitionsTest {

    private GameStateMachine fsm;

    @BeforeEach
    void setUp() {
        fsm = new GameStateMachine((from, to) -> { });
    }

    @Test
    @DisplayName("começa em WAITING")
    void startsWaiting() {
        assertEquals(WAITING, fsm.state());
    }

    @Test
    @DisplayName("ciclo completo: WAITING -> COUNTDOWN -> GRACE -> RUNNING -> ENDING -> WAITING")
    void fullCycle() {
        for (GameState next : new GameState[]{COUNTDOWN, GRACE, RUNNING, ENDING, WAITING}) {
            assertTrue(fsm.canTransition(next), "deveria permitir -> " + next + " a partir de " + fsm.state());
            fsm.transition(next);
            assertEquals(next, fsm.state());
        }
    }

    @Test
    @DisplayName("início rápido: WAITING -> GRACE (sem contagem)")
    void quickStart() {
        fsm.transition(GRACE);
        assertEquals(GRACE, fsm.state());
    }

    @ParameterizedTest(name = "{0} -> ENDING (parada manual ou vencedor)")
    @CsvSource({"COUNTDOWN", "GRACE", "RUNNING"})
    void canEndFromAnyActiveState(GameState origin) {
        goTo(origin);
        assertTrue(fsm.canTransition(ENDING));
        fsm.transition(ENDING);
        assertEquals(ENDING, fsm.state());
    }

    @Test
    @DisplayName("após ENDING -> WAITING uma nova partida pode começar (estado volta a WAITING)")
    void canStartAnotherMatchAfterEnding() {
        fsm.transition(GRACE);
        fsm.transition(ENDING);
        fsm.transition(WAITING);
        assertTrue(fsm.canTransition(COUNTDOWN));
        assertTrue(fsm.tryTransition(COUNTDOWN));
    }

    /** Leva a máquina ao estado pedido pelo caminho feliz. */
    private void goTo(GameState target) {
        for (GameState step : new GameState[]{COUNTDOWN, GRACE, RUNNING}) {
            fsm.transition(step);
            if (step == target) {
                return;
            }
        }
    }
}
