package al3ncar.al3hg.game;

import al3ncar.al3hg.api.GameState;
import al3ncar.al3hg.api.event.HgGameEndEvent;
import al3ncar.al3hg.api.event.HgGameStartEvent;
import al3ncar.al3hg.api.event.HgStateChangeEvent;
import al3ncar.al3hg.support.EventCollector;
import al3ncar.al3hg.support.GameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** beginGrace precisa conferir match.isOver(): partida sem jogadores suficientes não pode ficar sem vencedor. */
class GameManagerGraceTest {

    private GameFixture f;
    private EventCollector events;

    @BeforeEach
    void setUp() {
        f = new GameFixture();
        events = new EventCollector(f.server.getPluginManager(), f.plugin);
    }

    @AfterEach
    void tearDown() {
        f.close();
    }

    @Test
    void inicio_rapido_sem_jogadores_termina_sem_vencedor() {
        assertEquals(GameManager.StartResult.OK, f.game.start(true));
        assertEquals(GameState.ENDING, f.game.state());
        assertTrue(f.game.winner().isEmpty());
        assertTrue(events.of(HgGameEndEvent.class).get(0).getWinner().isEmpty());
        assertTrue(events.of(HgGameStartEvent.class).isEmpty());
    }

    @Test
    void sai_um_dos_dois_na_contagem_e_o_outro_vence_ao_sair_do_countdown() {
        f.addPlayer("a");
        PlayerMock b = f.addPlayer("b");
        f.game.start(false);
        f.game.handleQuit(f.server.getPlayer("a"));
        assertEquals(GameState.COUNTDOWN, f.game.state());
        f.passSeconds(4);
        assertEquals(GameState.ENDING, f.game.state());
        assertEquals(b.getUniqueId(), f.game.winner().orElseThrow());
        assertTrue(events.of(HgGameStartEvent.class).isEmpty(), "não anuncia início de jogo que não houve");
    }

    @Test
    void contagem_sem_minimo_vai_direto_de_countdown_para_ending() {
        f.addPlayer("a");
        PlayerMock b = f.addPlayer("b");
        f.game.start(false);
        f.game.handleQuit(f.server.getPlayer("a"));
        f.passSeconds(4);
        var changes = events.of(HgStateChangeEvent.class);
        HgStateChangeEvent last = changes.get(changes.size() - 1);
        assertEquals(GameState.COUNTDOWN, last.getFrom());
        assertEquals(GameState.ENDING, last.getTo());
        assertTrue(changes.stream().noneMatch(e -> e.getTo() == GameState.GRACE), "sem HgStateChangeEvent para GRACE");
        assertTrue(events.of(HgGameStartEvent.class).isEmpty());
        assertEquals(b.getUniqueId(), events.of(HgGameEndEvent.class).get(0).getWinner().orElseThrow());
    }

    @Test
    void contagem_sem_ninguem_termina_sem_vencedor() {
        f.addPlayer("a");
        f.addPlayer("b");
        f.game.start(false);
        f.game.handleQuit(f.server.getPlayer("a"));
        f.game.handleQuit(f.server.getPlayer("b"));
        assertEquals(GameState.ENDING, f.game.state());
        assertTrue(events.of(HgGameEndEvent.class).get(0).getWinner().isEmpty());
        assertTrue(events.of(HgGameStartEvent.class).isEmpty());
    }
}
