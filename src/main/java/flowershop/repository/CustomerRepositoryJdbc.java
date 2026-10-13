package flowershop.repository;
 
import flowershop.exception.BusinessException;
import flowershop.exception.DatabaseConnectionException;
import flowershop.model.Customer;
import flowershop.util.DatabaseManager;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
 
public class CustomerRepositoryJdbc implements CustomerRepository {
 
    private static final String DUPLICATE_EMAIL_MESSAGE = "Клиент с таким email уже существует";
 
    @Override
    public Customer save(Customer customer) {
        String sql = "INSERT INTO customers (full_name, phone, email) VALUES (?, ?, ?)";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
 
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.executeUpdate();
 
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getLong(1));
                }
            }
            return customer;
 
        } catch (SQLException e) {
            if (SqlErrors.isUniqueViolation(e)) {
                throw new BusinessException(DUPLICATE_EMAIL_MESSAGE, e);
            }
            throw new DatabaseConnectionException("Ошибка при сохранении клиента", e);
        }
    }
 
    @Override
    public Optional<Customer> findById(Long id) {
        String sql = "SELECT id, full_name, phone, email FROM customers WHERE id = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при поиске клиента по ID", e);
        }
    }
 
    @Override
    public List<Customer> findAll() {
        String sql = "SELECT id, full_name, phone, email FROM customers ORDER BY id";
        List<Customer> result = new ArrayList<>();
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при получении списка клиентов", e);
        }
    }
 
    @Override
    public Customer update(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, phone = ?, email = ? WHERE id = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setLong(4, customer.getId());
            ps.executeUpdate();
 
            return customer;
 
        } catch (SQLException e) {
            if (SqlErrors.isUniqueViolation(e)) {
                throw new BusinessException(DUPLICATE_EMAIL_MESSAGE, e);
            }
            throw new DatabaseConnectionException("Ошибка при обновлении клиента", e);
        }
    }
 
    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM customers WHERE id = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setLong(1, id);
            ps.executeUpdate();
 
        } catch (SQLException e) {
            if (SqlErrors.isForeignKeyViolation(e)) {
                throw new BusinessException("Нельзя удалить клиента: у него есть заказы", e);
            }
            throw new DatabaseConnectionException("Ошибка при удалении клиента", e);
        }
    }
 
    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM customers WHERE LOWER(email) = LOWER(?)";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при проверке уникальности email", e);
        }
    }
 
    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        String sql = "SELECT 1 FROM customers WHERE LOWER(email) = LOWER(?) AND id <> ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setString(1, email);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при проверке уникальности email", e);
        }
    }
 
    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email")
        );
    }
}