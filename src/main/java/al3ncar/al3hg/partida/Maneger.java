package al3ncar.al3hg.partida;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Maneger {
    private static Partida instance;

    public static void init() {
        instance = new Partida();
    }

    public static Partida getPartida() {
        return instance;
    }
}
