package al3ncar.al3hg.addon.stats.repository;

class InMemoryStatsRepositoryTest extends StatsRepositoryContract {

    private final InMemoryStatsRepository repository = new InMemoryStatsRepository();

    @Override
    protected StatsRepository repository() {
        return repository;
    }
}
