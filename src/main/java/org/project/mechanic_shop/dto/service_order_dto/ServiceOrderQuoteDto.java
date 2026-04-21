package org.project.mechanic_shop.dto.service_order_dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ServiceOrderQuoteDto(
        @NotBlank String mechanicDiagnosis,
        List<ServiceOrderStockItemManDto> parts,
        List<ServiceOrderLaborManDto> labors
) {}