package org.project.mechanic_shop.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.models.enums.StockItemTypeEnum;

@Entity
@Table(name = "stock_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockItem extends BaseAuditEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StockItemTypeEnum type;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false)
	private Integer pendingDemand = 0;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal costPrice;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal salePrice;
}
