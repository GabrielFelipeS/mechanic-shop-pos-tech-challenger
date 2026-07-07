package org.project.mechanic_shop.domain.entities.service_order_labor;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.domain.entities.mechanic_service.MechanicService;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderLabor {
	private Long id;
	private MechanicService mechanicService;
	private Integer quantity;
	private BigDecimal unitPrice;
	private BigDecimal totalPrice;
}
