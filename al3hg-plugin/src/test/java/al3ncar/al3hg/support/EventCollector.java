package al3ncar.al3hg.support;

import al3ncar.al3hg.api.event.HgEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

/** Guarda, em ordem, os eventos da API disparados durante o teste. */
public final class EventCollector implements Listener {

    public final List<HgEvent> events = new ArrayList<>();

    public EventCollector(PluginManager pm, Plugin plugin) {
        pm.registerEvents(this, plugin);
    }

    @EventHandler
    public void onAny(al3ncar.al3hg.api.event.HgStateChangeEvent e) { events.add(e); }

    @EventHandler
    public void onStart(al3ncar.al3hg.api.event.HgGameStartEvent e) { events.add(e); }

    @EventHandler
    public void onEliminated(al3ncar.al3hg.api.event.HgPlayerEliminatedEvent e) { events.add(e); }

    @EventHandler
    public void onEnd(al3ncar.al3hg.api.event.HgGameEndEvent e) { events.add(e); }

    public <T extends HgEvent> List<T> of(Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).toList();
    }
}
