package al3ncar.al3hg.arena;

import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link SlimeLoader} em memória para os clones das partidas: nada é gravado em disco,
 * então não sobram arquivos de arenas antigas (substitui o {@code limparMapa}).
 */
public final class MemorySlimeLoader implements SlimeLoader {

    private final Map<String, byte[]> worlds = new ConcurrentHashMap<>();

    @Override
    public byte[] readWorld(String worldName) throws UnknownWorldException {
        byte[] data = worlds.get(worldName);
        if (data == null) {
            throw new UnknownWorldException(worldName);
        }
        return data.clone();
    }

    @Override
    public boolean worldExists(String worldName) {
        return worlds.containsKey(worldName);
    }

    @Override
    public List<String> listWorlds() {
        return new ArrayList<>(worlds.keySet());
    }

    @Override
    public void saveWorld(String worldName, byte[] serializedWorld) {
        worlds.put(worldName, serializedWorld.clone());
    }

    @Override
    public void deleteWorld(String worldName) throws UnknownWorldException {
        if (worlds.remove(worldName) == null) {
            throw new UnknownWorldException(worldName);
        }
    }

    /** Remove sem erro se não existir. */
    public void deleteQuietly(String worldName) {
        worlds.remove(worldName);
    }
}
