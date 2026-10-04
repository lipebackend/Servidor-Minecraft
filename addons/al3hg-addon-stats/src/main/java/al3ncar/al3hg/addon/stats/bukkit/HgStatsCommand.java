package al3ncar.al3hg.addon.stats.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * {@code /hgstats [jogador]}. A consulta roda no executor do repositorio; a resposta ao
 * {@link CommandSender} volta a thread principal via {@code mainThread}. A permissao
 * ({@code hg.stats}) e verificada pelo Bukkit a partir do plugin.yml.
 */
public final class HgStatsCommand implements CommandExecutor, TabCompleter {

    private final StatsService service;
    private final Executor mainThread;

    public HgStatsCommand(StatsService service, Executor mainThread) {
        this.service = Objects.requireNonNull(service, "service");
        this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        OfflinePlayer target;
        if (args.length == 0) {
            if (!(sender instanceof Player self)) {
                sender.sendMessage(Messages.consoleNeedsPlayer());
                return true;
            }
            target = self;
        } else {
            target = Bukkit.getOfflinePlayerIfCached(args[0]); // so le o cache local: sem rede na thread principal
            if (target == null) {
                sender.sendMessage(Messages.playerNotFound(args[0]));
                return true;
            }
        }

        String name = PlayerNames.of(target);
        service.find(target.getUniqueId()).whenCompleteAsync((found, error) -> {
            if (error != null) {
                service.logFailure("Falha ao consultar estatísticas de " + name, error);
                sender.sendMessage(Messages.loadFailed());
            } else if (found.isEmpty()) {
                sender.sendMessage(Messages.noStats(name));
            } else {
                Messages.stats(name, found.get(), service.score(found.get())).forEach(sender::sendMessage);
            }
        }, mainThread);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
    }
}
