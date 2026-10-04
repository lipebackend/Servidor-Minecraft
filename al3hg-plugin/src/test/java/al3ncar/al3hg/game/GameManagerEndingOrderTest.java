package al3ncar.al3hg.game;

import al3ncar.al3hg.api.GameState;
import al3ncar.al3hg.support.GameFixture;
import org.bukkit.GameMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bug #11: /hgc stop e rest faziam shutdown() imediato, antes de enviar os jogadores ao lobby. */
class GameManagerEndingOrderTest {

    private GameFixture f;
    private PlayerMock a, b;

    @BeforeEach
    void setUp() {
        f = new GameFixture();
        a = f.addPlayer("a");
        b = f.addPlayer("b");
        f.game.start(true);
    }

    @AfterEach
    void tearDown() {
        f.close();
    }

    @Test
    void stop_envia_ao_lobby_e_so_depois_descarta_a_arena() {
        assertTrue(f.game.stop());
        assertEquals(GameState.ENDING, f.game.state());
        assertEquals(List.of("prepare"), f.arena.calls, "nada descartado ainda");
        f.passSeconds(5); // espera do lobby (1s) já passou
        assertEquals(List.of("prepare", "lobby:a", "lobby:b", "dispose"), f.arena.calls);
        assertEquals(GameState.WAITING, f.game.state());
        assertTrue(f.plugin.isEnabled(), "o plugin/servidor não é desligado");
    }

    @Test
    void ao_voltar_a_waiting_os_jogadores_voltam_para_adventure() {
        f.game.stop();
        f.passSeconds(5);
        assertEquals(GameMode.ADVENTURE, a.getGameMode());
        assertEquals(GameMode.ADVENTURE, b.getGameMode());
        assertTrue(f.game.matchId().isEmpty());
    }

    @Test
    void shutdown_do_plugin_descarta_todas_as_arenas_e_zera_o_estado() {
        f.game.shutdown();
        assertEquals(GameState.WAITING, f.game.state());
        assertEquals(List.of("prepare", "disposeAll"), f.arena.calls);
    }
}
