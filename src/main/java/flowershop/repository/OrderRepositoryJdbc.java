package flowershop.repository;
 
import flowershop.dto.OrderRow;
import flowershop.exception.BusinessException;
import flowershop.exception.DatabaseConnectionException;
import flowershop.model.BouquetOrder;
import flowershop.model.OrderStatus;
import flowershop.util.DatabaseManager;
 
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
 
public class OrderRepositoryJdbc implements OrderRepository {
 
    private static final String SELECT_ORDERS =
            "SELECT id, customer_id, bouquet_id, quantity, total_price, status, " +
            "order_date, delivery_date FROM orders";
 
    private static final String SELECT_ORDER_ROWS =
            "SELECT o.id, o.customer_id, o.bouquet_id, c.full_name AS customer_name, " +
            "b.name AS bouquet_name, o.quantity, o.total_price, o.status, " +
            "o.order_date, o.delivery_date " +
            "FROM orders o " +
            "JOIN customers c ON c.id = o.customer_id " +
            "JOIN bouquets b ON b.id = o.bouquet_id " +
            "ORDER BY o.order_date DESC, o.id DESC";
 
    @Override
    public BouquetOrder save(BouquetOrder order) {
        String sql = "INSERT INTO orders " +
                "(customer_id, bouquet_id, quantity, total_price, status, order_date, delivery_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
 
            fillStatement(ps, order);
            ps.executeUpdate();
 
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setId(keys.getLong(1));
                }
            }
            return order;
 
        } catch (SQLException e) {
            throw translate(e, "Ошибка при сохранении заказа");
        }
    }
 
    @Override
    public Optional<BouquetOrder> findById(Long id) {
        String sql = SELECT_ORDERS + " WHERE id = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapOrder(rs));
                }
                return Optional.empty();
            }
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при поиске заказа по ID", e);
        }
    }
 
    @Override
    public List<BouquetOrder> findAll() {
        String sql = SELECT_ORDERS + " ORDER BY id";
        List<BouquetOrder> result = new ArrayList<>();
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                result.add(mapOrder(rs));
            }
            return result;
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при получении списка заказов", e);
        }
    }
 
    @Override
    public BouquetOrder update(BouquetOrder order) {
        String sql = "UPDATE orders SET customer_id = ?, bouquet_id = ?, quantity = ?, " +
                "total_price = ?, status = ?, order_date = ?, delivery_date = ? WHERE id = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            fillStatement(ps, order);
            ps.setLong(8, order.getId());
            ps.executeUpdate();
 
            return order;
 
        } catch (SQLException e) {
            throw translate(e, "Ошибка при обновлении заказа");
        }
    }
 
    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM orders WHERE id = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setLong(1, id);
            ps.executeUpdate();
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при удалении заказа", e);
        }
    }
 
    @Override
    public List<BouquetOrder> findByBouquetId(Long bouquetId) {
        return findByForeignKey("bouquet_id", bouquetId, "Ошибка при поиске заказов по букету");
    }
 
    @Override
    public List<BouquetOrder> findByCustomerId(Long customerId) {
        return findByForeignKey("customer_id", customerId, "Ошибка при поиске заказов по клиенту");
    }
 
    @Override
    public List<OrderRow> findAllRows() {
        List<OrderRow> result = new ArrayList<>();
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ORDER_ROWS);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при загрузке таблицы заказов", e);
        }
    }
 
    // Имя столбца подставляется только из двух констант выше, пользовательский ввод сюда не попадает
    private List<BouquetOrder> findByForeignKey(String column, Long id, String errorMessage) {
        String sql = SELECT_ORDERS + " WHERE " + column + " = ? ORDER BY id";
        List<BouquetOrder> result = new ArrayList<>();
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapOrder(rs));
                }
            }
            return result;
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException(errorMessage, e);
        }
    }
 
    private RuntimeException translate(SQLException e, String defaultMessage) {
        if (SqlErrors.isForeignKeyViolation(e)) {
            return new BusinessException(
                    "Клиент или букет не найден: возможно, запись была удалена", e);
        }
        return new DatabaseConnectionException(defaultMessage, e);
    }
 
    private void fillStatement(PreparedStatement ps, BouquetOrder order) throws SQLException {
        ps.setLong(1, order.getCustomerId());
        ps.setLong(2, order.getBouquetId());
        ps.setInt(3, order.getQuantity());
        ps.setBigDecimal(4, order.getTotalPrice());
        ps.setString(5, order.getStatus().name());
        ps.setDate(6, Date.valueOf(order.getOrderDate()));
        ps.setDate(7, order.getDeliveryDate() != null ? Date.valueOf(order.getDeliveryDate()) : null);
    }
 
    private BouquetOrder mapOrder(ResultSet rs) throws SQLException {
        Date deliveryDate = rs.getDate("delivery_date");
        return new BouquetOrder(
                rs.getLong("id"),
                rs.getLong("customer_id"),
                rs.getLong("bouquet_id"),
                rs.getInt("quantity"),
                rs.getBigDecimal("total_price"),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getDate("order_date").toLocalDate(),
                deliveryDate != null ? deliveryDate.toLocalDate() : null
        );
    }
 
    private OrderRow mapRow(ResultSet rs) throws SQLException {
        Date deliveryDate = rs.getDate("delivery_date");
        return new OrderRow(
                rs.getLong("id"),
                rs.getLong("customer_id"),
                rs.getLong("bouquet_id"),
                rs.getString("customer_name"),
                rs.getString("bouquet_name"),
                rs.getInt("quantity"),
                rs.getBigDecimal("total_price"),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getDate("order_date").toLocalDate(),
                deliveryDate != null ? deliveryDate.toLocalDate() : null
        );
    }
}