package org.project.mechanic_shop.dto.service_order_dto;

import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderShortDto(
	UUID externalId,
	LocalDateTime createdAt,
	ServiceOrderStatusEnum status,
	String licensePlate,
	String customerName
) implements Serializable {}
