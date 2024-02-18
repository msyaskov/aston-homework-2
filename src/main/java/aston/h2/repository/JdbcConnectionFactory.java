package aston.h2.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Фабрика jdbc-подключений.
 *
 * @author Максим Яськов
 * @see Connection
 */
public class JdbcConnectionFactory {

    private final String url;
    private final String username;
    private final String password;

    public JdbcConnectionFactory(String driverClassName, String url) {
        this(driverClassName, url, null, null);
    }

    public JdbcConnectionFactory(String driverClassName, String url, String username, String password) {
        try {
            Class.forName(driverClassName);
        } catch(ClassNotFoundException e) {
            throw new IllegalArgumentException("Class " + driverClassName + "not found", e);
        }

        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Создает новое подключение.
     *
     * @return новое подключение
     * @throws SQLException при ошибке подключения к базе данных
     */
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

}
