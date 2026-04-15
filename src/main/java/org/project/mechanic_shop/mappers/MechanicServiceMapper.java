package org.project.mechanic_shop.mappers;

import org.mapstruct.Mapper;
import org.project.mechanic_shop.dto.mechanic_service_dto.MechanicServiceDto;
import org.project.mechanic_shop.dto.mechanic_service_dto.MechanicServiceManDto;
import org.project.mechanic_shop.dto.mechanic_service_dto.MechanicServiceShortDto;
import org.project.mechanic_shop.models.MechanicService;

@Mapper(componentModel = "spring")
public interface MechanicServiceMapper {

    MechanicService toEntity(MechanicServiceManDto dto);
    MechanicServiceShortDto toShortDto(MechanicService entity);
    MechanicServiceDto toDto(MechanicService entity);
}