package aston.h2.repository;

import aston.h2.entity.Curator;
import aston.h2.entity.Group;
import aston.h2.entity.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcGroupRepositoryTests {

    private static JdbcGroupRepository groupRepository;

    private static TestJdbcHelper jdbc;

    @BeforeAll
    public static void setUp() throws SQLException {
        JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory("org.h2.Driver", "jdbc:h2:mem:test_db;DEFAULT_LOCK_TIMEOUT=10000;LOCK_MODE=0;DB_CLOSE_DELAY=-1");
        groupRepository = new JdbcGroupRepository(jdbcConnectionFactory);
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
        jdbc.insertCurator("c1name", "c1email", 1, groupId1);
        jdbc.insertStudent("s1name", LocalDate.now(), groupId1);
        jdbc.insertStudent("s2name", LocalDate.now(), groupId1);

        int groupId2 = jdbc.insertGroup("g2name", LocalDate.now());
        jdbc.insertCurator("c2name", "c2email", 1, groupId2);
        jdbc.insertStudent("s3name", LocalDate.now(), groupId2);
        jdbc.insertStudent("s4name", LocalDate.now(), groupId2);

        Collection<Group> groups = groupRepository.findAll().collect(Collectors.toList());

        assertAll(() -> assertNotNull(groups),
                () -> assertEquals(2, groups.size()));

        for (Group group : groups) {
            assertAll(() -> assertNotNull(group),
                    () -> assertNotNull(group.getId()),
                    () -> assertNotNull(group.getName()),
                    () -> assertNotNull(group.getGraduationDate()),

                    () -> assertNotNull(group.getCurator()),
                    () -> assertNotNull(group.getCurator().getId()),
                    () -> assertNotNull(group.getCurator().getName()),
                    () -> assertNotNull(group.getCurator().getEmail()),
                    () -> assertEquals(1, group.getCurator().getExperience()),
                    () -> assertEquals(group, group.getCurator().getGroup()),

                    () -> assertEquals(2, group.getStudents().size()));

            for (Student student : group.getStudents()) {
                assertAll(() -> assertNotNull(student),
                        () -> assertNotNull(student.getId()),
                        () -> assertNotNull(student.getName()),
                        () -> assertNotNull(student.getDateOfBirth()),
                        () -> assertEquals(group, student.getGroup()));
            }
        }
    }

    @Test
    public void test_findAll_notFound_returnsNotNull() {
        assertNotNull(groupRepository.findAll());
    }

    @Test
    public void test_findById_returnsNotNull() throws SQLException {
        int groupId = jdbc.insertGroup("gname", LocalDate.now());
        int curatorId = jdbc.insertCurator("cname", "cemail", 123, groupId);
        int studentId1 = jdbc.insertStudent("s1name", LocalDate.now(), groupId);
        int studentId2 = jdbc.insertStudent("s2name", LocalDate.now(), groupId);
        int studentId3 = jdbc.insertStudent("s3name", LocalDate.now(), groupId);

        Group group = groupRepository.findById(groupId);

        assertAll(() -> assertNotNull(group),
                () -> assertEquals(groupId, group.getId()),
                () -> assertEquals("gname", group.getName()),
                () -> assertNotNull(group.getGraduationDate()),

                () -> assertNotNull(group.getCurator()),
                () -> assertEquals(curatorId, group.getCurator().getId()),
                () -> assertEquals("cname", group.getCurator().getName()),
                () -> assertEquals("cemail", group.getCurator().getEmail()),
                () -> assertEquals(123, group.getCurator().getExperience()),
                () -> assertEquals(group, group.getCurator().getGroup()),

                () -> assertEquals(3, group.getStudents().size()));

        for (Student student : group.getStudents()) {
            assertAll(() -> assertNotNull(student),
                    () -> assertNotNull(student.getDateOfBirth()),
                    () -> assertEquals(group, student.getGroup()),
                    () -> {
                        if (student.getId() == studentId1) {
                            assertEquals("s1name", student.getName());
                        } else if (student.getId() == studentId2) {
                            assertEquals("s2name", student.getName());
                        } else if (student.getId() == studentId3) {
                            assertEquals("s3name", student.getName());
                        } else {
                            throw new AssertionError("Unexpected studentId");
                        }});
        }
    }

    @Test
    public void test_findById_nonExistingId_returnsNull() {
        assertNull(groupRepository.findById(1));
    }

    @Test
    public void test_findByName() throws SQLException {
        int groupId = jdbc.insertGroup("gname", LocalDate.now());
        jdbc.insertCurator("cname", "cemail", 1, groupId);
        jdbc.insertStudent("s1name", LocalDate.now(), groupId);
        jdbc.insertStudent("s2name", LocalDate.now(), groupId);
        jdbc.insertStudent("s3name", LocalDate.now(), groupId);

        Group group = groupRepository.findByName("gname");

        assertAll(() -> assertNotNull(group),
                () -> assertNotNull(group.getId()),
                () -> assertEquals("gname", group.getName()),
                () -> assertNotNull(group.getGraduationDate()),

                () -> assertNotNull(group.getCurator()),
                () -> assertNotNull(group.getCurator().getId()),
                () -> assertNotNull(group.getCurator().getName()),
                () -> assertNotNull(group.getCurator().getEmail()),
                () -> assertEquals(1, group.getCurator().getExperience()),
                () -> assertEquals(group, group.getCurator().getGroup()),

                () -> assertEquals(3, group.getStudents().size()));

        for (Student student : group.getStudents()) {
            assertAll(() -> assertNotNull(student),
                    () -> assertNotNull(student.getId()),
                    () -> assertNotNull(student.getName()),
                    () -> assertNotNull(student.getDateOfBirth()),
                    () -> assertEquals(group, student.getGroup()));
        }
    }

    @Test
    public void test_findByName_nonExistingName_returnsNull() {
        assertNull(groupRepository.findByName("name"));
    }

    @Test
    public void test_removeById() throws SQLException {
        LocalDate now = LocalDate.now();
        int groupId = jdbc.insertGroup("gname", now);
        int curatorId = jdbc.insertCurator("cname", "cemail", 1, groupId);
        int studentId = jdbc.insertStudent("sname", now, groupId);

        groupRepository.removeById(groupId);

        assertAll(() -> assertFalse(jdbc.containsGroup(groupId, "gname", now)),
                () -> assertTrue(jdbc.containsCurator(curatorId, "cname", "cemail", 1, null)),
                () -> assertTrue(jdbc.containsStudent(studentId, "sname", now, null)));
    }

    @Test
    public void test_removeById_nonExistingId_returnsNull() {
        assertNull(groupRepository.removeById(1));
    }

    @Test
    public void test_save_withCascadeInserting() {
        LocalDate now = LocalDate.now();
        Group group = new Group("gname", now, null, new LinkedList<>());

        Curator curator = new Curator("cname", "cemail", 1, group);
        group.setCurator(curator);

        Student student = new Student("sname", now, group);
        group.getStudents().add(student);

        Group saved = groupRepository.save(group);
        assertSame(group, saved);

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

        groupRepository.save(group);

        // update
        group.getCurator().setExperience(2);
        group.getStudents().clear();

        Group saved = groupRepository.save(group);
        assertSame(group, saved);

        assertAll(() -> assertTrue(jdbc.containsGroup(group.getId(), "gname", now), "Group not contained"),
                () -> assertTrue(jdbc.containsCurator(curator.getId(), "cname", "cemail", 2, curator.getGroup().getId()), "Curator not contained"),
                () -> assertTrue(jdbc.containsStudent(student.getId(), "sname", now, null), "Student not contained"));
    }
}