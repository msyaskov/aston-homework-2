package aston.h2.repository;

import aston.h2.entity.Curator;
import aston.h2.entity.Group;
import aston.h2.entity.Student;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcStudentRepositoryTests {

    private static JdbcStudentRepository studentRepository;

    private static TestJdbcHelper jdbc;

    @BeforeAll
    public static void setUp() throws SQLException {
        JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory("org.h2.Driver", "jdbc:h2:mem:test_db;DEFAULT_LOCK_TIMEOUT=10000;LOCK_MODE=0;DB_CLOSE_DELAY=-1");
        studentRepository = new JdbcStudentRepository(jdbcConnectionFactory, new JdbcGroupRepository(jdbcConnectionFactory));
        jdbc = new TestJdbcHelper(jdbcConnectionFactory);
        jdbc.createTables();
    }

    @AfterEach
    public void clearDBTables() throws SQLException {
        jdbc.clearTables();
    }

    @Test
    public void test_findAll() throws SQLException {
        int groupId1 = jdbc.insertGroup("g1name", LocalDate.now());
        int curatorId1 = jdbc.insertCurator("c1name", "c1email", 1, groupId1);
        int studentId1 = jdbc.insertStudent("s1name", LocalDate.now(), groupId1);
        int studentId2 = jdbc.insertStudent("s2name", LocalDate.now(), null);

        Collection<Student> students = studentRepository.findAll().collect(Collectors.toList());

        assertAll(() -> assertNotNull(students),
                () -> assertEquals(2, students.size()));

        for (Student student : students) {
            assertNotNull(student);
            assertNotNull(student.getId());
            assertNotNull(student.getDateOfBirth());
            if (student.getId() == studentId1) {
                assertEquals("s1name", student.getName());
                assertNotNull(student.getGroup());
                assertEquals(groupId1, student.getGroup().getId());
                assertEquals("g1name", student.getGroup().getName());
                assertNotNull(student.getGroup().getGraduationDate());
                assertEquals(1, student.getGroup().getStudents().size());
                assertTrue(student.getGroup().getStudents().contains(student));
                assertNotNull(student.getGroup().getCurator());
                assertEquals(curatorId1, student.getGroup().getCurator().getId());
                assertEquals("c1name", student.getGroup().getCurator().getName());
                assertEquals("c1email", student.getGroup().getCurator().getEmail());
                assertEquals(1, student.getGroup().getCurator().getExperience());
                assertEquals(student.getGroup(), student.getGroup().getCurator().getGroup());
            } else if (student.getId() == studentId2) {
                assertEquals("s2name", student.getName());
                assertNull(student.getGroup());
            } else {
                throw new AssertionError("Unexpected studentId");
            }
        }
    }

    @Test
    public void test_findAll_notFound_returnsNotNull() {
        Collection<Student> students = studentRepository.findAll().collect(Collectors.toList());
        assertNotNull(students);
        assertTrue(students.isEmpty());
    }

    @Test
    public void test_findById_returnsNotNull() throws SQLException {
        int groupId = jdbc.insertGroup("gname", LocalDate.now());
        int curatorId = jdbc.insertCurator("cname", "cemail", 1, groupId);
        int studentId = jdbc.insertStudent("sname", LocalDate.now(), groupId);

        Student student = studentRepository.findById(studentId);

        assertNotNull(student);
        assertEquals(studentId, student.getId());
        assertEquals("sname", student.getName());
        assertNotNull(student.getDateOfBirth());
        assertNotNull(student.getGroup());
        assertEquals(groupId, student.getGroup().getId());
        assertEquals("gname", student.getGroup().getName());
        assertNotNull(student.getGroup().getGraduationDate());
        assertEquals(1, student.getGroup().getStudents().size());
        assertTrue(student.getGroup().getStudents().contains(student));
        assertNotNull(student.getGroup().getCurator());
        assertEquals(curatorId, student.getGroup().getCurator().getId());
        assertEquals("cname", student.getGroup().getCurator().getName());
        assertEquals("cemail", student.getGroup().getCurator().getEmail());
        assertEquals(1, student.getGroup().getCurator().getExperience());
        assertEquals(student.getGroup(), student.getGroup().getCurator().getGroup());
    }

    @Test
    public void test_findById_nonExistingId_returnsNull() {
        assertNull(studentRepository.findById(1));
    }

    @Test
    public void test_removeById() throws SQLException {
        LocalDate now = LocalDate.now();
        int groupId = jdbc.insertGroup("gname", now);
        int curatorId = jdbc.insertCurator("cname", "cemail", 1, groupId);
        int studentId = jdbc.insertStudent("sname", now, groupId);

        studentRepository.removeById(studentId);

        assertAll(() -> assertTrue(jdbc.containsGroup(groupId, "gname", now)),
                () -> assertTrue(jdbc.containsCurator(curatorId, "cname", "cemail", 1, groupId)),
                () -> assertFalse(jdbc.containsStudent(studentId, "sname", now, groupId)));
    }

    @Test
    public void test_removeById_nonExistingId_returnsNull() {
        assertNull(studentRepository.removeById(1));
    }

    @Test
    public void test_save_withCascadeInserting() {
        LocalDate now = LocalDate.now();
        Group group = new Group("gname", now, null, new LinkedList<>());

        Curator curator = new Curator("cname", "cemail", 1, group);
        group.setCurator(curator);

        Student student = new Student("sname", now, group);
        group.getStudents().add(student);

        Student saved = studentRepository.save(student);
        assertSame(student, saved);

        assertNotNull(student.getId());
        assertNotNull(group.getId());
        assertNotNull(curator.getId());

        assertAll(() -> assertTrue(jdbc.containsGroup(group.getId(), "gname", now), "Group not contained"),
                () -> assertTrue(jdbc.containsCurator(curator.getId(), "cname", "cemail", 1, curator.getGroup().getId()), "Curator not contained"),
                () -> assertTrue(jdbc.containsStudent(student.getId(), "sname", now, student.getGroup().getId()), "Student not contained"));
    }

    @Test
    public void test_save_withCascadeUpdating() {
        LocalDate now = LocalDate.now();
        Group group = new Group("gname", now, null, new LinkedList<>());

        Curator curator = new Curator("cname", "cemail", 1, group);
        group.setCurator(curator);

        Student student = new Student("sname", now, group);
        group.getStudents().add(student);

        studentRepository.save(student);

        // update
        student.setName("updated_sname");
        student.getGroup().setName("updated_gname");
        student.getGroup().setCurator(null);

        Student saved = studentRepository.save(student);
        assertSame(student, saved);

        assertAll(() -> assertTrue(jdbc.containsGroup(group.getId(), "updated_gname", now), "Group not contained"),
                () -> assertTrue(jdbc.containsCurator(curator.getId(), "cname", "cemail", 1, group.getId()), "Curator not contained"),
                () -> assertTrue(jdbc.containsStudent(student.getId(), "updated_sname", now, group.getId()), "Student not contained"));
    }
}