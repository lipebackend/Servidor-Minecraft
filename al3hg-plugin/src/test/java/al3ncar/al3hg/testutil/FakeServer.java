package al3ncar.al3hg.testutil;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Logger;

import static al3ncar.al3hg.testutil.Stubs.PASS;
import static al3ncar.al3hg.testutil.Stubs.stub;

/**
 * Servidor Bukkit FALSO mínimo (Proxy) só para exercitar o GameManager sem MockBukkit.
 * Registra a ORDEM das ações em {@link #log} ("lobby:Nome", "dispose", "shutdown", "border:300.0").
 * Substituir por MockBukkit quando ele entrar no pom.
 */
public final class FakeServer {

    public record Task(long delay, Runnable action, boolean[] cancelled) { }

    public static final String ARENA = "arena1";
    public static final FakeServer I = new FakeServer();

    public final List<String> log = new ArrayList<>();
    public final List<Event> events = new ArrayList<>();
    public final List<Task> tasks = new ArrayList<>();
    public final Map<UUID, Player> online = new LinkedHashMap<>();
    public final Map<String, GameMode> modes = new HashMap<>();
    public final Plugin plugin = stub(Plugin.class, (m, a) -> m.equals("getLogger") ? Logger.getLogger("test") : PASS);
    private final World world = stub(World.class, (m, a) -> switch (m) {
        case "getName" -> ARENA;
        case "getWorldBorder" -> border;
        default -> PASS;
    });
    private static final WorldBorder border = stub(WorldBorder.class, (m, a) -> {
        if (m.equals("setSize")) I.log.add("border:" + a[0]);
        return PASS;
    });

    public static void install() {
        if (Bukkit.getServer() == null) setBukkitServer(I.server());
        I.reset();
    }

    /** Bukkit.setServer exige ServerBuildInfo (só existe num Paper real); grava o campo direto. */
    public static void setBukkitServer(Server server) {
        try {
            var f = Bukkit.class.getDeclaredField("server");
            f.setAccessible(true);
            f.set(null, server);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Desfaz a instalação (ex.: antes de um teste com MockBukkit na mesma JVM). */
    public static void uninstall() {
        setBukkitServer(null);
    }

    public void reset() {
        log.clear(); events.clear(); tasks.clear(); online.clear(); modes.clear();
    }

    public Player player(String name) {
        UUID id = UUID.nameUUIDFromBytes(name.getBytes());
        Player p = stub(Player.class, (m, a) -> switch (m) {
            case "getUniqueId" -> id;
            case "getName" -> name;
            case "isOnline" -> true;
            case "getWorld" -> world;
            case "getInventory" -> stub(org.bukkit.inventory.PlayerInventory.class, (im, ia) -> PASS);
            case "setGameMode" -> modes.put(name, (GameMode) a[0]);
            case "sendPluginMessage" -> log.add("lobby:" + name);
            default -> PASS;
        });
        online.put(id, p);
        return p;
    }

    /** Executa as tasks não canceladas em ordem de atraso (simula a passagem do tempo). */
    public void runTasks(long upToTicks) {
        tasks.stream().filter(t -> !t.cancelled()[0] && t.delay() <= upToTicks)
                .sorted(Comparator.comparingLong(Task::delay)).toList().forEach(t -> t.action().run());
    }

    /** ArenaRules usa GameRule (precisa de registry do Paper): para essa chamada o mundo "não está carregado". */
    private static boolean fromArenaRules() {
        return Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(f -> f.getClassName().endsWith(".ArenaRules"));
    }

    private Server server() {
        BukkitScheduler scheduler = stub(BukkitScheduler.class, (m, a) -> {
            if (!m.startsWith("run") || !(a[1] instanceof Runnable r)) return PASS;
            boolean[] c = new boolean[1];
            tasks.add(new Task(m.equals("runTask") ? 0 : (long) a[2], r, c));
            return stub(BukkitTask.class, (tm, ta) -> { if (tm.equals("cancel")) c[0] = true; return PASS; });
        });
        PluginManager pm = stub(PluginManager.class, (m, a) -> { if (m.equals("callEvent")) events.add((Event) a[0]); return PASS; });
        return stub(Server.class, (m, a) -> switch (m) {
            case "getLogger" -> Logger.getLogger("fake");
            case "getName", "getVersion", "getBukkitVersion" -> "fake";
            case "getOnlinePlayers" -> List.copyOf(online.values());
            case "getPlayer" -> online.get(a[0]);
            case "getWorld" -> ARENA.equals(a[0]) && !fromArenaRules() ? world : null;
            case "getOfflinePlayer" -> stub(OfflinePlayer.class, (om, oa) -> om.equals("getName") ? "alguem" : PASS);
            case "getScheduler" -> scheduler;
            case "getPluginManager" -> pm;
            case "shutdown" -> log.add("shutdown");
            default -> PASS;
        });
    }
}
