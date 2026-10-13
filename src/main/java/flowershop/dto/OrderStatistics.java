package flowershop.dto;
 
import flowershop.model.OrderStatus;
 
import java.math.BigDecimal;
import java.util.Map;
 
// Набор показателей для вкладки «Статистика»
public record OrderStatistics(
        long totalOrders,
        long totalCustomers,
        long totalBouquets,
        Map<OrderStatus, Long> ordersByStatus,
        BigDecimal deliveredRevenue,
        BigDecimal averageOrderTotal,
        BigDecimal minOrderTotal,
        BigDecimal maxOrderTotal,
        String mostPopularBouquet) {
 
    // Доля отменённых заказов в процентах (0, если заказов нет)
    public double cancelledPercent() {
        if (totalOrders == 0) {
            return 0;
        }
        long cancelled = ordersByStatus.getOrDefault(OrderStatus.CANCELLED, 0L);
        return cancelled * 100.0 / totalOrders;
    }
}