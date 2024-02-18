package aston.h2.repository;

import aston.h2.entity.Curator;
import aston.h2.entity.Group;
import aston.h2.entity.Student;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Репозиторий групп для работы с базой данных (БД) посредством JDBC.
 * Реализует каскадные поиск, вставку и обновление.
 *
 * @author Максим Яськов
 */
public class JdbcGroupRepository extends JdbcAbstractRepository<Group, Integer> implements GroupRepository {
    
    public JdbcGroupRepository(JdbcConnectionFactory jdbcConnectionFactory) {
        super(jdbcConnectionFactory);
    }

    @Override
    public Stream<Group> findAll() {
        return useConnection(connection -> {
            final Stream.Builder<Group> streamBuilder = Stream.builder();

            try (PreparedStatement selectGroupsWithCuratorsPS = connection.prepareStatement(SQLNamespace.Query.SELECT_ALL_GROUPS_WITH_CURATORS);
                 PreparedStatement selectStudentsPS = connection.prepareStatement(SQLNamespace.Query.SELECT_STUDENTS_BY_GROUP_ID)) {

                ResultSet groupsWithCuratorsRS = selectGroupsWithCuratorsPS.executeQuery();
                while (groupsWithCuratorsRS.next()) {
                    Group group = readGroup(groupsWithCuratorsRS);
                    streamBuilder.add(group);

                    Curator curator = readCurator(groupsWithCuratorsRS);
                    if (curator != null) {
                        curator.setGroup(group);
                        group.setCurator(curator);
                    }

                    selectStudentsPS.setInt(1, group.getId());
                    ResultSet studentsRS = selectStudentsPS.executeQuery();
                    while (studentsRS.next()) {
                        Student student = readStudent(studentsRS);
                        student.setGroup(group);
                        group.getStudents().add(student);
                    }
                }
            }

            return streamBuilder.build();
        });

    }

    @Override
    public Group findById(final Integer id) {
        checkIdForNull(id);

        return useConnection(connection -> {
            try (PreparedStatement selectGroupAndCuratorPS = connection.prepareStatement(SQLNamespace.Query.SELECT_GROUP_WITH_CURATOR_BY_ID);
                 PreparedStatement selectStudentsPS = connection.prepareStatement(SQLNamespace.Query.SELECT_STUDENTS_BY_GROUP_ID)
            ) {
                selectGroupAndCuratorPS.setInt(1, id);
                selectStudentsPS.setInt(1, id);

                ResultSet groupAndCuratorRS = selectGroupAndCuratorPS.executeQuery();
                if (!groupAndCuratorRS.next()) {
                    return null;
                }

                final Group group = readGroup(groupAndCuratorRS);

                final Curator curator = readCurator(groupAndCuratorRS);
                if (curator != null) {
                    curator.setGroup(group);
                    group.setCurator(curator);
                }

                ResultSet studentsRS = selectStudentsPS.executeQuery();
                while (studentsRS.next()) {
                    Student student = readStudent(studentsRS);
                    student.setGroup(group);
                    group.getStudents().add(student);
                }

                return group;
            }
        });
    }

