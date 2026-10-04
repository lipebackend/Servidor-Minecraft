package al3ncar.al3hg.addon.stats.repository;

import al3ncar.al3hg.addon.stats.PlayerStats;

import java.util.Objects;
import java.util.UUID;

/** Validacoes compartilhadas pelas implementacoes de {@link StatsRepository#update}. */
final class StatsUpdates {

    private StatsUpdates() {
    }

    /**
     * Garante que o resultado de {@code change} nao e null e pertence ao mesmo
     * jogador. Lanca NullPointerException ou IllegalStateException; nada deve ser gravado.
     */
    static PlayerStats requireSameUuid(UUID expected, PlayerStats result) {
        Objects.requireNonNull(result, "change retornou null");
        if (!expected.equals(result.uuid())) {
            throw new IllegalStateException(
                    "change retornou stats de outro jogador: esperado " + expected + " mas veio " + result.uuid());
        }
        return result;
    }
}
