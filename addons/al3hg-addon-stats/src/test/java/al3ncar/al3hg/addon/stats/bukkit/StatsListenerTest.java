package al3ncar.al3hg.addon.stats.bukkit;

import al3ncar.al3hg.addon.stats.PlayerStats;
import al3ncar.al3hg.addon.stats.StatsRules;
import al3ncar.al3hg.addon.stats.repository.InMemoryStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Regras de negócio dos eventos: abate, morte, vitória e partida jogada (repositório em memória, síncrono). */
class StatsListenerTest {

    private static final UUID A = new UUID(0, 1);
    private static final UUID B = new UUID(0, 2);
    private static final UUID C = new UUID(0, 3);

    private InMemoryStatsRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryStatsRepository();
    }

    private StatsListener listener(boolean quitCountsAsDeath) {
        return new StatsListener(new StatsService(repo, StatsRules.defaults(), Logger.getLogger("teste")), quitCountsAsDeath);
    }

    private PlayerStats stats(UUID id) {
        return repo.load(id).join().orElse(PlayerStats.empty(id));
    }

    @Test
    void morte_com_autor_conta_abate_para_o_autor_e_morte_para_a_vitima() {
        listener(false).onPlayerEliminated("m", B, A, true);
        assertEquals(1, stats(A).kills());
        assertEquals(1, stats(B).deaths());
        assertEquals(0, stats(A).deaths());
    }

    @Test
    void suicidio_nao_conta_abate_mas_conta_morte() {
        listener(false).onPlayerEliminated("m", A, A, true);
        assertEquals(0, stats(A).kills());
        assertEquals(1, stats(A).deaths());
    }

    @Test
    void morte_sem_autor_conta_so_a_morte() {
        listener(false).onPlayerEliminated("m", A, null, true);
        assertEquals(1, stats(A).deaths());
        assertEquals(0, stats(A).kills());
    }

    @Test
    void saida_conta_morte_somente_com_count_quit_as_death() {
        listener(false).onPlayerEliminated("m", A, null, false);
        assertEquals(0, stats(A).deaths());
        listener(true).onPlayerEliminated("m", A, null, false);
        assertEquals(1, stats(A).deaths());
    }

    @Test
    void saida_nunca_conta_abate_mesmo_com_autor() {
        listener(true).onPlayerEliminated("m", B, A, false);
        assertEquals(0, stats(A).kills());
    }

    @Test
    void fim_da_partida_conta_vitoria_e_partida_para_quem_comecou() {
        StatsListener l = listener(false);
        l.onGameStart("m", Set.of(A, B, C));
        l.onGameEnd("m", A);
        assertEquals(1, stats(A).wins());
        assertEquals(1, stats(A).gamesPlayed());
        assertEquals(1, stats(B).gamesPlayed());
        assertEquals(0, stats(B).wins());
    }

    @Test
    void fim_sem_vencedor_conta_so_partidas_e_nao_conta_duas_vezes() {
        StatsListener l = listener(false);
        l.onGameStart("m", Set.of(A, B));
        l.onGameEnd("m", null);
        l.onGameEnd("m", null);
        assertEquals(1, stats(A).gamesPlayed());
        assertEquals(0, stats(A).wins());
    }

    @Test
    void nova_partida_descarta_participantes_de_partida_sem_fim() {
        StatsListener l = listener(false);
        l.onGameStart("velha", Set.of(A));
        l.onGameStart("nova", Set.of(B));
        l.onGameEnd("nova", null);
        assertEquals(0, stats(A).gamesPlayed());
        assertEquals(1, stats(B).gamesPlayed());
    }
}
