package aston.hw2.repository;

import java.sql.SQLException;

public class SQLRuntimeException extends RuntimeException {

    private final SQLException sqlException;

    public SQLRuntimeException(SQLException e) {
        super(e.getMessage(), e.getCause());
        this.sqlException = e;
    }

    public final SQLException getSQLException() {
        return sqlException;
    }

}
