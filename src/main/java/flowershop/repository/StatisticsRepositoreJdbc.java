package flowershop.repository;
 
import flowershop.dto.OrderStatistics;
import flowershop.exception.DatabaseConnectionException;
import flowershop.model.OrderStatus;
import flowershop.util.DatabaseManager;
 
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;
 
public class StatisticsRepositoryJdbc implements StatisticsRepository {
 
    private static final String COUNTS_SQL =
            "SELECT (SELECT COUNT(*) FROM orders), " +
            "       (SELECT COUNT(*) FROM customers), " +
            "       (SELECT COUNT(*) FROM bouquets)";
 
    private static final String BY_STATUS_SQL =
            "SELECT status, COUNT(*) FROM orders GROUP BY status";
 
    private static final String AMOUNTS_SQL =
            "SELECT COALESCE(SUM(total_price) FILTER (WHERE status = 'DELIVERED'), 0), " +
            "       COALESCE(AVG(total_price), 0), " +
            "       COALESCE(MIN(total_price), 0), " +
            "       COALESCE(MAX(total_price), 0) " +
            "FROM orders";
 
    // Самый популярный букет по суммарному количеству в неотменённых заказах
    private static final String POPULAR_SQL =
            "SELECT b.name " +
            "FROM orders o JOIN bouquets b ON b.id = o.bouquet_id " +
            "WHERE o.status <> 'CANCELLED' " +
            "GROUP BY b.id, b.name " +
            "ORDER BY SUM(o.quantity) DESC, b.name " +
            "LIMIT 1";
 
    @Override
    public OrderStatistics load() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement()) {
 
            long orders;
            long customers;
            long bouquets;
            try (ResultSet rs = st.executeQuery(COUNTS_SQL)) {
                rs.next();
                orders = rs.getLong(1);
                customers = rs.getLong(2);
                bouquets = rs.getLong(3);
            }
 
            Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
            for (OrderStatus status : OrderStatus.values()) {
                byStatus.put(status, 0L);
            }
            try (ResultSet rs = st.executeQuery(BY_STATUS_SQL)) {
                while (rs.next()) {
                    byStatus.put(OrderStatus.valueOf(rs.getString(1)), rs.getLong(2));
                }
            }
 
            BigDecimal revenue;
            BigDecimal average;
            BigDecimal min;
            BigDecimal max;
            try (ResultSet rs = st.executeQuery(AMOUNTS_SQL)) {
                rs.next();
                revenue = rs.getBigDecimal(1);
                average = rs.getBigDecimal(2).setScale(2, RoundingMode.HALF_UP);
                min = rs.getBigDecimal(3);
                max = rs.getBigDecimal(4);
            }
 
            String popular = null;
            try (ResultSet rs = st.executeQuery(POPULAR_SQL)) {
                if (rs.next()) {
                    popular = rs.getString(1);
                }
            }
 
            return new OrderStatistics(orders, customers, bouquets, byStatus,
                    revenue, average, min, max, popular);
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при расчёте статистики", e);
        }
    }
}