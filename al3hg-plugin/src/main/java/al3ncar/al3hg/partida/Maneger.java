package al3ncar.al3hg.partida;

public class Maneger {
    private static Partida instance;

    public static void init() {
        instance = new Partida();
    }

    public static Partida getPartida() {
        return instance;
    }
}
