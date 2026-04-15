package org.project.mechanic_shop.mappers;

import org.mapstruct.Mapper;
import org.project.mechanic_shop.dto.part_dto.PartDto;
import org.project.mechanic_shop.dto.part_dto.PartManDto;
import org.project.mechanic_shop.dto.part_dto.PartShortDto;
import org.project.mechanic_shop.models.Part;

@Mapper(componentModel = "spring")
public interface PartMapper {

    Part toEntity(PartManDto partManDto);

    PartShortDto toShortDto(Part part);

    PartDto toDto(Part part);
}