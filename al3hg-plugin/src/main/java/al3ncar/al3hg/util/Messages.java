package al3ncar.al3hg.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** Prefixo e envio de mensagens (texto legado com §). Centraliza o que é mostrado ao jogador. */
public final class Messages {

    /** Corrigido: o original era "§a[HGC] &7 " (misturava § e &). */
    public static final String PREFIX = "§a[HGC] §7";

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private Messages() {
    }

    public static Component component(String legacyText) {
        return LEGACY.deserialize(legacyText);
    }

    public static void send(CommandSender to, String text) {
        to.sendMessage(component(PREFIX + text));
    }

    public static void broadcast(String text) {
        Bukkit.broadcast(component(PREFIX + text));
    }
}
