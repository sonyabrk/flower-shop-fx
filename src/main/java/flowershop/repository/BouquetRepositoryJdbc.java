package flowershop.repository;
 
import flowershop.exception.BusinessException;
import flowershop.exception.DatabaseConnectionException;
import flowershop.model.Bouquet;
import flowershop.util.DatabaseManager;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
 
public class BouquetRepositoryJdbc implements BouquetRepository {
 
    @Override
    public Bouquet save(Bouquet bouquet) {
        String sql = "INSERT INTO bouquets (name, description, price) VALUES (?, ?, ?)";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
 
            ps.setString(1, bouquet.getName());
            ps.setString(2, bouquet.getDescription());
            ps.setBigDecimal(3, bouquet.getPrice());
            ps.executeUpdate();
 
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    bouquet.setId(keys.getLong(1));
                }
            }
            return bouquet;
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при сохранении букета", e);
        }
    }
 
    @Override
    public Optional<Bouquet> findById(Long id) {
        String sql = "SELECT id, name, description, price FROM bouquets WHERE id = ?";
 
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
            throw new DatabaseConnectionException("Ошибка при поиске букета по ID", e);
        }
    }
 
    @Override
    public List<Bouquet> findAll() {
        String sql = "SELECT id, name, description, price FROM bouquets ORDER BY id";
        List<Bouquet> result = new ArrayList<>();
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при получении списка букетов", e);
        }
    }
 
    @Override
    public Bouquet update(Bouquet bouquet) {
        String sql = "UPDATE bouquets SET name = ?, description = ?, price = ? WHERE id = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setString(1, bouquet.getName());
            ps.setString(2, bouquet.getDescription());
            ps.setBigDecimal(3, bouquet.getPrice());
            ps.setLong(4, bouquet.getId());
            ps.executeUpdate();
 
            return bouquet;
 
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка при обновлении букета", e);
        }
    }
 
    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM bouquets WHERE id = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setLong(1, id);
            ps.executeUpdate();
 
        } catch (SQLException e) {
            if (SqlErrors.isForeignKeyViolation(e)) {
                throw new BusinessException("Нельзя удалить букет: он используется в заказах", e);
            }
            throw new DatabaseConnectionException("Ошибка при удалении букета", e);
        }
    }
 
    private Bouquet mapRow(ResultSet rs) throws SQLException {
        return new Bouquet(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price")
        );
    }
}