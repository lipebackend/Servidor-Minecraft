package al3ncar.al3hg.arena;

import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loader SOMENTE LEITURA de arquivos {@code <diretório>/<nome>.slime}, usado para ler o
 * mapa-modelo uma vez. (O loader de arquivos do ASP vive no plugin do servidor, não na API.)
 */
public final class FileSlimeLoader implements SlimeLoader {

    private static final String EXTENSION = ".slime";

    private final Path directory;

    public FileSlimeLoader(Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
    }

    private Path resolve(String worldName) throws UnknownWorldException {
        if (worldName == null || worldName.isBlank() || worldName.contains("/") || worldName.contains("\\")
                || worldName.contains("..")) {
            throw new UnknownWorldException(String.valueOf(worldName));
        }
        return directory.resolve(worldName + EXTENSION);
    }

    @Override
    public byte[] readWorld(String worldName) throws UnknownWorldException, IOException {
        Path file = resolve(worldName);
        if (!Files.isRegularFile(file)) {
            throw new UnknownWorldException(worldName);
        }
        return Files.readAllBytes(file);
    }

    @Override
    public boolean worldExists(String worldName) {
        try {
            return Files.isRegularFile(resolve(worldName));
        } catch (UnknownWorldException e) {
            return false;
        }
    }

    @Override
    public List<String> listWorlds() throws IOException {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(EXTENSION))
                    .map(n -> n.substring(0, n.length() - EXTENSION.length()))
                    .sorted()
                    .toList();
        }
    }

    @Override
    public void saveWorld(String worldName, byte[] serializedWorld) throws IOException {
        throw new IOException("FileSlimeLoader é somente leitura");
    }

    @Override
    public void deleteWorld(String worldName) throws IOException {
        throw new IOException("FileSlimeLoader é somente leitura");
    }
}
