package al3ncar.al3hg.game;

import al3ncar.al3hg.api.event.HgEvent;
import al3ncar.al3hg.api.event.HgGameEndEvent;
import al3ncar.al3hg.api.event.HgGameStartEvent;
import al3ncar.al3hg.api.event.HgPlayerEliminatedEvent;
import al3ncar.al3hg.api.event.HgPlayerEliminatedEvent.Reason;
import al3ncar.al3hg.api.event.HgStateChangeEvent;
import al3ncar.al3hg.support.EventCollector;
import al3ncar.al3hg.support.GameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Ordem exata dos eventos da API no fluxo normal e no /hgc fs sem jogadores suficientes. */
class GameManagerEventSequenceTest {

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

    private List<String> sequencia() {
        return events.events.stream().map(GameManagerEventSequenceTest::descricao).toList();
    }

    private static String descricao(HgEvent e) {
        return switch (e) {
            case HgStateChangeEvent s -> s.getFrom() + "->" + s.getTo();
            case HgGameStartEvent s -> "START(" + s.getPlayers().size() + ")";
            case HgPlayerEliminatedEvent p -> "ELIM(" + p.getReason() + "," + p.getRemaining() + ")";
            case HgGameEndEvent g -> "END(" + (g.getWinner().isPresent() ? "vencedor" : "sem vencedor") + ")";
            default -> e.getClass().getSimpleName();
        };
    }

    @Test
    void fluxo_normal_countdown_grace_running_ending() {
        PlayerMock a = f.addPlayer("a");
        PlayerMock b = f.addPlayer("b");
        PlayerMock c = f.addPlayer("c");
        f.game.start(false);
        f.passSeconds(10); // contagem (3s) + graça (5s)
        f.game.eliminate(a.getUniqueId(), c.getUniqueId(), Reason.DEATH);
        f.game.handleQuit(b);

        assertEquals(List.of("WAITING->COUNTDOWN", "COUNTDOWN->GRACE", "START(3)", "GRACE->RUNNING",
                "ELIM(DEATH,2)", "ELIM(QUIT,1)", "RUNNING->ENDING", "END(vencedor)"), sequencia());
    }

    @Test
    void inicio_rapido_sem_jogadores_passa_por_countdown_e_vai_direto_a_ending() {
        f.game.start(true);
        assertEquals(List.of("WAITING->COUNTDOWN", "COUNTDOWN->ENDING", "END(sem vencedor)"), sequencia());
    }
}
