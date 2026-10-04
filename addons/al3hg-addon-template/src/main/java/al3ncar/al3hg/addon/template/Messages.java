package al3ncar.al3hg.addon.template;

/**
 * Todos os textos mostrados ao jogador ficam aqui (em portugues).
 * Ao copiar o template, troque/expanda conforme o addon.
 */
public final class Messages {

    public static final String PREFIX = "[Template] ";

    private Messages() {
    }

    public static String gameStarted() {
        return PREFIX + "A partida comecou! Boa sorte.";
    }

    public static String eliminated(int processedChars) {
        return PREFIX + "Voce foi eliminado. (processado de forma assincrona: " + processedChars + ")";
    }
}
