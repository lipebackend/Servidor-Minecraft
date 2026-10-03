package al3ncar.al3hg.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import al3ncar.al3hg.al3hg;
import al3ncar.al3hg.partida.Maneger;
import al3ncar.al3hg.partida.PartidaRolando;
import al3ncar.al3hg.partida.StatsPartida;
import al3ncar.al3hg.utils.EnviarServer;
import al3ncar.al3hg.utils.PrefixoC;

public class HgCore implements CommandExecutor {
    private static StatsPartida a;
    private String ps = PrefixoC.PREFIXO;
    private final PartidaRolando b = new PartidaRolando();

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
                for (int a = 100; a > 0; a--) {
                    double b = a/10;
                    sender.sendMessage(ps + "Iniciando a partida em " + b);
                }
                Maneger.getPartida().setStatusP(a.MEIO);
                b.Partidakk();
                return true;
            }
            case "stop": {
                for (int a = 100; a > 0; a--) {
                    double b = a/10;
                    sender.sendMessage(ps + "Parando a partida em " + b);
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    EnviarServer.SendServer(p, "lobby");
                }
                Bukkit.getServer().shutdown();
                return true;
            }
            case "rest": {
                for (int a = 100; a > 0; a--) {
                    double b = a/10;
                    sender.sendMessage(ps + "Reniciando a partida em " + b);
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    EnviarServer.SendServer(p, "lobby");
                }
                Bukkit.restart();
                return true;
            }
            case "help": {
                sender.sendMessage(ps + "/hgc reload > Reload do plugin");
                sender.sendMessage(ps + "/hgc fs > Fast Start partida");
                return true;
            }
            default: {
                sender.sendMessage(ps + "ERRO na syntax");
                return false;
            }
        }
    }
}
