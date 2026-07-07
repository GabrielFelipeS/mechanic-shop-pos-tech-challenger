package org.project.mechanic_shop.domain.dto.service_order_dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "A labour item to be included in the service order budget")
public record ServiceOrderLaborManDto(
	@Schema(description = "External ID of the mechanic service from the catalog", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	@NotNull UUID mechanicServiceExternalId,

	@Schema(description = "Number of times this service will be performed", example = "1")
	@NotNull @Min(1) Integer quantity
) {}
