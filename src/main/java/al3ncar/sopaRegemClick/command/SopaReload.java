package al3ncar.sopaRegemClick.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import al3ncar.sopaRegemClick.utils.PrefixoC;

public class SopaReload implements CommandExecutor {
    private PrefixoC ps;
    @Override 
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args){
        if(!sender.hasPermission("hgsopa.admin")){
            sender.sendMessage(ps.PrefixoA() + "§aVocê não tem permição pra usar esse comando!");
            return false;
        }
        if(args.length == 0){
            sender.sendMessage(ps.PrefixoA() + "Use /" + label + " realod");
            return true;
        }
        if(args[0] == "reload"){
            sender.sendMessage(ps.PrefixoA() + "§aConfigurações recarregadas!");
            return true;
        }
        return false;
    }
}