package org.project.mechanic_shop.application.mappers;

import org.mapstruct.Mapper;
import org.project.mechanic_shop.domain.dto.mechanic_service_dto.MechanicServiceDto;
import org.project.mechanic_shop.domain.dto.mechanic_service_dto.MechanicServiceManDto;
import org.project.mechanic_shop.domain.dto.mechanic_service_dto.MechanicServiceShortDto;
import org.project.mechanic_shop.domain.entities.mechanic_service.MechanicService;

@Mapper(componentModel = "spring")
public interface MechanicServiceMapper {
	MechanicService toEntity(MechanicServiceManDto dto);
	MechanicServiceShortDto toShortDto(MechanicService entity);
	MechanicServiceDto toDto(MechanicService entity);
}
