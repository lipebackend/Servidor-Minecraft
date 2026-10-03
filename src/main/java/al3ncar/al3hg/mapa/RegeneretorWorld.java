package al3ncar.al3hg.mapa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RegeneretorWorld {

    public static boolean limparMapa(String nomeMundo) {
        World world = Bukkit.getWorld(nomeMundo);
        if (world == null) {
            return false; // mundo não existe / não carregado
        }

        // Escolhe um mundo de destino diferente do que será apagado
        World destino = Bukkit.getWorlds().stream()
                .filter(w -> !w.equals(world))
                .findFirst()
                .orElse(null);

        if (destino == null) {
            return false; // não tem pra onde mandar os players
        }

        // Tira todos os players de dentro
        for (Player p : world.getPlayers()) {
            p.teleport(destino.getSpawnLocation());
        }

        // Descarrega SEM salvar (false), porque vamos deletar
        if (!Bukkit.unloadWorld(world, false)) {
            return false; // não conseguiu descarregar
        }

        // Deleta a pasta
        Path pasta = world.getWorldFolder().toPath();
        try (var s = Files.walk(pasta)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}