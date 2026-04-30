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
public record StockItemDto(
	UUID externalId,
	LocalDateTime createdAt,
	String createdFor,
	LocalDateTime lastUpdatedAt,
	String lastUpdatedFor,
	StockItemTypeEnum type,
	String code,
	String name,
	String description,
	Integer quantity,
	Integer pendingDemand,
	BigDecimal costPrice,
	BigDecimal salePrice
) implements Serializable {}
