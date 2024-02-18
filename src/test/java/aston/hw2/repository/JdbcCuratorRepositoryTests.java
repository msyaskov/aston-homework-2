package aston.hw2.repository;

import aston.hw2.entity.Curator;
import aston.hw2.entity.Group;
import aston.hw2.entity.Student;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcCuratorRepositoryTests {

    private static JdbcCuratorRepository curatorRepository;

    private static TestJdbcHelper jdbc;

    @BeforeAll
    public static void setUp() throws SQLException {
        JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory("org.h2.Driver", "jdbc:h2:mem:test_db;DEFAULT_LOCK_TIMEOUT=10000;LOCK_MODE=0;DB_CLOSE_DELAY=-1");
        curatorRepository = new JdbcCuratorRepository(jdbcConnectionFactory, new JdbcGroupRepository(jdbcConnectionFactory));
        jdbc = new TestJdbcHelper(jdbcConnectionFactory);
        jdbc.createTables();
    }

    @AfterEach
    public void clearDBTables() throws SQLException {
        jdbc.clearTables();
    }

    @Test
    public void test_findAll() throws SQLException {
        int groupId = jdbc.insertGroup("gname", LocalDate.now());
        int curatorId = jdbc.insertCurator("cname", "cemail", 1, groupId);
        int studentId1 = jdbc.insertStudent("s1name", LocalDate.now(), groupId);

        Collection<Curator> curators = curatorRepository.findAll().collect(Collectors.toList());

        assertAll(() -> assertNotNull(curators),
                () -> assertEquals(1, curators.size()));

        Curator curator = curators.iterator().next();
        assertNotNull(curator);
        assertEquals(curatorId, curator.getId());
        assertEquals("cname", curator.getName());
        assertEquals("cemail", curator.getEmail());
        assertEquals(1, curator.getExperience());

        assertNotNull(curator.getGroup());
        assertEquals(groupId, curator.getGroup().getId());
        assertEquals("gname", curator.getGroup().getName());
        assertNotNull(curator.getGroup().getGraduationDate());
        assertEquals(curator, curator.getGroup().getCurator());

        assertNotNull(curator.getGroup().getStudents());
        assertEquals(1, curator.getGroup().getStudents().size());
    }

    @Test
    public void test_findAll_notFound_returnsNotNull() {
        Collection<Curator> curators = curatorRepository.findAll().collect(Collectors.toList());
        assertNotNull(curators);
        assertTrue(curators.isEmpty());
    }

    @Test
    public void test_findById_returnsNotNull() throws SQLException {
        int groupId = jdbc.insertGroup("gname", LocalDate.now());
        int curatorId = jdbc.insertCurator("cname", "cemail", 1, groupId);
        int studentId = jdbc.insertStudent("sname", LocalDate.now(), groupId);

        Curator curator = curatorRepository.findById(studentId);

        assertNotNull(curator);
        assertEquals(curatorId, curator.getId());
        assertEquals("cname", curator.getName());
        assertEquals("cemail", curator.getEmail());
        assertEquals(1, curator.getExperience());

        assertNotNull(curator.getGroup());
        assertEquals(groupId, curator.getGroup().getId());
        assertEquals("gname", curator.getGroup().getName());
        assertNotNull(curator.getGroup().getGraduationDate());
        assertEquals(curator, curator.getGroup().getCurator());

        assertNotNull(curator.getGroup().getStudents());
        assertEquals(1, curator.getGroup().getStudents().size());
    }

    @Test
    public void test_findById_nonExistingId_returnsNull() {
        assertNull(curatorRepository.findById(1));
    }

    @Test
    public void test_removeById() throws SQLException {
        LocalDate now = LocalDate.now();
        int groupId = jdbc.insertGroup("gname", now);
        int curatorId = jdbc.insertCurator("cname", "cemail", 1, groupId);
        int studentId = jdbc.insertStudent("sname", now, groupId);

        curatorRepository.removeById(studentId);

        assertAll(() -> assertTrue(jdbc.containsGroup(groupId, "gname", now)),
                () -> assertFalse(jdbc.containsCurator(curatorId, "cname", "cemail", 1, groupId)),
                () -> assertTrue(jdbc.containsStudent(studentId, "sname", now, groupId)));
    }

    @Test
    public void test_removeById_nonExistingId_returnsNull() {
        assertNull(curatorRepository.removeById(1));
    }

    @Test
    public void test_save_withCascadeInserting() {
        LocalDate now = LocalDate.now();
        Group group = new Group("gname", now, null, new LinkedList<>());

        Curator curator = new Curator("cname", "cemail", 1, group);
        group.setCurator(curator);

        Student student = new Student("sname", now, group);
        group.getStudents().add(student);

        Curator saved = curatorRepository.save(curator);
        assertSame(curator, saved);

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

        curatorRepository.save(curator);

        // update
        student.setName("updated_sname");
        curator.getGroup().setName("updated_gname");
        curator.setName("updated_cname");

        Curator saved = curatorRepository.save(curator);
        assertSame(curator, saved);

        assertAll(() -> assertTrue(jdbc.containsGroup(group.getId(), "updated_gname", now), "Group not contained"),
                () -> assertTrue(jdbc.containsCurator(curator.getId(), "updated_cname", "cemail", 1, group.getId()), "Curator not contained"),
                () -> assertTrue(jdbc.containsStudent(student.getId(), "updated_sname", now, group.getId()), "Student not contained"));
    }
}