package al3ncar.al3hg.flow;

import al3ncar.al3hg.api.GameState;
import al3ncar.al3hg.api.event.HgGameEndEvent;
import al3ncar.al3hg.api.event.HgPlayerEliminatedEvent;
import al3ncar.al3hg.testutil.GameHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Bug #3 (quit não removia dos vivos) no GameManager real, sobre servidor falso. */
class GameManagerQuitFlowTest {

    private GameHarness h;

    @BeforeEach
    void setUp() {
        h = new GameHarness(GameHarness.settings(false), "ana", "bia", "caio");
        assertEquals(al3ncar.al3hg.game.GameManager.StartResult.OK, h.game.start(true));
    }

    private List<HgPlayerEliminatedEvent> eliminations() {
        return h.server.events.stream().filter(HgPlayerEliminatedEvent.class::isInstance)
                .map(HgPlayerEliminatedEvent.class::cast).toList();
    }

    @Test
    @DisplayName("quit na GRACE: sai dos vivos, evento QUIT com remaining=2 e sem killer")
    void quitRemovesFromAlive() {
        h.game.handleQuit(h.players.get(0));
        assertEquals(2, h.game.alivePlayers().size());
        assertFalse(h.game.isAlive(h.players.get(0).getUniqueId()));
        HgPlayerEliminatedEvent e = eliminations().getFirst();
        assertEquals(HgPlayerEliminatedEvent.Reason.QUIT, e.getReason());
        assertEquals(2, e.getRemaining());
        assertTrue(e.getKiller().isEmpty());
    }

    @Test
    @DisplayName("quit duas vezes (ou morte + quit) conta uma só eliminação")
    void doubleQuitCountsOnce() {
        h.game.handleQuit(h.players.get(0));
        h.game.handleQuit(h.players.get(0));
        h.game.eliminate(h.players.get(0).getUniqueId(), null, HgPlayerEliminatedEvent.Reason.DEATH);
        assertEquals(1, eliminations().size());
    }

    @Test
    @DisplayName("morte vira espectador; quit não")
    void deathSpectatorQuitNot() {
        h.game.eliminate(h.players.get(0).getUniqueId(), null, HgPlayerEliminatedEvent.Reason.DEATH);
        h.game.handleQuit(h.players.get(1));
        assertTrue(h.game.isSpectator(h.players.get(0).getUniqueId()));
        assertFalse(h.game.isSpectator(h.players.get(1).getUniqueId()));
    }

    @Test
    @DisplayName("último adversário sai: ENDING com o outro como vencedor (HgGameEndEvent)")
    void lastQuitEndsMatchWithWinner() {
        h.game.handleQuit(h.players.get(0));
        h.game.handleQuit(h.players.get(1));
        assertEquals(GameState.ENDING, h.game.state());
        assertEquals(h.players.get(2).getUniqueId(), h.game.winner().orElseThrow());
        HgGameEndEvent end = h.server.events.stream().filter(HgGameEndEvent.class::isInstance)
                .map(HgGameEndEvent.class::cast).findFirst().orElseThrow();
        assertEquals(h.players.get(2).getUniqueId(), end.getWinner().orElseThrow());
    }

    @Test
    @DisplayName("todos saem: ENDING sem vencedor")
    void everybodyQuitsEndsWithoutWinner() {
        h.players.forEach(h.game::handleQuit);
        assertEquals(GameState.ENDING, h.game.state());
        assertTrue(h.game.winner().isEmpty());
        assertEquals(List.of(2, 1, 0), eliminations().stream().map(HgPlayerEliminatedEvent::getRemaining).toList());
    }
}
