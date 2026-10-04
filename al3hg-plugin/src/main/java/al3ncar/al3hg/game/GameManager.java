package al3ncar.al3hg.game;

public class GameManager {
    private static Match instance;

    public static void init() {
        instance = new Match();
    }

    public static Match current() {
        return instance;
    }
}
