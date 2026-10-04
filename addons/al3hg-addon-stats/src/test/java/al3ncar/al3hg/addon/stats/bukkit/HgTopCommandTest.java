package al3ncar.al3hg.addon.stats.bukkit;

import al3ncar.al3hg.addon.stats.PlayerStats;
import al3ncar.al3hg.addon.stats.StatsRules;
import al3ncar.al3hg.addon.stats.repository.InMemoryStatsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** /hgtop com MockBukkit: ranking vazio, tipo inválido, ordenação por vitórias/abates e tab completion. */
class HgTopCommandTest {

    private ServerMock server;
    private InMemoryStatsRepository repo;
    private HgTopCommand command;
    private PlayerMock sender;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        repo = new InMemoryStatsRepository();
        StatsService service = new StatsService(repo, StatsRules.defaults(), Logger.getLogger("teste"));
        command = new HgTopCommand(service, Runnable::run, 3); // "thread principal" síncrona
        sender = server.addPlayer("consulta");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void run(String... args) {
        command.onCommand(sender, null, "hgtop", args);
    }

    @Test
    void ranking_vazio_avisa_o_jogador() {
        run();
        assertTrue(sender.nextMessage().contains("ranking ainda está vazio"));
        assertNull(sender.nextMessage());
    }

    @Test
    void tipo_invalido_mostra_uso() {
        run("xyz");
        assertTrue(sender.nextMessage().contains("inválido"));
    }

    @Test
    void padrao_ordena_por_vitorias_e_kills_ordena_por_abates() {
        PlayerMock a = server.addPlayer("ana");
        PlayerMock b = server.addPlayer("bia");
        repo.save(PlayerStats.empty(a.getUniqueId()).withWins(5).withKills(1)).join();
        repo.save(PlayerStats.empty(b.getUniqueId()).withWins(2).withKills(9)).join();

        run();
        assertTrue(sender.nextMessage().contains("Top 3 por vitórias"));
        assertTrue(sender.nextMessage().contains("1. ana"));
        assertTrue(sender.nextMessage().contains("2. bia"));

        run("KILLS");
        assertTrue(sender.nextMessage().contains("Top 3 por abates"));
        assertTrue(sender.nextMessage().contains("1. bia"));
    }

    @Test
    void tab_completion_filtra_por_prefixo() {
        assertEquals(List.of("wins", "kills"), command.onTabComplete(sender, null, "hgtop", new String[]{""}));
        assertEquals(List.of("kills"), command.onTabComplete(sender, null, "hgtop", new String[]{"K"}));
        assertEquals(List.of(), command.onTabComplete(sender, null, "hgtop", new String[]{"wins", "x"}));
    }
}
