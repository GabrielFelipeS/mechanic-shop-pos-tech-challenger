package org.project.mechanic_shop.events;

import org.project.mechanic_shop.models.ServiceOrder;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;

public record ServiceOrderStatusChangedEvent(
        ServiceOrder serviceOrder,
        ServiceOrderStatusEnum oldStatus,
        ServiceOrderStatusEnum newStatus
) {}