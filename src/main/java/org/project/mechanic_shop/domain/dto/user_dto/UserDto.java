package org.project.mechanic_shop.domain.dto.user_dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.dto.vehicle_dto.VehicleShortDto;

/**
 * DTO for {@link User}
 */
public record UserDto(
	UUID externalId,
	LocalDateTime createdAt,
	String createdFor,
	LocalDateTime lastUpdatedAt,
	String lastUpdatedFor,
	String document,
	String name,
	String email,
	Boolean active,
	String phone,
	List<VehicleShortDto> vehicles
) implements Serializable {}
