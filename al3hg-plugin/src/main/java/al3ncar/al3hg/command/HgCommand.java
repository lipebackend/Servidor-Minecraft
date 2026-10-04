package al3ncar.al3hg.command;

import al3ncar.al3hg.Al3HgPlugin;
import al3ncar.al3hg.game.GameManager;
import al3ncar.al3hg.util.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/** Comando {@code /hgc} (antes {@code HgCore}). */
public final class HgCommand implements TabExecutor {

    private static final List<String> SUBCOMMANDS = List.of("start", "fs", "stop", "rest", "reload", "help");

    private final Al3HgPlugin plugin;
    private final GameManager game;

    public HgCommand(Al3HgPlugin plugin, GameManager game) {
        this.plugin = plugin;
        this.game = game;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("hg.admin")) {
            Messages.send(sender, "§cVocê não tem permissão para usar esse comando!");
            return true;
        }
        if (args.length == 0) {
            Messages.send(sender, "Use /" + label + " help");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                try {
                    plugin.reloadSettings();
                    Messages.send(sender, "§aConfigurações recarregadas!");
                } catch (RuntimeException e) {
                    Messages.send(sender, "§cFalha ao recarregar o plugin: " + e);
                }
            }
            case "start" -> reportStart(sender, game.start(false));
            case "fs" -> reportStart(sender, game.start(true));
            case "stop" -> {
                if (game.stop()) {
                    Messages.send(sender, "§aEncerrando a partida: enviando os jogadores ao lobby...");
                } else {
                    Messages.send(sender, "§cNão há partida em andamento.");
                }
            }
            case "rest" -> {
                if (game.restart()) {
                    Messages.send(sender, "§aReiniciando: jogadores vão ao lobby e a arena será recriada no próximo /" + label + " start.");
                } else {
                    Messages.send(sender, "§cNão há partida em andamento.");
                }
            }
            case "help" -> {
                Messages.send(sender, "/hgc start > inicia a partida (com contagem)");
                Messages.send(sender, "/hgc fs > início rápido (sem contagem)");
                Messages.send(sender, "/hgc stop > para a partida e envia todos ao lobby");
                Messages.send(sender, "/hgc rest > reinicia a partida");
                Messages.send(sender, "/hgc reload > recarrega a configuração");
            }
            default -> Messages.send(sender, "§cSubcomando desconhecido. Use /" + label + " help");
        }
        return true;
    }

    private void reportStart(CommandSender sender, GameManager.StartResult result) {
        switch (result) {
            case OK -> Messages.send(sender, "§aPartida iniciada.");
            case ALREADY_RUNNING -> Messages.send(sender, "§cJá existe uma partida em andamento.");
            case NOT_ENOUGH_PLAYERS -> Messages.send(sender, "§cJogadores insuficientes (use /hgc fs para forçar).");
            case ARENA_UNAVAILABLE -> Messages.send(sender, "§cArena indisponível (veja o console: mapa-modelo ausente ou falha ao carregar).");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
            @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("hg.admin") || args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return SUBCOMMANDS.stream().filter(s -> s.startsWith(prefix)).toList();
    }
}
