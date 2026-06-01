package org.project.mechanic_shop.domain.entities.service_order_labor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.domain.entities.mechanic_service.MechanicService;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;

@Entity
@Table(name = "service_order_labors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderLabor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_order_id", nullable = false)
	private ServiceOrder serviceOrder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mechanic_service_id", nullable = false)
	private MechanicService mechanicService;

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "total_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalPrice;
}
