package al3ncar.al3hg.game;

import al3ncar.al3hg.api.GameState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static al3ncar.al3hg.api.GameState.*;
import static org.junit.jupiter.api.Assertions.*;

/** Fatia 2: transições INVÁLIDAS. Verifica a matriz 5x5 inteira contra a tabela esperada. */
class GameStateMachineInvalidTransitionsTest {

    /** Tabela esperada (docs/arquitetura/03 + Javadoc da classe). Qualquer outra combinação é inválida. */
    private static final Map<GameState, EnumSet<GameState>> EXPECTED = new EnumMap<>(GameState.class);

    static {
        EXPECTED.put(WAITING, EnumSet.of(COUNTDOWN, GRACE));
        EXPECTED.put(COUNTDOWN, EnumSet.of(GRACE, ENDING));
        EXPECTED.put(GRACE, EnumSet.of(RUNNING, ENDING));
        EXPECTED.put(RUNNING, EnumSet.of(ENDING));
        EXPECTED.put(ENDING, EnumSet.of(WAITING));
    }

    /** Caminhos do caminho feliz até cada estado (começando de WAITING). */
    private static final Map<GameState, List<GameState>> PATH = Map.of(
            WAITING, List.of(),
            COUNTDOWN, List.of(COUNTDOWN),
            GRACE, List.of(COUNTDOWN, GRACE),
            RUNNING, List.of(COUNTDOWN, GRACE, RUNNING),
            ENDING, List.of(COUNTDOWN, GRACE, RUNNING, ENDING));

    private static GameStateMachine at(GameState origin) {
        GameStateMachine fsm = new GameStateMachine((f, t) -> { });
        PATH.get(origin).forEach(fsm::transition);
        return fsm;
    }

    @ParameterizedTest(name = "a partir de {0}: só as transições da tabela são permitidas")
    @EnumSource(GameState.class)
    void matrixMatchesTable(GameState origin) {
        for (GameState target : GameState.values()) {
            boolean expected = EXPECTED.get(origin).contains(target);
            assertEquals(expected, at(origin).canTransition(target), origin + " -> " + target);
        }
    }

    @ParameterizedTest(name = "{0} -> {0} (auto-transição) é inválida")
    @EnumSource(GameState.class)
    void selfTransitionIsInvalid(GameState state) {
        assertFalse(at(state).canTransition(state));
    }

    @ParameterizedTest(name = "transition() lança IllegalStateException e mantém {0} em transição inválida")
    @EnumSource(GameState.class)
    void invalidTransitionThrowsAndKeepsState(GameState origin) {
        GameState invalid = EnumSet.complementOf(EXPECTED.get(origin)).iterator().next();
        GameStateMachine fsm = at(origin);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> fsm.transition(invalid));
        assertTrue(ex.getMessage().contains(origin.name()) && ex.getMessage().contains(invalid.name()));
        assertEquals(origin, fsm.state());
    }

    @Test
    @DisplayName("alvo null nunca é permitido (sem NullPointerException)")
    void nullTargetIsRejected() {
        GameStateMachine fsm = at(WAITING);
        assertFalse(fsm.canTransition(null));
        assertFalse(fsm.tryTransition(null));
        assertThrows(IllegalStateException.class, () -> fsm.transition(null));
    }

    @Test
    @DisplayName("RUNNING não volta para GRACE/COUNTDOWN/WAITING e ENDING não volta para RUNNING")
    void noBackwardsTransitions() {
        assertFalse(at(RUNNING).canTransition(GRACE));
        assertFalse(at(RUNNING).canTransition(WAITING));
        assertFalse(at(ENDING).canTransition(RUNNING));
    }
}
