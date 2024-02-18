package aston.h2.service;

import aston.h2.entity.Curator;
import aston.h2.entity.Group;
import aston.h2.repository.CuratorRepository;
import aston.h2.repository.GroupRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCuratorServiceTests {

    @Mock
    private CuratorRepository curatorRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private DefaultCuratorService curatorService;

    @Test
    public void testAssignGroup() {
        Group group = new Group(1, "gname", LocalDate.now(), null, List.of());
        Curator curator = new Curator(1, "cname", "cemail", 1, null);

        when(groupRepository.findById(group.getId())).thenReturn(clone(group));
        when(curatorRepository.findById(curator.getId())).thenReturn(clone(curator));

        curatorService.assignGroup(curator.getId(), group.getId());

        ArgumentCaptor<Curator> saveCaptor = ArgumentCaptor.forClass(Curator.class);
        verify(curatorRepository).save(saveCaptor.capture());
        Curator saved = saveCaptor.getValue();

        assertAll(() -> assertNotNull(saved),
                () -> assertEquals(curator.getId(), saved.getId()),
                () -> assertEquals(curator.getName(), saved.getName()),
                () -> assertEquals(curator.getEmail(), saved.getEmail()),
                () -> assertEquals(curator.getExperience(), saved.getExperience()),
                () -> assertEquals(group.getId(), saved.getGroup().getId()),
                () -> assertEquals(group.getName(), saved.getGroup().getName()),
                () -> assertEquals(group.getGraduationDate(), saved.getGroup().getGraduationDate()));
    }

    @Test
    public void testAssignGroup_whenAlreadyAssign_thenDoNothing() {
        Group group = new Group(1, "gname", LocalDate.now(), null, List.of());
        Curator curator = new Curator(1, "cname", "cemail", 1, group);
        when(curatorRepository.findById(curator.getId())).thenReturn(clone(curator));

        curatorService.assignGroup(curator.getId(), group.getId());
        verify(curatorRepository, never()).save(any());
    }

    @Test
    public void testCreateCuratorByCandidate() {
        Curator candidate = new Curator(null, "name", "email", 1, null);

        curatorService.createCuratorByCandidate(candidate);

        ArgumentCaptor<Curator> saveCaptor = ArgumentCaptor.forClass(Curator.class);
        verify(curatorRepository).save(saveCaptor.capture());
        Curator saved = saveCaptor.getValue();

        assertAll(() -> assertNull(saved.getId()),
                () -> assertEquals(candidate.getName(), saved.getName()),
                () -> assertEquals(candidate.getEmail(), saved.getEmail()),
                () -> assertEquals(candidate.getExperience(), saved.getExperience()));
    }

    @Test
    public void testCreateCuratorByCandidate_whenInvalidCandidate_throwInvalidCandidateException() {
        Curator candidate = new Curator();

        assertThrows(InvalidCandidateException.class,
                () -> curatorService.createCuratorByCandidate(candidate));

        verify(curatorRepository, never()).save(any());
    }

    @Test
    public void testGetAllCurators() {
        Curator curator1 = new Curator(1, "c1name", "c1email", 1, null);
        Curator curator2 = new Curator(2, "c2name", "c2email", 2, null);
        when(curatorRepository.findAll()).thenReturn(Stream.of(curator1, curator2));

        List<Curator> curators = curatorService.getAllCurators().toList();
        assertAll(() -> assertEquals(2, curators.size()),
                () -> assertEquals(Arrays.asList(curator1, curator2), curators));
    }

    @Test
    public void testGetCurator_whenExistingId_returnCurator() {
        Curator curator = new Curator(1, "name", "email", 1, null);
        when(curatorRepository.findById(curator.getId())).thenReturn(curator);

        Curator found = curatorService.getCurator(curator.getId());
        assertEquals(curator, found);
    }

    @Test
    public void testGetCurator_whenNotExistingId_throwsCuratorNotFoundException() {
        when(curatorRepository.findById(anyInt())).thenReturn(null);

        Assertions.assertThrows(CuratorNotFoundException.class,
                () -> curatorService.getCurator(anyInt()));
    }

    @Test
    public void testRemoveCurator() {
        Curator curator = new Curator(1, "name", "email", 1, null);
        when(curatorRepository.findById(curator.getId())).thenReturn(curator);

        curatorService.removeCurator(curator.getId());

        ArgumentCaptor<Integer> argRemoveByCuratorId = ArgumentCaptor.forClass(Integer.TYPE);
        verify(curatorRepository).removeById(argRemoveByCuratorId.capture());
        assertEquals(curator.getId(), argRemoveByCuratorId.getValue());
    }

    @Test
    public void testRemoveCurator_whenNotExistingId_throwsCuratorNotFoundException() {
        when(curatorRepository.findById(anyInt())).thenReturn(null);

        Assertions.assertThrows(CuratorNotFoundException.class,
                () -> curatorService.removeCurator(anyInt()));
    }

    @Test
    public void testUnassignGroup() {
        Group group = new Group(1, "gname", LocalDate.now(), null, List.of());
        Curator curator = new Curator(1, "cname", "cemail", 1, group);
        group.setCurator(curator);
        when(curatorRepository.findById(curator.getId())).thenReturn(clone(curator));

        curatorService.unassignGroup(group.getId());

        ArgumentCaptor<Curator> saveCaptor = ArgumentCaptor.forClass(Curator.class);
        verify(curatorRepository).save(saveCaptor.capture());
        Curator saved = saveCaptor.getValue();
        assertAll(() -> assertNotNull(saved),
                () -> assertNull(saved.getGroup()));
    }

    @Test
    public void testUnassignGroup_whenNoGroup_thenDoNothing() {
        Curator curator = new Curator(1, "cname", "cemail", 1, null);
        when(curatorRepository.findById(curator.getId())).thenReturn(clone(curator));

        curatorService.unassignGroup(curator.getId());

        verify(curatorRepository, never()).save(any(Curator.class));
    }

    @Test
    public void testUpdateCuratorByCandidate() {
        Curator curator = new Curator(1, "name", "email", 1, null);
        Curator candidate = new Curator(null, "updated_name", "updated_email", 2, null);
        when(curatorRepository.findById(curator.getId())).thenReturn(clone(curator));

        curatorService.updateCurator(curator.getId(), candidate);

        ArgumentCaptor<Curator> saveCaptor = ArgumentCaptor.forClass(Curator.class);
        verify(curatorRepository).save(saveCaptor.capture());
        Curator saved = saveCaptor.getValue();

        assertAll(() -> assertEquals(curator.getId(), saved.getId()),
                () -> assertEquals(candidate.getName(), saved.getName()),
                () -> assertEquals(candidate.getEmail(), saved.getEmail()),
                () -> assertEquals(candidate.getExperience(), saved.getExperience()));
    }

    @Test
    public void testUpdateCuratorByCandidate_whenInvalidCandidate_throwInvalidCandidateException() {
        Curator candidate = new Curator();

        assertThrows(InvalidCandidateException.class,
                () -> curatorService.updateCurator(1, candidate));

        verify(curatorRepository, never()).save(any());
    }

    private Group clone(Group group) {
        if (group == null) {
            return null;
        }

        return new Group(group.getId(), group.getName(), group.getGraduationDate(),
                group.getCurator(), new ArrayList<>(group.getStudents()));
    }

    private Curator clone(Curator curator) {
        if (curator == null) {
            return null;
        }

        return new Curator(curator.getId(), curator.getName(), curator.getEmail(),
                curator.getExperience(), curator.getGroup());
    }
}