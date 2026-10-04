package al3ncar.al3hg.addon.stats.bukkit;

import al3ncar.al3hg.addon.stats.PlayerStats;
import al3ncar.al3hg.addon.stats.RankingType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * {@code /hgtop [wins|kills]} (padrao: wins). Consulta fora da thread principal; os nomes
 * (Bukkit) e o envio so sao resolvidos de volta nela. Permissao {@code hg.stats} no plugin.yml.
 */
public final class HgTopCommand implements CommandExecutor, TabCompleter {

    private static final List<String> TYPES = List.of("wins", "kills");

    private final StatsService service;
    private final Executor mainThread;
    private final int topSize;

    public HgTopCommand(StatsService service, Executor mainThread, int topSize) {
        this.service = Objects.requireNonNull(service, "service");
        this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
        this.topSize = topSize;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        RankingType type = RankingType.WINS;
        if (args.length > 0) {
            try {
                type = RankingType.valueOf(args[0].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Messages.unknownRanking(args[0]));
                return true;
            }
        }

        RankingType ranking = type;
        service.top(ranking, topSize).whenCompleteAsync((top, error) -> {
            if (error != null) {
                service.logFailure("Falha ao consultar o ranking", error);
                sender.sendMessage(Messages.loadFailed());
            } else if (top.isEmpty()) {
                sender.sendMessage(Messages.emptyRanking());
            } else {
                sender.sendMessage(Messages.rankingHeader(ranking, topSize));
                for (int i = 0; i < top.size(); i++) {
                    PlayerStats stats = top.get(i);
                    String name = PlayerNames.of(Bukkit.getOfflinePlayer(stats.uuid()));
                    sender.sendMessage(Messages.rankingLine(i + 1, name, stats, service.score(stats)));
                }
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
        return TYPES.stream().filter(type -> type.startsWith(prefix)).toList();
    }
}
