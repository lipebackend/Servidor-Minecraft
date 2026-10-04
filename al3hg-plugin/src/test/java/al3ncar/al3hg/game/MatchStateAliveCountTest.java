package al3ncar.al3hg.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fatia 4: contagem de vivos. Eliminar por DEATH (MatchState.eliminate) ou por QUIT
 * (MatchState.remove) tira do conjunto de vivos, uma única vez; "remaining" = aliveCount() depois.
 */
class MatchStateAliveCountTest {

    private final UUID a = UUID.randomUUID();
    private final UUID b = UUID.randomUUID();
    private final UUID c = UUID.randomUUID();
    private MatchState match;

    @BeforeEach
    void setUp() {
        match = new MatchState("m1", "hgmapa_m1");
        match.addParticipants(List.of(a, b, c));
    }

    @Test
    @DisplayName("participantes entram vivos e não duplicam")
    void participantsStartAlive() {
        match.addParticipant(a);
        assertEquals(3, match.aliveCount());
    }

    @Test
    @DisplayName("DEATH: sai dos vivos, vira espectador e remaining cai para 2")
    void deathRemovesFromAlive() {
        assertTrue(match.eliminate(a));
        assertFalse(match.isAlive(a));
        assertTrue(match.isSpectator(a));
        assertEquals(2, match.aliveCount());
    }

    @Test
    @DisplayName("QUIT: sai dos vivos, NÃO vira espectador e remaining cai para 2")
    void quitRemovesFromAlive() {
        assertTrue(match.remove(a));
        assertFalse(match.isAlive(a));
        assertFalse(match.isSpectator(a));
        assertEquals(2, match.aliveCount());
    }

    @Test
    @DisplayName("morrer duas vezes conta uma só: segunda eliminação devolve false")
    void doubleDeathCountsOnce() {
        assertTrue(match.eliminate(a));
        assertFalse(match.eliminate(a));
        assertEquals(2, match.aliveCount());
    }

    @Test
    @DisplayName("morrer e depois sair não conta de novo (QUIT de quem já morreu devolve false)")
    void deathThenQuitCountsOnce() {
        match.eliminate(a);
        assertFalse(match.remove(a));
        assertEquals(2, match.aliveCount());
        assertFalse(match.isSpectator(a), "quit limpa o espectador também");
    }

    @Test
    @DisplayName("sair e depois 'morrer' não conta de novo")
    void quitThenDeathCountsOnce() {
        match.remove(a);
        assertFalse(match.eliminate(a));
        assertEquals(2, match.aliveCount());
        assertFalse(match.isSpectator(a), "quem saiu não pode voltar como espectador");
    }

    @Test
    @DisplayName("remaining sequencial: 2, 1, 0 misturando DEATH e QUIT")
    void remainingSequence() {
        match.eliminate(a);
        assertEquals(2, match.aliveCount());
        match.remove(b);
        assertEquals(1, match.aliveCount());
        match.eliminate(c);
        assertEquals(0, match.aliveCount());
    }

    @Test
    @DisplayName("eliminar/sair quem nunca participou não altera nada")
    void unknownPlayerIsIgnored() {
        UUID stranger = UUID.randomUUID();
        assertFalse(match.eliminate(stranger));
        assertFalse(match.remove(stranger));
        assertEquals(3, match.aliveCount());
        assertFalse(match.isSpectator(stranger));
    }
}
