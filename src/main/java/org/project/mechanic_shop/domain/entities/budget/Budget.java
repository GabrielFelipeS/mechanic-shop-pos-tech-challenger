package org.project.mechanic_shop.domain.entities.budget;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.domain.entities.base_audit_entity.BaseAuditEntity;
import org.project.mechanic_shop.domain.enums.BudgetStatusEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Budget extends BaseAuditEntity {
	private Long id;
	private BigDecimal totalAmount = BigDecimal.ZERO;
	private BudgetStatusEnum status = BudgetStatusEnum.OPEN;
}
