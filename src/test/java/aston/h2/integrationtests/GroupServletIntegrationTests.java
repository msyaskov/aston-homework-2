package aston.h2.integrationtests;

import aston.h2.dto.CuratorDto;
import aston.h2.dto.GroupDto;
import aston.h2.dto.StudentDto;
import aston.h2.repository.TestJdbcHelper;
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
public class GroupServletIntegrationTests {

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
    public void testGetGroups_whenNoGroups_thenReturnsOkWithEmptyList() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        ResponseEntity<List<GroupDto>> response = rt.exchange(RequestEntity.get("/groups").build(),
                new ParameterizedTypeReference<List<GroupDto>>() {});

        List<GroupDto> groups = response.getBody();

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertNotNull(groups),
                () -> assertTrue(groups.isEmpty()));
    }

    @Test
    public void testGetGroups_returnsOkWithGroupList() throws SQLException {
        LocalDate graduationDate = LocalDate.now();
        int gId1 = jdbc.insertGroup("g1name", graduationDate);
        int gId2 = jdbc.insertGroup("g2name", graduationDate);
        assertEquals(2, jdbc.countOfGroups());

        ResponseEntity<List<GroupDto>> response = rt.exchange(RequestEntity.get("/groups").build(),
                new ParameterizedTypeReference<List<GroupDto>>() {});

        List<GroupDto> groups = response.getBody();

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertTrue(response.hasBody()),
                () -> assertNotNull(groups),
                () -> assertEquals(2, groups.size()));

        for (GroupDto group : groups) {
            if (group.getId() == gId1) {
                assertAll(() -> assertEquals("g1name", group.getName()),
                        () -> assertEquals(graduationDate, group.getGraduationDate()));
            } else if (group.getId() == gId2) {
                assertAll(() -> assertEquals("g2name", group.getName()),
                        () -> assertEquals(graduationDate, group.getGraduationDate()));
            } else {
                throw new AssertionError("Unexpected group id: " + groups);
            }
        }
    }

    @Test
    public void testPostGroup_returnsCreatedWithNewGroup() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        GroupDto candidate = new GroupDto(null, "candidateName", LocalDate.now());
        ResponseEntity<GroupDto> response = rt.exchange(RequestEntity.post("/groups").body(candidate), GroupDto.class);

        GroupDto group = response.getBody();
        assertAll(() -> assertEquals(HttpServletResponse.SC_CREATED, response.getStatusCode().value()),
                () -> assertTrue(response.hasBody()),
                () -> assertNotNull(group),
                () -> assertNotNull(group.getId()),
                () -> assertEquals(candidate.getName(), group.getName()),
                () -> assertEquals(candidate.getGraduationDate(), group.getGraduationDate()),
                () -> assertEquals(1, jdbc.countOfGroups()),
                () -> assertTrue(jdbc.containsGroup(group.getId(), group.getName(), group.getGraduationDate())));
    }

    @Test
    public void testPostGroup_whenInvalidCandidate_thenReturnsBadRequest() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        GroupDto candidate = new GroupDto(null, null, null);
        ResponseEntity<GroupDto> response = rt.exchange(RequestEntity.post("/groups").body(candidate), GroupDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfGroups()));
    }

    @Test
    public void testGetGroup_returnsOkWithGroup() throws SQLException {
        LocalDate graduationDate = LocalDate.now();
        int gId = jdbc.insertGroup("gname", graduationDate);
        assertEquals(1, jdbc.countOfGroups());

        ResponseEntity<GroupDto> response = rt.exchange(RequestEntity.get("/groups/"+gId).build(), GroupDto.class);

        GroupDto group = response.getBody();
        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertNotNull(group),
                () -> assertEquals(gId, group.getId()),
                () -> assertEquals("gname", group.getName()),
                () -> assertEquals(graduationDate, group.getGraduationDate()),
                () -> assertEquals(1, jdbc.countOfGroups()));
    }

    @Test
    public void testGetGroup_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        int anyGroupId = 123;
        ResponseEntity<GroupDto> response = rt.exchange(RequestEntity.get("/groups/"+anyGroupId).build(), GroupDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfGroups()));
    }

    @Test
    public void testPutGroup_returnsOkWithUpdatedGroup() throws SQLException {
        LocalDate graduationDate = LocalDate.now();
        int gId = jdbc.insertGroup("gname", graduationDate);
        assertEquals(1, jdbc.countOfGroups());

        GroupDto candidate = new GroupDto(null, "updated_gname", graduationDate);
        ResponseEntity<GroupDto> response = rt.exchange(RequestEntity.put("/groups/"+gId).body(candidate), GroupDto.class);

        GroupDto group = response.getBody();
        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(gId, group.getId()),
                () -> assertEquals(candidate.getName(), group.getName()),
                () -> assertEquals(graduationDate, group.getGraduationDate()),
                () -> assertTrue(jdbc.containsGroup(group.getId(), group.getName(), group.getGraduationDate())),
                () -> assertEquals(1, jdbc.countOfGroups()));
    }

    @Test
    public void testPutGroup_whenInvalidCandidate_thenReturnsBadRequest() throws SQLException {
        LocalDate graduationDate = LocalDate.now();
        int gId = jdbc.insertGroup("gname", graduationDate);
        assertEquals(1, jdbc.countOfGroups());

        GroupDto candidate = new GroupDto(null, null, null);
        ResponseEntity<GroupDto> response = rt.exchange(RequestEntity.put("/groups/"+gId).body(candidate), GroupDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusCode().value()),
                () -> assertTrue(jdbc.containsGroup(gId, "gname", graduationDate)),
                () -> assertEquals(1, jdbc.countOfGroups()));
    }

    @Test
    public void testPutGroup_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        int anyGroupId = 123;
        GroupDto candidate = new GroupDto(null, "candidateName", LocalDate.now());
        ResponseEntity<GroupDto> response = rt.exchange(RequestEntity.put("/groups/"+anyGroupId).body(candidate), GroupDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfGroups()));
    }

    @Test
    public void testDeleteGroup_returnsOk() throws SQLException {
        int gId = jdbc.insertGroup("gname", LocalDate.now());
        assertEquals(1, jdbc.countOfGroups());

        ResponseEntity<Void> response = rt.exchange(RequestEntity.delete("/groups/"+gId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfGroups()));
    }

    @Test
    public void testDeleteGroup_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        int anyGroupId = 123;
        ResponseEntity<Void> response = rt.exchange(RequestEntity.delete("/groups/"+anyGroupId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfGroups()));
    }

    @Test
    public void testGetGroupCurator_returnsOkWithCurator() throws SQLException {
        int gId = jdbc.insertGroup("gname", LocalDate.now());
        int cId = jdbc.insertCurator("cname", "cemail", 1, gId);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(1, jdbc.countOfCurators());

        ResponseEntity<CuratorDto> response = rt.exchange(RequestEntity.get("/groups/"+gId+"/curator").build(), CuratorDto.class);

        CuratorDto curator = response.getBody();
        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(cId, curator.getId()),
                () -> assertEquals("cname", curator.getName()),
                () -> assertEquals("cemail", curator.getEmail()),
                () -> assertEquals(1, curator.getExperience()),
                () -> assertEquals(1, jdbc.countOfCurators()),
                () -> assertEquals(1, jdbc.countOfGroups()));
    }

    @Test
    public void testGetGroupCurator_whenGroupWithoutCurator_thenReturnsNoContent() throws SQLException {
        int gId = jdbc.insertGroup("gname", LocalDate.now());
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(0, jdbc.countOfCurators());

        ResponseEntity<CuratorDto> response = rt.exchange(RequestEntity.get("/groups/"+gId+"/curator").build(), CuratorDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatusCode().value()),
                () -> assertFalse(response.hasBody()),
                () -> assertEquals(0, jdbc.countOfCurators()),
                () -> assertEquals(1, jdbc.countOfGroups()));
    }

    @Test
    public void testGetGroupCurator_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        int anyGroupId = 123;
        ResponseEntity<CuratorDto> response = rt.exchange(RequestEntity.get("/groups/"+anyGroupId+"/curator").build(), CuratorDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }

    @Test
    public void testDeleteGroupCurator_returnsOk() throws SQLException {
        LocalDate graduationDate = LocalDate.now();
        int gId = jdbc.insertGroup("gname", graduationDate);
        int cId = jdbc.insertCurator("cname", "cemail", 1, gId);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(1, jdbc.countOfCurators());

        ResponseEntity<Void> response = rt.exchange(RequestEntity.delete("/groups/"+gId+"/curator").build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertTrue(jdbc.containsGroup(gId, "gname", graduationDate)),
                () -> assertTrue(jdbc.containsCurator(cId, "cname", "cemail", 1, null)),
                () -> assertEquals(1, jdbc.countOfCurators()),
                () -> assertEquals(1, jdbc.countOfGroups()));
    }

    @Test
    public void testDeleteGroupCurator_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        int anyGroupId = 123;
        ResponseEntity<Void> response = rt.exchange(RequestEntity.delete("/groups/"+anyGroupId+"/curator").build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }

    @Test
    public void testPutGroupCurator_returnsOk() throws SQLException {
        LocalDate graduationDate = LocalDate.now();
        int gId = jdbc.insertGroup("gname", graduationDate);
        int cId = jdbc.insertCurator("cname", "cemail", 1, null);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(1, jdbc.countOfCurators());

        ResponseEntity<Void> response = rt.exchange(RequestEntity.put("/groups/"+gId+"/curator/"+cId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertTrue(jdbc.containsGroup(gId, "gname", graduationDate)),
                () -> assertTrue(jdbc.containsCurator(cId, "cname", "cemail", 1, gId)),
                () -> assertEquals(1, jdbc.countOfCurators()),
                () -> assertEquals(1, jdbc.countOfGroups()));
    }

    @Test
    public void testPutGroupCurator_whenNotExistingIds_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        int anyGroupId = 123;
        int anyCuratorId = 321;
        ResponseEntity<Void> response = rt.exchange(RequestEntity.put("/groups/"+anyGroupId+"/curator/"+anyCuratorId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }

    @Test
    public void testGetGroupStudents_returnsOkWithStudents() throws SQLException {
        LocalDate now = LocalDate.now();
        int gId = jdbc.insertGroup("gname", now);
        int sId1 = jdbc.insertStudent("s1name", now, gId);
        int sId2 = jdbc.insertStudent("s2name", now, gId);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(2, jdbc.countOfStudents());

        ResponseEntity<List<StudentDto>> response = rt.exchange(
                RequestEntity.get("/groups/" + gId + "/students").build(), new ParameterizedTypeReference<List<StudentDto>>() {});

        List<StudentDto> students = response.getBody();

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertNotNull(students),
                () -> assertEquals(2, students.size()));

        for (StudentDto student : students) {
            if (student.getId() == sId1) {
                assertAll(() -> assertEquals("s1name", student.getName()),
                        () -> assertEquals(now, student.getDateOfBirth()));
            } else if (student.getId() == sId2) {
                assertAll(() -> assertEquals("s2name", student.getName()),
                        () -> assertEquals(now, student.getDateOfBirth()));
            } else {
                throw new AssertionError("Unexpected studentId");
            }
        }
    }

    @Test
    public void testGetGroupStudents_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());
        assertEquals(0, jdbc.countOfStudents());

        int anyGroupId = 123;
        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.get("/groups/" + anyGroupId + "/students").build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }

    @Test
    public void testPutGroupStudent_returnsOk() throws SQLException {
        LocalDate now = LocalDate.now();
        int gId = jdbc.insertGroup("gname", now);
        int sId = jdbc.insertStudent("sname", now, null);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(1, jdbc.countOfStudents());

        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.put("/groups/" + gId + "/students/" + sId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertTrue(jdbc.containsGroup(gId, "gname", now)),
                () -> assertTrue(jdbc.containsStudent(sId, "sname", now, gId)),
                () -> assertEquals(1, jdbc.countOfGroups()),
                () -> assertEquals(1, jdbc.countOfStudents()));
    }

    @Test
    public void testPutGroupStudent_whenNotExistingIds_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());
        assertEquals(0, jdbc.countOfStudents());

        int anyGroupId = 123;
        int anyStudentId = 321;
        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.put("/groups/" + anyGroupId + "/students/" + anyStudentId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }

    @Test
    public void testDeleteGroupStudent_returnsOk() throws SQLException {
        LocalDate now = LocalDate.now();
        int gId = jdbc.insertGroup("gname", now);
        int sId = jdbc.insertStudent("sname", now, gId);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(1, jdbc.countOfStudents());

        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.delete("/groups/" + gId + "/students/" + sId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertTrue(jdbc.containsGroup(gId, "gname", now)),
                () -> assertTrue(jdbc.containsStudent(sId, "sname", now, null)),
                () -> assertEquals(1, jdbc.countOfGroups()),
                () -> assertEquals(1, jdbc.countOfStudents()));
    }

    @Test
    public void testDeleteGroupStudent_whenNotExistingIds_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());
        assertEquals(0, jdbc.countOfStudents());

        int anyGroupId = 123;
        int anyStudentId = 321;
        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.delete("/groups/" + anyGroupId + "/students/" + anyStudentId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }
}
