package al3ncar.al3hg.partida;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

public class Partida {
    private final Set<UUID> jogadores = new HashSet<>();
    private StatsPartida estadoP = StatsPartida.INCIOS;

    public void setStatusP(StatsPartida stts) {
        estadoP = stts;
    }

    public StatsPartida getStatusP() {
        return estadoP;
    }

    public void removerJogadores(Player p) {
        jogadores.remove(p.getUniqueId());
    }

    public void adicionarJogadores(Player p) {
        jogadores.add(p.getUniqueId());
    }

    public int getJogadoresVivos() {
        return jogadores.size();
    }
}
