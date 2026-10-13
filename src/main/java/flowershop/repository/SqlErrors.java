package flowershop.repository;
 
import java.sql.SQLException;
 

// Распознавание типовых ошибок PostgreSQL по коду SQLState
// Позволяет показать пользователю понятное сообщение вместо технической ошибки БД
final class SqlErrors {
 
    // Нарушение внешнего ключа (удаление используемой записи или ссылка на несуществующую)
    private static final String FOREIGN_KEY_VIOLATION = "23503";
 
    // Нарушение уникальности (например, повторяющийся email)
    private static final String UNIQUE_VIOLATION = "23505";
 
    private SqlErrors() {
        // утилитный класс
    }
 
    static boolean isForeignKeyViolation(SQLException e) {
        return FOREIGN_KEY_VIOLATION.equals(e.getSQLState());
    }
 
    static boolean isUniqueViolation(SQLException e) {
        return UNIQUE_VIOLATION.equals(e.getSQLState());
    }
}
