package org.project.mechanic_shop.domain.events;

import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;

public record NewServiceOrderEvent(ServiceOrder serviceOrder) {}
