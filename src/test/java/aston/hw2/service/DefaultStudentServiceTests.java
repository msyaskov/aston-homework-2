package aston.hw2.service;

import aston.hw2.entity.Group;
import aston.hw2.entity.Student;
import aston.hw2.repository.GroupRepository;
import aston.hw2.repository.StudentRepository;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultStudentServiceTests {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private DefaultStudentService studentService;

    @Test
    public void testAssignGroup() {
        Group group = new Group(1, "gname", LocalDate.now(), null, List.of());
        Student student = new Student(1, "sname", LocalDate.now(), null);
        when(groupRepository.findById(group.getId())).thenReturn(clone(group));
        when(studentRepository.findById(student.getId())).thenReturn(clone(student));

        studentService.assignGroup(student.getId(), group.getId());

        ArgumentCaptor<Student> saveCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(saveCaptor.capture());
        Student saved = saveCaptor.getValue();

        assertAll(() -> assertNotNull(saved),
                () -> assertEquals(student.getId(), saved.getId()),
                () -> assertEquals(student.getName(), saved.getName()),
                () -> assertEquals(student.getDateOfBirth(), saved.getDateOfBirth()),
                () -> assertEquals(group.getId(), saved.getGroup().getId()),
                () -> assertEquals(group.getName(), saved.getGroup().getName()),
                () -> assertEquals(group.getGraduationDate(), saved.getGroup().getGraduationDate()));
    }

    @Test
    public void testAssignGroup_whenAlreadyAssign_thenDoNothing() {
        Group group = new Group(1, "gname", LocalDate.now(), null, List.of());
        Student student = new Student(1, "sname", LocalDate.now(), group);
        when(studentRepository.findById(student.getId())).thenReturn(clone(student));

        studentService.assignGroup(student.getId(), group.getId());
        verify(studentRepository, never()).save(any());
    }

    @Test
    public void testCreateStudentByCandidate() {
        Student candidate = new Student(null, "name", LocalDate.now(), null);

        studentService.createStudentByCandidate(candidate);

        ArgumentCaptor<Student> saveCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(saveCaptor.capture());
        Student saved = saveCaptor.getValue();

        assertAll(() -> assertNull(saved.getId()),
                () -> assertEquals(candidate.getName(), saved.getName()),
                () -> assertEquals(candidate.getDateOfBirth(), saved.getDateOfBirth()));
    }

    @Test
    public void testCreateStudentByCandidate_whenInvalidCandidate_throwInvalidCandidateException() {
        Student candidate = new Student();

        assertThrows(InvalidCandidateException.class,
                () -> studentService.createStudentByCandidate(candidate));

        verify(studentRepository, never()).save(any());
    }

    @Test
    public void testGetAllStudents() {
        Student student1 = new Student(1, "s1name", LocalDate.now(), null);
        Student student2 = new Student(2, "s2name", LocalDate.now(), null);
        when(studentRepository.findAll()).thenReturn(Stream.of(student1, student2));

        List<Student> students = studentService.getAllStudents().toList();
        assertAll(() -> assertEquals(2, students.size()),
                () -> assertEquals(Arrays.asList(student1, student2), students));
    }

    @Test
    public void testGetStudent_whenExistingId_returnStudent() {
        Student student = new Student(1, "sname", LocalDate.now(), null);
        when(studentRepository.findById(student.getId())).thenReturn(student);

        Student found = studentService.getStudent(student.getId());
        assertEquals(student, found);
    }

    @Test
    public void testGetStudent_whenNotExistingId_throwsStudentNotFoundException() {
        when(studentRepository.findById(anyInt())).thenReturn(null);

        Assertions.assertThrows(StudentNotFoundException.class,
                () -> studentService.getStudent(anyInt()));
    }

    @Test
    public void testRemoveStudent() {
        Student student = new Student(1, "name", LocalDate.now(), null);
        when(studentRepository.findById(student.getId())).thenReturn(student);

        studentService.removeStudent(student.getId());

        ArgumentCaptor<Integer> argRemoveByStudentId = ArgumentCaptor.forClass(Integer.TYPE);
        verify(studentRepository).removeById(argRemoveByStudentId.capture());
        assertEquals(student.getId(), argRemoveByStudentId.getValue());
    }

    @Test
    public void testRemoveStudent_whenNotExistingId_throwsStudentNotFoundException() {
        when(studentRepository.findById(anyInt())).thenReturn(null);

        Assertions.assertThrows(StudentNotFoundException.class,
                () -> studentService.removeStudent(anyInt()));
    }

    @Test
    public void testUnassignGroup() {
        Group group = new Group(1, "gname", LocalDate.now(), null, List.of());
        Student student = new Student(1, "name", LocalDate.now(), group);
        when(studentRepository.findById(student.getId())).thenReturn(clone(student));

        studentService.unassignGroup(group.getId());

        ArgumentCaptor<Student> saveCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(saveCaptor.capture());
        Student saved = saveCaptor.getValue();
        assertAll(() -> assertNotNull(saved),
                () -> assertNull(saved.getGroup()));
    }

    @Test
    public void testUnassignGroup_whenNoGroup_thenDoNothing() {
        Student student = new Student(1, "name", LocalDate.now(), null);
        when(studentRepository.findById(student.getId())).thenReturn(clone(student));

        studentService.unassignGroup(student.getId());

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    public void testUpdateStudentByCandidate() {
        Student student = new Student(1, "name", LocalDate.now(), null);
        Student candidate = new Student(null, "updated_name", LocalDate.now(), null);
        when(studentRepository.findById(student.getId())).thenReturn(clone(student));

        studentService.updateStudent(student.getId(), candidate);

        ArgumentCaptor<Student> saveCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(saveCaptor.capture());
        Student saved = saveCaptor.getValue();

        assertAll(() -> assertEquals(student.getId(), saved.getId()),
                () -> assertEquals(candidate.getName(), saved.getName()),
                () -> assertEquals(candidate.getDateOfBirth(), saved.getDateOfBirth()));
    }

    @Test
    public void testUpdateStudentByCandidate_whenInvalidCandidate_throwInvalidCandidateException() {
        Student candidate = new Student();

        assertThrows(InvalidCandidateException.class,
                () -> studentService.updateStudent(1, candidate));

        verify(studentRepository, never()).save(any());
    }

    private Group clone(Group group) {
        if (group == null) {
            return null;
        }

        return new Group(group.getId(), group.getName(), group.getGraduationDate(),
                group.getCurator(), new ArrayList<>(group.getStudents()));
    }

    private Student clone(Student student) {
        if (student == null) {
            return null;
        }

        return new Student(student.getId(), student.getName(), student.getDateOfBirth(), student.getGroup());
    }
}