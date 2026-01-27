package com.lorman.ref.ee.mapper;

import com.lorman.ref.ee.domain.Kamion;
import com.lorman.ref.ee.dto.KamionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface KamionMapper {

    KamionDto toDto(Kamion entity);

    List<KamionDto> toDtoList(List<Kamion> entities);

    @Mapping(target = "id", ignore = true)
    Kamion toEntity(KamionDto dto);
}
