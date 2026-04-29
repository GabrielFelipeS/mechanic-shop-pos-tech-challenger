package org.project.mechanic_shop.dto.vehicle_dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link org.project.mechanic_shop.models.Vehicle}
 */
public record VehicleShortDto(
	UUID externalId,
	LocalDateTime createdAt,
	LocalDateTime lastUpdatedAt,
	String licensePlate,
	String brand,
	String model,
	Integer year,
	String ownerName
) implements Serializable {}
