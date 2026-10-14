package flowershop.util;

import flowershop.exception.DatabaseConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private static final String CONFIG_FILE = "database.properties";
    private static final int LOGIN_TIMEOUT_SECONDS = 5;

    // Настройки читаются при первом обращении, а не в static-блоке:
    // ошибку чтения файла можно перехватить и показать в диалоге.
    private static Properties config;

    private DatabaseManager() {
        // утилитный класс, экземпляры не создаются
    }

    public static Connection getConnection() {
        Properties props = getConfig();
        try {
            return DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password"));
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Не удалось подключиться к базе данных", e);
        }
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (!conn.isValid(2)) {
                throw new DatabaseConnectionException("База данных не отвечает");
            }
            log.info("Подключение к базе данных успешно установлено");
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Ошибка проверки подключения к базе данных", e);
        }
    }

    private static synchronized Properties getConfig() {
        if (config == null) {
            config = loadConfig();
        }
        return config;
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream input = DatabaseManager.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                throw new DatabaseConnectionException(
                        "Файл конфигурации " + CONFIG_FILE + " не найден в resources. "
                                + "Скопируйте database.properties.example в database.properties");
            }
            props.load(input);

        } catch (IOException e) {
            throw new DatabaseConnectionException("Ошибка чтения файла конфигурации БД", e);
        }

        if (props.getProperty("db.url") == null
                || props.getProperty("db.user") == null
                || props.getProperty("db.password") == null) {
            throw new DatabaseConnectionException(
                    "Не заданы параметры db.url / db.user / db.password в " + CONFIG_FILE);
        }

        // Если БД недоступна, окно не должно «висеть» дольше 5 секунд
        DriverManager.setLoginTimeout(LOGIN_TIMEOUT_SECONDS);
        return props;
    }
}