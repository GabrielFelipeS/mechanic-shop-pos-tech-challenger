package org.project.mechanic_shop.events;

import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;

import java.util.UUID;

public record ServiceOrderStatusChangedEvent(
	UUID serviceOrderExternalId,
	ServiceOrderStatusEnum oldStatus,
	ServiceOrderStatusEnum newStatus
) {}
