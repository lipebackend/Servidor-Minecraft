package al3ncar.al3hg.addon.stats.bukkit;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Regras de negocio dos eventos: abate para o autor (so em morte, nunca em saida nem suicidio),
 * morte para o jogador, vitoria para o vencedor e partida jogada para quem comecou a partida.
 *
 * <p>Os callbacks chegam na thread principal (contrato da API), que e a unica a tocar em
 * {@link #participants}; a gravacao em si e feita pelo {@link StatsService} fora dela.
 */
public final class StatsListener implements GameEventsHandler {

    private final StatsService service;
    private final boolean quitCountsAsDeath;
    /** Jogadores que iniciaram cada partida em andamento (matchId -> jogadores). */
    private final Map<String, Set<UUID>> participants = new HashMap<>();

    public StatsListener(StatsService service, boolean quitCountsAsDeath) {
        this.service = Objects.requireNonNull(service, "service");
        this.quitCountsAsDeath = quitCountsAsDeath;
    }

    @Override
    public void onGameStart(String matchId, Set<UUID> players) {
        participants.clear(); // so existe uma partida por vez: descarta restos de partidas sem HgGameEndEvent
        participants.put(matchId, Set.copyOf(players));
    }

    @Override
    public void onPlayerEliminated(String matchId, UUID player, @Nullable UUID killer, boolean byDeath) {
        if (byDeath && killer != null && !killer.equals(player)) {
            service.recordKill(killer);
        }
        if (byDeath || quitCountsAsDeath) {
            service.recordDeath(player);
        }
    }

    @Override
    public void onGameEnd(String matchId, @Nullable UUID winner) {
        if (winner != null) {
            service.recordWin(winner);
        }
        Set<UUID> players = participants.remove(matchId);
        if (players != null) {
            service.recordGamePlayed(players);
        }
    }
}
