package al3ncar.al3hg.util;

import al3ncar.al3hg.listener.PlayerConnectionListener;
import al3ncar.al3hg.support.GameFixture;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bug #9 (border resetada a cada join) e #10 (mundo nulo sem checagem). */
class WorldBorderAndJoinTest {

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
    void mundo_inexistente_nao_lanca_excecao_e_devolve_false() {
        Logger log = Logger.getLogger("teste");
        assertFalse(WorldBorderController.shrink("nao_existe", 100, 0, log));
        assertFalse(ArenaRules.apply("nao_existe", log));
    }

    @Test
    void mundo_carregado_recebe_o_tamanho_da_border() {
        World w = f.server.addSimpleWorld("arena_teste");
        assertTrue(WorldBorderController.shrink("arena_teste", 150, 0, Logger.getLogger("teste")));
        assertEquals(150, w.getWorldBorder().getSize());
    }

    @Test
    void join_no_meio_da_partida_nao_mexe_na_border_e_entra_como_espectador() {
        f.server.getPluginManager().registerEvents(new PlayerConnectionListener(f.game), f.plugin);
        f.addPlayer("a");
        f.addPlayer("b");
        f.game.start(true);
        World arena = f.server.getWorld(f.game.worldName().orElseThrow());
        assertEquals(300, arena.getWorldBorder().getSize());

        arena.getWorldBorder().setSize(77); // border já encolhendo
        PlayerMock tardio = f.addPlayer("tardio");
        assertEquals(77, arena.getWorldBorder().getSize(), "join não reseta a border");
        assertTrue(f.game.isSpectator(tardio.getUniqueId()));
        assertEquals(GameMode.SPECTATOR, tardio.getGameMode());
    }
}
