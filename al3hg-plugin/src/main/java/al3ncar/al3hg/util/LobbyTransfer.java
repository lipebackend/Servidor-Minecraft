package al3ncar.al3hg.util;

import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import al3ncar.al3hg.Al3HgPlugin;

public class LobbyTransfer {
    public static void SendServer(Player p, String sr) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(sr);
        p.sendPluginMessage(Al3HgPlugin.getInts(), "BungeeCord", out.toByteArray());
    }
}