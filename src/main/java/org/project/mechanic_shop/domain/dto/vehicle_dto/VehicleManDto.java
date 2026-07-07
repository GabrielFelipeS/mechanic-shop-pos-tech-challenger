package org.project.mechanic_shop.domain.dto.vehicle_dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.UUID;

@Schema(description = "Payload for registering or updating a vehicle")
public record VehicleManDto(
	@Schema(description = "License plate in Mercosul (ABC1D23) or standard (ABC1234) format", example = "ABC1D23")
	@NotBlank(message = "License plate is required.")
	@Pattern(regexp = "^[A-Z]{3}\\d[A-Z\\d]\\d{2}$", message = "Invalid license plate format (Mercosul/Standard).")
	String licensePlate,

	@Schema(description = "Vehicle brand", example = "Volkswagen")
	@NotBlank(message = "Brand is required.")
	@Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters.")
	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.")
	String brand,

	@Schema(description = "Vehicle model", example = "Gol")
	@NotBlank(message = "Model is required.")
	@Size(min = 2, max = 100, message = "Model must be between 2 and 100 characters.")
	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.")
	String model,

	@Schema(description = "Manufacturing year", example = "2022")
	@NotNull(message = "Year is required.")
	@Min(value = 1900, message = "Invalid year.")
	@Max(value = 2100, message = "Invalid year.")
	Integer year,

	@Schema(description = "Vehicle color", example = "Prata")
	@Size(max = 30, message = "Color must not exceed 30 characters.")
	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.")
	String color,

	@Schema(description = "External ID of the vehicle owner (User)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	@NotNull(message = "Owner ID is required.") UUID ownerId
) implements Serializable {}
