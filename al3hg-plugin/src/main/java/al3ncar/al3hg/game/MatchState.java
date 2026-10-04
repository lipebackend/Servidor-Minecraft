package al3ncar.al3hg.game;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Estado puro de uma partida (sem Bukkit). Guarda apenas o id da partida, o NOME do mundo
 * e os UUIDs dos jogadores — nunca World, Chunk ou Location.
 */
public final class MatchState {

    private final String matchId;
    private final String worldName;
    private final Set<UUID> alive = new LinkedHashSet<>();
    private final Set<UUID> spectators = new LinkedHashSet<>();
    private UUID winner;
    private boolean pvpEnabled;

    public MatchState(String matchId, String worldName) {
        this.matchId = Objects.requireNonNull(matchId, "matchId");
        this.worldName = Objects.requireNonNull(worldName, "worldName");
    }

    public String matchId() {
        return matchId;
    }

    public String worldName() {
        return worldName;
    }

    /** Adiciona um participante vivo (remove da lista de espectadores, se estiver). */
    public void addParticipant(UUID id) {
        spectators.remove(id);
        alive.add(id);
    }

    public void addParticipants(Collection<UUID> ids) {
        ids.forEach(this::addParticipant);
    }

    /** Adiciona um espectador (quem entra com a partida em andamento). */
    public void addSpectator(UUID id) {
        alive.remove(id);
        spectators.add(id);
    }

    /** Morte: sai dos vivos e vira espectador. @return {@code true} se estava vivo */
    public boolean eliminate(UUID id) {
        if (!alive.remove(id)) {
            return false;
        }
        spectators.add(id);
        return true;
    }

    /** Saída do servidor: remove de vivos e espectadores. @return {@code true} se estava vivo */
    public boolean remove(UUID id) {
        boolean wasAlive = alive.remove(id);
        spectators.remove(id);
        return wasAlive;
    }

    public boolean isAlive(UUID id) {
        return alive.contains(id);
    }

    public boolean isSpectator(UUID id) {
        return spectators.contains(id);
    }

    public int aliveCount() {
        return alive.size();
    }

    /** Snapshot imutável. */
    public Set<UUID> alivePlayers() {
        return Set.copyOf(alive);
    }

    /** Snapshot imutável. */
    public Set<UUID> spectators() {
        return Set.copyOf(spectators);
    }

    /** A partida acaba quando sobra no máximo um vivo. */
    public boolean isOver() {
        return alive.size() <= 1;
    }

    /** O único vivo, se houver exatamente um. */
    public Optional<UUID> soleSurvivor() {
        return alive.size() == 1 ? Optional.of(alive.iterator().next()) : Optional.empty();
    }

    public void setWinner(UUID winner) {
        this.winner = winner;
    }

    public Optional<UUID> winner() {
        return Optional.ofNullable(winner);
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }
}
