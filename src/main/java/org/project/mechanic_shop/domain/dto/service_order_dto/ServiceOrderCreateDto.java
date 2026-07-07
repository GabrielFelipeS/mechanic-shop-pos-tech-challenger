package org.project.mechanic_shop.domain.dto.service_order_dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Schema(description = "Payload for opening a new service order")
public record ServiceOrderCreateDto(
	@Schema(description = "External ID of the vehicle to be serviced", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	@NotNull UUID vehicleExternalId,

	@Schema(description = "Customer's description of the problem", example = "Barulho ao frear")
	@NotBlank String customerComplaint,

	@Schema(description = "Current odometer reading in kilometres", example = "85000")
	Integer odometerReading,

	@Schema(description = "External ID of the mechanic to assign (optional)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	UUID mechanicExternalId,

	@Schema(description = "Labour items to include in the initial budget (optional — triggers DIAGNOSIS status)")
	List<ServiceOrderLaborManDto> labors,

	@Schema(description = "Parts/consumables to include in the initial budget (optional — triggers DIAGNOSIS status)")
	List<ServiceOrderStockItemManDto> parts
) {}
