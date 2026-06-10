package org.project.mechanic_shop.domain.dto.service_order_dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Schema(description = "Mechanic's diagnosis and budget items for a service order")
public record ServiceOrderQuoteDto(
	@Schema(description = "Technical description of the diagnosed problem", example = "Pastilhas dianteiras desgastadas, troca necessária")
	@NotBlank String mechanicDiagnosis,

	@Schema(description = "List of parts/consumables to be used")
	List<ServiceOrderStockItemManDto> parts,

	@Schema(description = "List of labour items to be performed")
	List<ServiceOrderLaborManDto> labors
) {}
