package org.project.mechanic_shop.domain.dto.service_order_dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.project.mechanic_shop.domain.enums.BudgetStatusEnum;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Current status summary of a service order")
public record ServiceOrderStatusDto(
	@Schema(description = "Service order external ID")
	UUID externalId,

	@Schema(description = "Current workflow status", example = "DIAGNOSIS")
	ServiceOrderStatusEnum status,

	@Schema(description = "Current budget status", example = "OPEN")
	BudgetStatusEnum budgetStatus,

	@Schema(description = "Estimated completion date")
	LocalDateTime estimatedCompletionDate,

	@Schema(description = "Estimated completion in days", example = "3")
	Integer estimatedCompletionDays
) {}
