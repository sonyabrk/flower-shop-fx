package flowershop.repository;
 
import flowershop.dto.OrderStatistics;
 
public interface StatisticsRepository {
 
    // Рассчитывает все показатели статистики агрегатными SQL-запросами 
    OrderStatistics load();
}