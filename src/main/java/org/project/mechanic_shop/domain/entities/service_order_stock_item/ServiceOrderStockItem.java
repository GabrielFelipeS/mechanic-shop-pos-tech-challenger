package org.project.mechanic_shop.domain.entities.service_order_stock_item;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.domain.entities.stock_item.StockItem;
import org.project.mechanic_shop.domain.enums.StockItemTypeEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderStockItem {
	private Long id;
	private StockItem stockItem;
	private StockItemTypeEnum stockItemType;
	private Integer quantity;
	private BigDecimal unitPrice;
	private BigDecimal totalPrice;
}
