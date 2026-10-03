package al3ncar.al3hg.partida;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import al3ncar.al3hg.enums.StatusPartida;
import al3ncar.al3hg.enums.StatusPvp;

public class Partida {
    private final Set<UUID> jogadores = new HashSet<>();
    private StatusPartida estadoP = StatusPartida.INCIOS;
    private StatusPvp estadoPvp = StatusPvp.OFF;

    public void setStatusP(StatusPartida stts) {
        estadoP = stts;
    }
    
    public StatusPartida getStatusP() {
        return estadoP;
    }

    public void setPvpStatus(StatusPvp pvp){
        estadoPvp = pvp;
    }

    public StatusPvp getStatusPvp(){
        return estadoPvp;
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
