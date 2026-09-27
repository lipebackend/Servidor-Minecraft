package al3ncar.al3hg.utils;

import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import al3ncar.al3hg.al3hg;

public class EnviarServer {
    public static void SendServer(Player p, String sr) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(sr);
        p.sendPluginMessage(al3hg.getInts(), "BungeeCord", out.toByteArray());
    }
}