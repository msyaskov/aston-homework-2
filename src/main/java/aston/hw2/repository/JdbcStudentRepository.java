package aston.hw2.repository;

import aston.hw2.entity.Group;
import aston.hw2.entity.Student;

import java.sql.*;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Репозиторий студентов для работы с базой данных (БД) посредством JDBC.
 *
 * Все операции со студентами, которые связаны с какой-либо группой, а также логику управления связями делегирует {@link JdbcGroupRepository}.
 *
 * @author Максим Яськов
 * @see JdbcGroupRepository
 */
public class JdbcStudentRepository extends JdbcAbstractRepository<Student, Integer> implements StudentRepository {

    private final GroupRepository groupRepository;

    public JdbcStudentRepository(JdbcConnectionFactory jdbcConnectionFactory, GroupRepository groupRepository) {
        super(jdbcConnectionFactory);
        this.groupRepository = groupRepository;
    }

    @Override
    public Stream<Student> findAll() {
        return useConnection(connection -> {
            Stream.Builder<Student> streamBuilder = Stream.builder();

            try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.SELECT_STUDENTS_BY_NULL_GROUP_ID)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        streamBuilder.add(readStudent(rs));
                    }
                }
            }

            return Stream.concat(streamBuilder.build(),
                    groupRepository.findAll()
                            .flatMap(group -> group.getStudents().stream()));
        });
    }

    @Override
    public Student findById(final Integer id) {
        checkIdForNull(id);

        return useConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.SELECT_STUDENT_BY_ID)) {
                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    int groupId = rs.getInt(SQLNamespace.Student.KEY_GROUP_ID);
                    if (rs.wasNull()) {
                        return readStudent(rs);
                    }

                    Group group = groupRepository.findById(groupId);
                    for (Student s : group.getStudents()) {
                        if (Objects.equals(s.getId(), id)) {
                            return s;
                        }
                    }
                }
            }

            return null;
        });
    }

    private Student insert(final Student student) {
        return useConnection(connection -> {
            if (student.getGroup() == null) {
                try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.INSERT_STUDENT, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, student.getName());
                    ps.setDate(2, Date.valueOf(student.getDateOfBirth()));
                    ps.setNull(3, JDBCType.INTEGER.getVendorTypeNumber());

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            student.setId(rs.getInt(SQLNamespace.Student.KEY_ID));
                        }
                    }
                }
            } else {
                student.getGroup().getStudents().add(student);
                groupRepository.save(student.getGroup());
            }

            return student;
        });
    }

    private Student readStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt(SQLNamespace.Student.KEY_ID));
        student.setName(rs.getString(SQLNamespace.Student.KEY_NAME));
        student.setDateOfBirth(rs.getDate(SQLNamespace.Student.KEY_DATE_OF_BIRTH).toLocalDate());

        return student;
    }

    @Override
    public Student removeById(Integer id) {
        checkIdForNull(id);

        Student student = findById(id);
        if (student == null) {
            return null;
        }

        useConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.DELETE_STUDENT_BY_ID)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        });

        return student;
    }

    @Override
    public Student save(Student student) {
        checkEntityForNull(student);

        if (student.getId() == null) {
            return insert(student);
        } else {
            return update(student);
        }
    }

    private Student update(Student student) {
        return useConnection(connection -> {
            if (student.getGroup() == null) {
                try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.UPDATE_STUDENT)) {
                    ps.setString(1, student.getName());
                    ps.setDate(2, Date.valueOf(student.getDateOfBirth()));
                    ps.setNull(3, JDBCType.INTEGER.getVendorTypeNumber());
                    ps.setInt(4, student.getId());
                    ps.executeUpdate();
                }
            } else {
                ListIterator<Student> it = student.getGroup().getStudents().listIterator();
                boolean set = false;
                while (it.hasNext()) {
                    Student s = it.next();
                    if (Objects.equals(s.getId(), student.getId())) {
                        it.set(student);
                        set = true;
                        break;
                    }
                }
                if (!set) {
                    student.getGroup().getStudents().add(student);
                }
                groupRepository.save(student.getGroup());
            }

            return student;
        });
    }
}
