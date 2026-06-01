package org.project.mechanic_shop.domain.dto.service_order_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ServiceOrderCreateDto(
	@NotNull UUID vehicleExternalId,
	@NotBlank String customerComplaint,
	Integer odometerReading,
	UUID mechanicExternalId
) {}
