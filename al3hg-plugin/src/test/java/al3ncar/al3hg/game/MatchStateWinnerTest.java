package al3ncar.al3hg.game;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Fatia 6: vencedor e fim de jogo (último vivo, empate/ninguém vivo). */
class MatchStateWinnerTest {

    private final UUID a = UUID.randomUUID();
    private final UUID b = UUID.randomUUID();
    private final UUID c = UUID.randomUUID();

    private MatchState matchWith(UUID... ids) {
        MatchState m = new MatchState("m1", "w");
        m.addParticipants(List.of(ids));
        return m;
    }

    @Test
    @DisplayName("com 3 vivos a partida NÃO acabou e não há sobrevivente único")
    void notOverWithThreeAlive() {
        MatchState m = matchWith(a, b, c);
        assertFalse(m.isOver());
        assertTrue(m.soleSurvivor().isEmpty());
    }

    @Test
    @DisplayName("2 vivos ainda não acaba")
    void notOverWithTwoAlive() {
        MatchState m = matchWith(a, b, c);
        m.eliminate(c);
        assertFalse(m.isOver());
    }

    @Test
    @DisplayName("último vivo vence (DEATH dos outros)")
    void lastAliveWinsByDeath() {
        MatchState m = matchWith(a, b, c);
        m.eliminate(a);
        m.eliminate(b);
        assertTrue(m.isOver());
        assertEquals(c, m.soleSurvivor().orElseThrow());
    }

    @Test
    @DisplayName("último vivo vence quando o adversário dá QUIT")
    void lastAliveWinsByQuit() {
        MatchState m = matchWith(a, b);
        m.remove(a);
        assertTrue(m.isOver());
        assertEquals(b, m.soleSurvivor().orElseThrow());
    }

    @Test
    @DisplayName("ninguém vivo (todos saem/morrem): acabou, sem sobrevivente único")
    void nobodyAliveIsOverWithoutSurvivor() {
        MatchState m = matchWith(a, b);
        m.eliminate(a);
        m.remove(b);
        assertTrue(m.isOver());
        assertTrue(m.soleSurvivor().isEmpty());
    }

    @Test
    @DisplayName("winner() é vazio até setWinner; setWinner(null) = partida sem vencedor")
    void winnerLifecycle() {
        MatchState m = matchWith(a, b);
        assertTrue(m.winner().isEmpty());
        m.setWinner(a);
        assertEquals(a, m.winner().orElseThrow());
        m.setWinner(null);
        assertTrue(m.winner().isEmpty());
    }

    @Test
    @DisplayName("o vencedor continua vivo (quem vence não é eliminado)")
    void winnerStaysAlive() {
        MatchState m = matchWith(a, b);
        m.eliminate(b);
        m.setWinner(m.soleSurvivor().orElse(null));
        assertTrue(m.isAlive(a));
        assertEquals(1, m.aliveCount());
    }
}
