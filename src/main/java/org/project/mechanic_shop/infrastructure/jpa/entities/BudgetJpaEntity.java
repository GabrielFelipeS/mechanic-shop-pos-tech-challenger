package org.project.mechanic_shop.infrastructure.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.domain.enums.BudgetStatusEnum;
import org.project.mechanic_shop.infrastructure.jpa.entities.base.BaseAuditJpaEntity;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetJpaEntity extends BaseAuditJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BudgetStatusEnum status = BudgetStatusEnum.OPEN;

	@OneToOne(mappedBy = "budget")
	private ServiceOrderJpaEntity serviceOrder;
}
