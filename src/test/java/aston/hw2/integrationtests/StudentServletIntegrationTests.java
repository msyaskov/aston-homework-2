package aston.hw2.integrationtests;

import aston.hw2.dto.GroupDto;
import aston.hw2.dto.StudentDto;
import aston.hw2.repository.TestJdbcHelper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentServletIntegrationTests {

    @Autowired
    private TestRestTemplate rt;

    @Autowired
    private TestJdbcHelper jdbc;

    @BeforeAll
    public void createDBTables() throws SQLException {
        jdbc.createTables();
    }

    @AfterEach
    public void clearDBTables() throws SQLException {
        jdbc.clearTables();
    }

    @Test
    public void testGetStudents_whenNoStudents_thenReturnsOkWithEmptyList() throws SQLException {
        assertEquals(0, jdbc.countOfStudents());

        ResponseEntity<List<StudentDto>> response = rt.exchange(RequestEntity.get("/students").build(),
                new ParameterizedTypeReference<List<StudentDto>>() {});

        List<StudentDto> students = response.getBody();

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertTrue(students.isEmpty()));
    }

    @Test
    public void testGetStudents_returnsOkWithStudents() throws SQLException {
        LocalDate now = LocalDate.now();
        int sId1 = jdbc.insertStudent("s1name", now, null);
        int sId2 = jdbc.insertStudent("s2name", now, null);
        assertEquals(2, jdbc.countOfStudents());

        ResponseEntity<List<StudentDto>> response = rt.exchange(RequestEntity.get("/students").build(),
                new ParameterizedTypeReference<List<StudentDto>>() {});

        List<StudentDto> students = response.getBody();

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(2, students.size()));

        for (StudentDto student : students) {
            if (student.getId() == sId1) {
                assertAll(() -> assertEquals("s1name", student.getName()),
                        () -> assertEquals(now, student.getDateOfBirth()));
            } else if (student.getId() == sId2) {
                assertAll(() -> assertEquals("s2name", student.getName()),
                        () -> assertEquals(now, student.getDateOfBirth()));
            } else {
                throw new AssertionError("Unexpected student id: " + students);
            }
        }
    }

    @Test
    public void testPostStudent_returnsCreatedWithNewStudent() throws SQLException {
        assertEquals(0, jdbc.countOfStudents());

        StudentDto candidate = new StudentDto(null, "name", LocalDate.now());
        ResponseEntity<StudentDto> response = rt.exchange(
                RequestEntity.post("/students").body(candidate), StudentDto.class);

        StudentDto student = response.getBody();
        assertAll(() -> assertEquals(HttpServletResponse.SC_CREATED, response.getStatusCode().value()),
                () -> assertNotNull(student.getId()),
                () -> assertEquals(candidate.getName(), student.getName()),
                () -> assertEquals(candidate.getDateOfBirth(), student.getDateOfBirth()),
                () -> assertEquals(1, jdbc.countOfStudents()),
                () -> assertTrue(jdbc.containsStudent(student.getId(), student.getName(), student.getDateOfBirth(), null)));
    }

    @Test
    public void testPostStudent_whenInvalidCandidate_thenReturnsBadRequest() throws SQLException {
        assertEquals(0, jdbc.countOfStudents());

        StudentDto candidate = new StudentDto(null, null, null);
        ResponseEntity<StudentDto> response = rt.exchange(
                RequestEntity.post("/students").body(candidate), StudentDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfStudents()));
    }

    @Test
    public void testGetStudent_returnsOkWithGroup() throws SQLException {
        LocalDate now = LocalDate.now();
        int sId = jdbc.insertStudent("name", now, null);
        assertEquals(1, jdbc.countOfStudents());

        ResponseEntity<StudentDto> response = rt.exchange(RequestEntity.get("/students/"+sId).build(), StudentDto.class);

        StudentDto student = response.getBody();
        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(sId, student.getId()),
                () -> assertEquals("name", student.getName()),
                () -> assertEquals(now, student.getDateOfBirth()),
                () -> assertEquals(1, jdbc.countOfStudents()));
    }

    @Test
    public void testGetStudent_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfStudents());

        int anyStudentId = 123;
        ResponseEntity<StudentDto> response = rt.exchange(
                RequestEntity.get("/students/"+anyStudentId).build(), StudentDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfStudents()));
    }

    @Test
    public void testPutStudent_returnsOkWithUpdatedGroup() throws SQLException {
        LocalDate now = LocalDate.now();
        int sId = jdbc.insertStudent("name", now, null);
        assertEquals(1, jdbc.countOfStudents());

        StudentDto candidate = new StudentDto(null, "updated_name", LocalDate.now());
        ResponseEntity<StudentDto> response = rt.exchange(
                RequestEntity.put("/students/"+sId).body(candidate), StudentDto.class);

        StudentDto student = response.getBody();
        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(sId, student.getId()),
                () -> assertEquals(candidate.getName(), student.getName()),
                () -> assertEquals(candidate.getDateOfBirth(), student.getDateOfBirth()),
                () -> assertTrue(jdbc.containsStudent(student.getId(), student.getName(), student.getDateOfBirth(), null)),
                () -> assertEquals(1, jdbc.countOfStudents()));
    }

    @Test
    public void testPutStudent_whenInvalidCandidate_thenReturnsBadRequest() throws SQLException {
        LocalDate now = LocalDate.now();
        int sId = jdbc.insertStudent("name", now, null);
        assertEquals(1, jdbc.countOfStudents());

        StudentDto candidate = new StudentDto(null, null, null);
        ResponseEntity<StudentDto> response = rt.exchange(RequestEntity.put("/students/"+sId).body(candidate), StudentDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusCode().value()),
                () -> assertTrue(jdbc.containsStudent(sId, "name", now, null)),
                () -> assertEquals(1, jdbc.countOfStudents()));
    }

    @Test
    public void testPutStudent_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfStudents());

        int anyStudentId = 123;
        StudentDto candidate = new StudentDto(null, "name", LocalDate.now());
        ResponseEntity<StudentDto> response = rt.exchange(RequestEntity.put("/students/"+anyStudentId).body(candidate), StudentDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfStudents()));
    }

    @Test
    public void testDeleteStudent_returnsOk() throws SQLException {
        int sId = jdbc.insertStudent("name", LocalDate.now(), null);
        assertEquals(1, jdbc.countOfStudents());

        ResponseEntity<Void> response = rt.exchange(RequestEntity.delete("/students/"+sId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfStudents()));
    }

    @Test
    public void testDeleteStudent_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        int anyStudentId = 123;
        ResponseEntity<Void> response = rt.exchange(RequestEntity.delete("/students/"+anyStudentId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfGroups()));
    }

    @Test
    public void testGetStudentGroup_returnsOkWithGroup() throws SQLException {
        LocalDate now = LocalDate.now();
        int gId = jdbc.insertGroup("gname", now);
        int sId = jdbc.insertStudent("sname", now, gId);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(1, jdbc.countOfStudents());

        ResponseEntity<GroupDto> response = rt.exchange(
                RequestEntity.get("/students/" + sId + "/group").build(), GroupDto.class);

        GroupDto group = response.getBody();

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertNotNull(group),
                () -> assertEquals(gId, group.getId()),
                () -> assertEquals("gname", group.getName()),
                () -> assertEquals(now, group.getGraduationDate()));
    }

    @Test
    public void testGetStudentGroup_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());
        assertEquals(0, jdbc.countOfStudents());

        int anyStudentId = 123;
        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.get("/students/" + anyStudentId + "/group").build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }

    @Test
    public void testPutStudentGroup_returnsOk() throws SQLException {
        LocalDate now = LocalDate.now();
        int gId = jdbc.insertGroup("gname", now);
        int sId = jdbc.insertStudent("sname", now, null);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(1, jdbc.countOfStudents());

        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.put("/students/" + sId + "/group/" + gId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertTrue(jdbc.containsGroup(gId, "gname", now)),
                () -> assertTrue(jdbc.containsStudent(sId, "sname", now, gId)),
                () -> assertEquals(1, jdbc.countOfGroups()),
                () -> assertEquals(1, jdbc.countOfStudents()));
    }

    @Test
    public void testPutStudentGroup_whenNotExistingIds_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());
        assertEquals(0, jdbc.countOfStudents());

        int anyGroupId = 123;
        int anyStudentId = 321;
        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.put("/students/" + anyStudentId + "/group/" + anyGroupId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }

    @Test
    public void testDeleteStudentGroup_returnsOk() throws SQLException {
        LocalDate now = LocalDate.now();
        int gId = jdbc.insertGroup("gname", now);
        int sId = jdbc.insertStudent("sname", now, gId);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(1, jdbc.countOfStudents());

        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.delete("/students/" + sId + "/group").build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertTrue(jdbc.containsGroup(gId, "gname", now)),
                () -> assertTrue(jdbc.containsStudent(sId, "sname", now, null)),
                () -> assertEquals(1, jdbc.countOfGroups()),
                () -> assertEquals(1, jdbc.countOfStudents()));
    }

    @Test
    public void testDeleteStudentGroup_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());
        assertEquals(0, jdbc.countOfStudents());

        int anyStudentId = 321;
        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.delete("/students/" + anyStudentId + "/group").build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }
}
