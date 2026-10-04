package al3ncar.al3hg.addon.stats.repository;

class InMemoryStatsRepositoryEdgeCaseTest extends StatsRepositoryEdgeCaseContract {

    private final InMemoryStatsRepository repository = new InMemoryStatsRepository();

    @Override
    protected StatsRepository repository() {
        return repository;
    }
}
