package org.project.mechanic_shop.dto.service_order_dto.budget_dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Value;
import org.project.mechanic_shop.models.enums.BudgetStatusEnum;

/**
 * DTO for {@link org.project.mechanic_shop.models.Budget}
 */
@Value
public class BudgetDto implements Serializable {

	UUID externalId;
	LocalDateTime createdAt;
	String createdFor;
	LocalDateTime lastUpdatedAt;
	String lastUpdatedFor;
	BigDecimal totalAmount;
	BudgetStatusEnum status;
}
