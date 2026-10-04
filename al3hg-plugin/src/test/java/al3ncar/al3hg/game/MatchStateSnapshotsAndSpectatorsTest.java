package al3ncar.al3hg.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Fatia 5: espectadores (quem entra com a partida em andamento) e snapshots imutáveis de HgGame. */
class MatchStateSnapshotsAndSpectatorsTest {

    private final UUID a = UUID.randomUUID();
    private final UUID b = UUID.randomUUID();
    private final UUID late = UUID.randomUUID();
    private MatchState match;

    @BeforeEach
    void setUp() {
        match = new MatchState("m1", "hgmapa_m1");
        match.addParticipants(List.of(a, b));
    }

    @Test
    @DisplayName("quem entra depois do início é espectador e NÃO conta como vivo")
    void lateJoinerIsSpectatorNotAlive() {
        match.addSpectator(late);
        assertTrue(match.isSpectator(late));
        assertFalse(match.isAlive(late));
        assertEquals(2, match.aliveCount());
    }

    @Test
    @DisplayName("espectador tardio não impede o fim: eliminar um dos 2 vivos encerra (isOver)")
    void lateJoinerDoesNotPreventEnd() {
        match.addSpectator(late);
        match.eliminate(a);
        assertTrue(match.isOver());
        assertEquals(b, match.soleSurvivor().orElseThrow());
    }

    @Test
    @DisplayName("addParticipant tira o jogador da lista de espectadores (e vice-versa)")
    void roleSwitchIsExclusive() {
        match.addSpectator(a);
        assertFalse(match.isAlive(a));
        assertTrue(match.isSpectator(a));
        match.addParticipant(a);
        assertTrue(match.isAlive(a));
        assertFalse(match.isSpectator(a));
    }

    @Test
    @DisplayName("alivePlayers() e spectators() são snapshots imutáveis")
    void snapshotsAreImmutable() {
        Set<UUID> alive = match.alivePlayers();
        Set<UUID> spectators = match.spectators();
        assertThrows(UnsupportedOperationException.class, () -> alive.add(late));
        assertThrows(UnsupportedOperationException.class, () -> spectators.add(late));
    }

    @Test
    @DisplayName("snapshot antigo não muda depois de uma eliminação")
    void snapshotDoesNotFollowChanges() {
        Set<UUID> before = match.alivePlayers();
        match.eliminate(a);
        assertEquals(2, before.size());
        assertEquals(1, match.alivePlayers().size());
        assertEquals(Set.of(a), match.spectators());
    }

    @Test
    @DisplayName("matchId/worldName são obrigatórios e PVP começa desligado")
    void constructorAndDefaults() {
        assertThrows(NullPointerException.class, () -> new MatchState(null, "w"));
        assertThrows(NullPointerException.class, () -> new MatchState("m", null));
        assertFalse(match.isPvpEnabled());
        assertEquals("hgmapa_m1", match.worldName());
    }
}
