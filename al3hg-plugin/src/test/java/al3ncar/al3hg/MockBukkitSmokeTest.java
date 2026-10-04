package al3ncar.al3hg;

import al3ncar.al3hg.api.GameState;
import al3ncar.al3hg.api.event.HgStateChangeEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Garante que o MockBukkit sobe junto com o paper-api do projeto e que os eventos da API disparam. */
class MockBukkitSmokeTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void servidorFalsoSobe() {
        assertNotNull(server);
        assertEquals(0, server.getOnlinePlayers().size());
    }

    @Test
    void eventoDaApiPodeSerDisparado() {
        HgStateChangeEvent evento = new HgStateChangeEvent("m1", GameState.WAITING, GameState.COUNTDOWN);
        server.getPluginManager().callEvent(evento);
        assertEquals("m1", evento.getMatchId());
        assertEquals(GameState.COUNTDOWN, evento.getTo());
    }
}
