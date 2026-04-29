package org.project.mechanic_shop.dto.service_order_dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record ServiceOrderStockItemDto(
	UUID partExternalId,
	String partName,
	String partCode,
	Integer quantity,
	BigDecimal unitPrice,
	BigDecimal totalPrice
) implements Serializable {}
