package flowershop.repository;
 
import flowershop.dto.OrderRow;
import flowershop.model.BouquetOrder;
 
import java.util.List;
 
public interface OrderRepository extends Repository<BouquetOrder, Long> {
 
    List<BouquetOrder> findByBouquetId(Long bouquetId);
 
    List<BouquetOrder> findByCustomerId(Long customerId);
 
    // Все заказы для таблицы: имя клиента и название букета подтягиваются одним JOIN-запросом
    List<OrderRow> findAllRows();
}