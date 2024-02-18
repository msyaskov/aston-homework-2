package aston.h2.mapper;

import aston.h2.dto.CuratorDto;
import aston.h2.entity.Curator;

public class CuratorMapper implements Mapper<Curator, CuratorDto> {

    @Override
    public CuratorDto map(Curator curator) {
        if (curator == null) {
            return null;
        }

        return new CuratorDto(curator.getId(), curator.getName(), curator.getEmail(), curator.getExperience());
    }

    @Override
    public Curator reverseMap(CuratorDto curatorDto) {
        if (curatorDto == null) {
            return null;
        }

        Curator curator = new Curator();
        curator.setId(curatorDto.getId());
        curator.setName(curatorDto.getName());
        curator.setEmail(curatorDto.getEmail());
        curator.setExperience(curatorDto.getExperience());

        return curator;
    }
}
