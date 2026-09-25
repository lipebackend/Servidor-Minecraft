package al3ncar.sopaRegemClick.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import al3ncar.sopaRegemClick.SopaRegemClick;
import al3ncar.sopaRegemClick.utils.PrefixoC;

public class SopaReload implements CommandExecutor {
    private String ps = PrefixoC.PREFIXO;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("hgsopa.admin")) {
            sender.sendMessage(ps + "§aVocê não tem permição pra usar esse comando!");
            return false;
        }
        if (args.length == 0) {
            sender.sendMessage(ps + "Use /" + label + " realod");
            return true;
        }
        switch (args[0]) {
            case "reload": {
                try {
                    SopaRegemClick.getInts().reloadConfig();
                    sender.sendMessage(ps + "§aConfigurações recarregadas!");
                    return true;
                } catch (Exception e) {
                    sender.sendMessage(ps + "§aFalha a recarregar o plugin");
                    sender.sendMessage(ps + "§aERRO " + e);
                    return true;
                }
            }
            default: {
                return false;
            }
        }
    }
}
