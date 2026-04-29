package org.project.mechanic_shop.dto.service_order_dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;

public record ServiceOrderShortDto(
	UUID externalId,
	LocalDateTime createdAt,
	ServiceOrderStatusEnum status,
	String licensePlate,
	String customerName
) implements Serializable {}
