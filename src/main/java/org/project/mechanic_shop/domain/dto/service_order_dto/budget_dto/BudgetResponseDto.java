package org.project.mechanic_shop.domain.dto.service_order_dto.budget_dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Customer's response to the service budget")
public record BudgetResponseDto(
	@Schema(description = "true = approve and start repairs; false = reject and cancel the order", example = "true")
	@NotNull(message = "The 'approved' field cannot be null") Boolean approved
) {}
