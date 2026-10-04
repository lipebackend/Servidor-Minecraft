package al3ncar.al3hg.listener;

import al3ncar.al3hg.api.GameState;
import al3ncar.al3hg.support.GameFixture;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bug #8 (CancelarDano com lógica invertida) e invulnerabilidade em ENDING (issue #23). */
class PvpDamageListenerTest {

    private GameFixture f;
    private PlayerMock a, b;

    @BeforeEach
    void setUp() {
        f = new GameFixture();
        a = f.addPlayer("a");
        b = f.addPlayer("b");
        f.server.getPluginManager().registerEvents(new PvpDamageListener(f.game), f.plugin);
        f.game.start(false);
    }

    @AfterEach
    void tearDown() {
        f.close();
    }

    private EntityDamageByEntityEvent golpe() {
        DamageSource src = DamageSource.builder(DamageType.PLAYER_ATTACK).withCausingEntity(a).withDirectEntity(a).build();
        EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(a, b, EntityDamageEvent.DamageCause.ENTITY_ATTACK, src, 4.0);
        f.server.getPluginManager().callEvent(e);
        return e;
    }

    @Test
    void pvp_desligado_cancela_dano_jogador_contra_jogador() {
        f.passSeconds(4); // GRACE: pvp desligado
        assertEquals(GameState.GRACE, f.game.state());
        assertTrue(golpe().isCancelled());
    }

    @Test
    void pvp_ligado_nao_cancela_o_dano() {
        f.passSeconds(10); // RUNNING
        assertEquals(GameState.RUNNING, f.game.state());
        assertFalse(golpe().isCancelled());
    }

    @Test
    void em_ending_qualquer_dano_a_jogador_e_cancelado() {
        f.passSeconds(10);
        f.game.stop();
        assertEquals(GameState.ENDING, f.game.state());
        EntityDamageEvent queda = new EntityDamageEvent(b, EntityDamageEvent.DamageCause.FALL,
                DamageSource.builder(DamageType.FALL).build(), 3.0);
        f.server.getPluginManager().callEvent(queda);
        assertTrue(queda.isCancelled());
    }
}
