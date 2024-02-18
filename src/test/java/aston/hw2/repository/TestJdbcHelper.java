package aston.hw2.repository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Collectors;

public class TestJdbcHelper {

    private static final String CREATE_TABLES_SCRIPT_PATH = "/sql/create_db.sql";

    private static final String CLEAR_TABLES_SCRIPT_PATH = "/sql/clear_db.sql";

    private final JdbcConnectionFactory jdbcConnectionFactory;

    public TestJdbcHelper(JdbcConnectionFactory jdbcConnectionFactory) {
        this.jdbcConnectionFactory = jdbcConnectionFactory;
    }

    public int insertGroup(String name, LocalDate graduationDate) throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO groups (name, graduation_date) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setDate(2, Date.valueOf(graduationDate));
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                int groupId = rs.getInt(1);
                if (rs.wasNull()) {
                    throw new NullPointerException("Id for inserted group is null");
                }

                return groupId;
            }
        }
    }

    public int insertStudent(String name, LocalDate dateOfBirth, Integer groupId) throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO students (name, date_of_birth, group_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setDate(2, Date.valueOf(dateOfBirth));
                if (groupId == null) {
                    ps.setNull(3, JDBCType.INTEGER.getVendorTypeNumber());
                } else {
                    ps.setInt(3, groupId);
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                int studentId = rs.getInt(1);
                if (rs.wasNull()) {
                    throw new NullPointerException("Id for inserted student is null");
                }

                return studentId;
            }
        }
    }

    public int insertCurator(String name, String email, int experience, Integer groupId) throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO curators (name, email, experience, group_id) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setInt(3, experience);
                if (groupId == null) {
                    ps.setNull(4, JDBCType.INTEGER.getVendorTypeNumber());
                } else {
                    ps.setInt(4, groupId);
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                int curatorId = rs.getInt(1);
                if (rs.wasNull()) {
                    throw new NullPointerException("Id for inserted curator is null");
                }

                return curatorId;
            }
        }
    }

    public boolean containsCurator(int curatorId, String name, String email, int experience, Integer groupId) throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM curators WHERE id = ?")) {
                ps.setInt(1, curatorId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return false;
                }

                if (!Objects.equals(rs.getString("name"), name)) {
                    return false;
                }
                if (!Objects.equals(rs.getString("email"), email)) {
                    return false;
                }

                if (rs.getInt("experience") != experience) {
                    return false;
                }

                int gId = rs.getInt("group_id");
                if (rs.wasNull() && groupId == null) {
                    return true;
                }

                return Objects.equals(groupId, gId);
            }
        }
    }

    public boolean containsGroup(int groupId, String name, LocalDate graduationDate) throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM groups WHERE id = ?")) {
                ps.setInt(1, groupId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return false;
                }

                if (!Objects.equals(rs.getString("name"), name)) {
                    return false;
                }

                return Objects.equals(rs.getDate("graduation_date").toLocalDate(), graduationDate);
            }
        }
    }

    public boolean containsStudent(int studentId, String name, LocalDate dateOfBirth, Integer groupId) throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM students WHERE id = ?")) {
                ps.setInt(1, studentId);

                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return false;
                }

                if (!Objects.equals(rs.getString("name"), name)) {
                    return false;
                }

                if (!Objects.equals(rs.getDate("date_of_birth").toLocalDate(), dateOfBirth)) {
                    return false;
                }

                int gId = rs.getInt("group_id");
                if (rs.wasNull() && groupId == null) {
                    return true;
                }

                return Objects.equals(groupId, gId);
            }
        }
    }

    public void createTables() throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(
                            Objects.requireNonNull(JdbcCuratorRepositoryTests.class.getResourceAsStream(CREATE_TABLES_SCRIPT_PATH))));

            try (Statement s = c.createStatement()) {
                bufferedReader.lines().forEach(sql -> {
                    try {
                        s.execute(sql);
                    } catch (SQLException e) {
                        throw new SQLRuntimeException(e);
                    }
                });
            }
        }
    }

    public void clearTables() throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            try (Statement s = c.createStatement()) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(JdbcCuratorRepositoryTests.class.getResourceAsStream(CLEAR_TABLES_SCRIPT_PATH))));
                String dropTablesSQL = bufferedReader.lines().collect(Collectors.joining());
                s.execute(dropTablesSQL);
            }
        }
    }

    public int countOfGroups() throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM groups;")) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                return rs.getInt(1);
            }
        }
    }
    public int countOfCurators() throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM curators;")) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countOfStudents() throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM students;")) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void printDump() throws SQLException {
        try (Connection c = jdbcConnectionFactory.createConnection()) {
            try (Statement s = c.createStatement()){
                ResultSet rs = s.executeQuery("SELECT * FROM curators;");
                System.out.println("\nCURATORS");
                System.out.println("id\t|\tname\t|\texperience\t|\temail\t|\tgroup_id");
                while (rs.next()) {
                    Integer groupId = rs.getInt("group_id");
                    if (rs.wasNull()) {
                        groupId = null;
                    }
                    System.out.printf("%d\t|\t%s\t|\t%d\t\t\t|\t%s\t|\t%d%n", rs.getInt("id"), rs.getString("name"), rs.getInt("experience"), rs.getString("email"), groupId);
                }
                rs.close();

                rs = s.executeQuery("SELECT * FROM groups;");
                System.out.println("\nGROUPS");
                System.out.println("id\t|\tname\t|\tgraduation_date");
                while (rs.next()) {
                    System.out.printf("%d\t|\t%s\t|\t%s%n", rs.getInt("id"), rs.getString("name"), rs.getDate("graduation_date"));
                }
                rs.close();

                rs = s.executeQuery("SELECT * FROM students;");
                System.out.println("\nSTUDENTS");
                System.out.println("id\t|\tname\t|\tdate_of_birth\t|\tgroup_id");
                while (rs.next()) {
                    System.out.printf("%d\t|\t%s\t|\t%s\t|\t%d%n", rs.getInt("id"), rs.getString("name"), rs.getDate("date_of_birth"), rs.getInt("group_id"));
                }
                rs.close();
            }
        }
    }
}
