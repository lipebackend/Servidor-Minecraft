package al3ncar.al3hg.addon.stats.bukkit;

import al3ncar.al3hg.addon.stats.PlayerStats;
import al3ncar.al3hg.addon.stats.RankingType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

/** Todos os textos mostrados ao jogador (em portugues) ficam aqui. */
public final class Messages {

    private static final Component PREFIX = Component.text("[Stats] ", NamedTextColor.GOLD);

    private Messages() {
    }

    public static Component consoleNeedsPlayer() {
        return error("No console informe o jogador: /hgstats <jogador>");
    }

    public static Component playerNotFound(String name) {
        return error("Jogador '" + name + "' não encontrado (precisa já ter entrado no servidor).");
    }

    public static Component noStats(String name) {
        return error(name + " ainda não tem estatísticas.");
    }

    public static Component unknownRanking(String given) {
        return error("Ranking '" + given + "' inválido. Use: /hgtop [wins|kills]");
    }

    public static Component emptyRanking() {
        return error("O ranking ainda está vazio.");
    }

    public static Component loadFailed() {
        return error("Não foi possível consultar as estatísticas agora. Tente novamente.");
    }

    public static List<Component> stats(String name, PlayerStats stats, long score) {
        return List.of(
                line("Estatísticas de " + name, NamedTextColor.YELLOW),
                line("Vitórias: " + stats.wins() + "  |  Abates: " + stats.kills()
                        + "  |  Mortes: " + stats.deaths(), NamedTextColor.WHITE),
                line("Partidas: " + stats.gamesPlayed() + "  |  Pontos: " + score, NamedTextColor.WHITE));
    }

    public static Component rankingHeader(RankingType type, int size) {
        String title = type == RankingType.WINS ? "vitórias" : "abates";
        return line("Top " + size + " por " + title, NamedTextColor.YELLOW);
    }

    public static Component rankingLine(int position, String name, PlayerStats stats, long score) {
        return line(position + ". " + name + " - vitórias: " + stats.wins()
                + ", abates: " + stats.kills() + " (" + score + " pts)", NamedTextColor.WHITE);
    }

    private static Component error(String text) {
        return line(text, NamedTextColor.RED);
    }

    private static Component line(String text, NamedTextColor color) {
        return PREFIX.append(Component.text(text, color));
    }
}
