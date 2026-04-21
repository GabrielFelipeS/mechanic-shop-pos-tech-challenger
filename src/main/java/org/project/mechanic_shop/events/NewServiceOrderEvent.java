package org.project.mechanic_shop.events;

import org.project.mechanic_shop.models.ServiceOrder;

public record NewServiceOrderEvent(ServiceOrder serviceOrder) {
}