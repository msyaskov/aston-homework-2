package aston.hw2.integrationtests;

import aston.hw2.dto.CuratorDto;
import aston.hw2.dto.GroupDto;
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
public class CuratorServletIntegrationTests {

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
    public void testGetCurators_whenNoCurators_thenReturnsOkWithEmptyList() throws SQLException {
        assertEquals(0, jdbc.countOfCurators());

        ResponseEntity<List<CuratorDto>> response = rt.exchange(RequestEntity.get("/curators").build(),
                new ParameterizedTypeReference<List<CuratorDto>>() {});

        List<CuratorDto> curators = response.getBody();

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertTrue(curators.isEmpty()));
    }

    @Test
    public void testGetCurators_returnsOkWithCurators() throws SQLException {
        int cId1 = jdbc.insertCurator("c1name", "c1email", 1, null);
        int cId2 = jdbc.insertCurator("c2name", "c2email", 2, null);
        assertEquals(2, jdbc.countOfCurators());

        ResponseEntity<List<CuratorDto>> response = rt.exchange(RequestEntity.get("/curators").build(),
                new ParameterizedTypeReference<List<CuratorDto>>() {});

        List<CuratorDto> curators = response.getBody();

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(2, curators.size()));

        for (CuratorDto curator : curators) {
            if (curator.getId() == cId1) {
                assertAll(() -> assertEquals("c1name", curator.getName()),
                        () -> assertEquals("c1email", curator.getEmail()),
                        () -> assertEquals(1, curator.getExperience()));
            } else if (curator.getId() == cId2) {
                assertAll(() -> assertEquals("c2name", curator.getName()),
                        () -> assertEquals("c2email", curator.getEmail()),
                        () -> assertEquals(2, curator.getExperience()));
            } else {
                throw new AssertionError("Unexpected curator id: " + curators);
            }
        }
    }

    @Test
    public void testPostCurator_returnsCreatedWithNewCurator() throws SQLException {
        assertEquals(0, jdbc.countOfCurators());

        CuratorDto candidate = new CuratorDto(null, "name", "email", 1);
        ResponseEntity<CuratorDto> response = rt.exchange(
                RequestEntity.post("/curators").body(candidate), CuratorDto.class);

        CuratorDto curator = response.getBody();
        assertAll(() -> assertEquals(HttpServletResponse.SC_CREATED, response.getStatusCode().value()),
                () -> assertTrue(response.hasBody()),
                () -> assertNotNull(curator.getId()),
                () -> assertEquals(candidate.getName(), curator.getName()),
                () -> assertEquals(candidate.getEmail(), curator.getEmail()),
                () -> assertEquals(candidate.getExperience(), curator.getExperience()),
                () -> assertEquals(1, jdbc.countOfCurators()),
                () -> assertTrue(jdbc.containsCurator(curator.getId(), curator.getName(), curator.getEmail(), curator.getExperience(), null)));
    }

    @Test
    public void testPostCurator_whenInvalidCandidate_thenReturnsBadRequest() throws SQLException {
        assertEquals(0, jdbc.countOfCurators());

        CuratorDto candidate = new CuratorDto(null, null, null, null);
        ResponseEntity<CuratorDto> response = rt.exchange(
                RequestEntity.post("/curators").body(candidate), CuratorDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfCurators()));
    }

    @Test
    public void testGetCurator_returnsOkWithCurator() throws SQLException {
        int cId = jdbc.insertCurator("cname", "cemail", 1, null);
        assertEquals(1, jdbc.countOfCurators());

        ResponseEntity<CuratorDto> response = rt.exchange(RequestEntity.get("/curators/"+cId).build(), CuratorDto.class);

        CuratorDto curator = response.getBody();
        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(cId, curator.getId()),
                () -> assertEquals("cname", curator.getName()),
                () -> assertEquals("cemail", curator.getEmail()),
                () -> assertEquals(1, curator.getExperience()),
                () -> assertEquals(1, jdbc.countOfCurators()));
    }

    @Test
    public void testGetCurator_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfCurators());

        int anyCuratorId = 123;
        ResponseEntity<CuratorDto> response = rt.exchange(
                RequestEntity.get("/curators/"+anyCuratorId).build(), CuratorDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfCurators()));
    }

    @Test
    public void testPutCurator_returnsOkWithUpdatedCurator() throws SQLException {
        int cId = jdbc.insertCurator("cname", "cemail", 1, null);
        assertEquals(1, jdbc.countOfCurators());

        CuratorDto candidate = new CuratorDto(null, "updated_name", "updated_email", 2);
        ResponseEntity<CuratorDto> response = rt.exchange(
                RequestEntity.put("/curators/"+cId).body(candidate), CuratorDto.class);

        CuratorDto curator = response.getBody();
        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(cId, curator.getId()),
                () -> assertEquals(candidate.getName(), curator.getName()),
                () -> assertEquals(candidate.getEmail(), curator.getEmail()),
                () -> assertEquals(candidate.getExperience(), curator.getExperience()),
                () -> assertTrue(jdbc.containsCurator(curator.getId(), curator.getName(), curator.getEmail(), curator.getExperience(), null)),
                () -> assertEquals(1, jdbc.countOfCurators()));
    }

    @Test
    public void testPutCurator_whenInvalidCandidate_thenReturnsBadRequest() throws SQLException {
        int cId = jdbc.insertCurator("cname", "cemail", 1, null);
        assertEquals(1, jdbc.countOfCurators());

        CuratorDto candidate = new CuratorDto(null, null, null, null);
        ResponseEntity<CuratorDto> response = rt.exchange(
                RequestEntity.put("/curators/"+cId).body(candidate), CuratorDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusCode().value()),
                () -> assertTrue(jdbc.containsCurator(cId, "cname", "cemail", 1, null)),
                () -> assertEquals(1, jdbc.countOfCurators()));
    }

    @Test
    public void testPutCurator_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfCurators());

        int anyCuratorId = 123;
        CuratorDto candidate = new CuratorDto(null, "updated_name", "updated_email", 2);
        ResponseEntity<CuratorDto> response = rt.exchange(
                RequestEntity.put("/curators/"+anyCuratorId).body(candidate), CuratorDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfCurators()));
    }

    @Test
    public void testDeleteCurator_returnsOk() throws SQLException {
        int cId = jdbc.insertCurator("cname", "cemail", 1, null);
        assertEquals(1, jdbc.countOfCurators());

        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.delete("/curators/"+cId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfCurators()));
    }

    @Test
    public void testDeleteCurator_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfCurators());

        int anyCuratorId = 123;
        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.delete("/curators/"+anyCuratorId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()),
                () -> assertEquals(0, jdbc.countOfCurators()));
    }

    @Test
    public void testGetCuratorGroup_returnsOkWithGroup() throws SQLException {
        LocalDate now = LocalDate.now();
        int gId = jdbc.insertGroup("gname", now);
        int cId = jdbc.insertCurator("cname", "cemail", 1, gId);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(1, jdbc.countOfCurators());

        ResponseEntity<GroupDto> response = rt.exchange(
                RequestEntity.get("/curators/"+cId+"/group").build(), GroupDto.class);

        GroupDto group = response.getBody();
        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertEquals(gId, group.getId()),
                () -> assertEquals("gname", group.getName()),
                () -> assertEquals(now, group.getGraduationDate()),
                () -> assertEquals(1, jdbc.countOfCurators()),
                () -> assertEquals(1, jdbc.countOfGroups()));
    }

    @Test
    public void testGetCuratorGroup_whenCuratorWithoutGroup_thenReturnsNoContent() throws SQLException {
        int cId = jdbc.insertCurator("cname", "cemail", 1, null);
        assertEquals(1, jdbc.countOfCurators());
        assertEquals(0, jdbc.countOfGroups());

        ResponseEntity<GroupDto> response = rt.exchange(
                RequestEntity.get("/curators/"+cId+"/group").build(), GroupDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatusCode().value()),
                () -> assertEquals(1, jdbc.countOfCurators()),
                () -> assertEquals(0, jdbc.countOfGroups()));
    }

    @Test
    public void testGetCuratorGroup_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        int anyCuratorId = 123;
        ResponseEntity<GroupDto> response = rt.exchange(
                RequestEntity.get("/curators/"+anyCuratorId+"/group").build(), GroupDto.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }

    @Test
    public void testDeleteCuratorGroup_returnsOk() throws SQLException {
        LocalDate graduationDate = LocalDate.now();
        int gId = jdbc.insertGroup("gname", graduationDate);
        int cId = jdbc.insertCurator("cname", "cemail", 1, gId);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(1, jdbc.countOfCurators());

        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.delete("/curators/"+gId+"/group").build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertTrue(jdbc.containsGroup(gId, "gname", graduationDate)),
                () -> assertTrue(jdbc.containsCurator(cId, "cname", "cemail", 1, null)),
                () -> assertEquals(1, jdbc.countOfCurators()),
                () -> assertEquals(1, jdbc.countOfGroups()));
    }

    @Test
    public void testDeleteCuratorGroup_whenNotExistingId_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());

        int anyCuratorId = 123;
        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.delete("/curators/"+anyCuratorId+"/group").build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }

    @Test
    public void testPutCuratorGroup_returnsOk() throws SQLException {
        LocalDate graduationDate = LocalDate.now();
        int gId = jdbc.insertGroup("gname", graduationDate);
        int cId = jdbc.insertCurator("cname", "cemail", 1, null);
        assertEquals(1, jdbc.countOfGroups());
        assertEquals(1, jdbc.countOfCurators());

        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.put("/curators/"+cId+"/group/"+gId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value()),
                () -> assertTrue(jdbc.containsGroup(gId, "gname", graduationDate)),
                () -> assertTrue(jdbc.containsCurator(cId, "cname", "cemail", 1, gId)),
                () -> assertEquals(1, jdbc.countOfCurators()),
                () -> assertEquals(1, jdbc.countOfGroups()));
    }

    @Test
    public void testPutCuratorGroup_whenNotExistingIds_thenReturnsNotFound() throws SQLException {
        assertEquals(0, jdbc.countOfGroups());
        assertEquals(0, jdbc.countOfCurators());

        int anyGroupId = 123;
        int anyCuratorId = 321;
        ResponseEntity<Void> response = rt.exchange(
                RequestEntity.put("/curators/"+anyCuratorId+"/group/"+anyGroupId).build(), Void.class);

        assertAll(() -> assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value()));
    }
}
