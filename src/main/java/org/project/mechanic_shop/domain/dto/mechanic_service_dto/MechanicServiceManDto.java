package org.project.mechanic_shop.domain.dto.mechanic_service_dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "Payload for creating or updating a mechanic service in the catalog")
public record MechanicServiceManDto(
	@Schema(description = "Service name", example = "Troca de óleo")
	@NotBlank(message = "Name is required.")
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.")
	String name,

	@Schema(description = "Optional description of the service", example = "Troca de óleo mineral 5W30")
	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.") String description,

	@Schema(description = "Estimated execution time in minutes", example = "60")
	@NotNull(message = "Estimated time is required.")
	@Min(value = 1, message = "Estimated time must be at least 1 minute.")
	Integer estimatedTimeMinutes,

	@Schema(description = "Price charged for this service", example = "150.00")
	@NotNull(message = "Price is required.")
	@DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative.")
	BigDecimal price
) implements Serializable {}
