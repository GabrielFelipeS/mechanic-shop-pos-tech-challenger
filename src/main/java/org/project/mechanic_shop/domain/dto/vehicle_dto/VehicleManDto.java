package org.project.mechanic_shop.domain.dto.vehicle_dto;

import jakarta.validation.constraints.*;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link Vehicle}
 */
public record VehicleManDto(
	@NotBlank(message = "License plate is required.")
	@Pattern(regexp = "^[A-Z]{3}\\d[A-Z\\d]\\d{2}$", message = "Invalid license plate format (Mercosul/Standard).")
	String licensePlate,

	@NotBlank(message = "Brand is required.")
	@Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters.")
	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.")
	String brand,

	@NotBlank(message = "Model is required.")
	@Size(min = 2, max = 100, message = "Model must be between 2 and 100 characters.")
	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.")
	String model,

	@NotNull(message = "Year is required.")
	@Min(value = 1900, message = "Invalid year.")
	@Max(value = 2100, message = "Invalid year.")
	Integer year,

	@Size(max = 30, message = "Color must not exceed 30 characters.")
	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.")
	String color,

	@NotNull(message = "Owner ID is required.") UUID ownerId
) implements Serializable {}
