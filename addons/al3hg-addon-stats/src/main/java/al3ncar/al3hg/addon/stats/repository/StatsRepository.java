package al3ncar.al3hg.addon.stats.repository;

import al3ncar.al3hg.addon.stats.PlayerStats;
import al3ncar.al3hg.addon.stats.RankingType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

/**
 * Persistencia assincrona de estatisticas. Os futures completam em threads do
 * proprio repositorio: quem for tocar na API do Bukkit deve voltar para a
 * thread principal antes.
 */
public interface StatsRepository extends AutoCloseable {

    CompletableFuture<Optional<PlayerStats>> load(UUID uuid);

    CompletableFuture<Void> save(PlayerStats stats);

    /** Os {@code limit} melhores, na ordem definida por {@code Ranking}. */
    CompletableFuture<List<PlayerStats>> top(RankingType type, int limit);

    /**
     * Le (ou cria vazio), aplica {@code change} e grava, de forma atomica em
     * relacao as demais operacoes do repositorio. Devolve o valor gravado.
     */
    CompletableFuture<PlayerStats> update(UUID uuid, UnaryOperator<PlayerStats> change);

    @Override
    void close();
}
