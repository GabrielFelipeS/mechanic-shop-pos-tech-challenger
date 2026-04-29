package org.project.mechanic_shop.dto.stock_item_dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.project.mechanic_shop.models.StockItem;
import org.project.mechanic_shop.models.enums.StockItemTypeEnum;

/**
 * DTO for {@link StockItem}
 */
public record StockItemShortDto(
	UUID externalId,
	LocalDateTime createdAt,
	String createdFor,
	StockItemTypeEnum type,
	LocalDateTime lastUpdatedAt,
	String lastUpdatedFor,
	String code,
	String name,
	Integer quantity,
	BigDecimal salePrice
) implements Serializable {}
