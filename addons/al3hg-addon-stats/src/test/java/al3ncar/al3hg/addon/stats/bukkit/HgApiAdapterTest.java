package al3ncar.al3hg.addon.stats.bukkit;

import al3ncar.al3hg.api.event.HgGameEndEvent;
import al3ncar.al3hg.api.event.HgGameStartEvent;
import al3ncar.al3hg.api.event.HgPlayerEliminatedEvent;
import al3ncar.al3hg.api.event.HgPlayerEliminatedEvent.Reason;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** O HgApiAdapter traduz os eventos da API para GameEventsHandler sem perder dados. */
class HgApiAdapterTest {

    private static final UUID A = new UUID(0, 1);
    private static final UUID B = new UUID(0, 2);

    private final List<String> chamadas = new ArrayList<>();
    private final HgApiAdapter adapter = new HgApiAdapter(new GameEventsHandler() {
        @Override
        public void onGameStart(String matchId, Set<UUID> players) {
            chamadas.add("start:" + matchId + ":" + players.size());
        }

        @Override
        public void onPlayerEliminated(String matchId, UUID player, @Nullable UUID killer, boolean byDeath) {
            chamadas.add("elim:" + matchId + ":" + player + ":" + killer + ":" + byDeath);
        }

        @Override
        public void onGameEnd(String matchId, @Nullable UUID winner) {
            chamadas.add("end:" + matchId + ":" + winner);
        }
    });

    @Test
    void inicio_repassa_id_e_jogadores() {
        adapter.onGameStart(new HgGameStartEvent("m1", Set.of(A, B)));
        assertEquals(List.of("start:m1:2"), chamadas);
    }

    @Test
    void morte_vira_bydeath_true_e_saida_vira_false() {
        adapter.onPlayerEliminated(new HgPlayerEliminatedEvent("m1", A, B, Reason.DEATH, 1));
        adapter.onPlayerEliminated(new HgPlayerEliminatedEvent("m1", B, null, Reason.QUIT, 0));
        assertEquals(List.of("elim:m1:" + A + ":" + B + ":true", "elim:m1:" + B + ":null:false"), chamadas);
    }

    @Test
    void fim_repassa_vencedor_ou_null() {
        adapter.onGameEnd(new HgGameEndEvent("m1", A));
        adapter.onGameEnd(new HgGameEndEvent("m1", null));
        assertEquals(List.of("end:m1:" + A, "end:m1:null"), chamadas);
    }
}
