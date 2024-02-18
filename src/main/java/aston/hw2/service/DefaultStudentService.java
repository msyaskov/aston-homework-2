package aston.hw2.service;

import aston.hw2.entity.Group;
import aston.hw2.entity.Student;
import aston.hw2.repository.GroupRepository;
import aston.hw2.repository.StudentRepository;
import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
public class DefaultStudentService implements StudentService {

    private final StudentRepository studentRepository;
    private final GroupRepository groupRepository;

    @Override
    public void assignGroup(int studentId, int groupId) {
        Student student = getStudent(studentId);
        Group group = student.getGroup();
        if (group != null && Objects.equals(group.getId(), groupId)) {
            return;
        }

        group = getGroup(groupId);
        student.setGroup(group);
        studentRepository.save(student);
    }

    @Override
    public Student createStudentByCandidate(Student candidate) {
        checkCandidate(candidate);

        Student student = new Student();
        student.setName(candidate.getName());
        student.setDateOfBirth(candidate.getDateOfBirth());

        return studentRepository.save(student);
    }

    @Override
    public Stream<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Student getStudent(int studentId) {
        Student student = studentRepository.findById(studentId);
        if (student == null) {
            throwStudentNotFoundException(studentId);
        }

        return student;
    }

    @Override
    public void removeStudent(int studentId) {
        Student student = getStudent(studentId); // throws StudentNotFoundException
        studentRepository.removeById(student.getId());
    }

    @Override
    public void unassignGroup(int studentId) {
        Student student = getStudent(studentId);
        if (student.getGroup() == null) {
            return;
        }

        student.setGroup(null);
        studentRepository.save(student);
    }

    @Override
    public Student updateStudent(int studentId, Student candidate) {
        checkCandidate(candidate);

        Student student = getStudent(studentId);

        boolean modified = false;
        if (!Objects.equals(student.getName(), candidate.getName())) {
            student.setName(candidate.getName());
            modified = true;
        }
        if (!Objects.equals(student.getDateOfBirth(), candidate.getDateOfBirth())) {
            student.setDateOfBirth(candidate.getDateOfBirth());
            modified = true;
        }
        if (modified) {
            return studentRepository.save(student);
        }

        return student;
    }

    private void checkCandidate(Student candidate) {
        if (candidate == null) {
            throw new InvalidCandidateException("Candidate is null");
        } else if (candidate.getName() == null) {
            throw new InvalidCandidateException("Candidate's name is null");
        } else if (candidate.getDateOfBirth() == null) {
            throw new InvalidCandidateException("Candidate's dateOfBirth is null");
        }
    }

    private Group getGroup(int groupId) {
        Group group = groupRepository.findById(groupId);
        if (group == null) {
            throwGroupNotFoundException(groupId);
        }

        return group;
    }

    private void throwGroupNotFoundException(int groupId) {
        throw new GroupNotFoundException("Group with id=%d not found".formatted(groupId));
    }

    private void throwStudentNotFoundException(int studentId) {
        throw new StudentNotFoundException("Student with id=%d not found".formatted(studentId));
    }
}
