package org.project.mechanic_shop.events;

import java.util.UUID;
import org.project.mechanic_shop.models.ServiceOrder;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;

public record ServiceOrderStatusChangedEvent(
	UUID serviceOrderExternalId,
	ServiceOrderStatusEnum oldStatus,
	ServiceOrderStatusEnum newStatus
) {}
