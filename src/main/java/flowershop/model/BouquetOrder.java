package flowershop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class BouquetOrder {

    private Long id;
    private Long customerId;
    private Long bouquetId;
    private int quantity;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDate orderDate;
    private LocalDate deliveryDate;

    public BouquetOrder() {
    }

    public BouquetOrder(Long customerId, Long bouquetId, int quantity,
                         BigDecimal totalPrice, OrderStatus status,
                         LocalDate orderDate, LocalDate deliveryDate) {
        this.customerId = customerId;
        this.bouquetId = bouquetId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
    }

    public BouquetOrder(Long id, Long customerId, Long bouquetId, int quantity,
                         BigDecimal totalPrice, OrderStatus status,
                         LocalDate orderDate, LocalDate deliveryDate) {
        this.id = id;
        this.customerId = customerId;
        this.bouquetId = bouquetId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getBouquetId() {
        return bouquetId;
    }

    public void setBouquetId(Long bouquetId) {
        this.bouquetId = bouquetId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BouquetOrder)) return false;
        BouquetOrder that = (BouquetOrder) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BouquetOrder{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", bouquetId=" + bouquetId +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                ", orderDate=" + orderDate +
                ", deliveryDate=" + deliveryDate +
                '}';
    }
}
