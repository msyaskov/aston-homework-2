package aston.h2.service;

import aston.h2.entity.Curator;
import aston.h2.entity.Group;
import aston.h2.entity.Student;
import aston.h2.repository.CuratorRepository;
import aston.h2.repository.GroupRepository;
import aston.h2.repository.StudentRepository;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class DefaultGroupService implements GroupService {

    private final GroupRepository groupRepository;
    private final CuratorRepository curatorRepository;
    private final StudentRepository studentRepository;

    @Override
    public void assignCurator(int groupId, int curatorId) {
        Group group = getGroup(groupId);

        Curator curator = group.getCurator();
        if (curator != null && Objects.equals(curator.getId(), curatorId)) {
            return;
        }

        curator = getCurator(curatorId);
        group.setCurator(curator);
        groupRepository.save(group);
    }

    @Override
    public void assignStudent(int groupId, int studentId) {
        Group group = getGroup(groupId);

        for (Student student : group.getStudents()) {
            if (Objects.equals(student.getId(), studentId)) {
                return;
            }
        }

        Student student = getStudent(studentId);
        group.getStudents().add(student);
        groupRepository.save(group);
    }

    @Override
    public Group createGroupByCandidate(Group candidate) {
        checkCandidate(candidate);

        Group group = groupRepository.findByName(candidate.getName());
        if (group != null) {
            throw new InvalidCandidateException(candidateNameOccupiedMessage(candidate.getName()));
        }

        group = new Group();
        group.setName(candidate.getName());
        group.setGraduationDate(candidate.getGraduationDate());

        return groupRepository.save(group);
    }

    @Override
    public Stream<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @Override
    public Group getGroup(int groupId) {
        Group group = groupRepository.findById(groupId);
        if (group == null) {
            throwGroupNotFoundException(groupId);
        }

        return group;
    }

    @Override
    public void removeGroup(int groupId) {
        Group group = getGroup(groupId); // throws GroupNotFoundException
        groupRepository.removeById(group.getId());
    }

    @Override
    public void unassignCurator(int groupId) {
        Group group = getGroup(groupId);
        if (group.getCurator() == null) {
            return;
        }

        Curator curator = group.getCurator();
        curator.setGroup(null);

        curatorRepository.save(curator);
    }

    @Override
    public void unassignStudent(int groupId, int studentId) {
        Group group = getGroup(groupId);

        Iterator<Student> it = group.getStudents().iterator();
        while (it.hasNext()) {
            Student student = it.next();
            if (Objects.equals(student.getId(), studentId)) {
                it.remove();
                groupRepository.save(group);
                return;
            }
        }
    }

    @Override
    public Group updateGroupByCandidate(int groupId, Group candidate) {
        checkCandidate(candidate);

        Group group = getGroup(groupId);
        if (!Objects.equals(group.getName(), candidate.getName()) && groupRepository.findByName(candidate.getName()) != null) {
            throw new InvalidCandidateException(candidateNameOccupiedMessage(candidate.getName()));
        }

        boolean modified = false;
        if (!Objects.equals(group.getName(), candidate.getName())) {
            group.setName(candidate.getName());
            modified = true;
        }
        if (!Objects.equals(group.getGraduationDate(), candidate.getGraduationDate())) {
            group.setGraduationDate(candidate.getGraduationDate());
            modified = true;
        }
        if (modified) {
            return groupRepository.save(group);
        }

        return group;
    }

    private String candidateNameOccupiedMessage(String candidateName) {
        return "Candidate's name is occupied: %s".formatted(candidateName);
    }

    private void checkCandidate(Group candidate) {
        if (candidate == null) {
            throw new InvalidCandidateException("Candidate is null");
        }

        if (candidate.getName() == null) {
            throw new InvalidCandidateException("Candidate's name is null");
        }

        if (candidate.getGraduationDate() == null) {
            throw new InvalidCandidateException("Candidate's graduationDate is null");
        }
    }

    private Curator getCurator(int curatorId) {
        Curator curator = curatorRepository.findById(curatorId);
        if (curator == null) {
            throwCuratorNotFoundException(curatorId);
        }

        return curator;
    }

    private Student getStudent(int studentId) {
        Student student = studentRepository.findById(studentId);
        if (student == null) {
            throwStudentNotFoundException(studentId);
        }

        return student;
    }

    private void throwGroupNotFoundException(int groupId) {
        throw new GroupNotFoundException("Group with id=%d not found".formatted(groupId));
    }

    private void throwCuratorNotFoundException(int curatorId) {
        throw new CuratorNotFoundException("Curator with id=%d not found".formatted(curatorId));
    }

    private void throwStudentNotFoundException(int curatorId) {
        throw new StudentNotFoundException("Student with id=%d not found".formatted(curatorId));
    }
}
