package aston.h2.mapper;

import aston.h2.dto.GroupDto;
import aston.h2.entity.Group;

public class GroupMapper implements Mapper<Group, GroupDto> {

    @Override
    public GroupDto map(Group group) {
        if (group == null) {
            return null;
        }

        return new GroupDto(group.getId(), group.getName(), group.getGraduationDate());
    }

    @Override
    public Group reverseMap(GroupDto groupDto) {
        if (groupDto == null) {
            return null;
        }

        Group group = new Group();
        group.setId(groupDto.getId());
        group.setName(groupDto.getName());
        group.setGraduationDate(groupDto.getGraduationDate());

        return group;
    }
}
