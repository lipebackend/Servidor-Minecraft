package al3ncar.al3hg.command;

import al3ncar.al3hg.enums.PvpStatus;
import al3ncar.al3hg.util.WorldBorderController;
import al3ncar.al3hg.util.ArenaRules;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import al3ncar.al3hg.Al3HgPlugin;
import al3ncar.al3hg.api.GameState;
import al3ncar.al3hg.game.GameManager;
import al3ncar.al3hg.game.MatchRunner;
import al3ncar.al3hg.util.LobbyTransfer;
import al3ncar.al3hg.util.Messages;

public class HgCommand implements CommandExecutor {
    private final GameState abs = null;
    private String ps = Messages.PREFIXO;
    private final MatchRunner b = new MatchRunner();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("hg.admin")) {
            sender.sendMessage(ps + "§aVocê não tem permição pra usar esse comando!");
            return false;
        }
        if (args.length == 0) {
            sender.sendMessage(ps + "Use /" + label + " help");
            return true;
        }
        switch (args[0]) {
            case "reload": {
                try {
                    Al3HgPlugin.getInts().reloadConfig();
                    sender.sendMessage(ps + "§aConfigurações recarregadas!");
                    return true;
                } catch (Exception e) {
                    sender.sendMessage(ps + "§aFalha a recarregar o plugin");
                    sender.sendMessage(ps + "§aERRO " + e);
                    return true;
                }
            }
            case "start": {
                try {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.teleportAsync(p.getWorld().getSpawnLocation());;
                    }
                } catch (Exception e){
                    sender.sendMessage("Erro no comando" + e);
                }
                GameManager.current().setStatusP(GameState.RUNNING);
                GameManager.current().setPvpStatus(PvpStatus.ON);
                WorldBorderController.shrink("hgmapa", 300);
                b.Partidakk();
                return true;
            }
            case "stop": {
                GameManager.current().setStatusP(GameState.ENDING);
                try {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        LobbyTransfer.SendServer(p, "lobby");
                    }
                } catch (Exception e){
                    sender.sendMessage("Erro no comando" + e);
                }
                Bukkit.getServer().shutdown();
                return true;
            }
            case "rest": {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    LobbyTransfer.SendServer(p, "lobby");
                }
                Bukkit.getServer().shutdown();
                return true;
            }
            case "help": {
                sender.sendMessage(ps + "/hgc reload > Reload do plugin");
                sender.sendMessage(ps + "/hgc start > Start partida");
                sender.sendMessage(ps + "/hgc stop > ele para a partida");
                sender.sendMessage(ps + "/hgc rest > ele restarta a partida");
                return true;
            }
            default: {
                sender.sendMessage(ps + "ERRO na syntax");
                return false;
            }
        }
    }
}
