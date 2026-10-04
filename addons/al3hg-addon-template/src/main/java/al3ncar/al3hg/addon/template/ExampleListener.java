package al3ncar.al3hg.addon.template;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reacao de exemplo aos eventos do jogo. Depende apenas de {@link GameEventHandler};
 * os eventos concretos (eventos reais da al3hg-api) ficam em {@link GameEventAdapter}.
 */
public final class ExampleListener implements GameEventHandler {

    private final AsyncService asyncService;
    private final Logger logger;

    public ExampleListener(AsyncService asyncService, Logger logger) {
        this.asyncService = asyncService;
        this.logger = logger;
    }

    @Override
    public void onGameStart() {
        Bukkit.broadcastMessage(Messages.gameStarted());
    }

    @Override
    public void onPlayerEliminated(UUID playerId, String playerName) {
        // Thread principal: os valores simples ja foram copiados pelo adapter.
        // Trabalha fora da thread principal sem usar Bukkit ...
        asyncService.supplyThenSync(
                () -> AsyncService.expensiveCalculation(playerName),
                // ... e volta para a thread principal antes de tocar no jogador.
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
