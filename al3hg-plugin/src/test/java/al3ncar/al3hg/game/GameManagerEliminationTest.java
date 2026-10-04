package al3ncar.al3hg.game;

import al3ncar.al3hg.api.GameState;
import al3ncar.al3hg.api.event.HgGameEndEvent;
import al3ncar.al3hg.api.event.HgPlayerEliminatedEvent;
import al3ncar.al3hg.api.event.HgPlayerEliminatedEvent.Reason;
import al3ncar.al3hg.support.EventCollector;
import al3ncar.al3hg.support.GameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bugs #12 (morto vira espectador) e #13 (quem sai continua vivo) + vencedor, com GameManager real. */
class GameManagerEliminationTest {

    private GameFixture f;
    private EventCollector events;
    private PlayerMock a, b, c;

    @BeforeEach
    void setUp() {
        f = new GameFixture();
        events = new EventCollector(f.server.getPluginManager(), f.plugin);
        a = f.addPlayer("a");
        b = f.addPlayer("b");
        c = f.addPlayer("c");
        f.game.start(true); // início rápido: já em GRACE
    }

    @AfterEach
    void tearDown() {
        f.close();
    }

    @Test
    void morte_tira_dos_vivos_e_vira_espectador() {
        f.game.eliminate(a.getUniqueId(), b.getUniqueId(), Reason.DEATH);
        assertFalse(f.game.isAlive(a.getUniqueId()));
        assertTrue(f.game.isSpectator(a.getUniqueId()));
        HgPlayerEliminatedEvent e = events.of(HgPlayerEliminatedEvent.class).get(0);
        assertEquals(Reason.DEATH, e.getReason());
        assertEquals(b.getUniqueId(), e.getKiller().orElseThrow());
        assertEquals(2, e.getRemaining());
        assertEquals(GameState.GRACE, f.game.state());
    }

    @Test
    void saida_remove_dos_vivos_dispara_evento_quit_e_nao_vira_espectador() {
        f.game.handleQuit(a);
        assertFalse(f.game.isAlive(a.getUniqueId()));
        assertFalse(f.game.isSpectator(a.getUniqueId()));
        HgPlayerEliminatedEvent e = events.of(HgPlayerEliminatedEvent.class).get(0);
        assertEquals(Reason.QUIT, e.getReason());
        assertEquals(2, e.getRemaining());
    }

    @Test
    void ultimo_vivo_vence_e_partida_vai_para_ending() {
        f.game.handleQuit(a);
        f.game.eliminate(b.getUniqueId(), c.getUniqueId(), Reason.DEATH);
        assertEquals(GameState.ENDING, f.game.state());
        assertEquals(c.getUniqueId(), f.game.winner().orElseThrow());
        assertEquals(c.getUniqueId(), events.of(HgGameEndEvent.class).get(0).getWinner().orElseThrow());
    }

    @Test
    void eliminar_duas_vezes_o_mesmo_jogador_nao_dispara_segundo_evento() {
        f.game.eliminate(a.getUniqueId(), null, Reason.DEATH);
        f.game.eliminate(a.getUniqueId(), null, Reason.DEATH);
        assertEquals(1, events.of(HgPlayerEliminatedEvent.class).size());
    }
}
