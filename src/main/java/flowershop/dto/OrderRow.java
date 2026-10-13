package flowershop.dto;
 
import flowershop.model.OrderStatus;
 
import java.math.BigDecimal;
import java.time.LocalDate;
 

// Строка таблицы заказов: данные заказа вместе с именем клиента и названием букета.
// customerId и bouquetId нужны для диалога редактирования, в таблице не показываются.
public class OrderRow {
 
    private final Long id;
    private final Long customerId;
    private final Long bouquetId;
    private final String customerName;
    private final String bouquetName;
    private final int quantity;
    private final BigDecimal totalPrice;
    private final OrderStatus status;
    private final LocalDate orderDate;
    private final LocalDate deliveryDate;
 
    public OrderRow(Long id, Long customerId, Long bouquetId, String customerName,
                    String bouquetName, int quantity, BigDecimal totalPrice,
                    OrderStatus status, LocalDate orderDate, LocalDate deliveryDate) {
        this.id = id;
        this.customerId = customerId;
        this.bouquetId = bouquetId;
        this.customerName = customerName;
        this.bouquetName = bouquetName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
    }
 
    public Long getId() { return id; }
 
    public Long getCustomerId() { return customerId; }
 
    public Long getBouquetId() { return bouquetId; }
 
    public String getCustomerName() { return customerName; }
 
    public String getBouquetName() { return bouquetName; }
 
    public int getQuantity() { return quantity; }
 
    public BigDecimal getTotalPrice() { return totalPrice; }
 
    public OrderStatus getStatus() { return status; }
 
    public LocalDate getOrderDate() { return orderDate; }
 
    public LocalDate getDeliveryDate() { return deliveryDate; }
}