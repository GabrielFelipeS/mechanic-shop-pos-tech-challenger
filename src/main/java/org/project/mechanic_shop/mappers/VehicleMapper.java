package org.project.mechanic_shop.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.mechanic_shop.dto.vehicle_dto.VehicleDto;
import org.project.mechanic_shop.dto.vehicle_dto.VehicleManDto;
import org.project.mechanic_shop.dto.vehicle_dto.VehicleShortDto;
import org.project.mechanic_shop.models.Vehicle;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "owner", ignore = true)
    Vehicle toEntity(VehicleManDto vehicleManDto);

    @Mapping(target = "ownerName", source = "owner.name")
    VehicleShortDto toShortDto(Vehicle vehicle);

    VehicleDto toDto(Vehicle vehicle);
}