    @Override
    public Group findByName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("A name must not be null");
        }

        Integer groupId = useConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.SELECT_GROUP_ID_BY_NAME)) {
                ps.setString(1, name);

                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return null;
                }

                return rs.getInt(SQLNamespace.Group.KEY_ID);
            }
        });

        return groupId != null ? findById(groupId) : null;
    }

    private Integer getIntGeneratedKey(Statement s, String columnLabel) throws SQLException {
        ResultSet rs = s.getGeneratedKeys();
        if (!rs.next()) {
            return null;
        }

        return rs.getInt(columnLabel);
    }

    private Group insert(final Group group) {
        return useConnection(connection -> {
            insertGroup(connection, group);

            final Curator curator = group.getCurator();
            if (curator != null) {
                if (curator.getId() == null) {
                    insertCurator(connection, curator, group.getId());
                } else {
                    updateCurator(connection, curator, group.getId());
                }
                curator.setGroup(group);
            }

            if (group.getStudents() == null) {
                group.setStudents(new LinkedList<>());
            }

            final Collection<Student> students = group.getStudents();
            try (PreparedStatement insertStudentPS = connection.prepareStatement(SQLNamespace.Query.INSERT_STUDENT, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement updateStudentPS = connection.prepareStatement(SQLNamespace.Query.UPDATE_STUDENT)) {

                for (Student student : students) {
                    if (student == null) {
                        throw new IllegalStateException("One of the students is null");
                    }

                    if (student.getId() == null) {
                        insertStudent(connection, student, group.getId());
                    } else {
                        updateStudent(connection, student, group.getId());
                    }
                    student.setGroup(group);
                }
            }

            return group;
        });
    }

    private void insertCurator(Connection c, Curator curator, int groupId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQLNamespace.Query.INSERT_CURATOR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, curator.getName());
            ps.setString(2, curator.getEmail());
            ps.setInt(3, curator.getExperience());
            ps.setInt(4, groupId);
            ps.executeUpdate();

            curator.setId(getIntGeneratedKey(ps, SQLNamespace.Curator.KEY_ID));
        }
    }

    private void insertGroup(Connection c, Group group) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQLNamespace.Query.INSERT_GROUP, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, group.getName());
            ps.setDate(2, Date.valueOf(group.getGraduationDate()));
            ps.executeUpdate();

            group.setId(getIntGeneratedKey(ps, SQLNamespace.Group.KEY_ID));
        }
    }

    private void insertStudent(Connection c, Student student, Integer groupId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQLNamespace.Query.INSERT_STUDENT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getName());
            ps.setDate(2, Date.valueOf(student.getDateOfBirth()));
            if (groupId == null) {
                ps.setNull(3, JDBCType.INTEGER.getVendorTypeNumber());
            } else {
                ps.setInt(3, groupId);
            }
            ps.executeUpdate();

            student.setId(getIntGeneratedKey(ps, SQLNamespace.Group.KEY_ID));
        }
    }

    private Curator readCurator(ResultSet rs) throws SQLException {
        try {
            rs.findColumn(SQLNamespace.Curator.FULL_KEY_ID);
        } catch (SQLException ignored) {
            return null;
        }

        int curatorId = rs.getInt(SQLNamespace.Curator.FULL_KEY_ID);
        if (rs.wasNull()) {
            return null;
        }

        Curator curator = new Curator();
        curator.setId(curatorId);
        curator.setName(rs.getString(SQLNamespace.Curator.FULL_KEY_NAME));
        curator.setEmail(rs.getString(SQLNamespace.Curator.FULL_KEY_EMAIL));
        curator.setExperience(rs.getInt(SQLNamespace.Curator.FULL_KEY_EXPERIENCE));

        return curator;
    }

    private Group readGroup(ResultSet rs) throws SQLException {
        Group group = new Group();
        group.setId(rs.getInt(SQLNamespace.Group.KEY_ID));
        group.setName(rs.getString(SQLNamespace.Group.KEY_NAME));
        group.setGraduationDate(rs.getDate(SQLNamespace.Group.KEY_GRADUATION_DATE).toLocalDate());
        group.setStudents(new LinkedList<>());

        return group;
    }

    private Student readStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt(SQLNamespace.Student.KEY_ID));
        student.setName(rs.getString(SQLNamespace.Student.KEY_NAME));
        student.setDateOfBirth(rs.getDate(SQLNamespace.Student.KEY_DATE_OF_BIRTH).toLocalDate());

        return student;
    }

    @Override
    public Group removeById(final Integer id) {
        checkIdForNull(id);

        final Group group = findById(id);
        if (group == null) {
            return null;
        }

        useConnection(connection -> {
            final Curator curator = group.getCurator();
            if (curator != null) {
                try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.UPDATE_CURATOR_SET_NULL_GROUP_ID_BY_GROUP_ID)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }

            final Collection<Student> students = group.getStudents();
            if (!students.isEmpty()) {
                try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.UPDATE_STUDENTS_SET_NULL_GROUP_ID_BY_GROUP_ID)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.DELETE_GROUP_BY_ID)) {
                ps.setInt(1, group.getId());
                ps.executeUpdate();
            }
        });

        return group;
    }

    @Override
    public Group save(Group group) {
        checkEntityForNull(group);

        if (group.getId() == null) {
            return insert(group);
        } else {
            return update(group);
        }
    }

    private Group update(final Group group) {
        return useConnection(connection -> {
            updateGroup(connection, group);

            final Curator curator = group.getCurator();
            if (curator != null) {
                if (curator.getId() == null) {
                    insertCurator(connection, curator, group.getId());
                } else {
                    updateCurator(connection, curator, group.getId());
                }
                curator.setGroup(group);
            }

            if (group.getStudents() == null) {
                group.setStudents(new LinkedList<>());
            }

            final Collection<Student> students = group.getStudents();
            try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.UPDATE_STUDENTS_SET_NULL_GROUP_ID_BY_GROUP_ID)) {
                ps.setInt(1, group.getId());
                ps.executeUpdate();
            }
            for (Student student : students) {
                if (student.getId() == null) {
                    insertStudent(connection, student, group.getId());
                } else {
                    updateStudent(connection, student, group.getId());
                }
                student.setGroup(group);
            }

            return group;
        });
    }

    private void updateCurator(Connection c, Curator curator, int groupId) throws SQLException {
        int curatorId = Objects.requireNonNull(curator.getId());
        try (PreparedStatement ps = c.prepareStatement(SQLNamespace.Query.UPDATE_CURATOR)) {
            ps.setString(1, curator.getName());
            ps.setString(2, curator.getEmail());
            ps.setInt(3, curator.getExperience());
            ps.setInt(4, groupId);
            ps.setInt(5, curatorId);
            ps.executeUpdate();
        }
    }

    private void updateGroup(Connection c, Group group) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQLNamespace.Query.UPDATE_GROUP)) {
            ps.setString(1, group.getName());
            ps.setDate(2, Date.valueOf(group.getGraduationDate()));
            ps.setInt(3, group.getId());
            ps.executeUpdate();
        }
    }

    private void updateStudent(Connection c, Student student, Integer groupId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQLNamespace.Query.UPDATE_STUDENT)) {
            ps.setString(1, student.getName());
            ps.setDate(2, Date.valueOf(student.getDateOfBirth()));
            if (groupId == null) {
                ps.setNull(3, JDBCType.INTEGER.getVendorTypeNumber());
            } else {
                ps.setInt(3, groupId);
            }
            ps.setInt(4, student.getId());
            ps.executeUpdate();
        }
    }
}
