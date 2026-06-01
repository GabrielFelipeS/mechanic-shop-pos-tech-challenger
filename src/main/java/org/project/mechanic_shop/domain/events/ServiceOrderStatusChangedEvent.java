package org.project.mechanic_shop.domain.events;

import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;

import java.util.UUID;

public record ServiceOrderStatusChangedEvent(
	UUID serviceOrderExternalId,
	ServiceOrderStatusEnum oldStatus,
	ServiceOrderStatusEnum newStatus
) {}
