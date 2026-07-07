package org.project.mechanic_shop.domain.dto.vehicle_dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.domain.dto.user_dto.UserShortDto;

/**
 * DTO for {@link Vehicle}
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
) implements Serializable {}
