package al3ncar.al3hg.addon.template;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.server.ServerLoadEvent;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listener de exemplo.
 *
 * <p>TODO(fase 1 - al3hg-api): trocar os eventos PLACEHOLDER do Bukkit pelos
 * eventos reais do al3HG assim que {@code al3hg-api} existir:
 * <pre>
 *   ServerLoadEvent      -> al3ncar.al3hg.api.event.HgGameStartEvent
 *   PlayerDeathEvent     -> al3ncar.al3hg.api.event.HgPlayerEliminatedEvent (player(), killer())
 * </pre>
 * Contrato da API: esses eventos sao disparados sempre na thread principal.
 * Tambem checar {@code game.state()} (HgGame via ServicesManager) antes de agir.
 */
public final class ExampleListener implements Listener {

    private final AsyncService asyncService;
    private final Logger logger;

    public ExampleListener(AsyncService asyncService, Logger logger) {
        this.asyncService = asyncService;
        this.logger = logger;
    }

    /** TODO(fase 1): PLACEHOLDER de HgGameStartEvent. */
    @EventHandler
    public void onGameStart(ServerLoadEvent event) {
        Bukkit.broadcastMessage(Messages.gameStarted());
    }

    /** TODO(fase 1): PLACEHOLDER de HgPlayerEliminatedEvent. */
    @EventHandler
    public void onPlayerEliminated(PlayerDeathEvent event) {
        // Thread principal: copie o que precisa do Bukkit (valores simples) ...
        UUID playerId = event.getEntity().getUniqueId();
        String name = event.getEntity().getName();

        // ... trabalhe fora da thread principal sem usar Bukkit ...
        asyncService.supplyThenSync(
                () -> AsyncService.expensiveCalculation(name),
                // ... e volte para a thread principal antes de tocar no jogador.
                result -> {
                    Player player = Bukkit.getPlayer(playerId); // pode ter saido nesse meio-tempo
                    if (player != null) {
                        player.sendMessage(Messages.eliminated(result));
                    }
                }
        ).exceptionally(error -> {
            logger.log(Level.WARNING, "Falha no processamento assincrono", error);
            return null;
        });
    }
}
