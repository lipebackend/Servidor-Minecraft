package al3ncar.al3hg.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import al3ncar.al3hg.al3hg;
import al3ncar.al3hg.utils.PrefixoC;

public class HgCore implements CommandExecutor {
    private String ps = PrefixoC.PREFIXO;

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
                    al3hg.getInts().reloadConfig();
                    sender.sendMessage(ps + "§aConfigurações recarregadas!");
                    return true;
                } catch (Exception e) {
                    sender.sendMessage(ps + "§aFalha a recarregar o plugin");
                    sender.sendMessage(ps + "§aERRO " + e);
                    return true;
                }
            }
            case "fs": {
                sender.sendMessage(ps + "Em desenvolvimento");
                return false;
            }
            case "help": {
                sender.sendMessage("");
                return true;
            }
            default: {
                sender.sendMessage(ps + "Em desenvolvimento");
                return false;
            }
        }
    }
}
