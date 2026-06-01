package org.project.mechanic_shop.domain.dto.vehicle_dto;

import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link Vehicle}
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
