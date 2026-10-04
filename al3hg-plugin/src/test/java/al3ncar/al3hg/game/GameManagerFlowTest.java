package al3ncar.al3hg.game;

import al3ncar.al3hg.api.GameState;
import al3ncar.al3hg.support.GameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fluxo completo com MockBukkit: WAITING -> COUNTDOWN -> GRACE -> RUNNING e início rápido. */
class GameManagerFlowTest {

    private GameFixture f;

    @BeforeEach
    void setUp() {
        f = new GameFixture();
    }

    @AfterEach
    void tearDown() {
        f.close();
    }

    @Test
    void sem_jogadores_suficientes_nao_inicia() {
        f.addPlayer("solo");
        assertEquals(GameManager.StartResult.NOT_ENOUGH_PLAYERS, f.game.start(false));
        assertEquals(GameState.WAITING, f.game.state());
        assertTrue(f.arena.calls.isEmpty());
    }

    @Test
    void fluxo_normal_passa_por_contagem_graca_e_pvp() {
        PlayerMock a = f.addPlayer("solo");
        PlayerMock b = f.addPlayer("solo");
        assertEquals(GameManager.StartResult.OK, f.game.start(false));
        assertEquals(GameState.COUNTDOWN, f.game.state());
        assertFalse(f.game.isPvpEnabled());

        f.passSeconds(4);
        assertEquals(GameState.GRACE, f.game.state());
        assertEquals(2, f.game.alivePlayers().size());
        assertTrue(f.game.isAlive(a.getUniqueId()) && f.game.isAlive(b.getUniqueId()));

        f.passSeconds(6);
        assertEquals(GameState.RUNNING, f.game.state());
        assertTrue(f.game.isPvpEnabled());
    }
}
