package org.project.mechanic_shop.dto.vehicle_dto;

import org.project.mechanic_shop.dto.user_dto.UserShortDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link org.project.mechanic_shop.models.Vehicle}
 */
public record VehicleDto(
        UUID externalId,
        LocalDateTime createdAt,
        String createdFor,
        LocalDateTime lastUpdatedAt,
        String lastUpdatedFor,
        String licensePlate,
        String brand,
        String model,
        Integer year,
        String color,
        UserShortDto owner

) implements Serializable {
}