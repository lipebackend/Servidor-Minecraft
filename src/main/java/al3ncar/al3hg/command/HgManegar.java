package al3ncar.al3hg.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import al3ncar.al3hg.utils.PrefixoC;

public class HgManegar implements CommandExecutor {
    private String ps = PrefixoC.PREFIXO;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("hg.admin")) {
            sender.sendMessage(ps + "§aVocê não tem permição pra usar esse comando!");
            return false;
        }
        return false;

    }
}
