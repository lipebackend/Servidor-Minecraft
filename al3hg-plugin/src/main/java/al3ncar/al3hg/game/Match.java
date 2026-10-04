package al3ncar.al3hg.game;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import al3ncar.al3hg.api.GameState;
import al3ncar.al3hg.enums.PvpStatus;

public class Match {
    private final Set<UUID> jogadores = new HashSet<>();
    private GameState estadoP = GameState.WAITING;
    private PvpStatus estadoPvp = PvpStatus.OFF;

    public void setStatusP(GameState stts) {
        estadoP = stts;
    }
    
    public GameState getStatusP() {
        return estadoP;
    }

    public void setPvpStatus(PvpStatus pvp){
        estadoPvp = pvp;
    }

    public PvpStatus getStatusPvp(){
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
