package al3ncar.al3hg.util;

import al3ncar.al3hg.config.HgSettings;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Supplier;

/** Envia jogadores a outro servidor via BungeeCord plugin messaging (antes {@code EnviarServer}). */
public final class LobbyTransfer {

    private final Plugin plugin;
    private final Supplier<HgSettings> settings;

    public LobbyTransfer(Plugin plugin, Supplier<HgSettings> settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    /** Envia ao servidor de lobby configurado. */
    public void sendToLobby(Player player) {
        send(player, settings.get().lobbyServer());
    }

    public void send(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
