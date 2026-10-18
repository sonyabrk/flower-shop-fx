package flowershop.service;

import flowershop.dto.OrderStatistics;
import flowershop.repository.StatisticsRepository;

public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    public StatisticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    // Все показатели считаются в БД агрегатными запросами, а не перебором списка в памяти
    public OrderStatistics getStatistics() {
        return statisticsRepository.load();
    }
}