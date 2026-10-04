package al3ncar.al3hg.arena;

import al3ncar.al3hg.config.HgSettings;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.exceptions.WorldAlreadyExistsException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * {@link ArenaProvider} baseado no AdvancedSlimePaper 4.2.0.
 *
 * <ul>
 *   <li>O mapa-modelo é lido UMA vez ({@code readWorld(..., readOnly=true, ...)}).</li>
 *   <li>Cada partida: {@code template.clone("hg-" + matchId, loader)} + {@code loadWorld} na thread principal.</li>
 *   <li>Descarte: jogadores restantes vão a um mundo de fallback e então
 *       {@code Bukkit.unloadWorld(nome, false)} (sem salvar).</li>
 * </ul>
 * Guarda apenas {@code matchId -> nome do mundo}; nenhum World/Chunk/Location.
 */
public final class AspArenaProvider implements ArenaProvider {

    private final AdvancedSlimePaperAPI asp;
    private final Supplier<HgSettings> settings;
    private final Logger log;
    private final MemorySlimeLoader cloneLoader = new MemorySlimeLoader();
    private final Map<String, String> worlds = new ConcurrentHashMap<>();
    private volatile SlimeWorld template;

    public AspArenaProvider(AdvancedSlimePaperAPI asp, Supplier<HgSettings> settings, Logger log) {
        this.asp = asp;
        this.settings = settings;
        this.log = log;
    }

    public static String worldNameFor(String matchId) {
        return "hg-" + matchId;
    }

    /** Lê o mapa-modelo do disco (somente leitura). Pode ser chamado de novo para recarregar. */
    @Override
    public void reloadTemplate() {
        HgSettings s = settings.get();
        Path dir = Bukkit.getWorldContainer().toPath().resolve(s.slimeDirectory());
        SlimeLoader loader = new FileSlimeLoader(dir);
        try {
            template = asp.readWorld(loader, s.templateWorld(), true, new SlimePropertyMap());
            log.info("Mapa-modelo '" + s.templateWorld() + "' carregado de " + dir);
        } catch (UnknownWorldException e) {
            template = null;
            log.severe("Mapa-modelo '" + s.templateWorld() + ".slime' não encontrado em " + dir);
        } catch (IOException | CorruptedWorldException | NewerFormatException e) {
            template = null;
            log.severe("Falha ao ler o mapa-modelo '" + s.templateWorld() + "': " + e);
        }
    }

    @Override
    public boolean isReady() {
        return template != null;
    }

    @Override
    public String prepare(String matchId) throws ArenaException {
        requireMainThread();
        SlimeWorld base = template;
        if (base == null) {
            throw new ArenaException("Mapa-modelo não carregado");
        }
        if (worlds.containsKey(matchId)) {
            throw new ArenaException("Já existe arena para a partida " + matchId);
        }
        String name = worldNameFor(matchId);
        try {
            SlimeWorld clone = base.clone(name, cloneLoader);
            SlimeWorldInstance instance = asp.loadWorld(clone, true);
            String loaded = instance.getBukkitWorld().getName();
            worlds.put(matchId, loaded);
            return loaded;
        } catch (WorldAlreadyExistsException | IOException | IllegalArgumentException e) {
            cloneLoader.deleteQuietly(name);
            throw new ArenaException("Falha ao criar a arena " + name, e);
        }
    }

    @Override
    public void dispose(String matchId) {
        requireMainThread();
        String name = worlds.remove(matchId);
        if (name == null) {
            return;
        }
        World world = Bukkit.getWorld(name);
        if (world != null) {
            evacuate(world);
            if (!Bukkit.unloadWorld(name, false)) {
                log.warning("Bukkit.unloadWorld(\"" + name + "\", false) retornou false");
            }
        }
        cloneLoader.deleteQuietly(name);
    }

    @Override
    public void disposeAll() {
        for (String matchId : java.util.List.copyOf(worlds.keySet())) {
            dispose(matchId);
        }
    }

    @Override
    public Optional<String> worldName(String matchId) {
        return Optional.ofNullable(worlds.get(matchId));
    }

    /** Move quem sobrou no mundo para um mundo de fallback (síncrono) — o unload falha com jogadores dentro. */
    private void evacuate(World arena) {
        World fallback = null;
        for (World w : Bukkit.getWorlds()) {
            if (!w.equals(arena) && !worlds.containsValue(w.getName())) {
                fallback = w;
                break;
            }
        }
        for (Player p : java.util.List.copyOf(arena.getPlayers())) {
            if (fallback != null) {
                Location spawn = fallback.getSpawnLocation();
                p.teleport(spawn);
            } else {
                p.kick(Component.text("Partida encerrada"));
            }
        }
    }

    private static void requireMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("ArenaProvider deve ser usado na thread principal");
        }
    }
}
