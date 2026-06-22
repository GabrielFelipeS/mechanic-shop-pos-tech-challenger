package org.project.mechanic_shop.domain.entities.stock_item;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.domain.entities.base_audit_entity.BaseAuditEntity;
import org.project.mechanic_shop.domain.enums.StockItemTypeEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockItem extends BaseAuditEntity {
	private Long id;
	private String code;
	private String name;
	private StockItemTypeEnum type;
	private String description;
	private Integer quantity;
	private Integer pendingDemand = 0;
	private BigDecimal costPrice;
	private BigDecimal salePrice;
}
