package al3ncar.al3hg.mapa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.bukkit.plugin.Plugin;

public class RegeneretorWorld {

    /**
     * Apaga a pasta do mundo direto no disco.
     * Deve ser chamado no onEnable() ANTES do mundo ser carregado,
     * ou com o mundo já descarregado.
     */
    public static boolean limparMapa(Plugin plugin, String nomeMundo) {
        // A pasta do mundo fica na raiz do servidor, ao lado de plugins/
        Path pastaMundo = plugin.getServer().getWorldContainer().toPath().resolve(nomeMundo);

        if (!Files.exists(pastaMundo)) {
            plugin.getLogger().info("Mundo " + nomeMundo + " não existe ainda, nada a apagar.");
            return false;
        }

        try (Stream<Path> stream = Files.walk(pastaMundo)) {
            stream.sorted(Comparator.reverseOrder())
                  .forEach(p -> {
                      try {
                          Files.deleteIfExists(p);
                      } catch (IOException e) {
                          plugin.getLogger().warning("Falha ao deletar " + p + ": " + e.getMessage());
                      }
                  });
            plugin.getLogger().info("Mundo " + nomeMundo + " apagado com sucesso.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao apagar mundo " + nomeMundo + ": " + e.getMessage());
            return false;
        }
    }
}