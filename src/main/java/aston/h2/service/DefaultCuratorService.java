package aston.h2.service;

import aston.h2.entity.Curator;
import aston.h2.entity.Group;
import aston.h2.entity.Student;
import aston.h2.repository.CuratorRepository;
import aston.h2.repository.GroupRepository;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class DefaultCuratorService implements CuratorService {

    private final CuratorRepository curatorRepository;
    private final GroupRepository groupRepository;

    @Override
    public void assignGroup(int curatorId, int groupId) {
        Curator curator = getCurator(curatorId);
        Group group = curator.getGroup();
        if (group != null && Objects.equals(group.getId(), groupId)) {
            return;
        }

        group = getGroup(groupId);
        curator.setGroup(group);
        curatorRepository.save(curator);
    }

    @Override
    public Curator createCuratorByCandidate(Curator candidate) {
        checkCandidate(candidate);

        Curator curator = new Curator();
        curator.setName(candidate.getName());
        curator.setEmail(candidate.getEmail());
        curator.setExperience(candidate.getExperience());

        return curatorRepository.save(curator);
    }

    @Override
    public Stream<Curator> getAllCurators() {
        return curatorRepository.findAll();
    }

    @Override
    public Curator getCurator(int curatorId) {
        Curator curator = curatorRepository.findById(curatorId);
        if (curator == null) {
            throwCuratorNotFoundException(curatorId);
        }

        return curator;
    }

    @Override
    public void removeCurator(int curatorId) {
        Curator curator = getCurator(curatorId);
        curatorRepository.removeById(curator.getId());
    }

    @Override
    public void unassignGroup(int curatorId) {
        Curator curator = getCurator(curatorId);
        if (curator.getGroup() == null) {
            return;
        }

        curator.setGroup(null);
        curatorRepository.save(curator);
    }

    @Override
    public Curator updateCurator(int curatorId, Curator candidate) {
        checkCandidate(candidate);

        Curator curator = getCurator(curatorId);

        boolean modified = false;
        if (!Objects.equals(curator.getName(), candidate.getName())) {
            curator.setName(candidate.getName());
            modified = true;
        }
        if (!Objects.equals(curator.getEmail(), candidate.getEmail())) {
            curator.setEmail(candidate.getEmail());
            modified = true;
        }
        if (curator.getExperience() != candidate.getExperience()) {
            curator.setExperience(candidate.getExperience());
            modified = true;
        }
        if (modified) {
            curatorRepository.save(curator);
        }

        return curator;
    }

    private void checkCandidate(Curator candidate) {
        if (candidate == null) {
            throw new InvalidCandidateException("Candidate is null");
        } else if (candidate.getName() == null) {
            throw new InvalidCandidateException("Candidate's name is null");
        } else if (candidate.getEmail() == null) {
            throw new InvalidCandidateException("Candidate's email is null");
        } else if (candidate.getExperience() < 0) {
            throw new InvalidCandidateException("Candidate's experience must be positive");
        }
    }

    private Group getGroup(int groupId) {
        Group group = groupRepository.findById(groupId);
        if (group == null) {
            throwGroupNotFoundException(groupId);
        }

        return group;
    }

    private void throwCuratorNotFoundException(int curatorId) {
        throw new CuratorNotFoundException("Curator with id=%d not found".formatted(curatorId));
    }

    private void throwGroupNotFoundException(int groupId) {
        throw new GroupNotFoundException("Group with id=%d not found".formatted(groupId));
    }
}
