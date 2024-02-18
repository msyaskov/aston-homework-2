package aston.hw2.repository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Базовый класс для jdbc-репозиториев.
 *
 * @author Максим Яськов
 * @see JdbcConnectionFactory
 */
public abstract class JdbcAbstractRepository<E, ID> implements Repository<E, ID> {

    private final JdbcConnectionFactory jdbcConnectionFactory;

    public JdbcAbstractRepository(JdbcConnectionFactory jdbcConnectionFactory) {
        if (jdbcConnectionFactory == null) {
            throw new IllegalArgumentException("An jdbcConnectionFactory must not be null");
        }

        this.jdbcConnectionFactory = jdbcConnectionFactory;
    }

    /**
     * Проверяет идентификатор на null.
     *
     * @param id проверяемый идентификатор
     * @throws IllegalArgumentException если указанный идентификатор равен null
     */
    protected void checkIdForNull(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("An id must not be null");
        }
    }

    /**
     * Проверяет сущность на null.
     *
     * @param entity проверяемая сущность
     * @throws IllegalArgumentException если указанная сущность равна null
     */
    protected void checkEntityForNull(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("An entity must not be null");
        }
    }

    /**
     * Выполняет переданный {@link ConnectionConsumer}.
     * Предназначена для инкапсуляции создания или получения экземпляра {@link Connection}.
     *
     * @param connectionConsumer потребитель подключения
     */
    protected void useConnection(ConnectionConsumer connectionConsumer) {
        try (Connection connection = jdbcConnectionFactory.createConnection()) {
            connectionConsumer.accept(connection);
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Выполняет переданную {@link ConnectionFunction} и возвращает её результат.
     * Предназначена для инкапсуляции создания или получения экземпляра {@link Connection}.
     *
     * @param connectionFunction потребитель подключения
     * @return возвращает результат выполнения переданной функции {@link ConnectionFunction}
     */
    protected <T> T useConnection(ConnectionFunction<T> connectionFunction) {
        T returnValue = null;
        try (Connection connection = jdbcConnectionFactory.createConnection()) {
            returnValue = connectionFunction.apply(connection);
        } catch (SQLException e) {
            handleSQLException(e);
        }

        return returnValue;
    }

    /**
     * Обрабатывает {@link SQLException} возникший в результате выполнения {@link ConnectionConsumer} или {@link ConnectionFunction}.
     * Реализации ничем не ограничены. Базовая реализация оборачивает SQLException в SQLRuntimeException
     *
     * @param e возникший SQLException
     */
    protected void handleSQLException(SQLException e) {
        throw new SQLRuntimeException(e);
    }

    /**
     * Представляет операцию над {@link Connection}.
     */
    @FunctionalInterface
    public interface ConnectionConsumer {
        void accept(Connection c) throws SQLException;
    }

    /**
     * Представляет операцию над {@link Connection}.
     * В отличии от {@link ConnectionConsumer} возвращает результат.
     */
    @FunctionalInterface
    public interface ConnectionFunction<T> {
        T apply(Connection c) throws SQLException;
    }
}
