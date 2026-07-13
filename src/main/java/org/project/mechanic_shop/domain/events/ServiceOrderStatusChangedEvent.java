package org.project.mechanic_shop.domain.events;

import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;

import java.util.UUID;

public record ServiceOrderStatusChangedEvent(
	ServiceOrder serviceOrder,
	ServiceOrderStatusEnum oldStatus,
	ServiceOrderStatusEnum newStatus
) {
	public UUID serviceOrderExternalId() {
		return serviceOrder.getExternalId();
	}
}
