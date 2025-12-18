package sk.lorman.pokus.ewg.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.lorman.pokus.ewg.domain.Kamion;
import sk.lorman.pokus.ewg.dto.KamionDto;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface KamionMapper {

    KamionDto toDto(Kamion entity);

    List<KamionDto> toDtoList(List<Kamion> entities);

    @Mapping(target = "id", ignore = true)
    Kamion toEntity(KamionDto dto);
}
