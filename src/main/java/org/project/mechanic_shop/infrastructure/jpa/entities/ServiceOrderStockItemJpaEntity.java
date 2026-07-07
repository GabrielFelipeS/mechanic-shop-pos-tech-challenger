package org.project.mechanic_shop.infrastructure.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.domain.enums.StockItemTypeEnum;

@Entity
@Table(name = "service_order_stock_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderStockItemJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_order_id", nullable = false)
	private ServiceOrderJpaEntity serviceOrder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_item_id", nullable = false)
	private StockItemJpaEntity stockItem;

	@Column(nullable = false)
	private StockItemTypeEnum stockItemType;

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "total_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalPrice;
}
