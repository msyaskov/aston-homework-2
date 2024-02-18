package aston.h2.service;

import aston.h2.entity.Curator;
import aston.h2.entity.Group;
import aston.h2.entity.Student;
import aston.h2.repository.CuratorRepository;
import aston.h2.repository.GroupRepository;
import aston.h2.repository.StudentRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGroupServiceTests {

    @Mock
    private CuratorRepository curatorRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private DefaultGroupService groupService;

    @Test
    public void testAssignCurator() {
        Group group = new Group(1, "gname", LocalDate.now(), null, List.of());
        Curator curator = new Curator(1, "cname", "cemail", 1, null);

        when(groupRepository.findById(group.getId())).thenReturn(clone(group));
        when(curatorRepository.findById(curator.getId())).thenReturn(clone(curator));

        groupService.assignCurator(group.getId(), curator.getId());

        ArgumentCaptor<Group> saveCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(saveCaptor.capture());
        Group saved = saveCaptor.getValue();

        assertAll(() -> assertNotNull(saved),
                () -> assertEquals(group.getId(), saved.getId()),
                () -> assertEquals(group.getName(), saved.getName()),
                () -> assertEquals(group.getGraduationDate(), saved.getGraduationDate()),
                () -> assertEquals(curator.getId(), saved.getCurator().getId()),
                () -> assertEquals(curator.getName(), saved.getCurator().getName()),
                () -> assertEquals(curator.getEmail(), saved.getCurator().getEmail()),
                () -> assertEquals(curator.getExperience(), saved.getCurator().getExperience()));
    }

    @Test
    public void testAssignCurator_whenAlreadyAssign_thenDoNothing() {
        Curator curator = new Curator(1, "cname", "cemail", 1, null);
        Group group = new Group(1, "gname", LocalDate.now(), curator, List.of());
        when(groupRepository.findById(group.getId())).thenReturn(group);

        groupService.assignCurator(group.getId(), curator.getId());
        verify(groupRepository, never()).save(any());
    }

    @Test
    public void testAssignStudent() {
        Group group = new Group(1, "gname", LocalDate.now(), null, new ArrayList<>());
        Student student = new Student(1, "sname", LocalDate.now(), null);

        when(groupRepository.findById(group.getId())).thenReturn(clone(group));
        when(studentRepository.findById(student.getId())).thenReturn(clone(student));

        groupService.assignStudent(group.getId(), student.getId());

        ArgumentCaptor<Group> saveCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(saveCaptor.capture());
        Group saved = saveCaptor.getValue();

        assertAll(() -> assertNotNull(saved),
                () -> assertEquals(group.getId(), saved.getId()),
                () -> assertEquals(group.getName(), saved.getName()),
                () -> assertEquals(group.getGraduationDate(), saved.getGraduationDate()),
                () -> assertEquals(1, saved.getStudents().size()));

        Student assignedStudent = saved.getStudents().iterator().next();
        assertAll(() -> assertEquals(student.getId(), assignedStudent.getId()),
                () -> assertEquals(student.getName(), assignedStudent.getName()),
                () -> assertEquals(student.getDateOfBirth(), assignedStudent.getDateOfBirth()));
    }

    @Test
    public void testAssignStudent_whenAlreadyAssign_thenDoNothing() {
        Student student = new Student(1, "sname", LocalDate.now(), null);
        Group group = new Group(1, "gname", LocalDate.now(), null, Arrays.asList(student));
        when(groupRepository.findById(group.getId())).thenReturn(group);

        groupService.assignStudent(group.getId(), student.getId());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    public void testCreateGroupByCandidate() {
        Group candidate = new Group(null, "name", LocalDate.now(), null, null);
        when(groupRepository.findByName(candidate.getName())).thenReturn(null);

        groupService.createGroupByCandidate(candidate);

        ArgumentCaptor<Group> saveCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(saveCaptor.capture());
        Group saved = saveCaptor.getValue();

        assertAll(() -> assertNull(saved.getId()),
                () -> assertEquals(candidate.getName(), saved.getName()),
                () -> assertEquals(candidate.getGraduationDate(), saved.getGraduationDate()));
    }

    @Test
    public void testCreateGroupByCandidate_whenInvalidCandidate_throwInvalidCandidateException() {
        Group candidate = new Group(null, "occupied", LocalDate.now(), null, null);
        Group group = new Group(1, candidate.getName(), LocalDate.now(), null, List.of());
        when(groupRepository.findByName(candidate.getName())).thenReturn(group);

        assertThrows(InvalidCandidateException.class, () -> groupService.createGroupByCandidate(candidate));

        verify(groupRepository, never()).save(any());
    }

    @Test
    public void testGetAllGroups() {
        Group group1 = new Group(1, "g1name", LocalDate.now(), null, List.of());
        Group group2 = new Group(2, "g2name", LocalDate.now(), null, List.of());
        when(groupRepository.findAll()).thenReturn(Stream.of(group1, group2));

        List<Group> groups = groupService.getAllGroups().toList();
        assertAll(() -> assertEquals(2, groups.size()),
                () -> assertEquals(Arrays.asList(group1, group2), groups));
    }

    @Test
    public void testGetGroup_whenExistingId_returnGroup() {
        int groupId = 1;
        Group group = new Group(groupId, "name", LocalDate.now(), null, List.of());
        when(groupRepository.findById(groupId)).thenReturn(group);

        Group found = groupService.getGroup(group.getId());
        assertEquals(group, found);
    }

    @Test
    public void testGetGroup_whenNotExistingId_throwsGroupNotFoundException() {
        int groupId = 1;
        when(groupRepository.findById(groupId)).thenReturn(null);

        Assertions.assertThrows(GroupNotFoundException.class,
                () -> groupService.getGroup(groupId));
    }

    @Test
    public void testRemoveGroup() {
        Group group = new Group(1, "name", LocalDate.now(), null, null);

        when(groupRepository.findById(group.getId())).thenReturn(group);

        groupService.removeGroup(group.getId());

        ArgumentCaptor<Integer> argRemoveByGroupId = ArgumentCaptor.forClass(Integer.TYPE);
        verify(groupRepository).removeById(argRemoveByGroupId.capture());
        assertEquals(group.getId(), argRemoveByGroupId.getValue());
    }

    @Test
    public void testRemoveGroup_whenNotExistingId_throwsGroupNotFoundException() {
        when(groupRepository.findById(anyInt())).thenReturn(null);

        Assertions.assertThrows(GroupNotFoundException.class,
                () -> groupService.removeGroup(anyInt()));
    }

    @Test
    public void testUnassignCurator() {
        Curator curator = new Curator(1, "cname", "cemail", 1, null);
        Group group = new Group(1, "gname", LocalDate.now(), curator, List.of());
        curator.setGroup(group);
        when(groupRepository.findById(group.getId())).thenReturn(clone(group));

        groupService.unassignCurator(group.getId());

        ArgumentCaptor<Curator> saveCaptor = ArgumentCaptor.forClass(Curator.class);
        verify(curatorRepository).save(saveCaptor.capture());
        Curator saved = saveCaptor.getValue();
        assertAll(() -> assertNotNull(saved),
                () -> assertNull(saved.getGroup()));
    }

    @Test
    public void testUnassignCurator_whenNoCurator_thenDoNothing() {
        Group group = new Group(1, "gname", LocalDate.now(), null, List.of());
        when(groupRepository.findById(group.getId())).thenReturn(group);

        groupService.unassignCurator(group.getId());

        verify(curatorRepository, never()).save(any(Curator.class));
    }

    @Test
    public void testUnassignStudent() {
        Student student = new Student(1, "sname", LocalDate.now(), null);
        Group group = new Group(1, "gname", LocalDate.now(), null, List.of(student));
        student.setGroup(group);
        when(groupRepository.findById(group.getId())).thenReturn(clone(group));

        groupService.unassignStudent(group.getId(), 1);

        ArgumentCaptor<Group> saveCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(saveCaptor.capture());
        Group saved = saveCaptor.getValue();
        assertAll(() -> assertNotNull(saved),
                () -> assertTrue(saved.getStudents().isEmpty()));
    }

    @Test
    public void testUnassignStudent_whenNoStudent_thenDoNothing() {
        Group group = new Group(1, "gname", LocalDate.now(), null, List.of());
        when(groupRepository.findById(group.getId())).thenReturn(group);

        groupService.unassignStudent(group.getId(), anyInt());

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    public void testUpdateGroupByCandidate() {
        Group group = new Group(1, "name", LocalDate.now(), null, null);
        Group candidate = new Group(null, "updated_name", LocalDate.now(), null, null);
        when(groupRepository.findByName(candidate.getName())).thenReturn(null);
        when(groupRepository.findById(group.getId())).thenReturn(group);

        groupService.updateGroupByCandidate(group.getId(), candidate);

        ArgumentCaptor<Group> saveCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(saveCaptor.capture());
        Group saved = saveCaptor.getValue();

        assertAll(() -> assertEquals(group.getId(), saved.getId()),
                () -> assertEquals(candidate.getName(), saved.getName()),
                () -> assertEquals(candidate.getGraduationDate(), saved.getGraduationDate()));
    }

    @Test
    public void testUpdateGroupByCandidate_whenInvalidCandidate_throwInvalidCandidateException() {
        Group candidate = new Group(null, "occupied", LocalDate.now(), null, null);
        Group group = new Group(1, "name", LocalDate.now(), null, List.of());
        Group group2 = new Group(2, candidate.getName(), LocalDate.now(), null, List.of());
        when(groupRepository.findById(group.getId())).thenReturn(group);
        when(groupRepository.findByName(candidate.getName())).thenReturn(group2);

        assertThrows(InvalidCandidateException.class,
                () -> groupService.updateGroupByCandidate(group.getId(), candidate));

        verify(groupRepository, never()).save(any());
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

    private Student clone(Student student) {
        if (student == null) {
            return null;
        }

        return new Student(student.getId(), student.getName(), student.getDateOfBirth(), student.getGroup());
    }
}