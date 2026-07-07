package org.project.mechanic_shop.domain.dto.service_order_dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "A part or consumable to be included in the service order budget")
public record ServiceOrderStockItemManDto(
	@Schema(description = "External ID of the stock item", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	@NotNull UUID partExternalId,

	@Schema(description = "Quantity to be withdrawn from stock", example = "2")
	@NotNull @Min(1) Integer quantity
) {}
